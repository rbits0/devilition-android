package com.rbits.devilition.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.rbits.devilition.R
import com.rbits.devilition.data.getImageIdFromSprite
import com.rbits.devilition.ui.theme.DevilitionTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun GridCell(
    dragAndDropState: DragAndDropState<GridItem.Piece>,
    position: PiecePos.GridPos,
    detonateStarted: Boolean,
    selectedForDetonation: Boolean,
    onItemDropped: (GridItem.Piece) -> Unit,
    onItemClicked: (GridItem.Piece) -> Unit,
    modifier: Modifier = Modifier,
    item: GridItem? = null,
    targeted: Boolean = false,
    armed: Boolean = false,
    explosion: Explosion? = null,
) {
    var isHoveredOver by remember { mutableStateOf(false) }
    var explosionAnimationState by remember { mutableIntStateOf(0) }


    // Can drop piece on this cell if this cell is empty
    // OR if dragging piece back to the same cell
    fun canDropPiece(draggedItem: GridItem.Piece) =
        item == null
        || (item is GridItem.Piece && draggedItem.id == item.id)

    val surfaceColor = if (item is GridItem.Hole || item is GridItem.BossHitbox) {
        Color.Transparent
    } else if (targeted) {
        MaterialTheme.colorScheme.tertiary
    } else if (isHoveredOver) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val isDraggable = item is GridItem.Piece && !item.placementConfirmed

    if (explosion != null) {
        LaunchedEffect(explosion.id) {
            for (i in 0..<NUM_EXPLOSION_FRAMES) {
                explosionAnimationState = i
                delay(DETONATION_STEP_TIME / NUM_EXPLOSION_FRAMES)
            }
        }
    } else {
        explosionAnimationState = 0
    }

    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = surfaceColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .aspectRatio(1f)
            .let {
                if (item is GridItem.Demon && item.demonType == DemonType.BOSS) {
                    // Make boss demon take up a 2x2 space
                    it.graphicsLayer {
                        val scale = 2f + (GRID_SPACING_DP.dp.toPx() / size.width)
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = scale
                        scaleY = scale
                    }
                } else {
                    it
                }
            }
            .dropTarget(
                state = dragAndDropState,
                key = position,
                onDrop = { state ->
                    if (canDropPiece(state.data)) {
                        onItemDropped(state.data)
                        isHoveredOver = false
                    }
                },
                onDragEnter = { state ->
                    if (canDropPiece(state.data)) {
                        isHoveredOver = true
                    }
                },
                onDragExit = { isHoveredOver = false },
            ),
    ) {
        if (item != null && item !is GridItem.Hole) {
            if (isDraggable) {
                // Draggable piece
                DraggableItem(
                    state = dragAndDropState,
                    key = item.id,
                    data = item,
                    enabled = true,
                    draggableContent = {
                        GridCellItem(
                            item,
                            onClick = {},
                            highlighted = true,
                        )
                    },
                ) {
                    GridCellItem(
                        item,
                        onClick = { onItemClicked(item) },
                        clickEnabled = true,
                        highlighted = true,
                        targeted = targeted,
                        armed = armed,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                    )
                }
            } else if (item is GridItem.Piece) {
                // Non-draggable piece
                GridCellItem(
                    item,
                    onClick = { onItemClicked(item) },
                    clickEnabled = detonateStarted,
                    highlighted = selectedForDetonation,
                    targeted = targeted,
                    armed = armed,
                )
            } else {
                // Non-piece
                GridCellItem(
                    item,
                    targeted = targeted,
                )
            }
        }

        if (explosion != null) {
            val explosionBitmap = ImageBitmap.imageResource(
                getImageIdFromSprite(explosion, explosionAnimationState)
            )

            Image(
                bitmap = explosionBitmap,
                contentDescription = stringResource(R.string.explosion),
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                filterQuality = FilterQuality.None,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}


@Preview()
@Composable
fun GridCellPreview() {
    val piece = GridItem.Piece(
        pieceType = PieceType.SNAKE,
        facing = Direction.DOWN,
        id = 0,
    )

    DevilitionTheme(darkTheme = true) {
        GridCell(
            rememberDragAndDropState(),
            position = PiecePos.GridPos(0, 0),
            onItemDropped = {},
            item = piece,
            onItemClicked = {},
            selectedForDetonation = false,
            detonateStarted = false,
            modifier = Modifier
                .size(60.dp, 60.dp),
        )
    }
}