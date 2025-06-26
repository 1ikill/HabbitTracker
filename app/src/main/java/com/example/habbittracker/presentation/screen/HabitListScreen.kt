package com.example.habbittracker.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.habbittracker.domain.model.Habit
import com.example.habbittracker.domain.model.HabitCategories
import com.example.habbittracker.domain.util.StreakCalculator
import com.example.habbittracker.domain.util.StreakRecovery
import com.example.habbittracker.presentation.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    viewModel: HabitViewModel = hiltViewModel(),
    navController: NavController
) {
    val habits by viewModel.habits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // State for category filtering
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    
    // Filter habits based on selected category
    val filteredHabits = remember(habits, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            habits
        } else {
            habits.filter { it.category == selectedCategoryFilter }
        }
    }

    // Show error message if any
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // You could show a snackbar here if needed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Habits") },
                actions = {
                    // Debug test button (only show if there are habits)
                    if (habits.isNotEmpty()) {
                        IconButton(
                            onClick = { 
                                // Add test data to the first habit for testing
                                viewModel.addTestCompletionDates(habits.first().id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Add Test Data",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_habit") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Category Filter Row
            if (habits.isNotEmpty()) {
                CategoryFilterRow(
                    selectedCategory = selectedCategoryFilter,
                    onCategorySelected = { category ->
                        selectedCategoryFilter = if (selectedCategoryFilter == category) null else category
                    },
                    availableCategories = habits.map { it.category }.distinct()
                )
            }
            
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    habits.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No habits yet!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap the + button to create your first habit",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    filteredHabits.isEmpty() && selectedCategoryFilter != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No habits found",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No habits in the selected category",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredHabits) { habit ->
                                HabitItem(
                                    habit = habit, 
                                    onEdit = { navController.navigate("edit_habit/${habit.id}") },
                                    onPin = { viewModel.togglePinHabit(habit.id) },
                                    onToggleCompletion = { viewModel.toggleHabitCompletion(habit.id) },
                                    onDelete = { viewModel.deleteHabit(habit.id) },
                                    onViewCalendar = { navController.navigate("habit_calendar/${habit.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onEdit: () -> Unit, onPin: () -> Unit, onToggleCompletion: () -> Unit, onDelete: () -> Unit, onViewCalendar: () -> Unit = {}) {
    val category = HabitCategories.getCategoryByName(habit.category)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (habit.pinned) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.pinned) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (habit.pinned) 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Action buttons in vertical column
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(40.dp)
            ) {
                // Completion button (most important - at top)
                val isCompletedToday = StreakCalculator.isCompletedToday(habit.completionDates)
                IconButton(
                    onClick = onToggleCompletion,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (isCompletedToday) "Mark incomplete" else "Mark complete",
                        tint = if (isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Pin button
                IconButton(
                    onClick = onPin,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (habit.pinned) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (habit.pinned) "Unpin habit" else "Pin habit",
                        tint = if (habit.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Center - Habit content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Habit Icon and Title row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Habit Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = habit.icon,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        // Title and Pin indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (habit.pinned) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Pinned",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        // Category
                        Surface(
                            modifier = Modifier.padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${category.icon} ${category.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(android.graphics.Color.parseColor(category.color)),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Streak information
                if (habit.currentStreak > 0) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "*",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${habit.currentStreak}-day streak!",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (habit.bestStreak > habit.currentStreak) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(Best: ${habit.bestStreak})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Streak recovery message
                val streakStatus = StreakRecovery.getStreakStatus(habit)
                if (streakStatus.isAtRisk || habit.currentStreak == 0) {
                    val messageColor = when (streakStatus.severity) {
                        StreakRecovery.RecoverySeverity.URGENT -> MaterialTheme.colorScheme.error
                        StreakRecovery.RecoverySeverity.MODERATE -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.primary
                    }
                    
                    Text(
                        text = streakStatus.message,
                        style = MaterialTheme.typography.labelSmall,
                        color = messageColor,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Description (if not empty)
                if (habit.description.isNotEmpty()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Right side - Secondary actions in vertical column
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(36.dp)
            ) {
                // Calendar button
                IconButton(
                    onClick = onViewCalendar,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "View calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Edit button  
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit habit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete habit",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,  
    availableCategories: List<String>
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableCategories) { categoryName ->
            val category = HabitCategories.getCategoryByName(categoryName)
            val isSelected = selectedCategory == categoryName
            
            FilterChip(
                onClick = { onCategorySelected(categoryName) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = category.icon)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = category.name)
                    }
                },
                selected = isSelected,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            )
        }
    }
}