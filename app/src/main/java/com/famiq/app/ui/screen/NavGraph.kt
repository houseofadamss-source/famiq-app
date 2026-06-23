package com.famiq.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.famiq.app.GoogleAuthHelper
import com.famiq.app.data.local.UserPreferences
import com.famiq.app.viewmodel.TransaksiViewModel
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN           = "login"
    const val BERANDA         = "beranda"
    const val FORM            = "form"
    const val RIWAYAT         = "riwayat"
    const val STATISTIK       = "statistik"
    const val SETTINGS        = "settings"
    const val PROFIL_KELUARGA = "profil_keluarga"
    const val NOTIFIKASI      = "notifikasi"
    const val BAHASA          = "bahasa"
    const val TENTANG         = "tentang"
    const val SIKLUS_GAJIAN   = "siklus_gajian"
    const val TARGET_BULANAN  = "target_bulanan"
    const val PENGATURAN      = "pengaturan"
    const val TIMELINE        = "timeline"
    const val ACCOUNT         = "account"
    const val MODE_SELECTION  = "mode_selection"
    const val THEME_SELECTION = "theme_selection"
    const val SECURITY        = "security"
    const val DATA_MANAGEMENT = "data_management"
    const val WELCOME         = "welcome"
    const val CHANGELOG       = "changelog"
    const val HUTANG_PIUTANG  = "hutang_piutang"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    userPreferences: UserPreferences
) {
    val onboardingCompleted by userPreferences.onboardingCompleted.collectAsState(initial = null)
    val sudahLogin = GoogleAuthHelper.isLoggedIn()

    if (onboardingCompleted == null) return // Tunggu DataStore

    val startDestination = if (onboardingCompleted == false) {
        Routes.WELCOME
    } else if (sudahLogin) {
        Routes.BERANDA
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.WELCOME) {
            val scope = rememberCoroutineScope()
            WelcomeScreen(navController = navController, onFinish = {
                scope.launch {
                    userPreferences.simpanOnboardingCompleted(true)
                    val nextDest = if (sudahLogin) Routes.BERANDA else Routes.LOGIN
                    navController.navigate(nextDest) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            })
        }
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.BERANDA) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.BERANDA) { BerandaScreen(navController = navController) }
        composable(Routes.FORM) { FormScreen(navController = navController) }
        composable(Routes.RIWAYAT) { RiwayatScreen(navController = navController) }
        composable(Routes.STATISTIK) { StatistikScreen(navController = navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController = navController) }
        composable(Routes.PROFIL_KELUARGA) { ProfilKeluargaScreen(navController = navController) }
        composable(Routes.NOTIFIKASI) { NotifikasiScreen(navController = navController) }
        composable(Routes.BAHASA) { BahasaScreen(navController = navController) }
        composable(Routes.TENTANG) { TentangScreen(navController = navController) }
        composable(Routes.SIKLUS_GAJIAN) { SiklusGajianScreen(navController = navController) }
        composable(Routes.TARGET_BULANAN) { TargetBulananScreen(navController = navController) }
        composable(Routes.PENGATURAN) { PengaturanScreen(navController = navController) }
        composable(Routes.ACCOUNT) { AccountScreen(navController = navController) }
        composable(Routes.MODE_SELECTION) { ModeSelectionScreen(navController = navController) }
        composable(Routes.THEME_SELECTION) { ThemeSelectionScreen(navController = navController) }
        composable(Routes.SECURITY) { SecurityScreen(navController = navController) }
        composable(Routes.DATA_MANAGEMENT) { DataManagementScreen(navController = navController) }
        composable(Routes.CHANGELOG) { ChangelogScreen(navController = navController) }
        composable(Routes.HUTANG_PIUTANG) { HutangPiutangScreen(navController = navController) }

        composable(Routes.TIMELINE) {
            val viewModel: TransaksiViewModel = viewModel()
            TimelineScreen(navController, viewModel)
        }
    }
}
