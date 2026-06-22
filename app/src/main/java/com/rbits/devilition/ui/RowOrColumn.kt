package com.rbits.devilition.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class RowOrColumnDirection {
    Row,
    Column,
}

@LayoutScopeMarker
class RowOrColumnScope(
    private val modifierWeight: Modifier.(weight: Float, fill: Boolean) -> Modifier
) {
    fun Modifier.weight(
        @FloatRange(from = 0.0, fromInclusive = false)
        weight: Float,
        fill: Boolean,
    ): Modifier {
        return this.modifierWeight(weight, fill)
    }
}

@Composable
fun RowOrColumn(
    direction: RowOrColumnDirection,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable (RowOrColumnScope.() -> Unit),
) {
    when (direction) {
        RowOrColumnDirection.Row -> Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement
        ) {
            with(RowOrColumnScope(
                modifierWeight = { weight, fill -> this.weight(weight, fill) }
            )) {
                content()
            }
        }
        RowOrColumnDirection.Column -> Column(
            modifier = modifier,
            verticalArrangement = verticalArrangement
        ) {
            with(RowOrColumnScope(
                modifierWeight = { weight, fill -> this.weight(weight, fill) }
            )) {
                content()
            }
        }
    }
}