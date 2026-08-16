package it.unibo.orma.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import it.unibo.orma.ui.screens.detail.HikeDetailScreen
import it.unibo.orma.ui.screens.detail.HikeDetailViewModel
import it.unibo.orma.ui.screens.hikes.HikesViewModel
import it.unibo.orma.ui.screens.hikes.HomeScreen
import it.unibo.orma.ui.screens.profile.ProfileScreen
import it.unibo.orma.ui.screens.profile.ProfileViewModel
import it.unibo.orma.ui.screens.stats.StatsScreen
import it.unibo.orma.ui.screens.stats.StatsViewModel
import it.unibo.orma.ui.screens.record.RecordScreen
import it.unibo.orma.ui.screens.record.RecordViewModel
import it.unibo.orma.ui.screens.settings.SettingsScreen
import it.unibo.orma.ui.screens.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OrmaNavGraph(
    navController: NavHostController,
    settingsVm: SettingsViewModel
) {
    // Il ViewModel delle escursioni sta qui: più schermate lo condividono
    // (home, dettaglio, registrazione) e così condividono anche la stessa repository.
    val hikesVm = koinViewModel<HikesViewModel>()
    val hikesState by hikesVm.state.collectAsStateWithLifecycle()
    val settingsState by settingsVm.state.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = OrmaRoute.Home
    ) {
        composable<OrmaRoute.Home> {
            HomeScreen(navController, hikesState, hikesVm.actions)
        }

        composable<OrmaRoute.HikeDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<OrmaRoute.HikeDetail>()
            val detailVm = koinViewModel<HikeDetailViewModel> { parametersOf(route.hikeId) }
            val detailState by detailVm.state.collectAsStateWithLifecycle()
            HikeDetailScreen(navController, detailState, detailVm)
        }

        composable<OrmaRoute.Record> {
            // Questo ViewModel è risolto dentro la rotta, non nel NavGraph: la
            // registrazione riguarda solo questa schermata e deve ripartire pulita
            // ogni volta che ci si entra.
            val recordVm = koinViewModel<RecordViewModel>()
            val recordState by recordVm.state.collectAsStateWithLifecycle()
            RecordScreen(navController, recordState, recordVm)
        }

        composable<OrmaRoute.Stats> {
            val statsVm = koinViewModel<StatsViewModel>()
            val statsState by statsVm.state.collectAsStateWithLifecycle()
            StatsScreen(navController, statsState)
        }

        composable<OrmaRoute.Profile> {
            val profileVm = koinViewModel<ProfileViewModel>()
            val profileState by profileVm.state.collectAsStateWithLifecycle()
            ProfileScreen(navController, profileState, profileVm)
        }

        composable<OrmaRoute.Settings> {
            SettingsScreen(navController, settingsState, settingsVm::setThemeMode)
        }
    }
}
