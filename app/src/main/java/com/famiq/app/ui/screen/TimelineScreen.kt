package com.famiq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val transaksiList by viewModel.semuaTransaksi.collectAsStateWithLifecycle(initialValue = emptyList())

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_activity), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    titleContentColor = onBg,
                    navigationIconContentColor = onBg
                )
            )
        }
    ) { paddingValues ->
        if (transaksiList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_activity), color = onBg.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = transaksiList, key = { it.id }) { trx ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Jalur Timeline Visual
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(GreenAccent)
                            )
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(100.dp)
                                    .background(onBg.copy(alpha = 0.1f))
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(
                                            R.string.activity_message,
                                            trx.diinputOleh,
                                            formatRupiah(trx.nominal),
                                            stringResource(getKategoriStringRes(trx.kategori))
                                        ),
                                        fontSize = 13.sp,
                                        color = onBg,
                                        lineHeight = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = SimpleDateFormat(stringResource(R.string.timeline_date_format), Locale.getDefault()).format(Date(trx.tanggal)),
                                        fontSize = 11.sp,
                                        color = onBg.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.SemiBold
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
