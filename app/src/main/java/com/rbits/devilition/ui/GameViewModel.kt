package com.rbits.devilition.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState.new())
    val uiState = _uiState.asStateFlow()

    fun roundStart() {
        _uiState.update {
            val newValue = it.clone()
            newValue.roundStart()
            newValue
        }
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        _uiState.update {
            val newValue = it.clone()
            newValue.movePiece(item, to)
            newValue
        }
    }

    fun rotatePiece(item: GridItem.Piece) {
        _uiState.update {
            val newValue = it.clone()
            newValue.rotatePiece(item)
            newValue
        }
    }

    fun confirmPlacement() {
        _uiState.update {
            val newValue = it.clone()
            newValue.confirmPlacement()
            newValue
        }
    }

    fun cancelPlacement() {
        _uiState.update {
            val newValue = it.clone()
            newValue.cancelPlacement()
            newValue
        }
    }

    fun armPiece(item: GridItem.Piece): GameUiState {
        return _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.armPiece(item)
            newValue
        }
    }

    fun runDetonationStep(): GameUiState {
        return _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.runDetonationStep()
            newValue
        }
    }

    fun roundEnd(): GameUiState {
        return _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.roundEnd()
            newValue
        }
    }

    fun calculateScore() {
        _uiState.update {
            val newValue = it.clone()
            newValue.calculateScore()
            newValue
        }
    }

    fun reset() {
        _uiState.update {
            GameUiState.new()
        }
    }
}
