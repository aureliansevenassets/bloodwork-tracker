package com.bloodworktracker.ui.screens.tests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodworktracker.ui.viewmodel.TestDetailViewModel
import com.bloodworktracker.ui.viewmodel.BloodworkViewModelFactory
import com.bloodworktracker.data.repository.BloodworkRepository
import com.bloodworktracker.data.database.BloodworkDatabase
import com.bloodworktracker.data.database.entities.ValueStatus
import com.bloodworktracker.data.model.BloodTestResultWithValue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailScreen(
    testId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val database = BloodworkDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
    val repository = BloodworkRepository(
        database.bloodValueDao(),
        database.bloodTestDao(),
        database.bloodTestResultDao()
    )
    val viewModel: TestDetailViewModel = viewModel(
        factory = BloodworkViewModelFactory(repository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    
    LaunchedEffect(testId) {
        viewModel.loadTest(testId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.test?.let { 
                            "Test vom ${dateFormat.format(it.bloodTest.testDate)}" 
                        } ?: "Testdetails"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    if (uiState.test != null && uiState.groupedResults.isNotEmpty()) {
                        IconButton(onClick = onNavigateToAnalysis) {
                            Icon(Icons.Default.Info, contentDescription = "Analyse")
                        }
                    }
                }
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
                        text = "Fehler beim Laden der Testdetails",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { 
                        viewModel.clearError()
                        viewModel.loadTest(testId)
                    }) {
                        Text("Erneut versuchen")
                    }
                }
            }
            
            uiState.test == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Test nicht gefunden",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Test Information Header
                    item {
                        TestInfoCard(
                            test = uiState.test!!,
                            dateFormat = dateFormat
                        )
                    }
                    
                    // Grouped Results by Category
                    uiState.groupedResults.forEach { (category, results) ->
                        item(key = category) {
                            CategoryResultsCard(
                                category = category,
                                results = results,
                                onValueUpdate = { result, newValue ->
                                    viewModel.updateResult(result, newValue)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestInfoCard(
    test: com.bloodworktracker.data.model.BloodTestWithResults,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Testinformationen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Datum:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = dateFormat.format(test.bloodTest.testDate),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "Anzahl Werte:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = test.results.size.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (test.bloodTest.labName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Labor:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = test.bloodTest.labName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (test.bloodTest.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notizen:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = test.bloodTest.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CategoryResultsCard(
    category: String,
    results: List<BloodTestResultWithValue>,
    onValueUpdate: (BloodTestResultWithValue, Double) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status summary
                    val statusCounts = results.groupingBy { it.status }.eachCount()
                    
                    statusCounts.forEach { (status, count) ->
                        if (count > 0) {
                            StatusBadge(status = status, count = count)
                        }
                    }
                    
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Einklappen" else "Ausklappen"
                        )
                    }
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                results.forEach { result ->
                    BloodValueResultItem(
                        result = result,
                        onValueUpdate = { newValue -> onValueUpdate(result, newValue) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun BloodValueResultItem(
    result: BloodTestResultWithValue,
    onValueUpdate: (Double) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(result.value) { mutableStateOf(result.value.toString()) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${result.nameDe} (${result.abbreviation})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = buildReferenceRange(result),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            textValue.toDoubleOrNull()?.let { newValue ->
                                onValueUpdate(newValue)
                                isEditing = false
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Speichern")
                    }
                    
                    IconButton(
                        onClick = {
                            textValue = result.value.toString()
                            isEditing = false
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Abbrechen")
                    }
                } else {
                    StatusBadge(status = result.status, count = null)
                    
                    Text(
                        text = "${result.value} ${result.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(80.dp)
                    )
                    
                    IconButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: ValueStatus,
    count: Int?
) {
    val (color, text) = when (status) {
        ValueStatus.NORMAL -> Color(0xFF4CAF50) to if (count != null) "$count Normal" else "Normal"
        ValueStatus.HIGH, ValueStatus.LOW -> Color(0xFFFF9800) to if (count != null) "$count Auffällig" else "Auffällig"
        ValueStatus.CRITICAL_HIGH, ValueStatus.CRITICAL_LOW -> Color(0xFFf44336) to if (count != null) "$count Kritisch" else "Kritisch"
    }
    
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun buildReferenceRange(result: BloodTestResultWithValue): String {
    return when {
        result.minMale != null && result.maxMale != null -> 
            "Referenz (m): ${result.minMale} - ${result.maxMale} ${result.unit}"
        result.minFemale != null && result.maxFemale != null -> 
            "Referenz (w): ${result.minFemale} - ${result.maxFemale} ${result.unit}"
        result.minNormal != null && result.maxNormal != null -> 
            "Referenz: ${result.minNormal} - ${result.maxNormal} ${result.unit}"
        else -> "Kein Referenzbereich verfügbar"
    }
}