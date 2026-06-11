package com.rbits.devilition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rbits.devilition.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
) : ViewModel() {
    // This uses a MutableStateFlow and updates the repository every time it is modified
    // We do this because repository is too slow to update the values, which means the UI doesn't
    // update fast enough when it uses repository.gameFlow

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    // Initialise the state from the repository
    // The repository should stay up to date while the application is running, so we only need to
    // get the initial value
    init {
        viewModelScope.launch {
            val gameUiState = repository.gameFlow.first()
            _uiState.update { gameUiState }
        }
    }


    fun roundStart() {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.roundStart()
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.movePiece(item, to)
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }
    }

    fun rotatePiece(item: GridItem.Piece) {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.rotatePiece(item)
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }
    }

    fun confirmPlacement() {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.confirmPlacement()
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }
    }

    fun cancelPlacement() {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.cancelPlacement()
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }
    }

    fun armPiece(item: GridItem.Piece): GameUiState {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.armPiece(item)
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }

        return newValue
    }

    fun runDetonationStep(): GameUiState {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.runDetonationStep()
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }

        return newValue
    }

    fun roundEnd(): GameUiState {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.roundEnd()
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }

        return newValue
    }

    fun calculateScore() {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            newValue.calculateScore()
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }
    }

    fun reset() {
        val newValue = _uiState.updateAndGet {
            GameUiState.new()
        }

        viewModelScope.launch {
            repository.updateState(newValue)
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
