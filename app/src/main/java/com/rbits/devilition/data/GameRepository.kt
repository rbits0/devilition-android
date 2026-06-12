package com.rbits.devilition.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.rbits.devilition.TAG
import com.rbits.devilition.ui.GameState
import kotlinx.coroutines.flow.catch

class GameRepository(private val gameStore: DataStore<GameState>) {

    val gameFlow = gameStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading gameState")
                emit(GameState())
            } else {
                throw exception
            }
        }

    suspend fun updateState(state: GameState) {
        gameStore.updateData { state }
    }
}