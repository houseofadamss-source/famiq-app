package com.famiq.app.ui.screen

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.data.model.Kategori
import com.famiq.app.data.model.TransactionType
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val namaSaya by viewModel.namaSaya.collectAsStateWithLifecycle()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val googleName = currentUser?.displayName ?: stringResource(R.string.family_member)

    // STATE UTAMA: TIPE TRANSAKSI
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var nominal by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var kategoriAktif by remember { mutableStateOf(Kategori.MAKAN) }
    var isLoading by remember { mutableStateOf(false) }

    // Reset kategori saat ganti tipe
    LaunchedEffect(selectedType) {
        kategoriAktif = if (selectedType == TransactionType.EXPENSE) Kategori.MAKAN else Kategori.GAJI
    }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    // Animasi warna header berdasarkan tipe
    val headerColorStart by animateColorAsState(if (selectedType == TransactionType.EXPENSE) GreenDark else Color(0xFF1B5E20))
    val headerColorEnd by animateColorAsState(if (selectedType == TransactionType.EXPENSE) GreenMid else Color(0xFF43A047))

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            Box(
                modifier = Modifier
                    .background(surfaceColor.copy(alpha = 0.75f))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        val nom = nominal.toLongOrNull()
                        if (nom != null && nom > 0) {
                            coroutineScope.launch {
                                isLoading = true
                                val penginput = if (isFamilyMode) googleName else namaSaya
                                val result = viewModel.tambahTransaksiRouter(nom, selectedType, kategoriAktif, catatan, penginput)

                                if (result != null) {
                                    viewModel.setStatusBadgeNotif(true)
                                    if (isFamilyMode && result != "local") {
                                        launch(Dispatchers.IO) { 
                                            // Kasih delay dikit biar data sinkron ke cloud dulu
                                            delay(500)
                                            kirimNotifikasiOneSignal(context, result, penginput, nom, kategoriAktif, selectedType) 
                                        }
                                    }
                                    isLoading = false
                                    Toast.makeText(context, context.getString(R.string.success_saved), Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, context.getString(R.string.failed_try_again), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedType == TransactionType.EXPENSE) GreenMain else Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text(if (selectedType == TransactionType.EXPENSE) stringResource(R.string.save_expense) else stringResource(R.string.save_income), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── HEADER MEWAH DENGAN TOGGLE ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(colors = listOf(headerColorStart, headerColorEnd)),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
                    .padding(top = 8.dp, bottom = 48.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                        }
                        Text(stringResource(R.string.record_cash_flow), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // CUSTOM DUAL TOGGLE (EXPENSE vs INCOME)
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedType == TransactionType.EXPENSE) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                                .clickable { selectedType = TransactionType.EXPENSE },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.expense_toggle), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selectedType == TransactionType.EXPENSE) GreenDark else Color.White.copy(alpha = 0.7f))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedType == TransactionType.INCOME) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                                .clickable { selectedType = TransactionType.INCOME },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.income_toggle), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selectedType == TransactionType.INCOME) Color(0xFF1B5E20) else Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // ── INPUT CARD ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            if (selectedType == TransactionType.EXPENSE) stringResource(R.string.nominal_expense) else stringResource(R.string.nominal_income),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onBg.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        BasicTextField(
                            value = nominal,
                            onValueChange = { if (it.all { char -> char.isDigit() }) nominal = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = onBg),
                            cursorBrush = SolidColor(GreenMain),
                            visualTransformation = RupiahVisualTransformation(),
                            decorationBox = { innerTextField ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Rp ", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = onBg)
                                    if (nominal.isEmpty()) Text("0", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = onBg.copy(alpha = 0.2f))
                                    else innerTextField()
                                }
                            }
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        placeholder = { Text(if (selectedType == TransactionType.EXPENSE) stringResource(R.string.note_placeholder_expense) else stringResource(R.string.note_placeholder_income) , fontSize = 13.sp, color = onBg.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = GreenMain),
                        textStyle = TextStyle(fontSize = 14.sp, color = onBg)
                    )
                }

                Text(stringResource(R.string.select_category), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = onBg, modifier = Modifier.padding(start = 4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val listKategori = if (selectedType == TransactionType.EXPENSE) {
                        Kategori.entries.filter { it.name in listOf("MAKAN", "TRANSPORT", "BELANJA", "RUMAH", "KESEHATAN", "LAINNYA") }
                    } else {
                        Kategori.entries.filter { it.name in listOf("GAJI", "BONUS", "INVESTASI", "HADIAH", "PENDAPATAN_LAIN") }
                    }
                    
                    for (i in listKategori.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BoxKategori(kategori = listKategori[i], isSelected = kategoriAktif == listKategori[i], modifier = Modifier.weight(1f), onClick = { kategoriAktif = listKategori[i] })
                            if (i + 1 < listKategori.size) {
                                BoxKategori(kategori = listKategori[i + 1], isSelected = kategoriAktif == listKategori[i + 1], modifier = Modifier.weight(1f), onClick = { kategoriAktif = listKategori[i + 1] })
                            } else { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoxKategori(kategori: Kategori, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier.height(65.dp).clip(RoundedCornerShape(16.dp)).background(if (isSelected) GreenAccent else onBg.copy(alpha = 0.03f)).clickable { onClick() }.padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(38.dp).background(if (isSelected) Color.White.copy(alpha = 0.2f) else onBg.copy(alpha = 0.06f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = ambilIconGrid(kategori), contentDescription = null, modifier = Modifier.size(20.dp), tint = if (isSelected) Color.White else GreenMain)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(getKategoriStringRes(kategori)), color = if (isSelected) Color.White else onBg.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold)
        }
    }
}

private fun ambilIconGrid(kategori: Kategori): ImageVector =
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

private fun kirimNotifikasiOneSignal(context: android.content.Context, familyId: String, penginput: String, nominal: Long, kategori: Kategori, tipe: TransactionType) {
    try {
        val url = URL("https://onesignal.com/api/v1/notifications")
        val con = url.openConnection() as HttpURLConnection
        con.useCaches = false; con.doOutput = true; con.doInput = true; con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        con.setRequestProperty("Authorization", "Basic ${com.famiq.app.BuildConfig.ONESIGNAL_REST_KEY}")

        val kategoriStr = context.getString(getKategoriStringRes(kategori))
        val judul = if (tipe == TransactionType.EXPENSE) context.getString(R.string.notif_expense_title) else context.getString(R.string.notif_income_title)
        val isi = if (tipe == TransactionType.EXPENSE) 
            context.getString(R.string.notif_expense_content, penginput, kategoriStr.lowercase(), formatRupiah(nominal))
        else
            context.getString(R.string.notif_income_content, penginput, kategoriStr.lowercase(), formatRupiah(nominal))

        val body = """
            {
              "app_id": "${com.famiq.app.BuildConfig.ONESIGNAL_APP_ID}",
              "filters": [
                {"field": "tag", "key": "family_id", "relation": "=", "value": "$familyId"},
                {"operator": "AND"},
                {"field": "email", "relation": "!=", "value": "${com.famiq.app.GoogleAuthHelper.getEmailUser()}"}
              ],
              "headings": {"en": "$judul"},
              "contents": {"en": "$isi"}
            }
        """.trimIndent()

        val os = con.outputStream; os.write(body.toByteArray(Charsets.UTF_8)); os.close()
        println("OneSignal Status: ${con.responseCode}")
    } catch (e: Exception) { e.printStackTrace() }
}
