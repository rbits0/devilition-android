package com.rbits.devilition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rbits.devilition.LIST_SCREEN_PADDING_DP
import com.rbits.devilition.R
import com.rbits.devilition.TITLE_BOTTOM_PADDING_DP
import com.rbits.devilition.data.Settings
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun SettingsScreen(
    settings: Settings,
    setTimeBonusEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val numListItems = 1
    val listItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier.padding(LIST_SCREEN_PADDING_DP.dp),
    ) {
        item {
            Text(
                stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(
                    bottom = TITLE_BOTTOM_PADDING_DP.dp - ListItemDefaults.SegmentedGap,
                )
            )
        }

        item {
            SegmentedListItem (
                checked = settings.timeBonusEnabled,
                onCheckedChange = setTimeBonusEnabled,
                shapes = ListItemDefaults.segmentedShapes(0, numListItems),
                colors = listItemColors,
                trailingContent = {
                    Switch(
                        checked = settings.timeBonusEnabled,
                        onCheckedChange = null
                    )
                }
            ) {
                Text(stringResource(R.string.enable_time_bonus))
            }
        }
    }
}


@Preview()
@Composable
fun SettingsScreenPreview() {
    DevilitionTheme(darkTheme = true) {
        SettingsScreen(
            settings = Settings(),
            setTimeBonusEnabled = {},
        )
    }
}