package com.rbits.devilition

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.rbits.devilition.data.GameRepository
import com.rbits.devilition.data.GameStateSerializer
import com.rbits.devilition.data.PastGamesSerializer
import com.rbits.devilition.data.SettingsRepository
import com.rbits.devilition.ui.DevilitionApp
import com.rbits.devilition.ui.GameState
import com.rbits.devilition.ui.GameViewModel
import com.rbits.devilition.ui.GameViewModelFactory
import com.rbits.devilition.ui.SettingsViewModel
import com.rbits.devilition.ui.SettingsViewModelFactory
import com.rbits.devilition.ui.theme.DevilitionTheme

const val TAG = "devilition"
const val GAME_STORE_FILE_NAME = "game_state.json"
const val PAST_GAMES_STORE_FILE_NAME = "past_games.json"

const val LIST_SCREEN_PADDING_DP = 4

private val Context.gameStore: DataStore<GameState> by dataStore(
    fileName = GAME_STORE_FILE_NAME,
    serializer = GameStateSerializer,
)

private val Context.pastGamesStore: DataStore<List<GameState>> by dataStore(
    fileName = PAST_GAMES_STORE_FILE_NAME,
    serializer = PastGamesSerializer,
)

private val Context.settingsStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private lateinit var gameViewModel: GameViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        gameViewModel = ViewModelProvider(
            this,
            GameViewModelFactory(GameRepository(gameStore, pastGamesStore))
        )[GameViewModel::class]

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(SettingsRepository(settingsStore))
        )[SettingsViewModel::class]

        setContent {
            DevilitionTheme {
                DevilitionApp(
                    gameViewModel = gameViewModel,
                    settingsViewModel = settingsViewModel,
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DevilitionTheme {
        Greeting("Android")
    }
}