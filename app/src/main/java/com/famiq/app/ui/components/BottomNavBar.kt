package com.famiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.famiq.app.ui.screen.Routes
import com.famiq.app.ui.theme.GreenDark
import com.famiq.app.ui.theme.GreenMid
import com.famiq.app.ui.theme.LocalDarkMode

data class NavItem(
    val route: String,
    val label: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        NavItem(Routes.BERANDA,   "Beranda",    Icons.Outlined.Home, Icons.Filled.Home),
        NavItem(Routes.STATISTIK, "Statistik",  Icons.Outlined.BarChart, Icons.Filled.BarChart),
        NavItem(Routes.FORM,      "Tambah",     Icons.Outlined.AddCircleOutline, Icons.Filled.AddCircle),
        NavItem(Routes.RIWAYAT,   "Riwayat",    Icons.Outlined.Receipt, Icons.Filled.Receipt),
        NavItem(Routes.SETTINGS,  "Pengaturan", Icons.Outlined.Settings, Icons.Filled.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDark = LocalDarkMode.current

    val shadowColor = if (isDark)
        Color.Black.copy(alpha = 0.5f)
    else
        Color.Black.copy(alpha = 0.2f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(RoundedCornerShape(28.dp)),
            color = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GreenDark, GreenMid)
                        ),
                        alpha = 0.95f
                    )
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.BERANDA) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = Color.White,
                            selectedTextColor   = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                            unselectedTextColor = Color.White.copy(alpha = 0.5f),
                            indicatorColor      = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}
