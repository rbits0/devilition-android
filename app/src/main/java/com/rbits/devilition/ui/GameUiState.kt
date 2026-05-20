package com.rbits.devilition.ui

import android.util.Log
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.data.HAND_SIZE
import com.rbits.devilition.data.demonTypeHealth
import com.rbits.devilition.data.demonsPerRound
import com.rbits.devilition.data.piecesPerRound
import kotlin.collections.component1
import kotlin.collections.component2

private const val TAG = "GameUiState"

enum class DemonType {
    MINOR,
    MAJOR,
    ELDER,
    BOSS,
}

enum class PieceType {
    SNAKE,
    STRAWMAN,
    TOAD,
    CROSS,
    PLUS,
    BOMB,
    ROCKET,
    ROCKET_PAD,
    CANNON,
}

enum class RocketColor {
    PINK,
    BLUE,
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

sealed class GridItem {

    data class Demon(
        val type: DemonType,
        val health: Int,
        val maxHealth: Int,
    ) : GridItem()

    // Boss takes up a 2x2 space
    // If this item is hit, it should damage the boss
    data class BossHitbox(
        val bossPos: Pair<Int, Int>,
    ) : GridItem()

    data class Piece(
        val type: PieceType,
        val facing: Direction,
        val id: Int,
        val position: Pair<Int, Int>? = null,
        val color: RocketColor? = null,
    ) : GridItem()

    class Hole() : GridItem()

}



data class GameUiState(
    val grid: Array<Array<GridItem?>> = Array(GRID_HEIGHT) { Array(GRID_WIDTH) { null } },
    val hand: Array<GridItem.Piece>,
    val bag: List<GridItem.Piece> = listOf(),
    val numAvailablePieces: Int = 0,
    val round: Int = 0,
    val score: Int = 0,
    val idCounter: Int = 0,
) {

    // Auto-generated function to handle the array
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameUiState

        if (score != other.score) return false
        if (!grid.contentDeepEquals(other.grid)) return false

        return true
    }

    // Auto-generated function to handle the array
    override fun hashCode(): Int {
        var result = score
        result = 31 * result + grid.contentDeepHashCode()
        return result
    }

    fun roundStart(): GameUiState {
        val grid = this.grid.map{ it.clone() }.toTypedArray()
        val round = this.round + 1

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

        val numAvailablePieces = this.numAvailablePieces + piecesPerRound(round)

        return this.copy(
            grid = grid,
            round = round,
            numAvailablePieces = numAvailablePieces,
        )
    }

    fun movePiece(item: GridItem.Piece, to: Pair<Int, Int>): GameUiState {
        val grid = this.grid.map { it.clone() }.toTypedArray()

        val from = item.position
        if (from == null) {
            Log.e(TAG, "Piece position is null")
            return this
        }
        grid[from.first][from.second] = null
        grid[to.first][to.second] = item.copy(position = to)

        return this.copy(grid = grid)
    }
}

fun getEmptySpaces(grid: Array<Array<GridItem?>>): List<Pair<Int, Int>> {
    return grid.flatMapIndexed { rowIndex, row ->
        row.mapIndexedNotNull { colIndex, item ->
            if (item == null) Pair(rowIndex, colIndex) else null
        }
    }
}

