package com.wlsanjos.castflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import com.wlsanjos.castflow.ui.theme.CastFlowTheme
import com.wlsanjos.castflow.navigation.NavGraph

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CastFlowApp()
        }
    }
}

@Composable
fun CastFlowApp() {
    CastFlowTheme {
        Surface {
            NavGraph()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CastFlowAppPreview() {
    CastFlowTheme { CastFlowApp() }
}
