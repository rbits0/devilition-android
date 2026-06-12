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
        modifyUiState {
            it.roundStart()
        }
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        modifyUiState {
            it.movePiece(item, to)
        }
    }

    fun rotatePiece(item: GridItem.Piece) {
        modifyUiState {
            it.rotatePiece(item)
        }
    }

    fun confirmPlacement() {
        modifyUiState {
            it.confirmPlacement()
        }
    }

    fun cancelPlacement() {
        modifyUiState {
            it.cancelPlacement()
        }
    }

    fun armPiece(item: GridItem.Piece): GameUiState {
        return modifyUiState {
            it.armPiece(item)
        }
    }

    fun runDetonationStep(): GameUiState {
        return modifyUiState {
            it.runDetonationStep()
        }
    }

    fun roundEnd(): GameUiState {
        return modifyUiState {
            it.roundEnd()
        }
    }

    fun calculateScore() {
        modifyUiState {
            it.calculateScore()
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

    private fun modifyUiState(action: (GameUiState) -> Unit): GameUiState {
        val newValue = _uiState.updateAndGet {
            val newValue = it.clone()
            action(newValue)
            newValue
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }

        return newValue
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
