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

// Item with a sprite
sealed interface SpriteItem

sealed class GridItem {

    data class Demon(
        val type: DemonType,
        val health: Int,
        val maxHealth: Int,
    ) : GridItem(), SpriteItem

    // Boss takes up a 2x2 space
    // If this item is hit, it should damage the boss
    data class BossHitbox(
        val bossPos: Pair<Int, Int>,
    ) : GridItem()

    data class Piece(
        val type: PieceType,
        val facing: Direction,
        val id: Int,
        val placementConfirmed: Boolean = true,
        val position: PiecePos? = null,
        val color: RocketColor = RocketColor.entries.random(),
        var rocketTargetId: Int? = null,
    ) : GridItem(), SpriteItem

    class Hole() : GridItem()

}



data class GameUiState(
    var grid: Array<Array<GridItem?>> = Array(GRID_HEIGHT) { Array(GRID_WIDTH) { null } },
    var hand: Array<GridItem.Piece?>,
    var bag: List<PieceType> = listOf(),
    var numAvailablePieces: Int = 0,
    var round: Int = 0,
    var score: Int = 0,
    var idCounter: Int = 0,
    // Position of the piece that has been dragged onto the board,
    // but hasn't had placement confirmed
    var unconfirmedPiecePos: PiecePos.GridPos? = null,
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
        if (unconfirmedPiecePos != other.unconfirmedPiecePos) return false

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
        result = 31 * result + (unconfirmedPiecePos?.hashCode() ?: 0)
        return result
    }


    fun clone(): GameUiState = this.copy(
        grid = grid.map{ it.clone() }.toTypedArray(),
        hand = hand.clone(),
    )

    fun roundStart() {
        round += 1

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
            grid[holePos.x][holePos.y] = GridItem.Hole()

            // Place demons
            demonsPerRound(round).forEach { (demonType, count) ->
                val health = demonTypeHealth(demonType)

                // Place `count` demons of `demonType`
                for (i in 0..<count) {
                    val demonPos = emptySpaces.random()
                    emptySpaces.remove(demonPos)
                    grid[demonPos.x][demonPos.y] = GridItem.Demon(
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
                grid[holePos.x][holePos.y] = GridItem.Hole()
            }
        }

        numAvailablePieces += piecesPerRound(round)
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        val from = item.position
        if (from == null) {
            Log.e(TAG, "Piece position is null")
            return
        }

        when (from) {

            is PiecePos.GridPos -> {
                // Move piece within grid

                grid[from.x][from.y] = null
                grid[to.x][to.y] = item.copy(position = to)
            }

            is PiecePos.HandPos -> {
                // Place piece from hand
                hand[from.pos] = null
                unconfirmedPiecePos = to

                grid[to.x][to.y] = item.copy(
                    position = to,
                    placementConfirmed = false,
                )
            }

        }
    }

    fun rotatePiece(item: GridItem.Piece) {
        when (val pos = item.position) {

            is PiecePos.GridPos -> {
                grid[pos.x][pos.y] = item.copy(
                    facing = rotateClockwise(item.facing)
                )
            }

            is PiecePos.HandPos -> {
                hand[pos.pos] = item.copy(
                    facing = rotateClockwise(item.facing)
                )
            }

            null -> {}

        }
    }

    // Confirm piece, and draw new piece to replace empty spot in hand
    fun confirmPlacement() {
        val unconfirmedPiecePos = this.unconfirmedPiecePos ?: return

        val item = grid[unconfirmedPiecePos.x][unconfirmedPiecePos.y]
        if (item == null || item !is GridItem.Piece) {
            return
        }

        val newPiecePos = PiecePos.HandPos(hand.indexOf(null))
        val id = idCounter
        idCounter += 1
        numAvailablePieces -= 1

        if (item.type == PieceType.ROCKET) {
            // Mark piece as confirmed
            grid[unconfirmedPiecePos.x][unconfirmedPiecePos.y] = item.copy(
                rocketTargetId = id,
                placementConfirmed = true,
            )

            // Replace rocket with pad instead of drawing from bag
            hand[newPiecePos.pos] = GridItem.Piece(
                type = PieceType.ROCKET_PAD,
                facing = Direction.DOWN,
                position = newPiecePos,
                id = id,
                color = item.color,
            )

        } else {
            // Mark piece as confirmed
            grid[unconfirmedPiecePos.x][unconfirmedPiecePos.y] = item.copy(
                rocketTargetId = id,
                placementConfirmed = true,
            )

            // Draw piece from bag to replace moved piece
            val newBag = bag.toMutableList()
            val type = drawNewPiece(newBag)
            hand[newPiecePos.pos] = GridItem.Piece(
                type = type,
                facing = Direction.DOWN,
                id = id,
                position = newPiecePos,
            )
            bag = newBag
        }

        this.unconfirmedPiecePos = null
    }

    fun cancelPlacement() {
        val unconfirmedPiecePos = unconfirmedPiecePos ?: return

        val item = grid[unconfirmedPiecePos.x][unconfirmedPiecePos.y]
        if (item == null || item !is GridItem.Piece) {
            return
        }

        val handEmptyPosition = PiecePos.HandPos(hand.indexOf(null))

        // Remove piece from grid
        grid[unconfirmedPiecePos.x][unconfirmedPiecePos.y] = null

        // Place back in hand
        hand[handEmptyPosition.pos] = item.copy(
            position = handEmptyPosition,
            placementConfirmed = true,
        )

        this.unconfirmedPiecePos = null
    }

    fun canPlacePieceFromHand(): Boolean = (
        numAvailablePieces > 0 && unconfirmedPiecePos == null
    )
}

fun getEmptySpaces(grid: Array<Array<GridItem?>>): List<PiecePos.GridPos> {
    return grid.flatMapIndexed { rowIndex, row ->
        row.mapIndexedNotNull { colIndex, item ->
            if (item == null) {
                PiecePos.GridPos(rowIndex, colIndex)
            } else null
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

