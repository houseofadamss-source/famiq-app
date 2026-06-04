package com.famiq.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anggota")
data class Anggota(
    @PrimaryKey
    val nama: String,
    val email: String,
    val fotoUrl: String = ""
)