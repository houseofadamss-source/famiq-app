package com.famiq.app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.data.model.DebtType
import com.famiq.app.data.model.HutangPiutang
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HutangPiutangScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val semuaHutang by viewModel.semuaHutang.collectAsStateWithLifecycle()
    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val isPersonalPro by viewModel.isPersonalPro.collectAsStateWithLifecycle()

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    var showAddSheet by remember { mutableStateOf(false) }
    var selectedHutangForEdit by remember { mutableStateOf<HutangPiutang?>(null) }
    var showDeleteConfirmByData by remember { mutableStateOf<HutangPiutang?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = bgColor,
        floatingActionButton = {
            if (isFamilyMode || isPersonalPro) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = GreenMain,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Tambah Hutang")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 40.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.popBackStack() }.padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Outlined.ArrowBackIosNew, stringResource(R.string.back), tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.back), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Manajemen Hutang", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Pantau cicilan dan pinjaman keluarga", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }

            if (!isFamilyMode && !isPersonalPro) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(64.dp), tint = GreenMain.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Fitur ini khusus untuk pengguna Pro", fontWeight = FontWeight.Bold, color = onBg)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.navigate(Routes.MODE_SELECTION) }, colors = ButtonDefaults.buttonColors(containerColor = GreenMain)) {
                            Text("Upgrade Sekarang")
                        }
                    }
                }
            } else {
                if (semuaHutang.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada data hutang/piutang", color = onBg.copy(alpha = 0.4f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(semuaHutang) { data ->
                            HutangItemCard(
                                data = data, 
                                onClick = { selectedHutangForEdit = data },
                                onPayCicilan = { 
                                    val updated = it.copy(
                                        tenorTerbayar = it.tenorTerbayar + 1,
                                        nominalTerbayar = it.nominalTerbayar + it.nominalPerBulan,
                                        isLunas = (it.tenorTerbayar + 1) >= it.tenorTotal
                                    )
                                    viewModel.updateHutangRouter(updated)
                                    
                                    coroutineScope.launch {
                                        viewModel.tambahTransaksiRouter(
                                            nominal = it.nominalPerBulan,
                                            tipe = com.famiq.app.data.model.TransactionType.EXPENSE,
                                            kategori = com.famiq.app.data.model.Kategori.LAINNYA,
                                            catatan = "Bayar Cicilan: ${it.kontak} (${updated.tenorTerbayar}/${it.tenorTotal})",
                                            diinputOleh = updated.diinputOleh,
                                            isNeed = true,
                                            isDebtPayment = true
                                        )
                                    }
                                },
                                onMarkLunas = { viewModel.updateHutangRouter(it.copy(nominalTerbayar = it.nominalTotal, isLunas = true, tenorTerbayar = it.tenorTotal)) }, 
                                onDelete = { showDeleteConfirmByData = it }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false }, 
            sheetState = sheetState,
            containerColor = surfaceColor
        ) {
            AddHutangContent(
                viewModel = viewModel,
                onSave = { 
                    viewModel.tambahHutangRouter(it)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }

    if (selectedHutangForEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedHutangForEdit = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = surfaceColor
        ) {
            AddHutangContent(
                viewModel = viewModel,
                existingData = selectedHutangForEdit,
                onSave = { 
                    viewModel.updateHutangRouter(it)
                    selectedHutangForEdit = null
                },
                onCancel = { selectedHutangForEdit = null }
            )
        }
    }

    if (showDeleteConfirmByData != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmByData = null },
            title = { Text("Hapus Data?", fontWeight = FontWeight.Bold) },
            text = { Text("Yakin ingin menghapus catatan ini? Data yang sudah dihapus tidak bisa dikembalikan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hapusHutangRouter(showDeleteConfirmByData!!)
                        showDeleteConfirmByData = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Hapus Permanen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmByData = null }) { Text("Batal") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun HutangItemCard(
    data: HutangPiutang, 
    onClick: () -> Unit,
    onPayCicilan: (HutangPiutang) -> Unit,
    onMarkLunas: (HutangPiutang) -> Unit, 
    onDelete: (HutangPiutang) -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val progress = if (data.nominalTotal > 0) data.nominalTerbayar.toFloat() / data.nominalTotal.toFloat() else 0f
    val isHutang = data.tipe == DebtType.HUTANG

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(if (isHutang) Color(0xFFFEF2F2) else Color(0xFFF0FDF4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isHutang) Icons.Outlined.FileUpload else Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = if (isHutang) Color(0xFFDC2626) else GreenMain
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(data.kontak, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        if (data.isCicilan) "Cicilan ${data.tenorTerbayar}/${data.tenorTotal} Bln" 
                        else if (isHutang) "Saya berhutang" else "Menghutangkan", 
                        fontSize = 11.sp, color = Color.Gray
                    )
                }
                IconButton(onClick = { onDelete(data) }) {
                    Icon(Icons.Outlined.Delete, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(if(data.isCicilan) "Total & Per Bulan" else "Total", fontSize = 10.sp, color = Color.Gray)
                    Text("Rp ${formatRupiah(data.nominalTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    if(data.isCicilan) Text("Rp ${formatRupiah(data.nominalPerBulan)} / Bln", fontSize = 11.sp, color = GreenMain, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if(data.isCicilan) "Tagihan Berikutnya" else "Jatuh Tempo", fontSize = 10.sp, color = Color.Gray)
                    Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(data.jatuhTempo)), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = if (isHutang) Color(0xFFEF4444) else GreenMain,
                trackColor = onBg.copy(alpha = 0.05f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    if(data.isCicilan) "${data.tenorTotal - data.tenorTerbayar} Bln tersisa" 
                    else "${(progress * 100).toInt()}% terbayar", 
                    fontSize = 10.sp, color = Color.Gray
                )
                Text("Sisa: Rp ${formatRupiah(data.nominalTotal - data.nominalTerbayar)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (data.isLunas) GreenMain else onBg)
            }

            if (!data.isLunas) {
                Spacer(modifier = Modifier.height(16.dp))
                if (data.isCicilan) {
                    Button(
                        onClick = { onPayCicilan(data) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                    ) {
                        Text("Bayar Cicilan Bulan Ini", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onMarkLunas(data) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isHutang) Color(0xFF991B1B) else GreenDark)
                    ) {
                        Text("Tandai Lunas", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddHutangContent(
    viewModel: TransaksiViewModel,
    existingData: HutangPiutang? = null,
    onSave: (HutangPiutang) -> Unit, 
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val namaSaya by viewModel.namaSaya.collectAsStateWithLifecycle()
    
    var nama by remember { mutableStateOf(existingData?.kontak ?: "") }
    var nominal by remember { mutableStateOf(existingData?.nominalTotal?.toString() ?: "") }
    var tipe by remember { mutableStateOf(existingData?.tipe ?: DebtType.HUTANG) }
    var isCicilan by remember { mutableStateOf(existingData?.isCicilan ?: false) }
    var nominalCicilanManual by remember { mutableStateOf(existingData?.nominalPerBulan?.toString() ?: "") }
    var tenor by remember { mutableStateOf(existingData?.tenorTotal?.toString() ?: "12") }
    var tenorTerbayar by remember { mutableStateOf(existingData?.tenorTerbayar?.toString() ?: "0") }
    var tanggalTagihan by remember { mutableStateOf(existingData?.tanggalTagihan?.toString() ?: "5") }
    var catatan by remember { mutableStateOf(existingData?.catatan ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tambah Data Hutang/Piutang", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        Row(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(4.dp)) {
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if(tipe == DebtType.HUTANG) Color.White else Color.Transparent).clickable { tipe = DebtType.HUTANG }.padding(8.dp), contentAlignment = Alignment.Center) {
                Text("Hutang Saya", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(tipe == DebtType.HUTANG) GreenDark else Color.Gray)
            }
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if(tipe == DebtType.PIUTANG) Color.White else Color.Transparent).clickable { tipe = DebtType.PIUTANG }.padding(8.dp), contentAlignment = Alignment.Center) {
                Text("Piutang", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(tipe == DebtType.PIUTANG) GreenDark else Color.Gray)
            }
        }

        OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Kontak / Instansi") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        
        OutlinedTextField(
            value = nominal, 
            onValueChange = { if(it.all { c -> c.isDigit() }) nominal = it }, 
            label = { Text(if(isCicilan) "Total Plafon" else "Nominal Total") }, 
            modifier = Modifier.fillMaxWidth(), 
            prefix = { Text("Rp ") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            visualTransformation = RupiahVisualTransformation()
        )

        // --- TOGGLE CICILAN ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isCicilan = !isCicilan }) {
            Checkbox(checked = isCicilan, onCheckedChange = { isCicilan = it }, colors = CheckboxDefaults.colors(checkedColor = GreenMain))
            Text("Ini adalah Cicilan Rutin", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        if (isCicilan) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tenor, 
                    onValueChange = { if(it.all { c -> c.isDigit() }) tenor = it }, 
                    label = { Text("Tenor Total") }, 
                    modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                OutlinedTextField(
                    value = tenorTerbayar, 
                    onValueChange = { if(it.all { c -> c.isDigit() }) tenorTerbayar = it }, 
                    label = { Text("Sdh Bayar") }, 
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Bulan") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                OutlinedTextField(
                    value = tanggalTagihan, 
                    onValueChange = { if(it.all { c -> c.isDigit() }) tanggalTagihan = it }, 
                    label = { Text("Tgl") }, 
                    modifier = Modifier.weight(0.7f),
                    placeholder = { Text("1-31") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
            val nomVal = nominal.toLongOrNull() ?: 0L
            val tenorVal = tenor.toIntOrNull() ?: 1
            
            // Auto-Suggest Logic
            LaunchedEffect(nominal, tenor) {
                if (nomVal > 0 && tenorVal > 0 && nominalCicilanManual.isEmpty()) {
                    nominalCicilanManual = (nomVal / tenorVal).toString()
                }
            }

            OutlinedTextField(
                value = nominalCicilanManual,
                onValueChange = { if(it.all { c -> c.isDigit() }) nominalCicilanManual = it },
                label = { Text("Cicilan per Bulan") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("Rp ") },
                placeholder = { Text("Input manual jika ada selisih") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                visualTransformation = RupiahVisualTransformation()
            )

            if (nomVal > 0 && tenorVal > 0) {
                val pembagianMurni = nomVal / tenorVal
                if (nominalCicilanManual.toLongOrNull() != pembagianMurni) {
                    Text("Catatan: Angka manual digunakan untuk akurasi tagihan asli.", fontSize = 11.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
        
        OutlinedTextField(value = catatan, onValueChange = { catatan = it }, label = { Text("Catatan (Opsional)") }, modifier = Modifier.fillMaxWidth())
        
        Button(
            onClick = {
                val nom = nominal.toLongOrNull() ?: 0L
                val tenorVal = tenor.toIntOrNull() ?: 1
                val sdhBayar = tenorTerbayar.toIntOrNull() ?: 0
                val tglTagihan = tanggalTagihan.toIntOrNull() ?: 1
                
                if (nama.isNotEmpty() && nom > 0) {
                    isSaving = true
                    val cal = Calendar.getInstance().apply {
                        if (get(Calendar.DAY_OF_MONTH) > tglTagihan) add(Calendar.MONTH, 1)
                        set(Calendar.DAY_OF_MONTH, tglTagihan)
                    }
                    
                    val perBulan = if(isCicilan) (nominalCicilanManual.toLongOrNull() ?: (nom / tenorVal)) else 0L
                    val newData = HutangPiutang(
                        id = existingData?.id ?: UUID.randomUUID().toString(),
                        kontak = nama, 
                        nominalTotal = nom, 
                        tipe = tipe, 
                        jatuhTempo = cal.timeInMillis, 
                        catatan = catatan, 
                        diinputOleh = existingData?.diinputOleh ?: namaSaya,
                        isCicilan = isCicilan,
                        tenorTotal = tenorVal,
                        tenorTerbayar = sdhBayar,
                        nominalPerBulan = perBulan,
                        nominalTerbayar = if(isCicilan) perBulan * sdhBayar else existingData?.nominalTerbayar ?: 0L,
                        tanggalTagihan = tglTagihan,
                        isLunas = if(isCicilan) sdhBayar >= tenorVal else existingData?.isLunas ?: false
                    )
                    onSave(newData)
                } else {
                    Toast.makeText(context, "Nama dan Nominal wajib diisi!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenMain),
            enabled = !isSaving
        ) {
            if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else Text("Simpan Data", fontWeight = FontWeight.Bold)
        }
    }
}
