package com.famiq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.famiq.app.ui.theme.*
import com.famiq.app.R
import com.famiq.app.viewmodel.TransaksiViewModel

@Composable
fun TentangScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsStateWithLifecycle()

    val bgColor      = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg         = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .verticalScroll(rememberScrollState())
        ) {
            // ── HEADER ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .padding(top = 48.dp, bottom = 32.dp, start = 18.dp, end = 18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { navController.popBackStack() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.back),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.about_app),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── LOGO / ICON APP ────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // ✅ FIX: pakai AsyncImage + R.mipmap, zero NullPointerException
                    AsyncImage(
                        model = R.mipmap.ic_launcher,
                        contentDescription = "Famiq",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.app_name),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = onBg
                )
                Text(
                    "${stringResource(R.string.version_prefix)} 1.1.1",
                    fontSize = 12.sp,
                    color = onBg.copy(alpha = 0.4f)
                )
                
                if (isUpdateAvailable) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(Routes.CHANGELOG) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(stringResource(R.string.update_available), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── DESKRIPSI ──────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Favorite,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            stringResource(R.string.made_for_families),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.app_description),
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = onBg.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── FITUR UNGGULAN ─────────────────────────────────────────────
            SettingsSectionLabel(icon = Icons.Outlined.Stars, label = stringResource(R.string.featured_features))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FiturItem(
                        icon = Icons.Outlined.People,
                        judul = stringResource(R.string.feat_sync_title),
                        deskripsi = stringResource(R.string.feat_sync_desc)
                    )
                    FiturItem(
                        icon = Icons.Outlined.DarkMode,
                        judul = stringResource(R.string.feat_night_title),
                        deskripsi = stringResource(R.string.feat_night_desc)
                    )
                    FiturItem(
                        icon = Icons.Outlined.BarChart,
                        judul = stringResource(R.string.feat_stat_title),
                        deskripsi = stringResource(R.string.feat_stat_desc)
                    )
                    FiturItem(
                        icon = Icons.Outlined.TrackChanges,
                        judul = stringResource(R.string.feat_target_title),
                        deskripsi = stringResource(R.string.feat_target_desc)
                    )
                    FiturItem(
                        icon = Icons.Outlined.WbSunny,
                        judul = stringResource(R.string.feat_weather_title),
                        deskripsi = stringResource(R.string.feat_weather_desc)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── LIHAT LAGI WELCOME SCREEN ──────────────────────────────────
            OutlinedButton(
                onClick = { navController.navigate(Routes.WELCOME) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenAccent.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.Outlined.WavingHand,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.review_welcome_screen),
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── DEVELOPER ──────────────────────────────────────────────────
            SettingsSectionLabel(icon = Icons.Outlined.Code, label = stringResource(R.string.developer_label))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "House of Adams",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBg
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.dev_tagline),
                        fontSize = 12.sp,
                        color = onBg.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FiturItem(icon: ImageVector, judul: String, deskripsi: String) {
    val onBg = MaterialTheme.colorScheme.onBackground
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = GreenAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                judul,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = onBg
            )
            Text(
                deskripsi,
                fontSize = 11.sp,
                color = onBg.copy(alpha = 0.5f),
                lineHeight = 16.sp
            )
        }
    }
}