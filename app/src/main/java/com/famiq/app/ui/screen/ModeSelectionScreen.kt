package com.famiq.app.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.famiq.app.ui.theme.*
import com.famiq.app.viewmodel.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val isFamilyMode by viewModel.isFamilyMode.collectAsStateWithLifecycle()
    val isPersonalPro by viewModel.isPersonalPro.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val bgColor = MaterialTheme.colorScheme.background

    var showActivationDialog by remember { mutableStateOf<Int?>(null) } // 1: Personal Pro, 2: Family Pro
    var inputCode by remember { mutableStateOf("") }

    val secretCodesPersonalPro = listOf("FAMIQ-PERSONAL-PRO", "ADAMS-LITE-77")
    val secretCodesFamilyPro = listOf("FAMIQ-EXCL-7721", "ADAMS-PRO-991", "UNIQUE-FAM-2024")

    if (showActivationDialog != null) {
        AlertDialog(
            onDismissRequest = { showActivationDialog = null },
            title = { Text(stringResource(R.string.enter_activation_code), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it.uppercase() },
                    placeholder = { Text(stringResource(R.string.activation_code_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenMain)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val codes = if (showActivationDialog == 1) secretCodesPersonalPro else secretCodesFamilyPro
                        if (codes.contains(inputCode)) {
                            if (showActivationDialog == 1) {
                                viewModel.setPersonalPro(true)
                                viewModel.setFamilyMode(false)
                            } else {
                                viewModel.setFamilyMode(true)
                            }
                            showActivationDialog = null
                            Toast.makeText(context, context.getString(R.string.success_activated), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.invalid_code), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                ) {
                    Text(stringResource(R.string.activate))
                }
            },
            dismissButton = {
                TextButton(onClick = { showActivationDialog = null }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = listOf(GreenDark, GreenMid)),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { navController.popBackStack() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Outlined.ArrowBackIosNew, stringResource(R.string.back), tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.back), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.select_app_mode), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.select_app_mode_desc), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 1. PERSONAL FREE ---
            PricingCard(
                title = stringResource(R.string.personal_mode_title),
                description = stringResource(R.string.personal_mode_description),
                icon = Icons.Outlined.Person,
                isSelected = !isFamilyMode && !isPersonalPro,
                price = "FREE",
                benefits = listOf(
                    stringResource(R.string.cat_food) + " etc tracking",
                    "Basic Statistics"
                ),
                onClick = { 
                    viewModel.setFamilyMode(false)
                    viewModel.setPersonalPro(false)
                }
            )

            // --- 2. PERSONAL PRO ---
            PricingCard(
                title = stringResource(R.string.personal_pro_title),
                description = stringResource(R.string.personal_pro_desc),
                icon = Icons.Outlined.Star,
                isSelected = !isFamilyMode && isPersonalPro,
                price = "Rp 25.000",
                isPremium = true,
                benefits = listOf(
                    stringResource(R.string.benefit_wants_needs),
                    stringResource(R.string.benefit_exports),
                    stringResource(R.string.benefit_recurring)
                ),
                onUnlockClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("houseofadamss@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Personal Pro Activation - Famiq")
                        putExtra(Intent.EXTRA_TEXT, "I want to activate Personal Pro.")
                    }
                    try { context.startActivity(intent) } catch (e: Exception) { }
                },
                onHaveCodeClick = { showActivationDialog = 1 }
            )

            // --- 3. FAMILY PRO ---
            PricingCard(
                title = stringResource(R.string.family_mode_title),
                description = stringResource(R.string.family_mode_premium_desc),
                icon = Icons.Outlined.Groups,
                isSelected = isFamilyMode,
                price = "Rp 50.000",
                isPremium = true,
                benefits = listOf(
                    stringResource(R.string.benefit_sync),
                    stringResource(R.string.benefit_notif),
                    stringResource(R.string.family_member_limit_info).take(30) + "..."
                ),
                onUnlockClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("houseofadamss@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Family Pro Activation - Famiq")
                        putExtra(Intent.EXTRA_TEXT, "I want to activate Family Pro.")
                    }
                    try { context.startActivity(intent) } catch (e: Exception) { }
                },
                onHaveCodeClick = { showActivationDialog = 2 }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PricingCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    price: String,
    benefits: List<String>,
    isPremium: Boolean = false,
    onClick: (() -> Unit)? = null,
    onUnlockClick: (() -> Unit)? = null,
    onHaveCodeClick: (() -> Unit)? = null
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) GreenMain else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(GreenSoft, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = GreenMain)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(price, color = GreenMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                if (isSelected) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = GreenMain)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(description, fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            benefits.forEach { benefit ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Outlined.Done, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(benefit, fontSize = 11.sp, color = onBg.copy(alpha = 0.6f))
                }
            }

            if (isPremium && !isSelected) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { onUnlockClick?.invoke() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                ) {
                    Text(stringResource(R.string.enable_family_mode), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.have_activation_code),
                    color = GreenMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().clickable { onHaveCodeClick?.invoke() },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
