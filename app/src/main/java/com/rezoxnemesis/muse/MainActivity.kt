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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rezoxnemesis.muse.ui.MuseExactVisualApp
import com.rezoxnemesis.muse.ui.MuseBrandMark
import com.rezoxnemesis.muse.ui.theme.MuseBackground
import com.rezoxnemesis.muse.ui.theme.MuseGreen
import com.rezoxnemesis.muse.ui.theme.MuseTheme

class MainActivity : ComponentActivity() {
    private var requestedRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
                    .apply()
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
            .testTag("MuseRoot")
            .background(
                Brush.verticalGradient(
                    colors = listOf(MuseBackground, MaterialTheme.colorScheme.surfaceVariant),
                )
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MuseBrandMark(
                modifier = Modifier.size(142.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Welcome to Muse",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Muse is local-first. Allow music access so it can discover and play audio already on this device.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(26.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MuseGreen,
                    contentColor = MuseBackground,
                ),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Allow music access")
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onContinueWithoutPermission) {
                Text("Continue with imported files")
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "You can grant device-library access later. Muse can still play files you choose through Android’s secure file picker.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
