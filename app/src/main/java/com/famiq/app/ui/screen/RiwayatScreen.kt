package com.famiq.app.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.ExportHelper
import com.famiq.app.R
import com.famiq.app.data.model.Kategori
import com.famiq.app.data.model.Transaksi
import com.famiq.app.data.model.TransactionType
import com.famiq.app.ui.components.BottomNavBar
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Buat ngatur mode Bottom Sheet
enum class SheetContent {
    NONE, MENU, EDIT, DELETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val transaksiList by viewModel.semuaTransaksi.collectAsStateWithLifecycle(initialValue = emptyList())
    val namaKeluarga by viewModel.namaKeluarga.collectAsStateWithLifecycle()

    val bgColor = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }

    var selectedTransaksi by remember { mutableStateOf<Transaksi?>(null) }
    var sheetContent by remember { mutableStateOf(SheetContent.NONE) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    var searchAktif by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val todayStr = stringResource(R.string.today)
    val yesterdayStr = stringResource(R.string.yesterday)

    val transaksiFiltered = remember(transaksiList, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            transaksiList
        } else {
            transaksiList.filter {
                it.catatan.contains(searchQuery, ignoreCase = true) ||
                        it.kategori.name.contains(searchQuery, ignoreCase = true) ||
                        it.diinputOleh.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val groupedTransaksi = remember(transaksiFiltered, todayStr, yesterdayStr) {
        val todayFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val yesterdayFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000))

        transaksiFiltered.groupBy { trx ->
            val trxFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(trx.tanggal))
            when (trxFmt) {
                todayFmt -> todayStr
                yesterdayFmt -> yesterdayStr
                else -> SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(trx.tanggal))
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

    LaunchedEffect(searchAktif) {
        if (searchAktif) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    if (sheetContent != SheetContent.NONE && selectedTransaksi != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetContent = SheetContent.NONE },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            when (sheetContent) {
                SheetContent.MENU -> {
                    Column(modifier = Modifier.padding(bottom = 32.dp)) {
                        Text(
                            text = stringResource(R.string.transaction_options),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sheetContent = SheetContent.EDIT }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, tint = GreenAccent)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.edit_expense), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sheetContent = SheetContent.DELETE }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = Red)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.delete_transaction), fontSize = 16.sp, color = Red, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                SheetContent.DELETE -> {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Text(stringResource(R.string.delete_transaction), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Red)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.delete_confirm_desc),
                            color = onBg.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { sheetContent = SheetContent.NONE }) {
                                Text(stringResource(R.string.cancel), color = onBg)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    viewModel.hapusTransaksi(selectedTransaksi!!)
                                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) sheetContent = SheetContent.NONE
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Red),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text(stringResource(R.string.yes_delete), fontWeight = FontWeight.Bold) }
                        }
                    }
                }
                SheetContent.EDIT -> {
                    var editNominal by remember { mutableStateOf(selectedTransaksi!!.nominal.toString()) }
                    var editCatatan by remember { mutableStateOf(selectedTransaksi!!.catatan) }
                    var editKategori by remember { mutableStateOf(selectedTransaksi!!.kategori) }
                    var isSaving by remember { mutableStateOf(false) }
                    var expandedKategori by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text(stringResource(R.string.edit_expense), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = editNominal,
                            onValueChange = { if (it.all { char -> char.isDigit() }) editNominal = it },
                            label = { Text(stringResource(R.string.nominal_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain),
                            visualTransformation = RupiahVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editCatatan,
                            onValueChange = { editCatatan = it },
                            label = { Text(stringResource(R.string.note_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box {
                            OutlinedTextField(
                                value = stringResource(getKategoriStringRes(editKategori)),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.category_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain),
                                trailingIcon = {
                                    IconButton(onClick = { expandedKategori = true }) {
                                        Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expandedKategori,
                                onDismissRequest = { expandedKategori = false }
                            ) {
                                Kategori.entries.forEach { kat ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(getKategoriStringRes(kat))) },
                                        onClick = {
                                            editKategori = kat
                                            expandedKategori = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { sheetContent = SheetContent.NONE },
                                enabled = !isSaving
                            ) { Text(stringResource(R.string.cancel), color = onBg) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    val nom = editNominal.toLongOrNull()
                                    if (nom != null && nom > 0) {
                                        coroutineScope.launch {
                                            isSaving = true
                                            viewModel.editTransaksiRouter(selectedTransaksi!!, nom, editKategori, editCatatan)
                                            sheetState.hide()
                                        }.invokeOnCompletion {
                                            if (!sheetState.isVisible) sheetContent = SheetContent.NONE
                                        }
                                    }
                                },
                                enabled = !isSaving,
                                colors = ButtonDefaults.buttonColors(containerColor = GreenMain),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                else Text(stringResource(R.string.save_changes), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.download_report_title), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text(stringResource(R.string.download_report_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        val namaBulan = SimpleDateFormat("MMMM_yyyy", Locale.getDefault()).format(Date())
                        ExportHelper.exportToPDF(context, transaksiList, namaBulan, namaKeluarga)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val namaBulan = SimpleDateFormat("MMMM_yyyy", Locale.getDefault()).format(Date())
                        ExportHelper.exportToCSV(context, transaksiList, namaBulan, namaKeluarga)
                        showExportDialog = false
                    }
                ) {
                    Icon(Icons.Outlined.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel (CSV)", color = GreenAccent)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = bgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 42.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.transaction_history),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            IconButton(
                                onClick = { showExportDialog = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = stringResource(R.string.download_report),
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.history_desc),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(modifier = Modifier.fillMaxWidth().background(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)).clickable { searchAktif = true }.padding(horizontal = 14.dp, vertical = 11.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                if (searchAktif) {
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
                                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                        cursorBrush = SolidColor(Color.White),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                                        decorationBox = { innerTextField ->
                                            if (searchQuery.isEmpty()) Text(stringResource(R.string.search_transaction_hint), color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                                            innerTextField()
                                        }
                                    )
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = stringResource(R.string.close),
                                        tint = Color.White.copy(alpha = 0.55f),
                                        modifier = Modifier.size(16.dp).clickable {
                                            if (searchQuery.isNotEmpty()) {
                                                searchQuery = ""
                                            } else {
                                                searchAktif = false
                                                keyboardController?.hide()
                                            }
                                        }
                                    )
                                } else {
                                    Text(text = stringResource(R.string.search_transaction_label), color = Color.White.copy(alpha = 0.45f), fontSize = 13.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .offset(y = (-32).dp)
                ) {
                    if (!searchAktif || searchQuery.isEmpty()) {
                        ModernTrendChart(transaksiList = transaksiList)
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.showing_results, transaksiFiltered.size),
                            modifier = Modifier.padding(horizontal = 16.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onBg.copy(alpha = 0.4f), letterSpacing = 0.8.sp
                        )
                    }

                    if (transaksiFiltered.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) stringResource(R.string.no_search_results) else stringResource(R.string.no_history),
                                color = onBg.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 4.dp, bottom = paddingValues.calculateBottomPadding() + 16.dp)
                        ) {
                            groupedTransaksi.forEach { (tanggalHeader, listTrx) ->
                                item {
                                    Text(
                                        text = tanggalHeader,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onBg.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp, start = 4.dp)
                                    )
                                }

                                items(items = listTrx, key = { it.id }) { transaksi ->
                                    RiwayatItemCard(
                                        transaksi = transaksi,
                                        onClick = {
                                            selectedTransaksi = transaksi
                                            sheetContent = SheetContent.MENU
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernTrendChart(transaksiList: List<Transaksi>) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    var tabIndex by remember { mutableStateOf(0) }
    var selectedPointIndex by remember { mutableStateOf(-1) }

    val weeklyTotals = remember(transaksiList) {
        val arr = FloatArray(7)
        val now = Calendar.getInstance().apply { 
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) 
        }.timeInMillis
        val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)

        transaksiList.filter { it.tanggal >= sevenDaysAgo && it.tipe == TransactionType.EXPENSE }.forEach { trx ->
            val cal = Calendar.getInstance().apply { timeInMillis = trx.tanggal }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            // Sen=0, Sel=1, ..., Min=6
            val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
            if (index in 0..6) arr[index] += trx.nominal.toFloat()
        }
        arr
    }
    val weeklyLabels = listOf(
        stringResource(R.string.mon),
        stringResource(R.string.tue),
        stringResource(R.string.wed),
        stringResource(R.string.thu),
        stringResource(R.string.fri),
        stringResource(R.string.sat),
        stringResource(R.string.sun)
    )

    val monthlyTotals = remember(transaksiList) {
        val arr = FloatArray(6)
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        transaksiList.forEach { trx ->
            if (trx.tipe == TransactionType.EXPENSE) {
                val trxCal = Calendar.getInstance().apply { timeInMillis = trx.tanggal }
                val trxMonth = trxCal.get(Calendar.MONTH)
                val trxYear = trxCal.get(Calendar.YEAR)

                val monthDiff = (currentYear - trxYear) * 12 + (currentMonth - trxMonth)
                if (monthDiff in 0..5) {
                    arr[5 - monthDiff] += trx.nominal.toFloat()
                }
            }
        }
        arr
    }
    val monthlyLabels = remember {
        val labels = mutableListOf<String>()
        val formatter = SimpleDateFormat("MMM", Locale.getDefault())
        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            labels.add(formatter.format(cal.time))
        }
        labels
    }

    val currentData = if (tabIndex == 0) weeklyTotals else monthlyTotals
    val currentLabels = if (tabIndex == 0) weeklyLabels else monthlyLabels
    val maxTotal = currentData.maxOrNull()?.takeIf { it > 0f } ?: 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.expense_trend),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = onBg
                    )
                    if (selectedPointIndex != -1) {
                        Text(
                            text = "${currentLabels[selectedPointIndex]}: Rp ${formatRupiah(currentData[selectedPointIndex].toLong())}",
                            fontSize = 11.sp,
                            color = GreenAccent,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.tap_point_detail),
                            fontSize = 11.sp,
                            color = onBg.copy(alpha = 0.4f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .background(onBg.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (tabIndex == 0) surfaceColor else Color.Transparent)
                            .clickable { 
                                tabIndex = 0
                                selectedPointIndex = -1
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.days_7),
                            fontSize = 10.sp,
                            fontWeight = if (tabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (tabIndex == 0) GreenMain else onBg.copy(alpha = 0.5f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (tabIndex == 1) surfaceColor else Color.Transparent)
                            .clickable { 
                                tabIndex = 1
                                selectedPointIndex = -1
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.months_6),
                            fontSize = 10.sp,
                            fontWeight = if (tabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (tabIndex == 1) GreenMain else onBg.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .pointerInput(currentData, tabIndex) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val spacing = width / (currentData.size - 1).coerceAtLeast(1)
                            val clickedIndex = (offset.x / spacing + 0.5f).toInt().coerceIn(0, currentData.size - 1)
                            selectedPointIndex = clickedIndex
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val spacing = width / (currentData.size - 1).coerceAtLeast(1)

                val linePath = Path()
                val fillPath = Path()

                var lastX = 0f

                currentData.forEachIndexed { index, value ->
                    val x = index * spacing
                    val normalizedValue = if (maxTotal == 1f && value == 0f) 0.05f else (value / maxTotal).coerceAtLeast(0.05f)
                    val y = height - (normalizedValue * height * 0.8f)

                    if (index == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                    lastX = x
                    
                    // Gambar titik interaktif
                    if (index == selectedPointIndex) {
                        drawCircle(
                            color = GreenAccent,
                            radius = 6.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }

                fillPath.lineTo(lastX, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(GreenAccent.copy(alpha = 0.35f), Color.Transparent)
                    )
                )

                drawPath(
                    path = linePath,
                    color = GreenAccent,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                currentLabels.forEachIndexed { index, label ->
                    val isToday = tabIndex == 0 && index == (Calendar.getInstance().get(Calendar.DAY_OF_WEEK).let { if (it == Calendar.SUNDAY) 6 else it - 2 })
                    val isSelected = index == selectedPointIndex
                    
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) GreenMain else if (isToday) GreenAccent else onBg.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun RiwayatItemCard(transaksi: Transaksi, onClick: () -> Unit) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                IkonKategori(kategori = transaksi.kategori)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaksi.catatan.ifEmpty { stringResource(getKategoriStringRes(transaksi.kategori)) },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onBg
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(transaksi.tanggal)) + stringResource(R.string.time_suffix),
                    fontSize = 11.sp,
                    color = onBg.copy(alpha = 0.6f)
                )

                Text(
                    text = stringResource(R.string.by, transaksi.diinputOleh),
                    fontSize = 10.sp,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            val prefix = if (transaksi.tipe == TransactionType.INCOME) "+Rp " else "-Rp "
            val textColor = if (transaksi.tipe == TransactionType.INCOME) GreenMain else Red
            Text(
                text = "$prefix${formatRupiah(transaksi.nominal)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun IkonKategori(kategori: Kategori, modifier: Modifier = Modifier) {
    val icon = when (kategori) {
        Kategori.MAKAN -> Icons.Outlined.Restaurant
        Kategori.TRANSPORT -> Icons.Outlined.DirectionsCar
        Kategori.BELANJA -> Icons.Outlined.ShoppingBag
        Kategori.RUMAH -> Icons.Outlined.Home
        Kategori.KESEHATAN -> Icons.Outlined.MedicalServices
        Kategori.LAINNYA -> Icons.Outlined.Category
        Kategori.GAJI -> Icons.Outlined.Payments
        Kategori.BONUS -> Icons.Outlined.CardGiftcard
        Kategori.INVESTASI -> Icons.AutoMirrored.Outlined.TrendingUp
        Kategori.HADIAH -> Icons.Outlined.VolunteerActivism
        Kategori.PENDAPATAN_LAIN -> Icons.Outlined.MoreHoriz
    }
    Icon(
        imageVector = icon,
        contentDescription = kategori.name,
        modifier = modifier.size(20.dp),
        tint = GreenAccent
    )
}
