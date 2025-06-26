package com.example.habbittracker.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habbittracker.domain.model.Habit
import com.example.habbittracker.domain.model.HabitCategories
import com.example.habbittracker.presentation.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    habitId: String,
    viewModel: HabitViewModel,
    navController: NavController
) {    
    // Find the habit to edit
    val habits by viewModel.habits.collectAsState()
    val habitToEdit = habits.find { it.id == habitId }
    
    if (habitToEdit == null) {
        // Show error or navigate back if habit not found
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    
    var title by remember { mutableStateOf(habitToEdit.title) }
    var description by remember { mutableStateOf(habitToEdit.description) }
    var selectedCategory by remember { mutableStateOf(HabitCategories.getCategoryByName(habitToEdit.category)) }
    var selectedIcon by remember { mutableStateOf(habitToEdit.icon) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HabitCategories.ALL_CATEGORIES) { category ->
                    EditCategoryChip(
                        category = category,
                        isSelected = selectedCategory == category,
                        onSelect = { 
                            selectedCategory = category
                            selectedIcon = category.icon
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Icon Selection
            Text(
                text = "Icon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val iconOptions = listOf("📋", "🏥", "💪", "📚", "⚡", "🧘", "🥗", "👥", "🎨", "💰", "🎯", "💡", "🌱", "🔍")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(iconOptions) { icon ->
                    EditIconChip(
                        icon = icon,
                        isSelected = selectedIcon == icon,
                        onSelect = { selectedIcon = icon }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val updatedHabit = habitToEdit.copy(
                                title = title,
                                description = description,
                                category = selectedCategory.name,
                                icon = selectedIcon
                            )
                            
                            viewModel.updateHabit(updatedHabit)
                            navController.popBackStack()
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
fun EditCategoryChip(
    category: com.example.habbittracker.domain.model.HabitCategory,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .clickable { onSelect() }
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.icon,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

@Composable
fun EditIconChip(
    icon: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
