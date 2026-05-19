package com.rbits.devilition.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val tag = "GameViewModel"

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    fun nextRound() {
        _uiState.update { currentState ->
            val grid = currentState.grid.map{ it.clone() }.toTypedArray()

            // TODO: Implement functionality
            val round = currentState.round + 1
            grid[1][2] = GridItem.Piece(
                PieceType.SNAKE,
                Direction.DOWN,
                0,
                position = Pair(1, 2)
            )

            currentState.copy(grid = grid, round = round)
        }
    }

    fun movePiece(item: GridItem.Piece, to: Pair<Int, Int>) {
        _uiState.update { currentState ->
            val grid = currentState.grid.map{ it.clone() }.toTypedArray()

            val from = item.position
            if (from == null) {
                Log.e(tag, "Piece position is null")
                return
            }
            grid[from.first][from.second] = null
            grid[to.first][to.second] = item.copy(position = to)

            currentState.copy(grid = grid)
        }
    }
}