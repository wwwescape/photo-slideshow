package com.wwwescape.photoslideshow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.wwwescape.photoslideshow.R

/** Every screen reachable from the app's own top-level navigation (the full-screen Slideshow
 * surface is a separate Activity, not part of this NavHost). */
enum class Destination(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.title_home, Icons.Rounded.Home),
    Sources("sources", R.string.title_sources, Icons.Rounded.PhotoLibrary),
    Settings("settings", R.string.title_settings, Icons.Rounded.Settings);

    companion object {
        fun fromRoute(route: String?): Destination = entries.find { it.route == route } ?: Home
    }
}
