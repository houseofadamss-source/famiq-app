package com.famiq.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.famiq.app.data.local.UserPreferences
import com.famiq.app.ui.screen.NavGraph
import com.famiq.app.ui.theme.CatatUangTheme
import android.util.Log
import androidx.core.os.LocaleListCompat
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {

    private lateinit var userPreferences: UserPreferences
    private var isAppLocked by mutableStateOf(false)
    private var isAuthSuccess by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferences = UserPreferences(this)

        // ✅ INITIALIZE NOTIFICATION CHANNEL (Lokal)
        NotificationHelper.createChannel(this)

        // ✅ INITIALIZE ONESIGNAL (Cloud)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        
        lifecycleScope.launch {
            // Beri waktu SDK inisialisasi sebentar
            delay(1000)
            OneSignal.Notifications.requestPermission(true)
            
            // Paksa Opt-In biar gak status "Unsubscribed" di dashboard
            OneSignal.User.pushSubscription.optIn()
            
            Log.d("OneSignal", "Subscription ID: ${OneSignal.User.pushSubscription.id}")
            Log.d("OneSignal", "Is Opted In: ${OneSignal.User.pushSubscription.optedIn}")
        }

        lifecycleScope.launch {
            val isFingerprintEnabled = readFingerprintSettingInstantly()
            if (isFingerprintEnabled && !isAuthSuccess) {
                isAppLocked = true
                tampilkanSensorSidikJari()
            }
        }

        lifecycleScope.launch {
            val bahasa = userPreferences.bahasaPreference.first()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                getSystemService(android.app.LocaleManager::class.java).applicationLocales = 
                    android.os.LocaleList(Locale(bahasa))
            } else {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(bahasa)
                )
            }
        }

        setContent {
            val themePreference by userPreferences.themePreference.collectAsState(initial = "auto")
            CatatUangTheme(themePreference = themePreference) {
                if (isAppLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                } else {
                    NavGraph(userPreferences = userPreferences)
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        lifecycleScope.launch {
            if (readFingerprintSettingInstantly()) {
                isAppLocked = true
                tampilkanSensorSidikJari()
            }
        }
    }

    private suspend fun readFingerprintSettingInstantly(): Boolean {
        return userPreferences.bacaFingerprintPreference().first()
    }

    private fun tampilkanSensorSidikJari() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, getString(R.string.access_denied, errString), Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthSuccess = true
                    isAppLocked = false
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@MainActivity, getString(R.string.status_fingerprint_not_recognized), Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
