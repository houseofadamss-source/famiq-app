package com.famiq.app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    var showActivationDialog by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf("") }

    // List kode rahasia lo (Bisa lo ganti sesuka hati)
    val secretCodes = listOf("FAMIQ-PREMIUM-2024", "HOUSE-OF-ADAMS", "PREMIUM-50K")

    if (showActivationDialog) {
        AlertDialog(
            onDismissRequest = { showActivationDialog = false },
            title = { Text(stringResource(R.string.enter_activation_code), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it.uppercase() },
                    placeholder = { Text(stringResource(R.string.activation_code_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (secretCodes.contains(inputCode)) {
                            viewModel.setFamilyMode(true)
                            showActivationDialog = false
                            Toast.makeText(context, context.getString(R.string.success_activated), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.invalid_code), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                ) {
                    Text(stringResource(R.string.activate))
                }
            },
            dismissButton = {
                TextButton(onClick = { showActivationDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

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
                        Text(
                            text = stringResource(R.string.back),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.select_app_mode),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.select_app_mode_desc),
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── MODE PERSONAL ──
            ModeCard(
                title = stringResource(R.string.personal_mode_title),
                description = stringResource(R.string.personal_mode_description),
                icon = Icons.Outlined.Person,
                isSelected = !isFamilyMode,
                onClick = { viewModel.setFamilyMode(false) }
            )

            // ── MODE KELUARGA (FREEMIUM/PREMIUM STYLE) ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isFamilyMode) 2.dp else 0.dp,
                        color = if (isFamilyMode) GreenMain else Color.Transparent,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(if (isFamilyMode) 8.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(GreenSoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Groups, contentDescription = null, tint = GreenMain)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(stringResource(R.string.family_mode_title), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                if (isFamilyMode) stringResource(R.string.in_use) else stringResource(R.string.premium_feature),
                                color = GreenMain,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = isFamilyMode,
                            onClick = { 
                                if (isFamilyMode) viewModel.setFamilyMode(false)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = GreenMain)
                        )
                    }

                    if (!isFamilyMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.family_mode_premium_desc),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = onBg.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        stringResource(R.string.family_mode_benefits),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    BenefitRow(icon = Icons.Outlined.Sync, text = stringResource(R.string.benefit_sync))
                    BenefitRow(icon = Icons.Outlined.NotificationsActive, text = stringResource(R.string.benefit_notif))
                    BenefitRow(icon = Icons.Outlined.Analytics, text = stringResource(R.string.benefit_report))
                    BenefitRow(icon = Icons.Outlined.CloudDone, text = stringResource(R.string.benefit_backup))

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isFamilyMode) {
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:")
                                    putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("houseofadamss@gmail.com"))
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Premium Activation Request - Famiq")
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Hello House of Adams,\n\nI am interested in activating the Premium Family Mode for Famiq. Please provide the contribution details.\n\nThank you.")
                                }
                                try { context.startActivity(intent) } catch (e: Exception) { }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                        ) {
                            Text(stringResource(R.string.enable_family_mode), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.have_activation_code),
                            color = GreenMain,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showActivationDialog = true },
                            textAlign = TextAlign.Center
                        )
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.setFamilyMode(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text(stringResource(R.string.back_to_personal), color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) GreenMain else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(GreenSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GreenMain)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(description, fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = GreenMain)
            )
        }
    }
}

@Composable
fun BenefitRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
    }
}
