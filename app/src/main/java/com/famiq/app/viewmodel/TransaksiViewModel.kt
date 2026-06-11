package com.famiq.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.famiq.app.ConnectivityObserver
import com.famiq.app.ConnectionStatus
import com.famiq.app.FirestoreHelper
import com.famiq.app.R
import com.famiq.app.UpdateHelper
import com.famiq.app.data.local.AppDatabase
import com.famiq.app.data.local.UserPreferences
import com.famiq.app.data.model.Anggota
import com.famiq.app.data.model.Kategori
import com.famiq.app.data.model.Transaksi
import com.famiq.app.data.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransaksiViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).transaksiDao()
    private val prefs = UserPreferences(application)
    private val connectivityObserver = ConnectivityObserver(application)

    val connectionStatus: StateFlow<ConnectionStatus> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionStatus.Available)

    val isFamilyMode = prefs.isFamilyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setFamilyMode(aktif: Boolean) {
        viewModelScope.launch { prefs.simpanIsFamilyMode(aktif) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val semuaTransaksi = isFamilyMode.flatMapLatest { isFamily ->
        if (isFamily) FirestoreHelper.listenTransaksiKeluarga() else dao.semuaTransaksi()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val semuaAnggota = isFamilyMode.flatMapLatest { isFamily ->
        if (isFamily) FirestoreHelper.listenAnggotaKeluarga() else dao.semuaAnggota()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val namaKeluarga = isFamilyMode.flatMapLatest { isFamily ->
        if (isFamily) FirestoreHelper.listenNamaKeluarga() else prefs.namaKeluarga
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), application.getString(R.string.family_mode_title))

    @OptIn(ExperimentalCoroutinesApi::class)
    val targetBulanan = isFamilyMode.flatMapLatest { isFamily ->
        if (isFamily) FirestoreHelper.listenTargetBulanan() else prefs.targetBulanan
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tanggalSiklusGajian = isFamilyMode.flatMapLatest { isFamily ->
        if (isFamily) FirestoreHelper.listenTanggalSiklus() else prefs.tanggalSiklusGajian
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val namaSaya = prefs.namaSaya.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), application.getString(R.string.user))
    val fotoKeluargaUri = prefs.fotoKeluargaUri.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    val notifMorningAktif = prefs.notifMorningAktif.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val notifAfternoonAktif = prefs.notifAfternoonAktif.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val notifEveningAktif = prefs.notifEveningAktif.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val jamMorning = prefs.jamMorning.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "09:00")
    val jamAfternoon = prefs.jamAfternoon.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "17:00")
    val jamEvening = prefs.jamEvening.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "21:00")

    fun simpanNotifMorningAktif(aktif: Boolean) { viewModelScope.launch { prefs.simpanNotifMorningAktif(aktif) } }
    fun simpanNotifAfternoonAktif(aktif: Boolean) { viewModelScope.launch { prefs.simpanNotifAfternoonAktif(aktif) } }
    fun simpanNotifEveningAktif(aktif: Boolean) { viewModelScope.launch { prefs.simpanNotifEveningAktif(aktif) } }
    
    fun simpanJamMorning(jam: String) { viewModelScope.launch { prefs.simpanJamMorning(jam) } }
    fun simpanJamAfternoon(jam: String) { viewModelScope.launch { prefs.simpanJamAfternoon(jam) } }
    fun simpanJamEvening(jam: String) { viewModelScope.launch { prefs.simpanJamEvening(jam) } }

    fun autoSyncGoogleProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && !user.displayName.isNullOrEmpty()) {
            val namaGoogle = user.displayName!!
            val emailGoogle = user.email ?: ""
            val fotoGoogle = user.photoUrl?.toString() ?: ""
            simpanNamaSaya(namaGoogle)
            viewModelScope.launch {
                try {
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .update(mapOf(
                            "nama" to namaGoogle,
                            "email" to emailGoogle,
                            "fotoUrl" to fotoGoogle
                        ))
                } catch (e: Exception) {}
            }
        }
    }

    suspend fun tambahTransaksiRouter(nominal: Long, tipe: TransactionType, kategori: Kategori, catatan: String, diinputOleh: String): String? {
        return if (isFamilyMode.value) {
            FirestoreHelper.tambahTransaksiCloud(nominal, tipe, kategori.name, catatan, diinputOleh, System.currentTimeMillis())
        } else {
            dao.tambahTransaksi(Transaksi(nominal = nominal, tipe = tipe, kategori = kategori, catatan = catatan, diinputOleh = diinputOleh))
            "local"
        }
    }

    fun hapusTransaksi(transaksi: Transaksi) {
        viewModelScope.launch {
            if (isFamilyMode.value) FirestoreHelper.hapusTransaksiCloud(transaksi.tanggal, transaksi.nominal)
            else dao.hapusTransaksi(transaksi)
        }
    }

    suspend fun editTransaksiRouter(transaksiLama: Transaksi, nominalBaru: Long, kategoriBaru: Kategori, catatanBaru: String): Boolean {
        return if (isFamilyMode.value) {
            FirestoreHelper.editTransaksiCloud(transaksiLama.tanggal, transaksiLama.nominal, nominalBaru, kategoriBaru.name, catatanBaru)
        } else {
            dao.updateTransaksi(transaksiLama.copy(nominal = nominalBaru, kategori = kategoriBaru, catatan = catatanBaru))
            true
        }
    }

    fun hapusSemuaTransaksi() { viewModelScope.launch { dao.hapusSemuaTransaksi() } }
    fun hapusAnggotaKeluarga(anggota: Anggota) { viewModelScope.launch { dao.hapusAnggota(anggota) } }

    fun simpanNamaKeluarga(nama: String) {
        viewModelScope.launch {
            prefs.simpanNamaKeluarga(nama)
            if (isFamilyMode.value) FirestoreHelper.updateNamaKeluargaCloud(nama)
        }
    }

    fun simpanTargetBulanan(target: Long) {
        viewModelScope.launch {
            prefs.simpanTargetBulanan(target)
            if (isFamilyMode.value) FirestoreHelper.updateTargetBulananCloud(target)
        }
    }

    fun simpanTanggalSiklusGajian(tanggal: Int) {
        viewModelScope.launch {
            prefs.simpanTanggalSiklusGajian(tanggal)
            if (isFamilyMode.value) FirestoreHelper.updateTanggalSiklusCloud(tanggal)
        }
    }

    fun simpanNamaSaya(nama: String) { viewModelScope.launch { prefs.simpanNamaSaya(nama) } }
    fun simpanFotoKeluargaUri(uri: String) { viewModelScope.launch { prefs.simpanFotoKeluargaUri(uri) } }

    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                val release = UpdateHelper.api.getLatestRelease()
                val url = release.assets.firstOrNull { it.name.endsWith(".apk") }?.downloadUrl ?: ""
                prefs.simpanUpdateInfo(release.tagName, release.body, url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        checkForUpdates()
        viewModelScope.launch {
            isFamilyMode.collect { familyActive ->
                if (familyActive) {
                    FirestoreHelper.listenTransaksiKeluarga().collect { transaksiCloud ->
                        transaksiCloud.forEach { trx -> dao.tambahTransaksi(trx) }
                    }
                }
            }
        }
        viewModelScope.launch {
            isFamilyMode.collect { familyActive ->
                if (familyActive) {
                    FirestoreHelper.listenAnggotaKeluarga().collect { anggotaCloud ->
                        anggotaCloud.forEach { anggota -> dao.tambahAnggota(anggota) }
                    }
                }
            }
        }
    }

    val statusBadgeNotif = prefs.statusBadgeNotif.stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), initialValue = false)
    fun setStatusBadgeNotif(ada: Boolean) { viewModelScope.launch { prefs.simpanStatusBadgeNotif(ada) } }

    val hideBalance = prefs.hideBalance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    fun setHideBalance(hide: Boolean) { viewModelScope.launch { prefs.simpanHideBalance(hide) } }

    val bahasaPreference = prefs.bahasaPreference.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")
    fun simpanBahasaPreference(value: String) { viewModelScope.launch { prefs.simpanBahasaPreference(value) } }

    // ── UPDATE SYSTEM ──
    val latestVersion = prefs.latestVersion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val updateChangelog = prefs.updateChangelog.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val downloadUrl = prefs.downloadUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isUpdateAvailable = latestVersion.flatMapLatest { latest ->
        val current = "v1.0.2"
        kotlinx.coroutines.flow.flowOf(UpdateHelper.isNewerVersion(current, latest))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
