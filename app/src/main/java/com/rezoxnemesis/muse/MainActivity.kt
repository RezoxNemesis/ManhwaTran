package com.rezoxnemesis.muse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rezoxnemesis.muse.ui.MuseExactVisualApp
import com.rezoxnemesis.muse.ui.MuseExactReferenceSurface
import com.rezoxnemesis.muse.ui.MuseReferenceScreen
import com.rezoxnemesis.muse.ui.MuseGlassVariant
import com.rezoxnemesis.muse.ui.MuseGlassSurface
import com.rezoxnemesis.muse.ui.theme.MuseBackground
import com.rezoxnemesis.muse.ui.theme.MuseGreen
import com.rezoxnemesis.muse.ui.theme.MuseTheme

class MainActivity : ComponentActivity() {
    private var requestedRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false
        requestedRoute = intent.safeMuseRoute()

        setContent {
            MuseTheme {
                val museViewModel: MuseViewModel = viewModel()
                PermissionAwareMuse(
                    viewModel = museViewModel,
                    requestedRoute = requestedRoute,
                    onRouteHandled = { requestedRoute = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedRoute = intent.safeMuseRoute()
    }

    private fun Intent.safeMuseRoute(): String? =
        getStringExtra(ExtraOpenRoute)
            ?.takeIf { it in AllowedExternalRoutes }

    companion object {
        const val ExtraOpenRoute =
            "com.rezoxnemesis.muse.extra.OPEN_ROUTE"

        private val AllowedExternalRoutes = setOf(
            "home",
            "explore",
            "library",
            "equalizer",
            "tools",
            "nowPlaying",
            "queue",
            "lyrics",
            "liked",
            "recent",
            "playlists",
            "downloads",
            "sleep",
            "settings",
            "diagnostics",
            "more",
        )
    }
}

@Composable
private fun PermissionAwareMuse(
    viewModel: MuseViewModel,
    requestedRoute: String?,
    onRouteHandled: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val onboardingPreferences = remember {
        context.getSharedPreferences(
            "muse_onboarding",
            android.content.Context.MODE_PRIVATE,
        )
    }
    var continueWithoutLibraryAccess by remember {
        mutableStateOf(
            onboardingPreferences.getBoolean(
                "continue_without_library_access",
                false,
            )
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { result ->
        granted = result
        if (result) {
            onboardingPreferences.edit()
                .remove("continue_without_library_access")
                .apply()
            continueWithoutLibraryAccess = false
            viewModel.refreshLibrary()
        }
    }

    LaunchedEffect(granted, continueWithoutLibraryAccess) {
        if (granted || continueWithoutLibraryAccess) {
            viewModel.refreshLibrary()
        }
    }

    if (granted || continueWithoutLibraryAccess) {
        MuseExactVisualApp(
            viewModel = viewModel,
            requestedRoute = requestedRoute,
            onRouteHandled = onRouteHandled,
        )
    } else {
        MusicPermissionScreen(
            onRequestPermission = { launcher.launch(permission) },
            onContinueWithoutPermission = {
                onboardingPreferences.edit()
                    .putBoolean(
                        "continue_without_library_access",
                        true,
                    )
                    .commit()
                continueWithoutLibraryAccess = true
            },
        )
    }
}

@Composable
private fun MusicPermissionScreen(
    onRequestPermission: () -> Unit,
    onContinueWithoutPermission: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("MuseRoot"),
    ) {
        MuseExactReferenceSurface(
            screen = MuseReferenceScreen.Splash,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f)),
        )

        MuseGlassSurface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .navigationBarsPadding(),
            variant = MuseGlassVariant.Strong,
            cornerRadius = 30.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Your music, on your device",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Allow music access so Muse can discover and play audio already on this device. Muse stays local-first.",
                    color = Color(0xFFD2E3D3),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MuseGreen,
                        contentColor = MuseBackground,
                    ),
                    shape = RoundedCornerShape(22.dp),
                ) {
                    Text(
                        text = "Allow music access",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(6.dp))
                TextButton(
                    onClick = onContinueWithoutPermission,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Continue with imported files",
                        color = MuseGreen,
                    )
                }
                Text(
                    text = "You can change this later. Imported files still work through Android’s secure file picker.",
                    color = Color(0xFF9FB4A2),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
