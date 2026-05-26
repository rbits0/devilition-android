package com.rbits.devilition.ui

import android.util.Log
import com.rbits.devilition.TAG
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.data.HAND_SIZE
import com.rbits.devilition.data.demonTypeHealth
import com.rbits.devilition.data.demonsPerRound
import com.rbits.devilition.data.piecesPerRound
import com.rbits.devilition.data.tierPieces
import kotlin.collections.component1
import kotlin.collections.component2

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

sealed class PiecePos {
    data class GridPos(val x: Int, val y: Int) : PiecePos()
    data class HandPos(val pos: Int) : PiecePos()
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
        val position: PiecePos? = null,
        val color: RocketColor = RocketColor.entries.random(),
        var rocketTargetId: Int? = null,
    ) : GridItem()

    class Hole() : GridItem()

}



data class GameUiState(
    val grid: Array<Array<GridItem?>> = Array(GRID_HEIGHT) { Array(GRID_WIDTH) { null } },
    val hand: Array<GridItem.Piece>,
    val bag: List<PieceType> = listOf(),
    val numAvailablePieces: Int = 0,
    val round: Int = 0,
    val score: Int = 0,
    val idCounter: Int = 0,
) {

    companion object {
        fun new(): GameUiState {
            val bag: MutableList<PieceType> = mutableListOf()
            var idCounter = 0
            val hand: MutableList<GridItem.Piece> = mutableListOf()

            // Start with `HAND_SIZE` pieces in hand
            for (i in 0..<HAND_SIZE) {
                val id = idCounter
                val type = drawNewPiece(bag)
                idCounter++
                hand.add(
                    GridItem.Piece(
                        type = type,
                        facing = Direction.DOWN,
                        id = id,
                        position = PiecePos.HandPos(i),
                    )
                )
            }

            return GameUiState(
                hand = hand.toTypedArray(),
                bag = bag,
                idCounter = idCounter,
            )
        }
    }


    // Auto-generated function to handle the array
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameUiState

        if (numAvailablePieces != other.numAvailablePieces) return false
        if (round != other.round) return false
        if (score != other.score) return false
        if (idCounter != other.idCounter) return false
        if (!grid.contentDeepEquals(other.grid)) return false
        if (!hand.contentEquals(other.hand)) return false
        if (bag != other.bag) return false

        return true
    }

    // Auto-generated function to handle the array
    override fun hashCode(): Int {
        var result = numAvailablePieces
        result = 31 * result + round
        result = 31 * result + score
        result = 31 * result + idCounter
        result = 31 * result + grid.contentDeepHashCode()
        result = 31 * result + hand.contentHashCode()
        result = 31 * result + bag.hashCode()
        return result
    }

    fun roundStart(): GameUiState {
        val grid = grid.map{ it.clone() }.toTypedArray()
        val round = round + 1

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

        val numAvailablePieces = numAvailablePieces + piecesPerRound(round)

        return this.copy(
            grid = grid,
            round = round,
            numAvailablePieces = numAvailablePieces,
        )
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos): GameUiState {
        val grid = grid.map { it.clone() }.toTypedArray()
        var idCounter = idCounter
        var bag = bag
        var hand = hand
        var numAvailablePieces = numAvailablePieces

        val from = item.position
        if (from == null) {
            Log.e(TAG, "Piece position is null")
            return this
        }

        when (from) {

            is PiecePos.GridPos -> {
                // Move piece within grid

                grid[from.x][from.y] = null
                grid[to.x][to.y] = item.copy(position = to)
            }

            is PiecePos.HandPos -> {
                // Place piece from hand

                val newBag: MutableList<PieceType> = mutableListOf()
                val newHand = hand.clone()
                val id = idCounter
                idCounter++

                if (item.type == PieceType.ROCKET) {
                    // Replace rocket with pad instead of drawing from bag
                    newHand[from.pos] = GridItem.Piece(
                        type = PieceType.ROCKET_PAD,
                        facing = Direction.DOWN,
                        position = from,
                        id = id,
                        color = item.color,
                    )

                    grid[to.x][to.y] = item.copy(
                        position = to,
                        rocketTargetId = id,
                    )
                } else {
                    // Draw piece from bag to replace moved piece
                    val type = drawNewPiece(newBag)
                    newHand[from.pos] = GridItem.Piece(
                        type = type,
                        facing = Direction.DOWN,
                        id = id,
                        position = from,
                    )

                    grid[to.x][to.y] = item.copy(position = to)
                }

                numAvailablePieces -= 1
                bag = newBag
                hand = newHand
            }

        }

        return this.copy(
            grid = grid,
            hand = hand,
            bag = bag,
            idCounter = idCounter,
            numAvailablePieces = numAvailablePieces,
        )
    }

    fun rotatePiece(item: GridItem.Piece): GameUiState {
        when (val pos = item.position) {

            is PiecePos.GridPos -> {
                val grid = grid.map{ it.clone() }.toTypedArray()
                grid[pos.x][pos.y] = item.copy(
                    facing = rotateClockwise(item.facing)
                )

                return this.copy(grid = grid)
            }

            is PiecePos.HandPos -> {
                val hand = hand.clone()
                hand[pos.pos] = item.copy(
                    facing = rotateClockwise(item.facing)
                )

                return this.copy(hand = hand)
            }

            null -> return this

        }
    }

    fun canPlacePiece(): Boolean = numAvailablePieces > 0
}

fun getEmptySpaces(grid: Array<Array<GridItem?>>): List<Pair<Int, Int>> {
    return grid.flatMapIndexed { rowIndex, row ->
        row.mapIndexedNotNull { colIndex, item ->
            if (item == null) Pair(rowIndex, colIndex) else null
        }
    }
}

fun drawNewPiece(bag: MutableList<PieceType>): PieceType {
    if (bag.isEmpty()) {
        // 1 Tier 1 piece
        bag.add(tierPieces[1]!!.random())

        // 3 Tier 2 pieces
        for (i in 0..<3) {
            bag.add(tierPieces[2]!!.random())
        }

        // 2 Tier 3 pieces
        for (i in 0..<2) {
            bag.add(tierPieces[3]!!.random())
        }

        bag.shuffle()
    }

    return bag.removeAt(bag.lastIndex)
}

fun rotateClockwise(direction: Direction) =
    when (direction) {
        Direction.UP -> Direction.RIGHT
        Direction.RIGHT -> Direction.DOWN
        Direction.DOWN -> Direction.LEFT
        Direction.LEFT -> Direction.UP
    }

