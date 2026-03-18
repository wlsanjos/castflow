package com.wlsanjos.castflow.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.wlsanjos.castflow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastFlowTopBar(title: String, onBack: (() -> Unit)? = null) {
    val navIcon: (@Composable () -> Unit)? = if (onBack != null) {
        @Composable {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        }
    } else null

    CenterAlignedTopAppBar(
        title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
        navigationIcon = navIcon ?: { }
    )
}
