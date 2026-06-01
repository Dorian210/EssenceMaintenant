package com.dorian_bichet.essence_maintenant.ui.components

import com.dorian_bichet.essence_maintenant.data.model.CarburantType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelSelector(
    selectedCarburants: Set<CarburantType>, // 🛠️ Reçoit maintenant un Set
    onCarburantsChanged: (Set<CarburantType>) -> Unit, // 🛠️ Notifie avec le nouveau Set
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items(CarburantType.entries) { carburant ->
            // Vérifie si ce carburant précis fait partie du Set
            val isSelected = selectedCarburants.contains(carburant)

            FilterChip(
                selected = isSelected,
                onClick = {
                    val nouveauxChoix = if (isSelected) {
                        // Sécurité : on empêche de tout décocher (il doit rester au moins 1 carburant)
                        if (selectedCarburants.size > 1) {
                            selectedCarburants - carburant // Retire le carburant du Set
                        } else {
                            selectedCarburants
                        }
                    } else {
                        selectedCarburants + carburant // Ajoute le carburant au Set
                    }
                    onCarburantsChanged(nouveauxChoix)
                },
                label = { Text(carburant.name) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color(0xFFE3F2FD) // Un bleu très léger quand sélectionné
                ),
                border = if (isSelected) FilterChipDefaults.filterChipBorder(enabled = true, selected = true) 
                         else null // Optionnel: enlever la bordure si non sélectionné pour un look plus propre
            )
        }
    }
}
