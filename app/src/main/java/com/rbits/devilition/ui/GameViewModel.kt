package com.rbits.devilition.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.rbits.devilition.data.demonTypeHealth
import com.rbits.devilition.data.demonsPerRound
import com.rbits.devilition.data.piecesPerRound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.mapOf

fun getEmptySpaces(grid: Array<Array<GridItem?>>): List<Pair<Int, Int>> {
    return grid.flatMapIndexed { rowIndex, row ->
        row.mapIndexedNotNull { colIndex, item ->
            if (item == null) Pair(rowIndex, colIndex) else null
        }
    }
}

class GameViewModel : ViewModel() {
    private val tag = "GameViewModel"

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    fun roundStart() {
        _uiState.update { currentState ->
            val grid = currentState.grid.map{ it.clone() }.toTypedArray()
            val round = currentState.round + 1

            if (round < 10) {
                // Heal all elder demons
                grid.forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { colIndex, item ->
                        if (item is GridItem.Demon && item.type == DemonType.ELDER) {
                            grid[rowIndex][colIndex] = item.copy(health = item.maxHealth)
                        }
                    }
                }

                val emptySpaces = getEmptySpaces(grid).toMutableSet()

                // Place hole
                val holePos = emptySpaces.random()
                emptySpaces.remove(holePos)
                grid[holePos.first][holePos.second] = GridItem.Hole()

                // Place demons
                demonsPerRound(round).forEach { (demonType, count) ->
                    val health = demonTypeHealth(demonType)

                    // Place `count` demons of `demonType`
                    for (i in 0..<count) {
                        val demonPos = emptySpaces.random()
                        emptySpaces.remove(demonPos)
                        grid[demonPos.first][demonPos.second] = GridItem.Demon(
                            type = demonType,
                            health = health,
                            maxHealth = health,
                        )
                    }
                }
            } else {
                // FINAL ROUND

                // Replace all demons with holes
                grid.forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { colIndex, item ->
                        if (item is GridItem.Demon) {
                            grid[rowIndex][colIndex] = GridItem.Hole()
                        }
                    }
                }

                // Replace 4x4 square in middle with holes
                // All spots from (3, 3) to (6, 6)
                for (rowIndex in 3..6) {
                    for (colIndex in 3..6) {
                        grid[rowIndex][colIndex] = GridItem.Hole()
                    }
                }

                // Place boss in middle
                // Boss takes up a 2x2 space
                grid[4][4] = GridItem.Demon(
                    type = DemonType.BOSS,
                    health = demonTypeHealth(DemonType.BOSS),
                    maxHealth = demonTypeHealth(DemonType.BOSS),
                )
                for (pos in listOf(Pair(4, 5), Pair(5, 4), Pair(5, 5))) {
                    grid[pos.first][pos.second] = GridItem.BossHitbox(
                        bossPos = Pair(4, 4),
                    )
                }

                // Place 2-4 holes
                // TODO: Check the specifics of how this is done in UFO 50
                val emptySpaces = getEmptySpaces(grid).toMutableSet()
                val numHoles = (2..4).random()
                for (i in 0..<numHoles) {
                    val holePos = emptySpaces.random()
                    emptySpaces.remove(holePos)
                    grid[holePos.first][holePos.second] = GridItem.Hole()
                }
            }

            val numAvailablePieces = currentState.numAvailablePieces + piecesPerRound(round)

            currentState.copy(
                grid = grid,
                round = round,
                numAvailablePieces = numAvailablePieces,
            )
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