package com.dorian_bichet.essence_maintenant.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dorian_bichet.essence_maintenant.ui.screens.MapStyle

@Composable
fun MapSettingsDrawer(
    currentMapStyle: MapStyle,
    onStyleSelected: (MapStyle) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp).fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Style de carte",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onClose) {
                Text("Fermer")
            }
        }
        HorizontalDivider()
        Column(modifier = Modifier.padding(8.dp)) {
            MapStyle.entries.forEach { style ->
                NavigationDrawerItem(
                    label = { Text(style.title) },
                    selected = currentMapStyle == style,
                    onClick = {
                        onStyleSelected(style)
                    },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
