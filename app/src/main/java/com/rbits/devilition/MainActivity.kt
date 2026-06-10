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
import com.rbits.devilition.ui.GameScreen
import com.rbits.devilition.ui.GameUiState
import com.rbits.devilition.ui.GameUiStateSerializer
import com.rbits.devilition.ui.GameViewModel
import com.rbits.devilition.ui.GameViewModelFactory
import com.rbits.devilition.ui.theme.DevilitionTheme

const val TAG = "devilition"
const val DATA_STORE_FILE_NAME = "game_state.json"

private val Context.gameStore: DataStore<GameUiState> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = GameUiStateSerializer,
)

class MainActivity : ComponentActivity() {
    private lateinit var gameViewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        gameViewModel = ViewModelProvider(
            this,
            GameViewModelFactory(GameRepository(gameStore))
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