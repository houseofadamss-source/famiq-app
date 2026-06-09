package com.famiq.app.ui.screen

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.famiq.app.R
import com.famiq.app.ReminderScheduler
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val mAktif by viewModel.notifMorningAktif.collectAsStateWithLifecycle()
    val aAktif by viewModel.notifAfternoonAktif.collectAsStateWithLifecycle()
    val eAktif by viewModel.notifEveningAktif.collectAsStateWithLifecycle()
    
    val mJam by viewModel.jamMorning.collectAsStateWithLifecycle()
    val aJam by viewModel.jamAfternoon.collectAsStateWithLifecycle()
    val eJam by viewModel.jamEvening.collectAsStateWithLifecycle()

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    var showPickerBySlot by remember { mutableStateOf<Int?>(null) } // 1: Morning, 2: Afternoon, 3: Evening
    var showSnackbar by remember { mutableStateOf(false) }

    val currentPickerTime = when(showPickerBySlot) {
        1 -> mJam
        2 -> aJam
        3 -> eJam
        else -> "00:00"
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            kotlinx.coroutines.delay(2000)
            showSnackbar = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
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
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.popBackStack() }.padding(vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.ArrowBackIosNew, contentDescription = stringResource(R.string.back), tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.back), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(R.string.reminder_center), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = stringResource(R.string.reminder_center_desc), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-24).dp)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // --- PERIZINAN ANDROID 12+ ---
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        PermissionWarningCard(context)
                    }
                }

                // --- SLOT 1: PAGI ---
                ReminderSlotCard(
                    title = stringResource(R.string.morning_session),
                    description = stringResource(R.string.morning_session_desc),
                    jam = mJam,
                    isAktif = mAktif,
                    onToggle = { 
                        viewModel.simpanNotifMorningAktif(it)
                        if (it) ReminderScheduler.schedule(context, ReminderScheduler.REQ_MORNING, mJam)
                        else ReminderScheduler.cancel(context, ReminderScheduler.REQ_MORNING)
                    },
                    onTimeClick = { showPickerBySlot = 1 }
                )

                // --- SLOT 2: SORE ---
                ReminderSlotCard(
                    title = stringResource(R.string.afternoon_session),
                    description = stringResource(R.string.afternoon_session_desc),
                    jam = aJam,
                    isAktif = aAktif,
                    onToggle = { 
                        viewModel.simpanNotifAfternoonAktif(it)
                        if (it) ReminderScheduler.schedule(context, ReminderScheduler.REQ_AFTERNOON, aJam)
                        else ReminderScheduler.cancel(context, ReminderScheduler.REQ_AFTERNOON)
                    },
                    onTimeClick = { showPickerBySlot = 2 }
                )

                // --- SLOT 3: MALAM ---
                ReminderSlotCard(
                    title = stringResource(R.string.evening_session),
                    description = stringResource(R.string.evening_session_desc),
                    jam = eJam,
                    isAktif = eAktif,
                    onToggle = { 
                        viewModel.simpanNotifEveningAktif(it)
                        if (it) ReminderScheduler.schedule(context, ReminderScheduler.REQ_EVENING, eJam)
                        else ReminderScheduler.cancel(context, ReminderScheduler.REQ_EVENING)
                    },
                    onTimeClick = { showPickerBySlot = 3 }
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // TIME PICKER DIALOG
        if (showPickerBySlot != null) {
            val timePickerState = key(showPickerBySlot) {
                val timeParts = currentPickerTime.split(":")
                val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
                val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                
                rememberTimePickerState(
                    initialHour = initialHour,
                    initialMinute = initialMinute,
                    is24Hour = true
                )
            }

            BasicAlertDialog(
                onDismissRequest = { showPickerBySlot = null },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when(showPickerBySlot) {
                                1 -> stringResource(R.string.morning_session)
                                2 -> stringResource(R.string.afternoon_session)
                                else -> stringResource(R.string.evening_session)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = GreenMain,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                selectorColor = GreenMain,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                periodSelectorSelectedContainerColor = GreenSoft,
                                periodSelectorSelectedContentColor = GreenDark,
                                timeSelectorSelectedContainerColor = GreenSoft,
                                timeSelectorSelectedContentColor = GreenDark
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showPickerBySlot = null }) {
                                Text(stringResource(R.string.cancel), color = Color.Gray)
                            }
                            TextButton(onClick = {
                                val newTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                                when(showPickerBySlot) {
                                    1 -> {
                                        viewModel.simpanJamMorning(newTime)
                                        if (mAktif) ReminderScheduler.schedule(context, ReminderScheduler.REQ_MORNING, newTime)
                                    }
                                    2 -> {
                                        viewModel.simpanJamAfternoon(newTime)
                                        if (aAktif) ReminderScheduler.schedule(context, ReminderScheduler.REQ_AFTERNOON, newTime)
                                    }
                                    3 -> {
                                        viewModel.simpanJamEvening(newTime)
                                        if (eAktif) ReminderScheduler.schedule(context, ReminderScheduler.REQ_EVENING, newTime)
                                    }
                                }
                                showPickerBySlot = null
                                showSnackbar = true
                            }) {
                                Text(stringResource(R.string.save), color = GreenMain, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showSnackbar) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp, start = 24.dp, end = 24.dp)) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = GreenDark)) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = GreenMain, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.reminder_schedule_updated), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderSlotCard(
    title: String,
    description: String,
    jam: String,
    isAktif: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = onBg)
                    Text(description, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                }
                Switch(
                    checked = isAktif,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = GreenMain, checkedTrackColor = GreenSoft)
                )
            }
            
            if (isAktif) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(onBg.copy(alpha = 0.04f))
                        .clickable { onTimeClick() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = GreenMain, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.reminder_time), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(jam, fontWeight = FontWeight.ExtraBold, color = GreenMain, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun PermissionWarningCard(context: Context) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.precision_alarm_permission), fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.precision_alarm_desc), fontSize = 11.sp, color = Color(0xFFB91C1C))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) { Text(stringResource(R.string.enable_permission), fontSize = 12.sp) }
        }
    }
}
