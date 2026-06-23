package com.famiq.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TransactionType {
    INCOME, EXPENSE
}

enum class Kategori {
    // Pengeluaran (Expense)
    MAKAN, TRANSPORT, BELANJA, RUMAH, KESEHATAN, LAINNYA,
    
    // Pemasukan (Income)
    GAJI, BONUS, INVESTASI, HADIAH, PENDAPATAN_LAIN
}

@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nominal: Long,
    val tipe: TransactionType = TransactionType.EXPENSE,
    val kategori: Kategori,
    val catatan: String = "",
    val diinputOleh: String,
    val tanggal: Long = System.currentTimeMillis(),
    val isNeed: Boolean = true,
    val isDebtPayment: Boolean = false
)
