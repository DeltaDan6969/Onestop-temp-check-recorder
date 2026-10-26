package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.data.model.ProductTempRecord
import com.example.ui.theme.GreenCompliant
import com.example.ui.theme.GreenCompliantLight
import com.example.ui.theme.RedNonCompliant
import com.example.ui.theme.RedNonCompliantLight
import com.example.ui.viewmodel.TempTrackerUiState
import com.example.ui.viewmodel.TempTrackerViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    viewModel: TempTrackerViewModel,
    uiState: TempTrackerUiState,
    allHistoryLogs: List<DailyLog>,
    onNavigateToHistory: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showExportDialog by remember { mutableStateOf(false) }
    var showGitHubDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showUserMenu by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<ProductTempRecord?>(null) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TempTracker",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${uiState.selectedDate} • ${uiState.dayOfWeek}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                },
                actions = {
                    // GitHub connection dialog
                    IconButton(
                        onClick = { showGitHubDialog = true },
                        modifier = Modifier.testTag("github_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "GitHub Integration",
                            tint = Color.White
                        )
                    }

                    // Export PDF/CSV dialog
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Reports",
                            tint = Color.White
                        )
                    }

                    // History screen
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("history_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Search History",
                            tint = Color.White
                        )
                    }

                    // User menu
                    Box {
                        IconButton(onClick = { showUserMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Menu",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sign Out") },
                                onClick = {
                                    showUserMenu = false
                                    onSignOut()
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveCurrentLog() },
                icon = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else if (uiState.isSavedToCloud) {
                        Icon(Icons.Default.CloudDone, contentDescription = null)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                    }
                },
                text = {
                    Text(
                        if (uiState.isSaving) "Saving..."
                        else if (uiState.isSavedToCloud) "Saved to Cloud"
                        else "Save to Cloud"
                    )
                },
                modifier = Modifier.testTag("save_to_cloud_button"),
                containerColor = if (uiState.isSavedToCloud) GreenCompliant else MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Date Navigation Header
            DateNavigationRow(
                selectedDate = uiState.selectedDate,
                dayOfWeek = uiState.dayOfWeek,
                onPrevious = { viewModel.goToPreviousDay() },
                onNext = { viewModel.goToNextDay() },
                onToday = { viewModel.goToToday() },
                onPickDate = { pickedDate -> viewModel.setDate(pickedDate) }
            )

            // Inspector & Summary Strip
            SummaryAndInspectorCard(
                checkedBy = uiState.checkedBy,
                onCheckedByChange = { viewModel.updateCheckedBy(it) },
                records = uiState.records
            )

            // Autofill Action Controls (Core & T1 Randomizers)
            AutofillActionRow(
                onRandomizeAllBoth = { viewModel.randomizeBothAll() },
                onRandomizeAllCore = { viewModel.randomizeAllCoreTemps() },
                onRandomizeAllT1 = { viewModel.randomizeAllT1Temps() },
                onClearAll = { viewModel.clearAllTemps() }
            )

            // Search Bar & Category Filter
            SearchAndCategoryChips(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                selectedCategory = uiState.selectedCategory,
                onCategorySelect = { viewModel.setSelectedCategory(it) },
                onAddProduct = { showAddProductDialog = true }
            )

            // Filtered Products List
            val filteredRecords = uiState.records.filter { record ->
                val matchesSearch = uiState.searchQuery.isBlank() ||
                        record.productName.contains(uiState.searchQuery, ignoreCase = true) ||
                        record.category.contains(uiState.searchQuery, ignoreCase = true)
                val matchesCategory = uiState.selectedCategory == "All" ||
                        record.category.equals(uiState.selectedCategory, ignoreCase = true)
                matchesSearch && matchesCategory
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("products_list"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRecords, key = { it.productId }) { record ->
                    ProductTempCard(
                        record = record,
                        onUpdateCore = { viewModel.updateProductCore(record.productId, it) },
                        onUpdateT1 = { viewModel.updateProductT1(record.productId, it) },
                        onRandomizeCore = { viewModel.randomizeSingleCore(record.productId) },
                        onRandomizeT1 = { viewModel.randomizeSingleT1(record.productId) },
                        onUpdateNotes = { viewModel.updateProductNotes(record.productId, it) },
                        onEdit = { editingRecord = record },
                        onDelete = { viewModel.deleteProduct(record.productId) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showExportDialog) {
        val currentLog = DailyLog.fromRecords(
            userId = "",
            date = uiState.selectedDate,
            dayOfWeek = uiState.dayOfWeek,
            checkedBy = uiState.checkedBy,
            records = uiState.records,
            notes = uiState.generalNotes
        )
        ExportDialog(
            currentLog = currentLog,
            allLogs = allHistoryLogs,
            onDismiss = { showExportDialog = false }
        )
    }

    if (showGitHubDialog) {
        GitHubSyncDialog(onDismiss = { showGitHubDialog = false })
    }

    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onAdd = { name, cat -> viewModel.addCustomProduct(name, cat) }
        )
    }

    editingRecord?.let { targetRecord ->
        EditProductDialog(
            record = targetRecord,
            onDismiss = { editingRecord = null },
            onSave = { newName, newCategory ->
                viewModel.editProduct(targetRecord.productId, newName, newCategory)
                editingRecord = null
            },
            onDelete = {
                viewModel.deleteProduct(targetRecord.productId)
                editingRecord = null
            }
        )
    }
}

@Composable
private fun DateNavigationRow(
    selectedDate: String,
    dayOfWeek: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onPickDate: (java.util.Date) -> Unit
) {
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.testTag("date_prev_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$selectedDate ($dayOfWeek)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Row {
                OutlinedButton(
                    onClick = onToday,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("today_button")
                ) {
                    Text("Today", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            cal.time = sdf.parse(selectedDate) ?: java.util.Date()
                        } catch (_: Exception) {}

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val selected = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }
                                onPickDate(selected.time)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.testTag("date_next_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                }
            }
        }
    }
}

