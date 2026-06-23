package com.famiq.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.famiq.app.data.model.Transaksi
import com.famiq.app.data.model.Anggota
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaksiDao {

    // ── CRUD TRANSAKSI ────────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun tambahTransaksi(transaksi: Transaksi)

    @Update
    suspend fun updateTransaksi(transaksi: Transaksi)

    @Delete
    suspend fun hapusTransaksi(transaksi: Transaksi)

    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun semuaTransaksi(): Flow<List<Transaksi>>

    @Query("SELECT * FROM transaksi WHERE diinputOleh = :nama ORDER BY tanggal DESC")
    fun transaksiByAnggota(nama: String): Flow<List<Transaksi>>

    @Query("DELETE FROM transaksi")
    suspend fun hapusSemuaTransaksi()

    // ── CRUD ANGGOTA KELUARGA (PREMIUM REVISION) ──────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun tambahAnggota(anggota: Anggota)

    @Delete
    suspend fun hapusAnggota(anggota: Anggota)

    @Query("SELECT * FROM anggota ORDER BY nama ASC")
    fun semuaAnggota(): Flow<List<Anggota>>

    // ── CRUD HUTANG PIUTANG (NEW FEATURE) ────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun tambahHutang(data: com.famiq.app.data.model.HutangPiutang)

    @Update
    suspend fun updateHutang(data: com.famiq.app.data.model.HutangPiutang)

    @Delete
    suspend fun hapusHutang(data: com.famiq.app.data.model.HutangPiutang)

    @Query("SELECT * FROM hutang_piutang ORDER BY jatuhTempo ASC")
    fun semuaHutang(): Flow<List<com.famiq.app.data.model.HutangPiutang>>

    @Query("DELETE FROM hutang_piutang")
    suspend fun hapusSemuaHutang()
}