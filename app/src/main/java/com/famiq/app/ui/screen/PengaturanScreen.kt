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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.famiq.app.R
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val namaSaya by viewModel.namaSaya.collectAsStateWithLifecycle()
    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val googleName = currentUser?.displayName ?: stringResource(R.string.user)
    val googleEmail = currentUser?.email ?: stringResource(R.string.email_not_available)
    val googlePhotoUrl = currentUser?.photoUrl

    val defaultUserName = stringResource(R.string.default_user_name)
    LaunchedEffect(currentUser) {
        if (namaSaya == defaultUserName && currentUser != null) {
            viewModel.simpanNamaSaya(googleName)
        }
    }

    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(namaSaya) }

    LaunchedEffect(namaSaya) {
        tempName = namaSaya
    }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── HEADER HIJAU PREMIUM ──
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
                        text = stringResource(R.string.account_settings),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.settings_desc),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // ── MAIN CONTENT ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. PROFIL PRIBADI
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(stringResource(R.string.my_account), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreenMain)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (googlePhotoUrl != null) {
                                AsyncImage(
                                    model = googlePhotoUrl,
                                    contentDescription = stringResource(R.string.user_photo),
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(60.dp).background(GreenSoft, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = namaSaya.take(1).uppercase(),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenMain
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                if (isEditingName) {
                                    OutlinedTextField(
                                        value = tempName,
                                        onValueChange = { tempName = it },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GreenMain,
                                            cursorColor = GreenAccent
                                        )
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth().padding(top=4.dp)
                                    ) {
                                        TextButton(onClick = { isEditingName = false }) {
                                            Text(stringResource(R.string.cancel), color = Color.Gray)
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.simpanNamaSaya(tempName)
                                                isEditingName = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                                        ) {
                                            Text(stringResource(R.string.save))
                                        }
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(namaSaya, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onBg)
                                        IconButton(onClick = {
                                            tempName = namaSaya
                                            isEditingName = true
                                        }) {
                                            Icon(
                                                Icons.Outlined.Edit,
                                                contentDescription = stringResource(R.string.edit_name),
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                    Text(googleEmail, fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // 2. STATUS MODE
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFamilyMode) GreenSoft else surfaceColor
                    ),
                    border = if (isFamilyMode) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(stringResource(R.string.app_mode), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreenMain)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFamilyMode) Icons.Outlined.Group else Icons.Outlined.Person,
                                contentDescription = null,
                                tint = if (isFamilyMode) GreenMain else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isFamilyMode) stringResource(R.string.family_mode_active) else stringResource(R.string.personal_mode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = onBg
                                )
                                Text(
                                    text = if (isFamilyMode) stringResource(R.string.family_mode_desc) else stringResource(R.string.personal_mode_desc),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!isFamilyMode) {
                            Button(
                                onClick = { viewModel.setFamilyMode(true) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenMain),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.upgrade_family_mode), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.setFamilyMode(false) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                            ) {
                                Text(stringResource(R.string.back_to_personal_mode), color = Color.Gray)
                            }
                        }
                    }
                }
                // CARD KEAMANAN SUDAH DIHAPUS DARI SINI
            }
        }
    }
}