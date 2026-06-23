package com.famiq.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.famiq.app.data.model.Anggota
import com.famiq.app.data.model.Transaksi

@Database(entities = [Transaksi::class, Anggota::class, com.famiq.app.data.model.HutangPiutang::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transaksiDao(): TransaksiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "catatuang_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}