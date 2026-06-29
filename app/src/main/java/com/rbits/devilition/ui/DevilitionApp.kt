package com.rbits.devilition.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fitInside
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.WindowInsetsRulers
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import com.rbits.devilition.R


// Routes
enum class Destination(
    val route: String,
    @field:StringRes
    val label: Int,
    @field:DrawableRes
    val icon: Int,
) {
    Game("game", R.string.game_screen, R.drawable.home),
    Scores("scores", R.string.scores, R.drawable.leaderboard),
    Settings("settings", R.string.settings, R.drawable.settings),
}

@Composable
fun DevilitionApp(
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    var selectedDestination by remember { mutableStateOf(Destination.Game) }
    val navigationSuiteState = rememberNavigationSuiteScaffoldState()
    val wideNavigationRailState = rememberWideNavigationRailState()
    val gameState by gameViewModel.gameState.collectAsStateWithLifecycle()
    val pastGamesState by gameViewModel.pastGamesState.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val navigationSuiteType = if (windowSizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
        )) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteType.NavigationBar
        }

    NavigationSuiteScaffold(
        state = navigationSuiteState,
        navigationSuiteType = navigationSuiteType,
        navigationItems = {
            for (destination in Destination.entries) {
                NavigationSuiteItem(
                    selected = selectedDestination == destination,
                    onClick = {
                        navController.navigate(destination.route)
                        selectedDestination = destination
                    },
                    icon = {
                        Icon(
                            painter = painterResource(destination.icon),
                            contentDescription = stringResource(destination.label),
                        )
                    },
                    label = {
                        Text(
                            stringResource(destination.label),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    navigationSuiteType = navigationSuiteType,
                )
            }
        },
    ) {
        NavHost(navController = navController, startDestination = Destination.Game.route) {
            for (destination in Destination.entries) {
                composable(destination.route) {
                    when(destination) {

                        Destination.Game -> GameScreen(
                            gameState,
                            roundStart = gameViewModel::roundStart,
                            movePiece = gameViewModel::movePiece,
                            rotatePiece = gameViewModel::rotatePiece,
                            confirmPlacement = gameViewModel::confirmPlacement,
                            cancelPlacement = gameViewModel::cancelPlacement,
                            armPiece = gameViewModel::armPiece,
                            runDetonationStep = gameViewModel::runDetonationStep,
                            roundEnd = gameViewModel::roundEnd,
                            reset = gameViewModel::reset,
                            addToPastGames = gameViewModel::addToPastGames,
                            startTimer = gameViewModel::startTimer,
                            stopTimer = gameViewModel::stopTimer,
                            modifier = Modifier
                                .fitInside(WindowInsetsRulers.SafeDrawing.current),
                        )

                        Destination.Scores -> ScoresScreen(
                            pastGames = pastGamesState,
                            modifier = Modifier
                                .fitInside(WindowInsetsRulers.SafeDrawing.current),
                        )

                        Destination.Settings -> SettingsScreen(
                            modifier = Modifier
                                .fitInside(WindowInsetsRulers.SafeDrawing.current),
                        )

                    }
                }
            }
        }
    }
}