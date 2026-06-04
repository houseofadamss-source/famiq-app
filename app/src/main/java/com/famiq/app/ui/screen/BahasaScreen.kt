package com.famiq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel
import java.util.Locale

@Composable
fun BahasaScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val context = LocalContext.current
    val bahasaPreference by viewModel.bahasaPreference.collectAsStateWithLifecycle()

    val bgColor      = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg         = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .padding(top = 48.dp, bottom = 32.dp, start = 18.dp, end = 18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.popBackStack() }.padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = stringResource(R.string.back),
                            tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.back), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.language), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.select_app_language), color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionLabel(icon = Icons.Outlined.Language, label = stringResource(R.string.select_language))
            SettingsCard {
                val languages = listOf(
                    stringResource(R.string.lang_id) to "in",
                    stringResource(R.string.lang_en) to "en"
                )
                languages.forEachIndexed { index, pair ->
                    val (label, code) = pair
                    val isSelected = bahasaPreference == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.simpanBahasaPreference(code)
                                updateAppLocale(context, code)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GreenAccent else onBg
                        )
                        if (isSelected) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null,
                                tint = GreenAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (index == 0) SettingsDivider()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null,
                        tint = GreenAccent, modifier = Modifier.size(16.dp))
                    Text(
                        stringResource(R.string.multi_language_info),
                        fontSize = 12.sp, color = onBg.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

fun updateAppLocale(context: android.content.Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(android.app.LocaleManager::class.java).applicationLocales = 
            android.os.LocaleList(locale)
    } else {
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
        )
    }
}
