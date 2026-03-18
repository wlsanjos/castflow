package com.wlsanjos.castflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wlsanjos.castflow.ui.screens.*
import com.wlsanjos.castflow.navigation.NavRoute as Route

@Composable
fun NavGraph(startDestination: String = Route.Discover.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Route.Home.route) {
            HomeScreen(
                onNavigateToPreview = { _ -> navController.navigate(Route.Preview.route) },
                onNavigateToDiscover = { navController.navigate(Route.Discover.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            )
        }

        composable(Route.Discover.route) {
            DiscoverTvScreen(
                onBack = { navController.popBackStack() },
                onConnect = { 
                    navController.navigate(Route.Library.route) {
                        popUpTo(Route.Discover.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Library.route) {
            LibraryScreen(
                onNavigateToPreview = { _ -> navController.navigate(Route.Preview.route) },
                onNavigateToDiscover = { navController.navigate(Route.Discover.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            )
        }

        composable(Route.Preview.route) {
            PreviewScreen(
                onCastSuccess = { navController.navigate(Route.Playback.route) },
                onBack = { navController.popBackStack() },
                onNavigateToLibrary = { navController.navigate(Route.Library.route) },
                onNavigateToDiscover = { navController.navigate(Route.Discover.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            )
        }

        composable(Route.Playback.route) {
            PlaybackControlScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLibrary = { navController.navigate(Route.Library.route) },
                onNavigateToDiscover = { navController.navigate(Route.Discover.route) }
            )
        }
    }
}
