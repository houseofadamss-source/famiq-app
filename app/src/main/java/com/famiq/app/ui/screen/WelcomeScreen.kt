package com.famiq.app.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.famiq.app.R
import com.famiq.app.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val titleRes: Int,
    val descRes: Int,
    val animationType: Int // 0: Logo, 1: Sync, 2: Stat, 3: Target
)

@Composable
fun WelcomeScreen(navController: NavController, onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(R.string.welcome_to_famiq, R.string.famiq_desc, 0),
        OnboardingPage(R.string.onboarding_sync_title, R.string.onboarding_sync_desc, 1),
        OnboardingPage(R.string.onboarding_stat_title, R.string.onboarding_stat_desc, 2),
        OnboardingPage(R.string.onboarding_target_title, R.string.onboarding_target_desc, 3)
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(GreenDark, GreenMid)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { position ->
                OnboardingPagerItem(pages[position])
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pager Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.3f)
                        val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Next/Finish Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = GreenDark),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) stringResource(R.string.get_started) else stringResource(R.string.next),
                        fontWeight = FontWeight.Bold
                    )
                    if (pagerState.currentPage < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        
        // Skip Button
        if (pagerState.currentPage < pages.size - 1) {
            TextButton(
                onClick = { onFinish() },
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp)
            ) {
                Text(text = stringResource(R.string.skip), color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun OnboardingPagerItem(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (page.animationType) {
                0 -> FloatingLogoAnimation()
                1 -> SyncAnimation()
                2 -> StatisticsAnimation()
                3 -> TargetAnimation()
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = stringResource(page.titleRes),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(page.descRes),
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun FloatingLogoAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val translateY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .graphicsLayer {
                translationY = translateY
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = R.mipmap.ic_launcher,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun SyncAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // Left phone-like shape
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(centerX - 60f - offset/2, centerY - 40f),
            size = androidx.compose.ui.geometry.Size(50f, 80f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
        )
        
        // Right phone-like shape
        drawRoundRect(
            color = GreenLight,
            topLeft = Offset(centerX + 10f + offset/2, centerY - 40f),
            size = androidx.compose.ui.geometry.Size(50f, 80f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
        )
        
        // Sync lines
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(centerX - 10f - offset/2, centerY),
            end = Offset(centerX + 10f + offset/2, centerY),
            strokeWidth = 4f
        )
    }
}

@Composable
fun StatisticsAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val progress1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val progress2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        val width = size.width
        val height = size.height
        val barWidth = 30f
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(width/2 - barWidth * 2, height - height * progress1),
            size = androidx.compose.ui.geometry.Size(barWidth, height * progress1),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )
        
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(width/2 - barWidth/2, height - height * progress2),
            size = androidx.compose.ui.geometry.Size(barWidth, height * progress2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )
        
        drawRoundRect(
            color = GreenLight,
            topLeft = Offset(width/2 + barWidth, height - height * 0.7f),
            size = androidx.compose.ui.geometry.Size(barWidth, height * 0.7f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )
    }
}

@Composable
fun TargetAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(150.dp).graphicsLayer { rotationZ = rotation }) {
            drawCircle(
                color = Color.White,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f)))
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward, // Placeholder for target icon
            contentDescription = null,
            tint = GreenLight,
            modifier = Modifier.size(60.dp).graphicsLayer { rotationZ = -45f }
        )
    }
}
