package com.rbits.devilition.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.rbits.devilition.R

@Composable
fun GridImage(
    item: GridItem,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.snake_vertical),
        contentDescription = stringResource(R.string.snake),
        modifier = modifier,
    )
}