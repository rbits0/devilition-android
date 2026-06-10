package com.rbits.devilition.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.rbits.devilition.TAG
import com.rbits.devilition.ui.GameUiState
import com.rbits.devilition.ui.GridItem
import com.rbits.devilition.ui.PiecePos
import kotlinx.coroutines.flow.catch

class GameRepository(private val gameStore: DataStore<GameUiState>) {

    val gameFlow = gameStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading gameUiState")
                emit(GameUiState())
            } else {
                throw exception
            }
        }

    suspend fun roundStart() {
        gameStore.updateData {
            val newValue = it.clone()
            newValue.roundStart()
            newValue
        }
    }

    suspend fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        gameStore.updateData {
            val newValue = it.clone()
            newValue.movePiece(item, to)
            newValue
        }
    }

    suspend fun rotatePiece(item: GridItem.Piece) {
        gameStore.updateData {
            val newValue = it.clone()
            newValue.rotatePiece(item)
            newValue
        }
    }

    suspend fun confirmPlacement() {
        gameStore.updateData {
            val newValue = it.clone()
            newValue.confirmPlacement()
            newValue
        }
    }

    suspend fun cancelPlacement() {
        gameStore.updateData {
            val newValue = it.clone()
            newValue.cancelPlacement()
            newValue
        }
    }

    suspend fun armPiece(item: GridItem.Piece): GameUiState {
        // TODO: Check that this is returning the right state
        return gameStore.updateData {
            val newValue = it.clone()
            newValue.armPiece(item)
            newValue
        }
    }

    suspend fun runDetonationStep(): GameUiState {
        return gameStore.updateData {
            val newValue = it.clone()
            newValue.runDetonationStep()
            newValue
        }
    }

    suspend fun roundEnd(): GameUiState {
        return gameStore.updateData {
            val newValue = it.clone()
            newValue.roundEnd()
            newValue
        }
    }

    suspend fun calculateScore() {
        gameStore.updateData {
            val newValue = it.clone()
            newValue.calculateScore()
            newValue
        }
    }

    suspend fun reset() {
        gameStore.updateData {
            GameUiState.new()
        }
    }
}