package com.famiq.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class DebtType {
    HUTANG, // Kita berhutang ke orang (Liabilities)
    PIUTANG  // Orang berhutang ke kita (Assets)
}

@Entity(tableName = "hutang_piutang")
data class HutangPiutang(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val kontak: String, // Nama orang/instansi
    val nominalTotal: Long,
    val nominalTerbayar: Long = 0L,
    val tipe: DebtType,
    val jatuhTempo: Long,
    val catatan: String = "",
    val diinputOleh: String,
    val tanggal: Long = System.currentTimeMillis(),
    val isLunas: Boolean = false
)
