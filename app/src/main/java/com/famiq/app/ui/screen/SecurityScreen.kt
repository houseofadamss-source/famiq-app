package com.famiq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.data.local.UserPreferences
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }
    
    val isFingerprintEnabled by userPrefs.bacaFingerprintPreference().collectAsState(initial = false)
    val hideBalance by viewModel.hideBalance.collectAsStateWithLifecycle()

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { navController.popBackStack() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.back), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.security_privacy),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.security_screen_desc),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.lock_method),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GreenAccent,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            // ── FINGERPRINT / BIOMETRIC ──
            SecurityOptionCard(
                title = stringResource(R.string.fingerprint_lock),
                description = stringResource(R.string.fingerprint_desc),
                icon = Icons.Outlined.Fingerprint,
                isEnabled = isFingerprintEnabled,
                onToggle = { newValue ->
                    coroutineScope.launch { userPrefs.simpanFingerprintPreference(newValue) }
                }
            )

            // ── PIN APLIKASI (PLACEHOLDER) ──
            SecurityOptionCard(
                title = stringResource(R.string.pin_app),
                description = stringResource(R.string.pin_app_desc),
                icon = Icons.Outlined.Dialpad,
                isEnabled = false,
                isComingSoon = true,
                onToggle = {}
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── PRIVASI SECTION ──
            Text(
                text = stringResource(R.string.data_privacy),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GreenAccent,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PrivacyRow(
                        title = stringResource(R.string.hide_balance_title),
                        description = stringResource(R.string.hide_balance_desc),
                        icon = Icons.Outlined.VisibilityOff,
                        isChecked = hideBalance,
                        onCheckedChange = { viewModel.setHideBalance(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = onBg.copy(alpha = 0.05f))
                    PrivacyRow(
                        title = stringResource(R.string.incognito_mode),
                        description = stringResource(R.string.incognito_desc),
                        icon = Icons.Outlined.PrivacyTip,
                        isChecked = false,
                        isEnabled = false,
                        onCheckedChange = {}
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isComingSoon: Boolean = false,
    onToggle: (Boolean) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GreenSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GreenMain, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onBg)
                    if (isComingSoon) {
                        Surface(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("SOON", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(description, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                enabled = !isComingSoon,
                colors = SwitchDefaults.colors(checkedThumbColor = GreenMain, checkedTrackColor = GreenSoft)
            )
        }
    }
}

@Composable
fun PrivacyRow(
    title: String, 
    description: String, 
    icon: ImageVector,
    isChecked: Boolean,
    isEnabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = if (isEnabled) GreenMain else Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray)
            Text(description, fontSize = 11.sp, color = Color.Gray)
        }
        Switch(
            checked = isChecked, 
            onCheckedChange = onCheckedChange, 
            enabled = isEnabled,
            colors = SwitchDefaults.colors(checkedThumbColor = GreenMain, checkedTrackColor = GreenSoft)
        )
    }
}
