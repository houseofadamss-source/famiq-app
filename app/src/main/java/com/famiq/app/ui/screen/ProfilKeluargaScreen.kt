package com.famiq.app.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.famiq.app.FirestoreHelper
import com.famiq.app.R
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilKeluargaScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val semuaAnggota by viewModel.semuaAnggota.collectAsStateWithLifecycle(initialValue = emptyList())
    val namaKeluarga by viewModel.namaKeluarga.collectAsStateWithLifecycle()
    val fotoKeluargaUri by viewModel.fotoKeluargaUri.collectAsStateWithLifecycle()

    var tempNamaKeluarga by remember { mutableStateOf(namaKeluarga) }
    var generatedCode by remember { mutableStateOf("") }
    var showJoinDialog by remember { mutableStateOf(false) }
    var inputInviteCode by remember { mutableStateOf("") }
    var isLoadingCloud by remember { mutableStateOf(false) }

    LaunchedEffect(namaKeluarga) { tempNamaKeluarga = namaKeluarga }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val file = File(context.filesDir, "avatar_keluarga_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                viewModel.simpanFotoKeluargaUri(Uri.fromFile(file).toString())
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.failed_process_photo), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = bgColor
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── HEADER HIJAU GRADASI (KONSISTEN) ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .statusBarsPadding()
                        .padding(top = 16.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        // Tombol Kembali (Atas Kiri Header)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.offset(x = (-12).dp)
                            ) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                            }
                            Text(stringResource(R.string.family_profile), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // FOTO PROFIL (DI TENAH HEADER)
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (fotoKeluargaUri.isNotEmpty()) {
                                    AsyncImage(model = Uri.parse(fotoKeluargaUri), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                } else {
                                    Icon(Icons.Outlined.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-2).dp, y = (-2).dp)
                                    .size(30.dp)
                                    .background(Color.White, CircleShape)
                                    .border(2.dp, GreenMid, CircleShape)
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.change), tint = GreenMain, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // NAMA KELUARGA (DI TENGAH HEADER)
                        BasicTextField(
                            value = tempNamaKeluarga,
                            onValueChange = { tempNamaKeluarga = it },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                            decorationBox = { innerTextField ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    if (tempNamaKeluarga.isEmpty()) Text(stringResource(R.string.family_name), color = Color.White.copy(alpha = 0.5f), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                    innerTextField()
                                    if (tempNamaKeluarga != namaKeluarga) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = stringResource(R.string.save),
                                            tint = Color.White,
                                            modifier = Modifier.padding(start = 8.dp).size(20.dp).clickable {
                                                viewModel.simpanNamaKeluarga(tempNamaKeluarga)
                                                Toast.makeText(context, context.getString(R.string.name_updated), Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(start = 8.dp).size(16.dp))
                                    }
                                }
                            }
                        )
                    }
                }

                // ── CONTENT AREA (OVERLAP) ──
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-24).dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Spacer biar gak nabrak header pas awal scroll
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // ── 1. INFO LIMIT ANGGOTA ──
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenSoft.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GreenMid.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = GreenMid,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.family_member_limit_info),
                                    fontSize = 12.sp,
                                    color = GreenDark,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // ── 2. CLOUD SYNC SECTION ──
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.CloudSync, contentDescription = null, tint = GreenMain)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(R.string.cloud_sync), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                if (generatedCode.isNotEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().background(GreenSoft, RoundedCornerShape(16.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(stringResource(R.string.invite_code), color = GreenMain, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                            Text(text = generatedCode, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = GreenMain, letterSpacing = 4.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(stringResource(R.string.use_code_on_partner), color = onBg.copy(alpha = 0.5f), fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    Text(stringResource(R.string.cloud_sync_desc), fontSize = 13.sp, color = onBg.copy(alpha = 0.6f), lineHeight = 20.sp)
                                    Spacer(modifier = Modifier.height(20.dp))
                                    
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isLoadingCloud = true
                                                val kode = FirestoreHelper.buatKeluargaBaru(tempNamaKeluarga)
                                                if (kode != null) generatedCode = kode
                                                isLoadingCloud = false
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenMain),
                                        enabled = !isLoadingCloud
                                    ) {
                                        if (isLoadingCloud) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                        else Text(stringResource(R.string.create_family_network), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    OutlinedButton(
                                        onClick = { showJoinDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, onBg.copy(alpha = 0.1f))
                                    ) {
                                        Text(stringResource(R.string.join_network), color = onBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // ── 3. REGISTERED MEMBERS ──
                    item {
                        Text(stringResource(R.string.registered_members), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                    }

                    if (semuaAnggota.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.no_members_yet), color = onBg.copy(alpha = 0.4f), fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(items = semuaAnggota, key = { it.email }) { anggota ->
                            MemberItem(anggota = anggota, onRemove = { viewModel.hapusAnggotaKeluarga(anggota) })
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text(stringResource(R.string.enter_code), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputInviteCode,
                    onValueChange = { inputInviteCode = it.take(6).uppercase() },
                    placeholder = { Text(stringResource(R.string.example_code)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain),
                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = 4.sp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        isLoadingCloud = true
                        val result = FirestoreHelper.gabungKeluarga(inputInviteCode)
                        if (result == "FULL") {
                            Toast.makeText(context, context.getString(R.string.family_full), Toast.LENGTH_LONG).show()
                        } else if (result != null) {
                            viewModel.simpanNamaKeluarga(result)
                            showJoinDialog = false
                        }
                        isLoadingCloud = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = GreenMain)) { Text(stringResource(R.string.join)) }
            },
            dismissButton = { TextButton(onClick = { showJoinDialog = false }) { Text(stringResource(R.string.cancel), color = Color.Gray) } },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun MemberItem(anggota: com.famiq.app.data.model.Anggota, onRemove: () -> Unit) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(GreenSoft), contentAlignment = Alignment.Center) {
                if (anggota.fotoUrl.isNotEmpty()) {
                    AsyncImage(model = anggota.fotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text(text = anggota.nama.take(1).uppercase(), color = GreenMain, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = anggota.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onBg)
                Text(text = anggota.email, fontSize = 12.sp, color = onBg.copy(alpha = 0.5f))
            }
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
            }
        }
    }
}
