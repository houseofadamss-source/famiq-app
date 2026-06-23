package com.famiq.app.data.local

import androidx.room.TypeConverter
import com.famiq.app.data.model.Kategori
import com.famiq.app.data.model.TransactionType
import com.famiq.app.data.model.DebtType

class Converters {
    @TypeConverter
    fun fromKategori(value: Kategori): String = value.name

    @TypeConverter
    fun toKategori(value: String): Kategori = Kategori.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromDebtType(value: DebtType): String = value.name

    @TypeConverter
    fun toDebtType(value: String): DebtType = DebtType.valueOf(value)
}
