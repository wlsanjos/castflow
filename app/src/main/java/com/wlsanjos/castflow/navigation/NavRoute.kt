package com.wlsanjos.castflow.navigation

sealed class NavRoute(val route: String) {
    object Home : NavRoute("home")
    object Discover : NavRoute("discover")
    object Library : NavRoute("library")
    object Preview : NavRoute("preview")
    object Playback : NavRoute("playback")
    object Settings : NavRoute("settings")
}