@Composable
private fun SummaryAndInspectorCard(
    checkedBy: String,
    onCheckedByChange: (String) -> Unit,
    records: List<ProductTempRecord>
) {
    val total = records.size
    val recorded = records.count { it.hasAnyTemp }
    val compliant = records.count { it.isCoreCompliant && it.isT1Compliant }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = checkedBy,
                    onValueChange = onCheckedByChange,
                    label = { Text("Checked By (Inspector / Staff)") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("checked_by_field"),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stat pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge(
                    label = "Total Items",
                    value = "$total",
                    color = MaterialTheme.colorScheme.primary
                )
                StatBadge(
                    label = "Recorded",
                    value = "$recorded / $total",
                    color = if (recorded == total) GreenCompliant else MaterialTheme.colorScheme.outline
                )
                StatBadge(
                    label = "Compliant",
                    value = "$compliant / $total",
                    color = if (compliant == total && total > 0) GreenCompliant else if (compliant < recorded) RedNonCompliant else GreenCompliant
                )
                StatBadge(
                    label = "Compliance",
                    value = if (total > 0) "${(compliant * 100) / total}%" else "0%",
                    color = if (compliant == total && total > 0) GreenCompliant else RedNonCompliant
                )
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AutofillActionRow(
    onRandomizeAllBoth: () -> Unit,
    onRandomizeAllCore: () -> Unit,
    onRandomizeAllT1: () -> Unit,
    onClearAll: () -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onRandomizeAllBoth,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("autofill_both_button")
        ) {
            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("⚡ Autofill All (Core & T1)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        ElevatedButton(
            onClick = onRandomizeAllCore,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("autofill_core_button")
        ) {
            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("🎲 Core (200-210°C)", fontSize = 13.sp)
        }

        ElevatedButton(
            onClick = onRandomizeAllT1,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("autofill_t1_button")
        ) {
            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("🎲 T1 (81-100°C)", fontSize = 13.sp)
        }

        OutlinedButton(
            onClick = onClearAll,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier.testTag("clear_all_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset", fontSize = 13.sp)
        }
    }
}

@Composable
private fun SearchAndCategoryChips(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onAddProduct: () -> Unit
) {
    val categories = listOf(
        "All",
        "Bakes & Turnovers",
        "Sausage Rolls",
        "Breakfast",
        "Burgers",
        "Toasties & Paninis",
        "Ciabattas & Wraps",
        "Rollover Hot Dogs",
        "Pasties",
        "Fry Food"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Filter products (e.g. pasty, wrap, fry...)") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_field"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onAddProduct,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .testTag("add_product_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add custom product",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (category in categories) {
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun ProductTempCard(
    record: ProductTempRecord,
    onUpdateCore: (Double?) -> Unit,
    onUpdateT1: (Double?) -> Unit,
    onRandomizeCore: () -> Unit,
    onRandomizeT1: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var coreInput by remember(record.coreTemp) {
        mutableStateOf(record.coreTemp?.toInt()?.toString() ?: "")
    }
    var t1Input by remember(record.t1Temp) {
        mutableStateOf(record.t1Temp?.toInt()?.toString() ?: "")
    }
    var showNotes by remember { mutableStateOf(record.notes.isNotEmpty()) }
    var notesInput by remember(record.notes) { mutableStateOf(record.notes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${record.productId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Product Name + Category + Edit + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.productName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = record.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Overall badge
                    val overallPass = record.isCoreCompliant && record.isT1Compliant
                    val hasAny = record.hasAnyTemp
                    if (hasAny) {
                        Surface(
                            color = if (overallPass) GreenCompliantLight else RedNonCompliantLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (overallPass) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (overallPass) GreenCompliant else RedNonCompliant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (overallPass) "PASS" else "CHECK",
                                    color = if (overallPass) GreenCompliant else RedNonCompliant,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Edit product button
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit product details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (record.productId.startsWith("custom_")) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete item",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two Column Temp inputs: Core & T1 (Whole Numbers only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CORE TEMP BOX
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Core Temp (200-210°C)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        IconButton(
                            onClick = onRandomizeCore,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Randomize Core (200-210°C)",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = coreInput,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            coreInput = digits
                            val parsed = digits.toIntOrNull()?.toDouble()
                            onUpdateCore(parsed)
                        },
                        placeholder = { Text("205") },
                        suffix = { Text("°C") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Target check
                    if (record.coreTemp != null) {
                        Text(
                            text = if (record.isCoreCompliant) "✓ Core >= 200°C" else "✗ Below 200°C",
                            color = if (record.isCoreCompliant) GreenCompliant else RedNonCompliant,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // T1 TEMP BOX
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "T1 Temp (81-100°C)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        IconButton(
                            onClick = onRandomizeT1,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Randomize T1 (81-100°C)",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = t1Input,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            t1Input = digits
                            val parsed = digits.toIntOrNull()?.toDouble()
                            onUpdateT1(parsed)
                        },
                        placeholder = { Text("88") },
                        suffix = { Text("°C") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Target check
                    if (record.t1Temp != null) {
                        Text(
                            text = if (record.isT1Compliant) "✓ T1 >= 81°C" else "✗ Below 81°C",
                            color = if (record.isT1Compliant) GreenCompliant else RedNonCompliant,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Optional note toggle
            if (showNotes) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = {
                        notesInput = it
                        onUpdateNotes(it)
                    },
                    label = { Text("Item notes / comments") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                TextButton(
                    onClick = { showNotes = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+ Add item note", fontSize = 11.sp)
                }
            }
        }
    }
}
