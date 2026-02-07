package com.bloodworktracker.ui.screens.values

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodworktracker.ui.viewmodel.ValueCategoriesViewModel
import com.bloodworktracker.ui.viewmodel.BloodworkViewModelFactory
import com.bloodworktracker.data.repository.BloodworkRepository
import com.bloodworktracker.data.database.BloodworkDatabase
import com.bloodworktracker.data.database.entities.BloodValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValueCategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToValue: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = BloodworkDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
    val repository = BloodworkRepository(
        database.bloodValueDao(),
        database.bloodTestDao(),
        database.bloodTestResultDao()
    )
    val viewModel: ValueCategoriesViewModel = viewModel(
        factory = BloodworkViewModelFactory(repository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blutwerte") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Suche nach Blutwerten...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Suche löschen")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { /* Hide keyboard manually if needed */ }
                ),
                singleLine = true
            )
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Fehler beim Laden der Blutwerte",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Erneut versuchen")
                        }
                    }
                }
                
                uiState.searchQuery.isNotEmpty() -> {
                    // Show search results
                    SearchResults(
                        searchQuery = uiState.searchQuery,
                        filteredValues = uiState.filteredValues,
                        onNavigateToValue = onNavigateToValue
                    )
                }
                
                else -> {
                    // Show categories
                    CategoriesView(
                        categories = uiState.categories,
                        expandedCategories = uiState.expandedCategories,
                        onToggleCategory = viewModel::toggleCategoryExpansion,
                        onNavigateToValue = onNavigateToValue
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    searchQuery: String,
    filteredValues: List<BloodValue>,
    onNavigateToValue: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "${filteredValues.size} Ergebnisse für \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(filteredValues, key = { it.id }) { bloodValue ->
            BloodValueItem(
                bloodValue = bloodValue,
                onNavigateToValue = onNavigateToValue
            )
        }
        
        if (filteredValues.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Keine Blutwerte gefunden",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesView(
    categories: List<com.bloodworktracker.data.model.BloodValueCategory>,
    expandedCategories: Set<String>,
    onToggleCategory: (String) -> Unit,
    onNavigateToValue: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories, key = { it.name }) { category ->
            CategoryCard(
                category = category,
                isExpanded = expandedCategories.contains(category.name),
                onToggleExpansion = { onToggleCategory(category.name) },
                onNavigateToValue = onNavigateToValue
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryCard(
    category: com.bloodworktracker.data.model.BloodValueCategory,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    onNavigateToValue: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggleExpansion
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${category.values.size} Werte",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Einklappen" else "Ausklappen"
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                category.values.forEach { bloodValue ->
                    BloodValueItem(
                        bloodValue = bloodValue,
                        onNavigateToValue = onNavigateToValue,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BloodValueItem(
    bloodValue: BloodValue,
    onNavigateToValue: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onNavigateToValue(bloodValue.id) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bloodValue.nameDe,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${bloodValue.abbreviation} • ${bloodValue.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Details anzeigen",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}