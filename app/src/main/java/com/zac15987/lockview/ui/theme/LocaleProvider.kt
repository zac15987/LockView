package com.zac15987.lockview.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zac15987.lockview.utils.LocaleHelper
import com.zac15987.lockview.viewmodel.SettingsViewModel

@Composable
fun LocaleProvider(
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val languagePreference by settingsViewModel.languagePreference.collectAsStateWithLifecycle()

    // Wrap the ORIGINAL context (Activity) so findOwner<ActivityResultRegistryOwner>(LocalContext)
    // can still walk baseContext and find the ComponentActivity. We only override resources/assets/
    // theme with the locale-configured ones. If LocaleHelper.wrap returned the same context
    // (Android 13+ path via LocaleManager), pass it through unchanged.
    val localizedContext = remember(languagePreference, baseContext) {
        val configured = LocaleHelper.wrap(baseContext, languagePreference)
        if (configured === baseContext) {
            baseContext
        } else {
            object : ContextWrapper(baseContext) {
                override fun getResources(): Resources = configured.resources
                override fun getAssets() = configured.assets
                override fun getTheme(): Resources.Theme = configured.theme
            }
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration
    ) {
        content()
    }
}
