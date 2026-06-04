package com.famiq.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "famiq_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("theme_preference")
        val FINGERPRINT_KEY = booleanPreferencesKey("fingerprint_lock_preference")
        val IS_FAMILY_MODE_KEY = booleanPreferencesKey("is_family_mode")
        val NAMA_KELUARGA_KEY = stringPreferencesKey("nama_keluarga")
        val TARGET_BULANAN_KEY = longPreferencesKey("target_bulanan")
        val TANGGAL_SIKLUS_GAJIAN_KEY = intPreferencesKey("tanggal_siklus_gajian")
        val NAMA_SAYA_KEY = stringPreferencesKey("nama_saya")
        val NAMA_PASANGAN_KEY = stringPreferencesKey("nama_pasangan")
        val FOTO_KELUARGA_URI_KEY = stringPreferencesKey("foto_keluarga_uri")
        val HARI_MULAI_MINGGU_KEY = stringPreferencesKey("hari_mulai_minggu")
        val STATUS_BADGE_NOTIF_KEY = booleanPreferencesKey("status_badge_notif")
        val HIDE_BALANCE_KEY = booleanPreferencesKey("hide_balance")
        val BAHASA_KEY = stringPreferencesKey("bahasa_preference")
        
        // ✅ NEW: 3 SLOTS NOTIFICATIONS
        val NOTIF_MORNING_AKTIF = booleanPreferencesKey("notif_morning_aktif")
        val NOTIF_AFTERNOON_AKTIF = booleanPreferencesKey("notif_afternoon_aktif")
        val NOTIF_EVENING_AKTIF = booleanPreferencesKey("notif_evening_aktif")
        
        val JAM_MORNING = stringPreferencesKey("jam_morning")
        val JAM_AFTERNOON = stringPreferencesKey("jam_afternoon")
        val JAM_EVENING = stringPreferencesKey("jam_evening")

        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    // ── ONBOARDING ──
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    suspend fun simpanOnboardingCompleted(completed: Boolean) { context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed } }

    // ── FITUR SEMBUNYIKAN SALDO ──
    val hideBalance: Flow<Boolean> = context.dataStore.data.map { it[HIDE_BALANCE_KEY] ?: false }
    suspend fun simpanHideBalance(hide: Boolean) { context.dataStore.edit { it[HIDE_BALANCE_KEY] = hide } }

    // ── FITUR BAHASA ──
    val bahasaPreference: Flow<String> = context.dataStore.data.map { it[BAHASA_KEY] ?: "en" }
    suspend fun simpanBahasaPreference(value: String) { context.dataStore.edit { it[BAHASA_KEY] = value } }

    // ── FITUR TITIK MERAH (BADGE) ──
    val statusBadgeNotif: Flow<Boolean> = context.dataStore.data.map { it[STATUS_BADGE_NOTIF_KEY] ?: false }
    suspend fun simpanStatusBadgeNotif(ada: Boolean) { context.dataStore.edit { it[STATUS_BADGE_NOTIF_KEY] = ada } }

    // ── TEMA APLIKASI ──
    val themePreference: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "auto" }
    suspend fun simpanThemePreference(value: String) { context.dataStore.edit { it[THEME_KEY] = value } }

    // ── KUNCI SIDIK JARI ──
    fun bacaFingerprintPreference(): Flow<Boolean> { return context.dataStore.data.map { it[FINGERPRINT_KEY] ?: false } }
    suspend fun simpanFingerprintPreference(enabled: Boolean) { context.dataStore.edit { it[FINGERPRINT_KEY] = enabled } }

    // ── MODE KELUARGA ──
    val isFamilyMode: Flow<Boolean> = context.dataStore.data.map { it[IS_FAMILY_MODE_KEY] ?: false }
    suspend fun simpanIsFamilyMode(aktif: Boolean) { context.dataStore.edit { it[IS_FAMILY_MODE_KEY] = aktif } }

    // ── NAMA KELUARGA ──
    val namaKeluarga: Flow<String> = context.dataStore.data.map { it[NAMA_KELUARGA_KEY] ?: "Keluarga" }
    suspend fun simpanNamaKeluarga(nama: String) { context.dataStore.edit { it[NAMA_KELUARGA_KEY] = nama } }

    // ── TARGET BULANAN ──
    val targetBulanan: Flow<Long> = context.dataStore.data.map { it[TARGET_BULANAN_KEY] ?: 0L }
    suspend fun simpanTargetBulanan(target: Long) { context.dataStore.edit { it[TARGET_BULANAN_KEY] = target } }

    // ── TANGGAL SIKLUS GAJIAN ──
    val tanggalSiklusGajian: Flow<Int> = context.dataStore.data.map { it[TANGGAL_SIKLUS_GAJIAN_KEY] ?: 1 }
    suspend fun simpanTanggalSiklusGajian(tanggal: Int) { context.dataStore.edit { it[TANGGAL_SIKLUS_GAJIAN_KEY] = tanggal } }

    // ── NAMA SAYA ──
    val namaSaya: Flow<String> = context.dataStore.data.map { it[NAMA_SAYA_KEY] ?: "Saya" }
    suspend fun simpanNamaSaya(nama: String) { context.dataStore.edit { it[NAMA_SAYA_KEY] = nama } }

    // ── NAMA PASANGAN ──
    val namaPasangan: Flow<String> = context.dataStore.data.map { it[NAMA_PASANGAN_KEY] ?: "Pasangan" }
    suspend fun simpanNamaPasangan(nama: String) { context.dataStore.edit { it[NAMA_PASANGAN_KEY] = nama } }

    // ── FOTO KELUARGA URI ──
    val fotoKeluargaUri: Flow<String> = context.dataStore.data.map { it[FOTO_KELUARGA_URI_KEY] ?: "" }
    suspend fun simpanFotoKeluargaUri(uri: String) { context.dataStore.edit { it[FOTO_KELUARGA_URI_KEY] = uri } }

    // ── HARI MULAI MINGGU ──
    val hariMulaiMinggu: Flow<String> = context.dataStore.data.map { it[HARI_MULAI_MINGGU_KEY] ?: "Senin" }
    suspend fun simpanHariMulaiMinggu(hari: String) { context.dataStore.edit { it[HARI_MULAI_MINGGU_KEY] = hari } }

    // ── NOTIFIKASI 3 SLOT ──
    val notifMorningAktif: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_MORNING_AKTIF] ?: false }
    val notifAfternoonAktif: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_AFTERNOON_AKTIF] ?: false }
    val notifEveningAktif: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_EVENING_AKTIF] ?: false }
    
    val jamMorning: Flow<String> = context.dataStore.data.map { it[JAM_MORNING] ?: "09:00" }
    val jamAfternoon: Flow<String> = context.dataStore.data.map { it[JAM_AFTERNOON] ?: "17:00" }
    val jamEvening: Flow<String> = context.dataStore.data.map { it[JAM_EVENING] ?: "21:00" }

    suspend fun simpanNotifMorningAktif(aktif: Boolean) { context.dataStore.edit { it[NOTIF_MORNING_AKTIF] = aktif } }
    suspend fun simpanNotifAfternoonAktif(aktif: Boolean) { context.dataStore.edit { it[NOTIF_AFTERNOON_AKTIF] = aktif } }
    suspend fun simpanNotifEveningAktif(aktif: Boolean) { context.dataStore.edit { it[NOTIF_EVENING_AKTIF] = aktif } }
    
    suspend fun simpanJamMorning(jam: String) { context.dataStore.edit { it[JAM_MORNING] = jam } }
    suspend fun simpanJamAfternoon(jam: String) { context.dataStore.edit { it[JAM_AFTERNOON] = jam } }
    suspend fun simpanJamEvening(jam: String) { context.dataStore.edit { it[JAM_EVENING] = jam } }
}
