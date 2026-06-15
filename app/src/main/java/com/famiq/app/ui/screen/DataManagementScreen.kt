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
import com.famiq.app.ExportHelper
import com.famiq.app.R
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    val transaksiList by viewModel.semuaTransaksi.collectAsStateWithLifecycle(initialValue = emptyList())
    val namaKeluarga by viewModel.namaKeluarga.collectAsStateWithLifecycle()
    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val isPersonalPro by viewModel.isPersonalPro.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.delete_all_data_q), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_all_data_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hapusSemuaTransaksi()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text(stringResource(R.string.delete_permanently)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.cancel), color = Color.Gray) }
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
                        Text(text = stringResource(R.string.back), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.data_backup),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.data_backup_desc),
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
            // ── EKSPOR LAPORAN ──
            Text(stringResource(R.string.export_report), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenAccent, letterSpacing = 1.sp)
            
            DataOptionCard(
                title = stringResource(R.string.download_pdf),
                description = stringResource(R.string.download_pdf_desc),
                icon = Icons.Outlined.PictureAsPdf,
                isLocked = !isPersonalPro && !isFamilyMode,
                onClick = {
                    if (isPersonalPro || isFamilyMode) {
                        val namaBulan = SimpleDateFormat("MMMM_yyyy", Locale.getDefault()).format(Date())
                        ExportHelper.exportToPDF(context, transaksiList, namaBulan, namaKeluarga)
                    } else {
                        navController.navigate("mode_selection")
                    }
                }
            )

            DataOptionCard(
                title = stringResource(R.string.export_csv),
                description = stringResource(R.string.export_csv_desc),
                icon = Icons.Outlined.TableChart,
                isLocked = !isPersonalPro && !isFamilyMode,
                onClick = {
                    if (isPersonalPro || isFamilyMode) {
                        val namaBulan = SimpleDateFormat("MMMM_yyyy", Locale.getDefault()).format(Date())
                        ExportHelper.exportToCSV(context, transaksiList, namaBulan, namaKeluarga)
                    } else {
                        navController.navigate("mode_selection")
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── MANAJEMEN DATABASE ──
            Text(stringResource(R.string.data_management_title), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenAccent, letterSpacing = 1.sp)

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showResetDialog = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFFFEF2F2), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = Color(0xFFDC2626))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.clear_all_data), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            Text(stringResource(R.string.clear_all_data_desc), fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataOptionCard(title: String, description: String, icon: ImageVector, isLocked: Boolean = false, onClick: () -> Unit) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(GreenSoft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = GreenMain, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onBg)
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.Lock, null, tint = GreenMain, modifier = Modifier.size(14.dp))
                    }
                }
                Text(description, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
