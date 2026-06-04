package com.famiq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.famiq.app.ui.theme.GreenAccent
import com.famiq.app.ui.theme.GreenLight
import com.famiq.app.ui.theme.GreenSoft

@Composable
fun ScrollPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    isDark: Boolean = false
) {
    // Mengunci posisi awal indeks agar langsung berada di tengah selektor
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    val itemHeightDp = 44.dp
    val visibleItems = 3

    val selectedBg    = if (isDark) Color(0xFF1A2E24) else GreenSoft
    val dividerColor  = if (isDark) Color(0xFF2E3D36) else GreenLight
    val unselectedTxt = if (isDark) Color(0xFF6B7280) else Color(0xFF94A3B8)

    // ✅ REVISI UTAMA: Menggunakan snapshotFlow agar perubahan data dibaca secara fluid di latar belakang
    // tanpa perlu memicu animasi paksa (.animateScrollToItem) yang bikin gerakan kaku/kasar.
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                val safeIndex = index.coerceIn(0, items.size - 1)
                if (safeIndex != selectedIndex) {
                    onSelectedChange(safeIndex)
                }
            }
    }

    Box(modifier = Modifier.height(itemHeightDp * visibleItems).width(75.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            // ✅ EFEK MAGNET FLUID: Menyerahkan 100% kendali animasi halus ke sistem jepret bawaan Android
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            // Spacer Atas agar item pertama bisa sejajar pas di baris tengah
            item { Spacer(modifier = Modifier.height(itemHeightDp)) }

            items.forEachIndexed { index, item ->
                item {
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .height(itemHeightDp)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .then(
                                if (isSelected) Modifier.background(selectedBg, RoundedCornerShape(8.dp))
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            fontSize = if (isSelected) 22.sp else 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GreenAccent else unselectedTxt,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.alpha(if (isSelected) 1f else 0.4f)
                        )
                    }
                }
            }

            // Spacer Bawah agar item terakhir bisa naik pas di baris tengah
            item { Spacer(modifier = Modifier.height(itemHeightDp)) }
        }

        // Garis Pembatas Hijau Indikator Tengah
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeightDp)
        ) {
            HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter), color = dividerColor, thickness = 1.5.dp)
            HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter), color = dividerColor, thickness = 1.5.dp)
        }
    }
}