package com.famiq.app.ui.screen

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.famiq.app.ConnectionStatus
import com.famiq.app.WeatherHelper
import com.famiq.app.data.model.Kategori
import com.famiq.app.data.model.Transaksi
import com.famiq.app.data.model.TransactionType
import com.famiq.app.ui.components.BottomNavBar
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import com.famiq.app.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BerandaScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val transaksiList by viewModel.semuaTransaksi.collectAsStateWithLifecycle(initialValue = emptyList())

    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val namaKeluarga by viewModel.namaKeluarga.collectAsStateWithLifecycle()
    val namaSaya by viewModel.namaSaya.collectAsStateWithLifecycle()
    val weatherData by WeatherHelper.weather.collectAsStateWithLifecycle()
    val fotoKeluargaUri by viewModel.fotoKeluargaUri.collectAsStateWithLifecycle()
    val targetBulanan by viewModel.targetBulanan.collectAsStateWithLifecycle()
    val tanggalSiklusGajian by viewModel.tanggalSiklusGajian.collectAsStateWithLifecycle()
    val hideBalance by viewModel.hideBalance.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    
    var hasShownOfflineDialog by remember { mutableStateOf(false) }
    var showOfflineDialog by remember { mutableStateOf(false) }
    val statusBadgeNotif by viewModel.statusBadgeNotif.collectAsStateWithLifecycle()
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsStateWithLifecycle()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val googlePhotoUrl = currentUser?.photoUrl

    var filterAktif by remember { mutableStateOf<Kategori?>(null) }
    var transaksiEdit by remember { mutableStateOf<Transaksi?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgColor = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface

    LaunchedEffect(connectionStatus) {
        if (connectionStatus == ConnectionStatus.Unavailable && !hasShownOfflineDialog) {
            showOfflineDialog = true; hasShownOfflineDialog = true
        }
    }

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..10 -> stringResource(R.string.good_morning)
        in 11..14 -> stringResource(R.string.good_afternoon)
        in 15..17 -> stringResource(R.string.good_evening)
        else -> stringResource(R.string.good_night)
    }

    val rentangSiklus = remember(tanggalSiklusGajian) {
        val sekarang = Calendar.getInstance()
        val tglHariIni = sekarang.get(Calendar.DAY_OF_MONTH)
        val mulaiSiklus = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val selesaiSiklus = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }

        if (tglHariIni >= tanggalSiklusGajian) {
            mulaiSiklus.set(Calendar.DAY_OF_MONTH, tanggalSiklusGajian)
            selesaiSiklus.add(Calendar.MONTH, 1)
            selesaiSiklus.set(Calendar.DAY_OF_MONTH, if (tanggalSiklusGajian == 1) selesaiSiklus.getActualMaximum(Calendar.DAY_OF_MONTH) else tanggalSiklusGajian - 1)
        } else {
            mulaiSiklus.add(Calendar.MONTH, -1)
            mulaiSiklus.set(Calendar.DAY_OF_MONTH, minOf(tanggalSiklusGajian, mulaiSiklus.getActualMaximum(Calendar.DAY_OF_MONTH)))
            selesaiSiklus.set(Calendar.DAY_OF_MONTH, if (tanggalSiklusGajian == 1) selesaiSiklus.getActualMaximum(Calendar.DAY_OF_MONTH) else selesaiSiklus.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        Pair(mulaiSiklus, selesaiSiklus)
    }

    val labelInfoSiklus = remember(rentangSiklus, tanggalSiklusGajian) {
        val sdfLabel = SimpleDateFormat("dd MMM", Locale.getDefault())
        val cycleLabel = if (tanggalSiklusGajian == 1) context.getString(R.string.gregorian) else context.getString(R.string.cycle)
        "$cycleLabel: ${sdfLabel.format(rentangSiklus.first.time)} – ${sdfLabel.format(rentangSiklus.second.time)}"
    }

    val transaksiSiklus = remember(transaksiList, rentangSiklus) {
        transaksiList.filter { it.tanggal in rentangSiklus.first.timeInMillis..rentangSiklus.second.timeInMillis }
    }
    val totalPemasukan = remember(transaksiSiklus) { transaksiSiklus.filter { it.tipe == TransactionType.INCOME }.sumOf { it.nominal } }
    val totalPengeluaran = remember(transaksiSiklus) { transaksiSiklus.filter { it.tipe == TransactionType.EXPENSE }.sumOf { it.nominal } }
    val sisaSaldo = totalPemasukan - totalPengeluaran
    
    val transaksiFiltered = remember(transaksiList, filterAktif) { transaksiList.filter { filterAktif == null || it.kategori == filterAktif } }

    LaunchedEffect(Unit) { viewModel.autoSyncGoogleProfile() }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Scaffold(
            bottomBar = { BottomNavBar(navController) },
            containerColor = bgColor
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize()) {
                
                AnimatedVisibility(visible = connectionStatus == ConnectionStatus.Unavailable, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Surface(color = Color(0xFF424242), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CloudOff, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.offline_mode_active), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(colors = listOf(GreenDark, GreenMid)), shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)).statusBarsPadding().padding(18.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GreenAccent.copy(alpha = 0.3f))
                                    .border(1.5.dp, GreenMain.copy(alpha = 0.6f), CircleShape)
                                    .clickable { navController.navigate(Routes.SETTINGS) }, // Klik foto masuk ke settings
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFamilyMode && fotoKeluargaUri.isNotEmpty()) AsyncImage(model = Uri.parse(fotoKeluargaUri), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                else if (!isFamilyMode && googlePhotoUrl != null) AsyncImage(model = googlePhotoUrl, null, modifier = Modifier.size(44.dp), contentScale = ContentScale.Crop)
                                else Text(text = (if(isFamilyMode) namaKeluarga else namaSaya).take(1).uppercase(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                
                                // ✅ RED DOT FOR UPDATE
                                if (isUpdateAvailable) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(10.dp)
                                            .background(Color.Red, CircleShape)
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = greeting, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
                                    if (weatherData != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Outlined.WbSunny, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                                            Text(text = "${weatherData!!.suhu}°C", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                Text(text = if (isFamilyMode) namaKeluarga else namaSaya, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.12f)).clickable { viewModel.setStatusBadgeNotif(false); navController.navigate("timeline") }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                if (statusBadgeNotif) Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp).size(8.dp).background(Color(0xFFDC2626), CircleShape))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = stringResource(R.string.balance_remaining), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Outlined.DateRange, null, tint = Color(0xFFFCD34D), modifier = Modifier.size(11.dp))
                                        Text(text = labelInfoSiklus, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                
                                val mask = "Rp ••••••"
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = if(hideBalance) mask else "Rp ${formatRupiah(sisaSaldo)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(imageVector = if (hideBalance) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp).clickable { viewModel.setHideBalance(!hideBalance) })
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.income), color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                        Text(if(hideBalance) "••••" else "Rp ${formatRupiah(totalPemasukan)}", color = Color(0xFF81C784), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.expense), color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                        Text(if(hideBalance) "••••" else "Rp ${formatRupiah(totalPengeluaran)}", color = Color(0xFFE57373), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (targetBulanan > 0) {
                                    val progress = (totalPengeluaran.toFloat() / targetBulanan.toFloat()).coerceIn(0f, 1f)
                                    Spacer(modifier = Modifier.height(14.dp)); HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "${stringResource(R.string.budget_limit)}: ${if(hideBalance) "•••" else "Rp ${formatRupiah(targetBulanan)} "}", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        Text(text = "${(progress * 100).toInt()}% ${stringResource(R.string.used)}", color = if (progress >= 0.85f) Color(0xFFFCA5A5) else Color(0xFFA7F3D0), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)), color = if (progress >= 0.85f) Color(0xFFEF4444) else Color(0xFF34D399), trackColor = Color.White.copy(alpha = 0.15f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipItem(label = stringResource(R.string.all), isSelected = filterAktif == null, onClick = { filterAktif = null }, icon = Icons.Outlined.GridView)
                    Kategori.entries.forEach { kat -> FilterChipItem(label = stringResource(getKategoriStringRes(kat)), isSelected = filterAktif == kat, onClick = { filterAktif = if (filterAktif == kat) null else kat }, icon = ikonVektorKategori(kat)) }
                }
                
                Spacer(modifier = Modifier.height(12.dp)); Text(text = if (filterAktif != null) stringResource(getKategoriStringRes(filterAktif!!)) else stringResource(R.string.all).uppercase(), modifier = Modifier.padding(horizontal = 16.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onBg.copy(alpha = 0.4f), letterSpacing = 0.8.sp)

                if (transaksiFiltered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { Text(stringResource(R.string.no_transactions), color = onBg.copy(alpha = 0.45f), fontSize = 14.sp) }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 4.dp, bottom = paddingValues.calculateBottomPadding() + 16.dp)) {
                        items(items = transaksiFiltered, key = { it.id }) { transaksi ->
                            SwipeToDeleteCard(transaksi = transaksi, onDelete = { viewModel.hapusTransaksi(transaksi) }, onEdit = { transaksiEdit = transaksi })
                        }
                    }
                }
            }
        }
    }

    if (showOfflineDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineDialog = false },
            icon = { Icon(Icons.Outlined.WifiOff, null, tint = GreenMain, modifier = Modifier.size(32.dp)) },
            title = { Text(stringResource(R.string.connection_lost), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.connection_lost_desc), textAlign = TextAlign.Center, fontSize = 13.sp) },
            confirmButton = { Button(onClick = { showOfflineDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GreenMain), shape = RoundedCornerShape(10.dp)) { Text(stringResource(R.string.i_understand), fontWeight = FontWeight.Bold) } },
            shape = RoundedCornerShape(24.dp), containerColor = surfaceColor
        )
    }

    if (transaksiEdit != null) {
        ModalBottomSheet(onDismissRequest = { transaksiEdit = null }, sheetState = sheetState, containerColor = surfaceColor, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
            var nominalEdit by remember { mutableStateOf(transaksiEdit!!.nominal.toString()) }
            var catatanEdit by remember { mutableStateOf(transaksiEdit!!.catatan) }
            var kategoriEdit by remember { mutableStateOf(transaksiEdit!!.kategori) }
            var isSaving by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(stringResource(R.string.edit_note), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onBg)
                OutlinedTextField(value = nominalEdit, onValueChange = { if (it.all { char -> char.isDigit() }) nominalEdit = it }, label = { Text(stringResource(R.string.nominal)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), prefix = { Text("Rp ", color = onBg.copy(alpha = 0.5f)) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain), visualTransformation = RupiahVisualTransformation(), singleLine = true)
                OutlinedTextField(value = catatanEdit, onValueChange = { catatanEdit = it }, label = { Text(stringResource(R.string.note_label)) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain), singleLine = true)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.category), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = onBg.copy(alpha = 0.7f))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val currentList = if (transaksiEdit!!.tipe == TransactionType.EXPENSE) 
                            Kategori.entries.filter { it.name in listOf("MAKAN", "TRANSPORT", "BELANJA", "RUMAH", "KESEHATAN", "LAINNYA") }
                        else
                            Kategori.entries.filter { it.name in listOf("GAJI", "BONUS", "INVESTASI", "HADIAH", "PENDAPATAN_LAIN") }
                        
                        currentList.forEach { kat ->
                            val isSelected = kategoriEdit == kat
                            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) GreenAccent else Color.Transparent).border(1.dp, if (isSelected) Color.Transparent else Color.LightGray, RoundedCornerShape(20.dp)).clickable { kategoriEdit = kat }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                Text(text = stringResource(getKategoriStringRes(kat)), color = if (isSelected) Color.White else onBg.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Button(onClick = { val nom = nominalEdit.toLongOrNull(); if (nom != null && nom > 0) { coroutineScope.launch { isSaving = true; viewModel.editTransaksiRouter(transaksiEdit!!, nom, kategoriEdit, catatanEdit); isSaving = false; sheetState.hide(); transaksiEdit = null } } }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = GreenMain), enabled = !isSaving) { Text(if (isSaving) stringResource(R.string.saving) else stringResource(R.string.save_changes), fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun FilterChipItem(label: String, isSelected: Boolean, onClick: () -> Unit, icon: ImageVector? = null) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier.clickable { onClick() }.background(color = if (isSelected) GreenAccent else surfaceColor, shape = RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (icon != null) Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (isSelected) Color.White else onBg.copy(alpha = 0.55f))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) Color.White else onBg.copy(alpha = 0.55f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteCard(transaksi: Transaksi, onDelete: () -> Unit, onEdit: () -> Unit = {}) {
    var showDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { value -> if (value == SwipeToDismissBoxValue.EndToStart) showDialog = true; false })

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text(stringResource(R.string.delete_note_q), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_note_desc), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
            confirmButton = { Button(onClick = { showDialog = false; onDelete() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) { Text(stringResource(R.string.delete), color = Color.White) } },
            dismissButton = { OutlinedButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFDC2626), RoundedCornerShape(13.dp)).padding(end = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete), tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    ) { TransaksiItemCard(transaksi, onClick = onEdit) }
}

