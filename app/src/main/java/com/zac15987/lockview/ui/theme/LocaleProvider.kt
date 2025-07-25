package com.zac15987.lockview.ui.theme

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zac15987.lockview.data.language.LanguagePreference
import com.zac15987.lockview.utils.LocaleHelper
import com.zac15987.lockview.viewmodel.SettingsViewModel

@Composable
fun LocaleProvider(
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val languagePreference by settingsViewModel.languagePreference.collectAsStateWithLifecycle()
    
    val localizedContext = remember(languagePreference) {
        LocaleHelper.wrap(baseContext, languagePreference)
    }
    
    // Create a context wrapper that provides the localized context
    val contextWrapper = remember(localizedContext) {
        object : ContextWrapper(localizedContext) {
            override fun getApplicationContext(): Context {
                return LocaleHelper.wrap(super.getApplicationContext(), languagePreference)
            }
        }
    }
    
    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalContext provides contextWrapper
    ) {
        content()
    }
}