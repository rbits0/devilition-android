package com.rbits.devilition.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rbits.devilition.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinLoseDialog(
    onDismiss: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(R.string.close_dialog))
            }
        },
    )
}