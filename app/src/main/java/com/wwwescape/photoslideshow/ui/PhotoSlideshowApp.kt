package com.wwwescape.photoslideshow.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wwwescape.photoslideshow.R
import com.wwwescape.photoslideshow.ui.components.CenteredCollapsingTopBar
import com.wwwescape.photoslideshow.ui.navigation.Destination
import com.wwwescape.photoslideshow.ui.screens.googlephotos.GooglePhotosSignInScreen
import com.wwwescape.photoslideshow.ui.screens.home.HomeScreen
import com.wwwescape.photoslideshow.ui.screens.settings.LicensesScreen
import com.wwwescape.photoslideshow.ui.screens.settings.SettingsScreen
import com.wwwescape.photoslideshow.ui.screens.sources.SourcesScreen

private const val LICENSES_ROUTE = "licenses"
private const val GOOGLE_PHOTOS_SIGN_IN_ROUTE = "google_photos_sign_in"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSlideshowApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = Destination.fromRoute(currentRoute)
    val isHome = currentRoute == Destination.Home.route
    val isSettingsFamily = currentRoute == Destination.Settings.route || currentRoute == LICENSES_ROUTE

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val titleText = when (currentRoute) {
        LICENSES_ROUTE -> stringResource(R.string.section_open_source_licenses)
        GOOGLE_PHOTOS_SIGN_IN_ROUTE -> stringResource(R.string.action_connect_google_photos)
        else -> stringResource(currentDestination.titleRes)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenteredCollapsingTopBar(
                title = titleText,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (isHome) {
                        Image(
                            painter = painterResource(R.drawable.ic_logo_mark),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(28.dp),
                        )
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
                actions = {
                    if (!isSettingsFamily) {
                        IconButton(onClick = { navController.navigate(Destination.Settings.route) }) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(R.string.title_settings),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.Home.route) {
                HomeScreen(onManageSources = { navController.navigate(Destination.Sources.route) })
            }
            composable(Destination.Sources.route) {
                SourcesScreen(onConnectGooglePhotos = { navController.navigate(GOOGLE_PHOTOS_SIGN_IN_ROUTE) })
            }
            composable(Destination.Settings.route) {
                SettingsScreen(onNavigateToLicenses = { navController.navigate(LICENSES_ROUTE) })
            }
            composable(LICENSES_ROUTE) {
                LicensesScreen()
            }
            composable(GOOGLE_PHOTOS_SIGN_IN_ROUTE) {
                GooglePhotosSignInScreen()
            }
        }
    }
}
