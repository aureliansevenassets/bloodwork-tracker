package com.bloodworktracker.ui.screens.analysis

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
import com.bloodworktracker.ui.viewmodel.AnalysisViewModel
import com.bloodworktracker.ui.viewmodel.BloodworkViewModelFactory
import com.bloodworktracker.data.repository.BloodworkRepository
import com.bloodworktracker.data.database.BloodworkDatabase
import com.bloodworktracker.data.model.AnalysisSeverity
import com.bloodworktracker.data.model.ConstellationAnalysis
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    testId: Long,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val database = BloodworkDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
    val repository = BloodworkRepository(
        database.bloodValueDao(),
        database.bloodTestDao(),
        database.bloodTestResultDao()
    )
    val viewModel: AnalysisViewModel = viewModel(
        factory = BloodworkViewModelFactory(repository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    
    LaunchedEffect(testId) {
        viewModel.loadTestAndAnalyze(testId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.test?.let { 
                            "Analyse vom ${dateFormat.format(it.bloodTest.testDate)}" 
                        } ?: "Konstellation Analyse"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    if (uiState.test != null && !uiState.isAnalyzing) {
                        IconButton(onClick = { viewModel.analyzeTest(testId) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Erneut analysieren")
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
                        text = "Fehler bei der Analyse",
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
                        viewModel.loadTestAndAnalyze(testId)
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
                        TestSummaryCard(
                            test = uiState.test!!,
                            dateFormat = dateFormat
                        )
                    }
                    
                    // Analysis Status
                    item {
                        AnalysisStatusCard(
                            isAnalyzing = uiState.isAnalyzing,
                            analysisCount = uiState.analyses.size
                        )
                    }
                    
                    // Analysis Results
                    if (uiState.analyses.isNotEmpty()) {
                        items(uiState.analyses, key = { "${it.title}_${it.severity}" }) { analysis ->
                            AnalysisResultCard(analysis = analysis)
                        }
                    } else if (!uiState.isAnalyzing) {
                        item {
                            NoAnalysisCard()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestSummaryCard(
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
                text = "Test-Übersicht",
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
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "Blutwerte:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = test.results.size.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (test.bloodTest.labName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Labor: ${test.bloodTest.labName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AnalysisStatusCard(
    isAnalyzing: Boolean,
    analysisCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isAnalyzing) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isAnalyzing) "Analysiere Konstellationen..." else "Analyse abgeschlossen",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (!isAnalyzing) {
                    Text(
                        text = if (analysisCount > 0) 
                            "$analysisCount Befunde gefunden" 
                        else 
                            "Alle Werte im Normalbereich",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisResultCard(analysis: ConstellationAnalysis) {
    val (backgroundColor, borderColor, iconColor) = when (analysis.severity) {
        AnalysisSeverity.INFO -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.1f),
            Color(0xFF2196F3),
            Color(0xFF2196F3)
        )
        AnalysisSeverity.WARNING -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.1f),
            Color(0xFFFF9800),
            Color(0xFFFF9800)
        )
        AnalysisSeverity.CRITICAL -> Triple(
            Color(0xFFf44336).copy(alpha = 0.1f),
            Color(0xFFf44336),
            Color(0xFFf44336)
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with severity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = when (analysis.severity) {
                        AnalysisSeverity.INFO -> Icons.Default.Info
                        AnalysisSeverity.WARNING -> Icons.Default.Warning
                        AnalysisSeverity.CRITICAL -> Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = analysis.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = analysis.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                SeverityBadge(severity = analysis.severity)
            }
            
            // Affected values
            if (analysis.affectedValues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Betroffene Werte:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    analysis.affectedValues.take(5).forEach { value -> // Show max 5 values
                        Box(
                            modifier = Modifier
                                .background(
                                    color = borderColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.labelSmall,
                                color = borderColor
                            )
                        }
                    }
                    
                    if (analysis.affectedValues.size > 5) {
                        Text(
                            text = "+${analysis.affectedValues.size - 5}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            // Recommendations
            if (analysis.recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Empfehlungen:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                analysis.recommendations.forEach { recommendation ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: AnalysisSeverity) {
    val (color, text) = when (severity) {
        AnalysisSeverity.INFO -> Color(0xFF2196F3) to "Info"
        AnalysisSeverity.WARNING -> Color(0xFFFF9800) to "Warnung"
        AnalysisSeverity.CRITICAL -> Color(0xFFf44336) to "Kritisch"
    }
    
    Box(
        modifier = Modifier
            .background(
                color = color,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NoAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            
            Column {
                Text(
                    text = "Keine Auffälligkeiten gefunden",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                Text(
                    text = "Ihre Blutwerte zeigen keine bekannten problematischen Konstellationen.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}