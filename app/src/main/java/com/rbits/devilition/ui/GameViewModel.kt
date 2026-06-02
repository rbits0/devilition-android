package com.rbits.devilition.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState.new())
    val uiState = _uiState.asStateFlow()

    fun roundStart() {
        _uiState.update { it.roundStart() }
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        _uiState.update { it.movePiece(item, to) }
    }

    fun rotatePiece(item: GridItem.Piece) {
        _uiState.update { it.rotatePiece(item) }
    }

    fun confirmPlacement() {
        _uiState.update { it.confirmPlacement() }
    }
}
