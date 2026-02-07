package com.bloodworktracker.ui.screens.tests

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodworktracker.ui.viewmodel.TestListViewModel
import com.bloodworktracker.ui.viewmodel.BloodworkViewModelFactory
import com.bloodworktracker.data.repository.BloodworkRepository
import com.bloodworktracker.data.database.BloodworkDatabase
import com.bloodworktracker.ui.viewmodel.TestListItem
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTest: (Long) -> Unit,
    onNavigateToAddTest: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val database = BloodworkDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
    val repository = BloodworkRepository(
        database.bloodValueDao(),
        database.bloodTestDao(),
        database.bloodTestResultDao()
    )
    val viewModel: TestListViewModel = viewModel(
        factory = BloodworkViewModelFactory(repository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    val scope = rememberCoroutineScope()
    
    // Manual refresh functionality
    // Pull-to-refresh will be added in a future update when the API is stable
    
    // Handle back navigation
    BackHandler {
        onNavigateBack()
    }
    
    // Error handling with snackbar
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = uiState.error ?: "Fehler beim Laden der Tests",
                    withDismissAction = true
                )
            }
            viewModel.clearError()
        }
    }
    
    // Delete success feedback
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Test erfolgreich gelöscht",
                    withDismissAction = true
                )
            }
            viewModel.clearDeleteSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meine Tests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshTests() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTest,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Test hinzufügen") }
            )
        }
    ) { paddingValues ->
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Fehler beim Laden der Tests",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.clearError(); viewModel.refreshTests() }) {
                        Text("Erneut versuchen")
                    }
                }
            }
            
            uiState.tests.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Noch keine Tests vorhanden",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fügen Sie Ihren ersten Bluttest hinzu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.tests,
                        key = { it.id }
                    ) { test ->
                        TestItemCard(
                            test = test,
                            dateFormat = dateFormat,
                            onTestClick = { onNavigateToTest(test.id) },
                            onDeleteClick = { viewModel.deleteTest(test.id) }
                        )
                    }
                    
                    // Add some padding at the bottom for the FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestItemCard(
    test: TestListItem,
    dateFormat: SimpleDateFormat,
    onTestClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        onClick = onTestClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = dateFormat.format(test.testDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (test.labName.isNotBlank()) {
                        Text(
                            text = test.labName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                IconButton(
                    onClick = { showDeleteConfirmation = true }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Test löschen",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total values
                StatusChip(
                    count = test.numberOfValues,
                    label = "Werte",
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                
                // Normal values
                if (test.normalCount > 0) {
                    StatusChip(
                        count = test.normalCount,
                        label = "Normal",
                        color = Color(0xFF4CAF50) // Green
                    )
                }
                
                // Abnormal values
                if (test.abnormalCount > 0) {
                    StatusChip(
                        count = test.abnormalCount,
                        label = "Auffällig",
                        color = Color(0xFFFF9800) // Orange
                    )
                }
                
                // Critical values
                if (test.criticalCount > 0) {
                    StatusChip(
                        count = test.criticalCount,
                        label = "Kritisch",
                        color = Color(0xFFf44336) // Red
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Test löschen") },
            text = { 
                Text("Möchten Sie diesen Test wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun StatusChip(
    count: Int,
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}