package com.famiq.app.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.famiq.app.GoogleAuthHelper
import com.famiq.app.ui.theme.GreenMain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.famiq.app.R


@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading      by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }

    // Animasi muncul (Fade + Scale)
    val scale = animateFloatAsState(
        targetValue = if (isLoading) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B2318))
    ) {
        // Radial glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF22C55E).copy(alpha = 0.07f),
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        // Nama app — tepat di tengah layar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .scale(scale.value),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Famiq",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenMain,
                    letterSpacing = (-2).sp,
                    lineHeight = 64.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.login_desc),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        // Tombol + terms — di bawah
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Error message
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                errorMessage?.let { pesan ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFDC2626).copy(alpha = 0.15f)
                        )
                    ) {
                        Text(
                            text = pesan,
                            color = Color(0xFFFF6B6B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Tombol Google
            Button(
                onClick = {
                    if (!isLoading) {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            // Tambahin delay dikit biar transisi loadingnya kerasa smooth
                            delay(300)
                            val result = GoogleAuthHelper.signIn(context)
                            result.fold(
                                onSuccess = {
                                    onLoginSuccess()
                                },
                                onFailure = { error ->
                                    isLoading = false
                                    errorMessage = when {
                                        error.message?.contains("canceled") == true ->
                                            context.getString(R.string.login_canceled)
                                        error.message?.contains("network") == true ->
                                            context.getString(R.string.check_internet)
                                        else -> context.getString(R.string.login_failed)
                                    }
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.7f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF16A34A),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = stringResource(R.string.connecting),
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoogleIcon()
                        Text(
                            text = stringResource(R.string.sign_in_google),
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Terms
            Text(
                text = stringResource(R.string.terms_info),
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.22f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Home indicator
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun GoogleIcon() {
    Icon(
        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_google),
        contentDescription = "Google",
        modifier = Modifier.size(20.dp),
        tint = Color.Unspecified   // ✅ penting! biar warna aslinya keliatan
    )
}