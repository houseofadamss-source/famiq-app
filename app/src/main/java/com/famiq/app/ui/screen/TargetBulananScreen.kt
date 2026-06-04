package com.famiq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel

@Composable
fun TargetBulananScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val targetBulananSaved by viewModel.targetBulanan.collectAsStateWithLifecycle()

    var targetInput by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(targetBulananSaved) {
        if (targetBulananSaved > 0) {
            targetInput = targetBulananSaved.toString()
        }
    }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 24.dp, start = 18.dp, end = 18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { navController.popBackStack() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.back),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.monthly_target),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.target_desc),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TrackChanges,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = stringResource(R.string.set_budget_limit),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = onBg
                            )
                        }

                        Text(
                            text = stringResource(R.string.set_budget_limit_desc),
                            fontSize = 12.sp,
                            color = onBg.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = targetInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) targetInput = input
                            },
                            label = { Text(stringResource(R.string.target_nominal)) },
                            placeholder = { Text(stringResource(R.string.target_placeholder)) },
                            leadingIcon = {
                                Text(
                                    "Rp",
                                    fontWeight = FontWeight.Bold,
                                    color = GreenAccent,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenAccent,
                                focusedLabelColor = GreenAccent
                            ),
                            visualTransformation = RupiahVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val nominal = targetInput.toLongOrNull() ?: 0L
                                viewModel.simpanTargetBulanan(nominal)
                                showSnackbar = true
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                        ) {
                            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.save_target),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.financial_tips_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBg.copy(alpha = 0.5f),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                TipsKeuanganCard(
                    icon = Icons.Outlined.PieChart,
                    judul = stringResource(R.string.tips_1_title),
                    deskripsi = stringResource(R.string.tips_1_desc)
                )

                TipsKeuanganCard(
                    icon = Icons.Outlined.ShoppingBag,
                    judul = stringResource(R.string.tips_2_title),
                    deskripsi = stringResource(R.string.tips_2_desc)
                )

                TipsKeuanganCard(
                    icon = Icons.Outlined.HealthAndSafety,
                    judul = stringResource(R.string.tips_3_title),
                    deskripsi = stringResource(R.string.tips_3_desc)
                )

                TipsKeuanganCard(
                    icon = Icons.Outlined.Savings,
                    judul = stringResource(R.string.tips_4_title),
                    deskripsi = stringResource(R.string.tips_4_desc)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showSnackbar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenDark),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.target_updated_success),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2500)
                    showSnackbar = false
                }
            }
        }
    }
}

@Composable
fun TipsKeuanganCard(
    icon: ImageVector,
    judul: String,
    deskripsi: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GreenAccent.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = judul,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = deskripsi,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
