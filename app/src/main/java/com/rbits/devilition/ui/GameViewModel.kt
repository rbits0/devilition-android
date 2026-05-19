package com.rbits.devilition.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    fun nextRound() {
        _uiState.update { currentState ->
            var grid = currentState.grid.map{ it.clone() }.toTypedArray()

            // TODO: Implement functionality
            val round = currentState.round + 1
            grid[1][2] = GridItem.Piece(PieceType.SNAKE, Direction.DOWN)

            currentState.copy(grid = grid, round = round)
        }
    }
}