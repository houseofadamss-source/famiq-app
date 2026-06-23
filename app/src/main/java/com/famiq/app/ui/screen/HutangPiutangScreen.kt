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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HutangPiutangScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    val semuaHutang by viewModel.semuaHutang.collectAsStateWithLifecycle()
    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val isPersonalPro by viewModel.isPersonalPro.collectAsStateWithLifecycle()

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    var showAddSheet by remember { mutableStateOf(false) }
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
                            HutangItemCard(data, onAction = { viewModel.updateHutangRouter(it) }, onDelete = { viewModel.hapusHutangRouter(it) })
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }, sheetState = sheetState) {
            AddHutangContent(
                onSave = { 
                    viewModel.tambahHutangRouter(it)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }
}

@Composable
fun HutangItemCard(data: HutangPiutang, onAction: (HutangPiutang) -> Unit, onDelete: (HutangPiutang) -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val progress = if (data.nominalTotal > 0) data.nominalTerbayar.toFloat() / data.nominalTotal.toFloat() else 0f
    val isHutang = data.tipe == DebtType.HUTANG

    Card(
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
                    Text(if (isHutang) "Saya berhutang" else "Menghutangkan", fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = { onDelete(data) }) {
                    Icon(Icons.Outlined.Delete, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total", fontSize = 10.sp, color = Color.Gray)
                    Text("Rp ${formatRupiah(data.nominalTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Jatuh Tempo", fontSize = 10.sp, color = Color.Gray)
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
                Text("${(progress * 100).toInt()}% terbayar", fontSize = 10.sp, color = Color.Gray)
                Text("Sisa: Rp ${formatRupiah(data.nominalTotal - data.nominalTerbayar)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (data.isLunas) GreenMain else onBg)
            }

            if (!data.isLunas) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        // Sederhananya, anggap langsung lunas atau tambah bayar
                        onAction(data.copy(nominalTerbayar = data.nominalTotal, isLunas = true))
                    },
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

@Composable
fun AddHutangContent(onSave: (HutangPiutang) -> Unit, onCancel: () -> Unit) {
    var nama by remember { mutableStateOf("") }
    var nominal by remember { mutableStateOf("") }
    var tipe by remember { mutableStateOf(DebtType.HUTANG) }
    var catatan by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tambah Data Hutang/Piutang", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        Row(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(4.dp)) {
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if(tipe == DebtType.HUTANG) Color.White else Color.Transparent).clickable { tipe = DebtType.HUTANG }.padding(8.dp), contentAlignment = Alignment.Center) {
                Text("Hutang Saya", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if(tipe == DebtType.PIUTANG) Color.White else Color.Transparent).clickable { tipe = DebtType.PIUTANG }.padding(8.dp), contentAlignment = Alignment.Center) {
                Text("Piutang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Kontak / Instansi") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nominal, onValueChange = { if(it.all { c -> c.isDigit() }) nominal = it }, label = { Text("Nominal Total") }, modifier = Modifier.fillMaxWidth(), prefix = { Text("Rp ") })
        OutlinedTextField(value = catatan, onValueChange = { catatan = it }, label = { Text("Catatan (Opsional)") }, modifier = Modifier.fillMaxWidth())
        
        Button(
            onClick = {
                val nom = nominal.toLongOrNull() ?: 0L
                if (nama.isNotEmpty() && nom > 0) {
                    onSave(HutangPiutang(kontak = nama, nominalTotal = nom, tipe = tipe, jatuhTempo = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), catatan = catatan, diinputOleh = "Saya"))
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
        ) {
            Text("Simpan Data", fontWeight = FontWeight.Bold)
        }
    }
}
