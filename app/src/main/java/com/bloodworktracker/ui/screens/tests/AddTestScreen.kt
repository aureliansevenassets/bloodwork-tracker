package com.bloodworktracker.ui.screens.tests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodworktracker.ui.viewmodel.AddTestViewModel
import com.bloodworktracker.ui.viewmodel.BloodworkViewModelFactory
import com.bloodworktracker.data.repository.BloodworkRepository
import com.bloodworktracker.data.database.BloodworkDatabase
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTestScreen(
    onNavigateBack: () -> Unit,
    onTestSaved: () -> Unit
) {
    val context = LocalContext.current
    val database = BloodworkDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
    val repository = BloodworkRepository(
        database.bloodValueDao(),
        database.bloodTestDao(),
        database.bloodTestResultDao()
    )
    val viewModel: AddTestViewModel = viewModel(
        factory = BloodworkViewModelFactory(repository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val dateDialogState = rememberMaterialDialogState()
    
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }
    
    // Date formatter
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    
    // Error handling
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            // Show error (could be improved with a proper error dialog/snackbar)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            if (uiState.selectedValues.isNotEmpty()) {
                                viewModel.saveTest(onTestSaved)
                            }
                        },
                        enabled = !uiState.isSaving && uiState.selectedValues.isNotEmpty()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Speichern")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Test Date
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Testdatum",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = dateFormat.format(uiState.testDate),
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { dateDialogState.show() }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Datum auswählen")
                                    }
                                },
                                label = { Text("Datum") }
                            )
                        }
                    }
                }
                
                // Lab Name and Notes
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Zusätzliche Informationen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            OutlinedTextField(
                                value = uiState.labName,
                                onValueChange = viewModel::updateLabName,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Labor (optional)") },
                                placeholder = { Text("z.B. Laborgemeinschaft Hamburg") }
                            )
                            
                            OutlinedTextField(
                                value = uiState.notes,
                                onValueChange = viewModel::updateNotes,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Notizen (optional)") },
                                placeholder = { Text("Zusätzliche Bemerkungen...") },
                                minLines = 2
                            )
                        }
                    }
                }
                
                // Blood Values by Category
                items(uiState.bloodValueCategories) { category ->
                    val isExpanded = expandedCategories.contains(category.name)
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            expandedCategories = if (isExpanded) {
                                expandedCategories - category.name
                            } else {
                                expandedCategories + category.name
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
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
                                
                                val selectedInCategory = category.values.count { value ->
                                    uiState.selectedValues.containsKey(value.id)
                                }
                                
                                if (selectedInCategory > 0) {
                                    Text(
                                        text = "$selectedInCategory ausgewählt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                category.values.forEach { bloodValue ->
                                    var textValue by remember(bloodValue.id) { 
                                        mutableStateOf(uiState.selectedValues[bloodValue.id]?.toString() ?: "") 
                                    }
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${bloodValue.nameDe} (${bloodValue.abbreviation})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = textValue,
                                                onValueChange = { newValue ->
                                                    textValue = newValue
                                                    val doubleValue = newValue.toDoubleOrNull()
                                                    viewModel.updateBloodValue(bloodValue.id, doubleValue)
                                                },
                                                modifier = Modifier.weight(1f),
                                                label = { Text("Wert") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true
                                            )
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Text(
                                                text = bloodValue.unit,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.width(60.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Add some padding at the bottom
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    
    // Date Picker Dialog
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton(text = "OK")
            negativeButton(text = "Abbrechen")
        }
    ) {
        datepicker(
            initialDate = LocalDate.now(),
            title = "Testdatum auswählen"
        ) { date ->
            val javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
            viewModel.updateTestDate(javaDate)
        }
    }
}