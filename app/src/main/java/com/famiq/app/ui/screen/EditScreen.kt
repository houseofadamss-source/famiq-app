package com.famiq.app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.data.model.Kategori
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavController,
    transaksiId: String? = null,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val daftarTransaksi by viewModel.semuaTransaksi.collectAsStateWithLifecycle()
    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val isPersonalPro by viewModel.isPersonalPro.collectAsStateWithLifecycle()
    val transaksiAktif = daftarTransaksi.find { it.id == transaksiId }

    var nominal by remember { mutableStateOf(transaksiAktif?.nominal?.toString() ?: "") }
    var catatan by remember { mutableStateOf(transaksiAktif?.catatan ?: "") }
    var kategoriAktif by remember { mutableStateOf(transaksiAktif?.kategori ?: Kategori.MAKAN) }
    var isNeed by remember { mutableStateOf(transaksiAktif?.isNeed ?: true) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(transaksiAktif) {
        if (transaksiAktif == null && transaksiId != null) {
            navController.popBackStack()
        }
    }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_expense), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    OutlinedTextField(
                        value = nominal,
                        onValueChange = { if (it.all { char -> char.isDigit() }) nominal = it },
                        label = { Text(stringResource(R.string.nominal_expense)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenMain,
                            cursorColor = GreenAccent
                        ),
                        singleLine = true,
                        prefix = { Text("Rp ", color = onBg.copy(alpha = 0.5f)) },
                        visualTransformation = RupiahVisualTransformation()
                    )

                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        label = { Text(stringResource(R.string.description)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenMain,
                            cursorColor = GreenAccent
                        ),
                        singleLine = true
                    )

                    if (transaksiAktif?.tipe == com.famiq.app.data.model.TransactionType.EXPENSE) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.wants_needs_label), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                if (!isPersonalPro && !isFamilyMode) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Outlined.Lock, null, tint = GreenMain, modifier = Modifier.size(14.dp))
                                }
                            }
                            
                            Row(
                                modifier = Modifier
                                    .background(onBg.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isNeed) GreenMain else Color.Transparent)
                                        .clickable(enabled = isPersonalPro || isFamilyMode) { isNeed = true }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(R.string.needs), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isNeed) Color.White else onBg.copy(alpha = 0.4f))
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isNeed) GreenMain else Color.Transparent)
                                        .clickable(enabled = isPersonalPro || isFamilyMode) { isNeed = false }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(R.string.wants), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isNeed) Color.White else onBg.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.select_category), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Kategori.entries.forEach { kat ->
                                val isSelected = kategoriAktif == kat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) GreenAccent else surfaceColor)
                                        .border(1.dp, if (isSelected) Color.Transparent else Color.LightGray, RoundedCornerShape(20.dp))
                                        .clickable { kategoriAktif = kat }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(getKategoriStringRes(kat)),
                                        color = if (isSelected) Color.White else onBg.copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val nom = nominal.toLongOrNull()
                    if (nom != null && nom > 0 && transaksiAktif != null) {
                        coroutineScope.launch {
                            isLoading = true
                            val finalIsNeed = if (isPersonalPro || isFamilyMode) isNeed else true
                            val sukses = viewModel.editTransaksiRouter(transaksiAktif, nom, kategoriAktif, catatan, finalIsNeed, transaksiAktif.isDebtPayment)
                            isLoading = false
                            if (sukses) {
                                Toast.makeText(context, context.getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, context.getString(R.string.failed_check_connection), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.enter_valid_nominal), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                Text(if (isLoading) stringResource(R.string.saving).uppercase() else stringResource(R.string.save_changes).uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
