package com.wlsanjos.castflow.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.wlsanjos.castflow.ui.theme.CastFlowTheme
import com.wlsanjos.castflow.ui.screens.DiscoverTvScreen

@AndroidEntryPoint
class DebugDiscoverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CastFlowTheme {
                DiscoverTvScreen(
                    onBack = { finish() },
                    onConnect = { /* No-op in debug */ }
                )
            }
        }
    }
}
