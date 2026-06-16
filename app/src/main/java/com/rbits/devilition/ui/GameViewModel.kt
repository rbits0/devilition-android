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

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    // Initialise the state from the repository
    // The repository should stay up to date while the application is running, so we only need to
    // get the initial value
    init {
        viewModelScope.launch {
            val gameState = repository.gameFlow.first()
            _gameState.update { gameState }
        }
    }


    fun roundStart() {
        modifyGameState {
            it.roundStart()
        }
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        modifyGameState {
            it.movePiece(item, to)
        }
    }

    fun rotatePiece(item: GridItem.Piece) {
        modifyGameState {
            it.rotatePiece(item)
        }
    }

    fun confirmPlacement() {
        modifyGameState {
            it.confirmPlacement()
        }
    }

    fun cancelPlacement() {
        modifyGameState {
            it.cancelPlacement()
        }
    }

    fun armPiece(item: GridItem.Piece): GameState {
        return modifyGameState {
            it.armPiece(item)
        }
    }

    fun runDetonationStep(): GameState {
        return modifyGameState {
            it.runDetonationStep()
        }
    }

    fun roundEnd(): GameState {
        return modifyGameState {
            it.roundEnd()
        }
    }

    fun reset() {
        val newValue = _gameState.updateAndGet {
            GameState.new()
        }

        viewModelScope.launch {
            repository.updateState(newValue)
        }
    }

    private fun modifyGameState(action: (GameState) -> Unit): GameState {
        val newValue = _gameState.updateAndGet {
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
