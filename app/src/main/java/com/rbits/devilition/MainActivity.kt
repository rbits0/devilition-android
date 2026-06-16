package com.rbits.devilition

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModelProvider
import com.rbits.devilition.data.GameRepository
import com.rbits.devilition.data.GameStateSerializer
import com.rbits.devilition.data.PastGamesSerializer
import com.rbits.devilition.ui.GameScreen
import com.rbits.devilition.ui.GameState
import com.rbits.devilition.ui.GameViewModel
import com.rbits.devilition.ui.GameViewModelFactory
import com.rbits.devilition.ui.theme.DevilitionTheme

const val TAG = "devilition"
const val GAME_STORE_FILE_NAME = "game_state.json"
const val PAST_GAMES_STORE_FILE_NAME = "past_games.json"

private val Context.gameStore: DataStore<GameState> by dataStore(
    fileName = GAME_STORE_FILE_NAME,
    serializer = GameStateSerializer,
)

private val Context.pastGamesStore: DataStore<List<GameState>> by dataStore(
    fileName = PAST_GAMES_STORE_FILE_NAME,
    serializer = PastGamesSerializer,
)

class MainActivity : ComponentActivity() {
    private lateinit var gameViewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        gameViewModel = ViewModelProvider(
            this,
            GameViewModelFactory(GameRepository(gameStore, pastGamesStore))
        )[GameViewModel::class]

        setContent {
            DevilitionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        gameViewModel = gameViewModel,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
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