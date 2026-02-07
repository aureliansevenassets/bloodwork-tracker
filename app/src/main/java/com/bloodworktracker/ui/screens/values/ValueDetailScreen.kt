package com.bloodworktracker.ui.screens.values

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodworktracker.ui.viewmodel.ValueDetailViewModel
import com.bloodworktracker.ui.viewmodel.BloodworkViewModelFactory
import com.bloodworktracker.data.repository.BloodworkRepository
import com.bloodworktracker.data.database.BloodworkDatabase
import com.bloodworktracker.data.database.entities.ValueStatus
import com.bloodworktracker.data.model.ChartDataPoint
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValueDetailScreen(
    valueId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = BloodworkDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
    val repository = BloodworkRepository(
        database.bloodValueDao(),
        database.bloodTestDao(),
        database.bloodTestResultDao()
    )
    val viewModel: ValueDetailViewModel = viewModel(
        factory = BloodworkViewModelFactory(repository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    
    LaunchedEffect(valueId) {
        viewModel.loadValue(valueId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.bloodValue?.nameDe ?: "Blutwert Details"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
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
                        text = "Fehler beim Laden der Blutwert-Details",
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
                        viewModel.loadValue(valueId)
                    }) {
                        Text("Erneut versuchen")
                    }
                }
            }
            
            uiState.bloodValue == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Blutwert nicht gefunden",
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
                    // Blood Value Info Card
                    item {
                        BloodValueInfoCard(bloodValue = uiState.bloodValue!!)
                    }
                    
                    // Chart Card
                    uiState.chartData?.let { chartData ->
                        item {
                            ChartCard(
                                chartData = chartData,
                                dateFormat = dateFormat
                            )
                        }
                    }
                    
                    // Reference Ranges Card
                    item {
                        ReferenceRangesCard(bloodValue = uiState.bloodValue!!)
                    }
                    
                    // Meaning Card
                    item {
                        MeaningCard(bloodValue = uiState.bloodValue!!)
                    }
                    
                    // History List
                    uiState.chartData?.let { chartData ->
                        if (chartData.dataPoints.isNotEmpty()) {
                            item {
                                HistoryCard(
                                    dataPoints = chartData.dataPoints.reversed(), // Newest first
                                    dateFormat = dateFormat,
                                    unit = uiState.bloodValue!!.unit
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
private fun BloodValueInfoCard(bloodValue: com.bloodworktracker.data.database.entities.BloodValue) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = bloodValue.nameDe,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${bloodValue.abbreviation} • ${bloodValue.unit}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Kategorie: ${bloodValue.category}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (bloodValue.descriptionDe.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Beschreibung:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bloodValue.descriptionDe,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ChartCard(
    chartData: com.bloodworktracker.data.model.ChartData,
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
                text = "Verlauf",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (chartData.dataPoints.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mindestens zwei Messwerte für Chart benötigt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                ValueChart(
                    chartData = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
        }
    }
}

@Composable
private fun ValueChart(
    chartData: com.bloodworktracker.data.model.ChartData,
    modifier: Modifier = Modifier
) {
    val dataPoints = chartData.dataPoints
    val bloodValue = chartData.bloodValue
    
    // Convert data points to chart points
    val points = dataPoints.mapIndexed { index, dataPoint ->
        Point(
            x = index.toFloat(),
            y = dataPoint.value.toFloat()
        )
    }
    
    // Calculate Y-axis range
    val allValues = dataPoints.map { it.value } + listOfNotNull(
        bloodValue.minNormal, bloodValue.maxNormal,
        bloodValue.minMale, bloodValue.maxMale,
        bloodValue.minFemale, bloodValue.maxFemale
    )
    val yMin = allValues.minOrNull()?.let { (it * 0.9).toFloat() } ?: 0f
    val yMax = allValues.maxOrNull()?.let { (it * 1.1).toFloat() } ?: 100f
    
    // X-axis labels (show only some dates to avoid crowding)
    val dateFormat = SimpleDateFormat("MM/yy", Locale.GERMAN)
    val xAxisLabels = dataPoints.mapIndexed { index, point ->
        if (index % maxOf(1, dataPoints.size / 4) == 0) {
            dateFormat.format(point.date)
        } else ""
    }
    
    val xAxisData = AxisData.Builder()
        .axisStepSize(50.dp)
        .steps(maxOf(dataPoints.size - 1, 1))
        .labelData { index ->
            if (index < xAxisLabels.size) xAxisLabels[index] else ""
        }
        .labelAndAxisLinePadding(15.dp)
        .build()
    
    val yAxisData = AxisData.Builder()
        .steps(5)
        .labelData { index ->
            val step = (yMax - yMin) / 5
            String.format("%.1f", yMin + (index * step))
        }
        .labelAndAxisLinePadding(20.dp)
        .build()
    
    // Line data
    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = points,
                    lineStyle = LineStyle(
                        color = MaterialTheme.colorScheme.primary,
                        lineType = LineType.Straight(isDotted = false)
                    ),
                    intersectionPoint = IntersectionPoint(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    selectionHighlightPopUp = SelectionHighlightPopUp()
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        backgroundColor = MaterialTheme.colorScheme.surface
    )
    
    LineChart(
        modifier = modifier,
        lineChartData = lineChartData
    )
}

@Composable
private fun ReferenceRangesCard(bloodValue: com.bloodworktracker.data.database.entities.BloodValue) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Referenzbereiche",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when {
                bloodValue.minMale != null && bloodValue.maxMale != null -> {
                    ReferenceRangeItem(
                        label = "Männlich",
                        min = bloodValue.minMale,
                        max = bloodValue.maxMale,
                        unit = bloodValue.unit
                    )
                }
                bloodValue.minFemale != null && bloodValue.maxFemale != null -> {
                    ReferenceRangeItem(
                        label = "Weiblich", 
                        min = bloodValue.minFemale,
                        max = bloodValue.maxFemale,
                        unit = bloodValue.unit
                    )
                }
                bloodValue.minNormal != null && bloodValue.maxNormal != null -> {
                    ReferenceRangeItem(
                        label = "Normal",
                        min = bloodValue.minNormal,
                        max = bloodValue.maxNormal,
                        unit = bloodValue.unit
                    )
                }
                else -> {
                    Text(
                        text = "Keine Referenzbereiche verfügbar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            // Critical ranges
            if (bloodValue.criticalLow != null || bloodValue.criticalHigh != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Kritische Bereiche",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                bloodValue.criticalLow?.let { low ->
                    Text(
                        text = "Kritisch niedrig: < $low ${bloodValue.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFf44336)
                    )
                }
                
                bloodValue.criticalHigh?.let { high ->
                    Text(
                        text = "Kritisch hoch: > $high ${bloodValue.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFf44336)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferenceRangeItem(
    label: String,
    min: Double,
    max: Double,
    unit: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "$min - $max $unit",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MeaningCard(bloodValue: com.bloodworktracker.data.database.entities.BloodValue) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Bedeutung von Abweichungen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (bloodValue.highMeaningDe.isNotBlank()) {
                Text(
                    text = "Erhöhte Werte:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = bloodValue.highMeaningDe,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (bloodValue.lowMeaningDe.isNotBlank()) {
                Text(
                    text = "Niedrige Werte:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2196F3)
                )
                Text(
                    text = bloodValue.lowMeaningDe,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(
    dataPoints: List<ChartDataPoint>,
    dateFormat: SimpleDateFormat,
    unit: String
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
                text = "Messwerte (${dataPoints.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            dataPoints.take(10).forEach { dataPoint -> // Show only last 10
                HistoryItem(
                    dataPoint = dataPoint,
                    dateFormat = dateFormat,
                    unit = unit
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            if (dataPoints.size > 10) {
                Text(
                    text = "...und ${dataPoints.size - 10} weitere Werte",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    dataPoint: ChartDataPoint,
    dateFormat: SimpleDateFormat,
    unit: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateFormat.format(dataPoint.date),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(status = dataPoint.status)
            
            Text(
                text = "${dataPoint.value} $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatusBadge(status: ValueStatus) {
    val (color, text) = when (status) {
        ValueStatus.NORMAL -> Color(0xFF4CAF50) to "Normal"
        ValueStatus.HIGH, ValueStatus.LOW -> Color(0xFFFF9800) to "Auffällig"
        ValueStatus.CRITICAL_HIGH, ValueStatus.CRITICAL_LOW -> Color(0xFFf44336) to "Kritisch"
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