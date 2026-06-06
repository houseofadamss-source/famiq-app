package com.famiq.app.ui.screen

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.data.model.Kategori
import com.famiq.app.data.model.TransactionType
import com.famiq.app.ui.components.BottomNavBar
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

val kategoriColors = mapOf(
    Kategori.MAKAN           to Color(0xFFF59E0B),
    Kategori.TRANSPORT       to Color(0xFF3B82F6),
    Kategori.BELANJA         to Color(0xFFEC4899),
    Kategori.RUMAH           to Color(0xFF8B5CF6),
    Kategori.KESEHATAN       to Color(0xFF10B981),
    Kategori.LAINNYA         to Color(0xFF6B7280),
    Kategori.GAJI            to Color(0xFF2E7D32),
    Kategori.BONUS           to Color(0xFFFBC02D),
    Kategori.INVESTASI       to Color(0xFF1976D2),
    Kategori.HADIAH          to Color(0xFFE91E63),
    Kategori.PENDAPATAN_LAIN to Color(0xFF7B1FA2)
)

@Composable
fun StatistikScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val transaksiList by viewModel.semuaTransaksi.collectAsStateWithLifecycle(initialValue = emptyList())

    val bgColor      = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg         = MaterialTheme.colorScheme.onBackground

    val totalPerKategori = Kategori.entries.map { kat ->
        kat to transaksiList.filter { it.kategori == kat && it.tipe == TransactionType.EXPENSE }.sumOf { it.nominal }
    }.filter { it.second > 0 }

    val totalSemua    = totalPerKategori.sumOf { it.second }.toFloat()
    val sortedKategori = totalPerKategori.sortedByDescending { it.second }

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
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
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .padding(bottom = 42.dp)
                ) {
                    Column {
                        Text(stringResource(R.string.statistics), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.stat_desc),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .offset(y = (-32).dp)
                ) {
                    if (totalPerKategori.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no_stat_data),
                                color = onBg.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(top = 4.dp, bottom = paddingValues.calculateBottomPadding() + 16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.expense_distribution),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = onBg
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))

                                        Box(
                                            modifier = Modifier.size(240.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AndroidView(
                                                factory = { context ->
                                                    PieChart(context).apply {
                                                        description.isEnabled = false
                                                        legend.isEnabled = false
                                                        setHoleColor(AndroidColor.TRANSPARENT)
                                                        holeRadius = 75f
                                                        transparentCircleRadius = 0f
                                                        setDrawEntryLabels(false)
                                                        setDrawCenterText(false)
                                                        // ✅ Paksa reset semua jarak internal sejak inisialisasi
                                                        setExtraOffsets(0f, 0f, 0f, 0f)
                                                        minOffset = 0f
                                                        setPadding(0,0,0,0)
                                                        isRotationEnabled = false // Kunci rotasi biar gak geser visual
                                                    }
                                                },
                                                modifier = Modifier.fillMaxSize(),
                                                update = { chart ->
                                                    val entries = sortedKategori.map {
                                                        PieEntry(it.second.toFloat(), it.first.name)
                                                    }
                                                    val dataSet = PieDataSet(entries, "").apply {
                                                        colors = sortedKategori.map { (kategoriColors[it.first] ?: Color.Gray).toArgb() }
                                                        setDrawValues(false)
                                                        sliceSpace = 2f
                                                    }
                                                    chart.data = PieData(dataSet)
                                                    
                                                    // ✅ Kunci posisi lagi di block update (Wajib setelah set data)
                                                    chart.setExtraOffsets(0f, 0f, 0f, 0f)
                                                    chart.minOffset = 0f

                                                    chart.animateY(1000)
                                                    chart.invalidate()
                                                }
                                            )

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = stringResource(R.string.total_expense),
                                                    fontSize = 11.sp,
                                                    color = onBg.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    text = "Rp ${formatRupiah(totalSemua.toLong())}",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = onBg
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(20.dp)) }

                            items(sortedKategori) { (kat, nominal) ->
                                val percent = (nominal.toFloat() / totalSemua * 100)
                                val animatedProgress by animateFloatAsState(
                                    targetValue = if (startAnimation) percent / 100f else 0f,
                                    animationSpec = tween(durationMillis = 1000)
                                )

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    (kategoriColors[kat] ?: Color.Gray).copy(alpha = 0.15f),
                                                    RoundedCornerShape(12.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (kat) {
                                                    Kategori.MAKAN           -> Icons.Outlined.Restaurant
                                                    Kategori.TRANSPORT       -> Icons.Outlined.DirectionsCar
                                                    Kategori.BELANJA         -> Icons.Outlined.ShoppingBag
                                                    Kategori.RUMAH           -> Icons.Outlined.Home
                                                    Kategori.KESEHATAN       -> Icons.Outlined.MedicalServices
                                                    Kategori.LAINNYA         -> Icons.Outlined.Category
                                                    Kategori.GAJI            -> Icons.Outlined.Payments
                                                    Kategori.BONUS           -> Icons.Outlined.CardGiftcard
                                                    Kategori.INVESTASI       -> Icons.AutoMirrored.Outlined.TrendingUp
                                                    Kategori.HADIAH          -> Icons.Outlined.VolunteerActivism
                                                    Kategori.PENDAPATAN_LAIN -> Icons.Outlined.MoreHoriz
                                                },
                                                contentDescription = null,
                                                tint = kategoriColors[kat] ?: Color.Gray,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                stringResource(getKategoriStringRes(kat)),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = onBg
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { animatedProgress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape),
                                                color = kategoriColors[kat] ?: Color.Gray,
                                                trackColor = (kategoriColors[kat] ?: Color.Gray).copy(alpha = 0.1f),
                                                strokeCap = StrokeCap.Round
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Rp ${formatRupiah(nominal)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = onBg
                                            )
                                            Text(
                                                text = "${percent.toInt()}%",
                                                fontSize = 11.sp,
                                                color = onBg.copy(alpha = 0.4f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