@Composable
fun TransaksiItemCard(transaksi: Transaksi, onClick: () -> Unit = {}) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = surfaceColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(if(transaksi.tipe == TransactionType.INCOME) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = ikonVektorKategori(transaksi.kategori), contentDescription = null, modifier = Modifier.size(20.dp), tint = if(transaksi.tipe == TransactionType.INCOME) Color(0xFF2E7D32) else GreenAccent)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaksi.catatan.ifEmpty { stringResource(getKategoriStringRes(transaksi.kategori)) }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = onBg)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(transaksi.diinputOleh, fontSize = 10.sp, color = GreenAccent, fontWeight = FontWeight.Bold)
                    Text("•", fontSize = 10.sp, color = onBg.copy(alpha = 0.3f))
                    Text(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(transaksi.tanggal)), fontSize = 10.sp, color = onBg.copy(alpha = 0.4f))
                }
            }
            val prefix = if(transaksi.tipe == TransactionType.INCOME) "+Rp " else "-Rp "
            val textColor = if(transaksi.tipe == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFDC2626)
            Text(text = "$prefix${formatRupiah(transaksi.nominal)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

fun ikonVektorKategori(kategori: Kategori): ImageVector =
    when (kategori) {
        Kategori.MAKAN -> Icons.Outlined.Restaurant
        Kategori.TRANSPORT -> Icons.Outlined.DirectionsCar
        Kategori.BELANJA -> Icons.Outlined.ShoppingBag
        Kategori.RUMAH -> Icons.Outlined.Home
        Kategori.KESEHATAN -> Icons.Outlined.MedicalServices
        Kategori.LAINNYA -> Icons.Outlined.Category
        Kategori.GAJI -> Icons.Outlined.Payments
        Kategori.BONUS -> Icons.Outlined.CardGiftcard
        Kategori.INVESTASI -> Icons.Outlined.TrendingUp
        Kategori.HADIAH -> Icons.Outlined.VolunteerActivism
        Kategori.PENDAPATAN_LAIN -> Icons.Outlined.MoreHoriz
    }
