package com.famiq.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.famiq.app.data.model.Transaksi
import com.famiq.app.data.model.Kategori
import com.famiq.app.data.model.Anggota
import com.famiq.app.data.model.TransactionType
import com.onesignal.OneSignal

object FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun buatKeluargaBaru(namaKeluarga: String): String? {
        val currentUser = auth.currentUser ?: return null
        val uid = currentUser.uid
        val inviteCode = generateInviteCode()

        val familyData = hashMapOf(
            "namaKeluarga" to namaKeluarga,
            "inviteCode" to inviteCode,
            "adminUid" to uid,
            "targetBulanan" to 0L,
            "tanggalSiklus" to 1
        )

        return try {
            val docRef = db.collection("families").add(familyData).await()
            db.collection("users").document(uid).set(
                hashMapOf(
                    "familyId" to docRef.id,
                    "role" to "admin",
                    "nama" to (currentUser.displayName ?: "Admin"),
                    "email" to (currentUser.email ?: ""),
                    "fotoUrl" to (currentUser.photoUrl?.toString() ?: "")
                )
            ).await()
            OneSignal.User.addTag("family_id", docRef.id)
            inviteCode
        } catch (e: Exception) {
            null
        }
    }

    suspend fun gabungKeluarga(inviteCode: String): String? {
        val currentUser = auth.currentUser ?: return null
        val uid = currentUser.uid

        return try {
            val querySnapshot = db.collection("families")
                .whereEqualTo("inviteCode", inviteCode.uppercase())
                .get()
                .await()

            if (querySnapshot.isEmpty) return null

            val familyDoc = querySnapshot.documents.first()
            val familyId = familyDoc.id
            val fetchedNamaKeluarga = familyDoc.getString("namaKeluarga") ?: "Keluarga"

            // ✅ CEK JUMLAH ANGGOTA (MAKS 6)
            val membersSnapshot = db.collection("users")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            
            if (membersSnapshot.size() >= 6) {
                return "FULL"
            }

            db.collection("users").document(uid).set(
                hashMapOf(
                    "familyId" to familyId,
                    "role" to "member",
                    "nama" to (currentUser.displayName ?: "Anggota"),
                    "email" to (currentUser.email ?: ""),
                    "fotoUrl" to (currentUser.photoUrl?.toString() ?: "")
                )
            ).await()
            OneSignal.User.addTag("family_id", familyId)

            fetchedNamaKeluarga
        } catch (e: Exception) {
            null
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    suspend fun updateNamaKeluargaCloud(namaBaru: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return false
            db.collection("families").document(familyId).update("namaKeluarga", namaBaru).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun updateTargetBulananCloud(target: Long): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return false
            db.collection("families").document(familyId).update("targetBulanan", target).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun updateTanggalSiklusCloud(tanggal: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return false
            db.collection("families").document(familyId).update("tanggalSiklus", tanggal).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun tambahTransaksiCloud(nominal: Long, tipe: TransactionType, kategori: String, catatan: String, diinputOleh: String, tanggal: Long): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val familyId = userDoc.getString("familyId")
            if (familyId.isNullOrEmpty()) return null

            val transaksiData = hashMapOf(
                "nominal" to nominal,
                "tipe" to tipe.name,
                "kategori" to kategori,
                "catatan" to catatan,
                "diinputOleh" to diinputOleh,
                "tanggal" to tanggal
            )
            db.collection("families").document(familyId).collection("transaksi").add(transaksiData).await()
            familyId
        } catch (e: Exception) {
            null
        }
    }

    suspend fun hapusTransaksiCloud(tanggal: Long, nominal: Long): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return false

            val query = db.collection("families").document(familyId).collection("transaksi")
                .whereEqualTo("tanggal", tanggal)
                .whereEqualTo("nominal", nominal)
                .limit(1)
                .get()
                .await()

            for (doc in query.documents) doc.reference.delete().await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun editTransaksiCloud(tanggalLama: Long, nominalLama: Long, nominalBaru: Long, kategoriBaru: String, catatanBaru: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return false

            val query = db.collection("families").document(familyId).collection("transaksi")
                .whereEqualTo("tanggal", tanggalLama)
                .whereEqualTo("nominal", nominalLama)
                .limit(1)
                .get()
                .await()

            for (doc in query.documents) {
                doc.reference.update(mapOf("nominal" to nominalBaru, "kategori" to kategoriBaru, "catatan" to catatanBaru)).await()
            }
            true
        } catch (e: Exception) { false }
    }

    fun listenNamaKeluarga(): Flow<String> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend("Keluarga"); close(); return@callbackFlow }
        val userListener = db.collection("users").document(uid).addSnapshotListener { userDoc, _ ->
            val familyId = userDoc?.getString("familyId")
            if (familyId.isNullOrEmpty()) { trySend("Keluarga"); return@addSnapshotListener }
            
            // ✅ SYNC ONESIGNAL TAG
            OneSignal.User.addTag("family_id", familyId)

            db.collection("families").document(familyId).addSnapshotListener { familyDoc, error ->
                if (error == null && familyDoc != null) {
                    trySend(familyDoc.getString("namaKeluarga") ?: "Keluarga")
                }
            }
        }
        awaitClose { userListener.remove() }
    }

    fun listenTargetBulanan(): Flow<Long> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(0L); close(); return@callbackFlow }
        val userListener = db.collection("users").document(uid).addSnapshotListener { userDoc, _ ->
            val familyId = userDoc?.getString("familyId")
            if (familyId.isNullOrEmpty()) { trySend(0L); return@addSnapshotListener }
            db.collection("families").document(familyId).addSnapshotListener { familyDoc, error ->
                if (error == null && familyDoc != null) {
                    trySend(familyDoc.getLong("targetBulanan") ?: 0L)
                }
            }
        }
        awaitClose { userListener.remove() }
    }

    fun listenTanggalSiklus(): Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(1); close(); return@callbackFlow }
        val userListener = db.collection("users").document(uid).addSnapshotListener { userDoc, _ ->
            val familyId = userDoc?.getString("familyId")
            if (familyId.isNullOrEmpty()) { trySend(1); return@addSnapshotListener }
            db.collection("families").document(familyId).addSnapshotListener { familyDoc, error ->
                if (error == null && familyDoc != null) {
                    trySend(familyDoc.getLong("tanggalSiklus")?.toInt() ?: 1)
                }
            }
        }
        awaitClose { userListener.remove() }
    }

    fun listenTransaksiKeluarga(): Flow<List<Transaksi>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(emptyList()); close(); return@callbackFlow }
        val userListener = db.collection("users").document(uid).addSnapshotListener { userDoc, _ ->
            val familyId = userDoc?.getString("familyId")
            if (familyId.isNullOrEmpty()) { trySend(emptyList()); return@addSnapshotListener }
            db.collection("families").document(familyId).collection("transaksi")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) { trySend(emptyList()); return@addSnapshotListener }
                    val listTransaksi = snapshot.documents.mapNotNull { doc ->
                        try {
                            Transaksi(
                                id = doc.id,
                                nominal = doc.getLong("nominal") ?: 0L,
                                tipe = TransactionType.valueOf(doc.getString("tipe") ?: "EXPENSE"),
                                kategori = Kategori.valueOf(doc.getString("kategori") ?: "LAINNYA"),
                                catatan = doc.getString("catatan") ?: "",
                                diinputOleh = doc.getString("diinputOleh") ?: "",
                                tanggal = doc.getLong("tanggal") ?: 0L
                            )
                        } catch (e: Exception) { null }
                    }.sortedByDescending { it.tanggal }
                    trySend(listTransaksi)
                }
        }
        awaitClose { userListener.remove() }
    }

    fun listenAnggotaKeluarga(): Flow<List<Anggota>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(emptyList()); close(); return@callbackFlow }
        val userListener = db.collection("users").document(uid).addSnapshotListener { userDoc, _ ->
            val familyId = userDoc?.getString("familyId")
            if (familyId.isNullOrEmpty()) { trySend(emptyList()); return@addSnapshotListener }
            db.collection("users").whereEqualTo("familyId", familyId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) { trySend(emptyList()); return@addSnapshotListener }
                    val listAnggota = snapshot.documents.mapNotNull { doc ->
                        try {
                            Anggota(
                                nama = doc.getString("nama") ?: "Anggota",
                                email = doc.getString("email") ?: "",
                                fotoUrl = doc.getString("fotoUrl") ?: ""
                            )
                        } catch (e: Exception) { null }
                    }
                    trySend(listAnggota)
                }
        }
        awaitClose { userListener.remove() }
    }
}
