package com.famiq.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.famiq.app.GoogleAuthHelper
import com.famiq.app.R
import com.famiq.app.data.local.UserPreferences
import com.famiq.app.ui.components.BottomNavBar
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val namaSaya by viewModel.namaSaya.collectAsStateWithLifecycle()
    val namaKeluarga by viewModel.namaKeluarga.collectAsStateWithLifecycle()
    val targetBulanan by viewModel.targetBulanan.collectAsStateWithLifecycle()
    val fotoKeluargaUri by viewModel.fotoKeluargaUri.collectAsStateWithLifecycle()
    val tanggalSiklusGajian by viewModel.tanggalSiklusGajian.collectAsStateWithLifecycle()
    val bahasaPreference by viewModel.bahasaPreference.collectAsStateWithLifecycle()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val googlePhotoUrl = currentUser?.photoUrl

    val scrollState = rememberScrollState()
    val bgColor = MaterialTheme.colorScheme.background
    val context = LocalContext.current

    val userPrefs = remember { UserPreferences(context) }
    val themePref by userPrefs.themePreference.collectAsState(initial = "auto")

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(stringResource(R.string.logout_confirm), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Text(stringResource(R.string.logout_desc), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 18.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        GoogleAuthHelper.signOut()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text(stringResource(R.string.logout), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 130.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isFamilyMode) {
                        if (fotoKeluargaUri.isNotEmpty() && fotoKeluargaUri.isNotBlank()) {
                            val fotoFile = File(fotoKeluargaUri)
                            if (fotoFile.exists()) {
                                AsyncImage(model = fotoFile, contentDescription = stringResource(R.string.family_profile), modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                AsyncImage(model = fotoKeluargaUri.toUri(), contentDescription = stringResource(R.string.family_profile), modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                        } else {
                            Box(modifier = Modifier.size(64.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(text = namaKeluarga.take(1).uppercase(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        if (googlePhotoUrl != null) {
                            AsyncImage(model = googlePhotoUrl, contentDescription = stringResource(R.string.user_photo), modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.size(64.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(text = namaSaya.take(1).uppercase(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (isFamilyMode) namaKeluarga else namaSaya, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(R.string.settings), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionLabel(icon = Icons.Outlined.Person, label = stringResource(R.string.personalization))
            SettingsCard {
                SettingsRowNavigate(label = stringResource(R.string.my_account), nilai = "", onClick = { navController.navigate("account") })
                SettingsDivider()
                SettingsRowNavigate(label = stringResource(R.string.app_mode), nilai = if (isFamilyMode) stringResource(R.string.family_mode) else stringResource(R.string.personal_mode), onClick = { navController.navigate("mode_selection") })
                
                if (isFamilyMode) {
                    SettingsDivider()
                    SettingsRowNavigate(label = stringResource(R.string.family_profile), nilai = stringResource(R.string.connected), onClick = { navController.navigate("profil_keluarga") })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── SECTION 2: KEUANGAN ──
            SettingsSectionLabel(icon = Icons.Outlined.AccountBalanceWallet, label = stringResource(R.string.budget_and_cycle))
            SettingsCard {
                SettingsRowNavigate(label = stringResource(R.string.monthly_target), nilai = if (targetBulanan > 0) formatRupiah(targetBulanan) else stringResource(R.string.not_set), onClick = { navController.navigate("target_bulanan") })
                SettingsDivider()
                SettingsRowNavigate(label = stringResource(R.string.salary_cycle), nilai = stringResource(R.string.cycle_start_at, tanggalSiklusGajian), onClick = { navController.navigate("siklus_gajian") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── SECTION 3: KEAMANAN (DEDICATED) ──
            SettingsSectionLabel(icon = Icons.Outlined.Security, label = stringResource(R.string.security))
            SettingsCard {
                SettingsRowNavigate(label = stringResource(R.string.fingerprint_lock), nilai = stringResource(R.string.status_protected), onClick = { navController.navigate("security") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── SECTION 4: DATA & EKSPOR (NEW PRO SECTION) ──
            SettingsSectionLabel(icon = Icons.Outlined.Storage, label = stringResource(R.string.data_management))
            SettingsCard {
                SettingsRowNavigate(label = stringResource(R.string.export_data), nilai = "PDF / Excel", onClick = { navController.navigate("data_management") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── SECTION 5: PREFERENSI ──
            SettingsSectionLabel(icon = Icons.Outlined.Tune, label = stringResource(R.string.preferences))
            SettingsCard {
                val temaLabel = when (themePref) {
                    "light" -> stringResource(R.string.theme_light)
                    "dark" -> stringResource(R.string.theme_dark)
                    else -> stringResource(R.string.theme_auto)
                }
                SettingsRowNavigate(label = stringResource(R.string.app_theme), nilai = temaLabel, onClick = { navController.navigate("theme_selection") })
                SettingsDivider()
                SettingsRowNavigate(label = stringResource(R.string.reminder_notif), nilai = "", onClick = { navController.navigate("notifikasi") })
                SettingsDivider()
                SettingsRowNavigate(label = stringResource(R.string.language), nilai = if (bahasaPreference == "en") stringResource(R.string.lang_en) else stringResource(R.string.lang_id), onClick = { navController.navigate("bahasa") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── SECTION 6: LAINNYA ──
            SettingsSectionLabel(icon = Icons.Outlined.Info, label = stringResource(R.string.about))
            SettingsCard {
                SettingsRowNavigate(label = stringResource(R.string.about), nilai = "", onClick = { navController.navigate("tentang") })
                SettingsDivider()
                SettingsRowNavigate(
                    label = stringResource(R.string.send_feedback),
                    nilai = "",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("houseofadamss@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_subject))
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.feedback_body))
                        }
                        try { context.startActivity(intent) } catch (e: Exception) { }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                SettingsRowDanger(label = stringResource(R.string.logout), onClick = { showLogoutDialog = true })
            }
        }

        Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
            BottomNavBar(navController = navController)
        }
    }
}

@Composable
fun SettingsSectionLabel(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(14.dp))
        Text(text = label.uppercase(Locale.getDefault()), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenAccent, letterSpacing = 0.5.sp)
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsRowNavigate(label: String, nilai: String, onClick: () -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val notPairedText = stringResource(R.string.not_paired)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = onBg)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (nilai.isNotEmpty()) {
                val textColor = if (nilai == notPairedText) Color.Gray else GreenAccent
                Text(nilai, fontSize = 13.sp, color = textColor, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = onBg.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SettingsRowSwitch(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = onBg)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = GreenMain, checkedTrackColor = GreenSoft)
        )
    }
}

@Composable
fun SettingsRowDanger(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626))
        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
}
