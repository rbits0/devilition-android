package com.rbits.devilition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rbits.devilition.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
) : ViewModel() {
    val gameUiState = repository.gameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameUiState.new(),
    )

    fun roundStart() {
        viewModelScope.launch {
            repository.roundStart()
        }
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        viewModelScope.launch {
            repository.movePiece(item, to)
        }
    }

    fun rotatePiece(item: GridItem.Piece) {
        viewModelScope.launch {
            repository.rotatePiece(item)
        }
    }

    fun confirmPlacement() {
        viewModelScope.launch {
            repository.confirmPlacement()
        }
    }

    fun cancelPlacement() {
        viewModelScope.launch {
            repository.cancelPlacement()
        }
    }

    suspend fun armPiece(item: GridItem.Piece): GameUiState {
        return repository.armPiece(item)
    }

    suspend fun runDetonationStep(): GameUiState {
        return repository.runDetonationStep()
    }

    suspend fun roundEnd(): GameUiState {
        return repository.roundEnd()
    }

    fun calculateScore() {
        viewModelScope.launch {
            repository.calculateScore()
        }
    }

    fun reset() {
        viewModelScope.launch {
            repository.reset()
        }
    }
}

class GameViewModelFactory(private val repository: GameRepository)
    : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Invalid ViewModel class")
        }
    }
}
