package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DailyLog
import com.example.data.model.DefaultProducts
import com.example.data.model.ProductTempRecord
import com.example.data.repository.TempTrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class TempTrackerUiState(
    val selectedDate: String = "",
    val dayOfWeek: String = "",
    val checkedBy: String = "Staff Member",
    val generalNotes: String = "",
    val records: List<ProductTempRecord> = emptyList(),
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val isSavedToCloud: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val userMessage: String? = null,
    val errorMessage: String? = null
)

class TempTrackerViewModel(
    private val repository: TempTrackerRepository,
    private val userId: String
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dayFormat = SimpleDateFormat("EEEE", Locale.US)

    private val _uiState = MutableStateFlow(TempTrackerUiState())
    val uiState: StateFlow<TempTrackerUiState> = _uiState.asStateFlow()

    val allHistoryLogs: StateFlow<List<DailyLog>> = repository.observeAllLogs()
        .catch { error ->
            _uiState.value = _uiState.value.copy(errorMessage = "Failed to load history: ${error.message}")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        val today = Calendar.getInstance()
        setDate(today.time)
    }

    fun setDate(date: Date) {
        val dateStr = dateFormat.format(date)
        val dayName = dayFormat.format(date)
        _uiState.value = _uiState.value.copy(
            selectedDate = dateStr,
            dayOfWeek = dayName,
            isLoading = true,
            isSavedToCloud = false,
            errorMessage = null
        )
        loadDay(dateStr, dayName)
    }

    private fun loadDay(dateStr: String, dayName: String) {
        repository.observeDailyLog(dateStr)
            .onEach { log ->
                if (log != null && log.items.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        selectedDate = log.date,
                        dayOfWeek = log.dayOfWeek,
                        checkedBy = log.checkedBy.ifEmpty { _uiState.value.checkedBy },
                        generalNotes = log.notes.orEmpty(),
                        records = log.toProductRecords(),
                        isLoading = false,
                        isSavedToCloud = true
                    )
                } else {
                    // Initialize with default template items from the sheet
                    _uiState.value = _uiState.value.copy(
                        selectedDate = dateStr,
                        dayOfWeek = dayName,
                        records = DefaultProducts.ITEMS.map { it.copy() },
                        isLoading = false,
                        isSavedToCloud = false
                    )
                }
            }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    records = DefaultProducts.ITEMS.map { it.copy() },
                    errorMessage = "Error syncing date: ${error.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    fun goToPreviousDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_uiState.value.selectedDate) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            setDate(cal.time)
        } catch (_: Exception) {}
    }

    fun goToNextDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_uiState.value.selectedDate) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            setDate(cal.time)
        } catch (_: Exception) {}
    }

    fun goToToday() {
        setDate(Calendar.getInstance().time)
    }

    fun updateCheckedBy(name: String) {
        _uiState.value = _uiState.value.copy(checkedBy = name, isSavedToCloud = false)
    }

    fun updateGeneralNotes(notes: String) {
        _uiState.value = _uiState.value.copy(generalNotes = notes, isSavedToCloud = false)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    /**
     * Randomize all products Core temperature between 200 and 210 °C as whole numbers
     */
    fun randomizeAllCoreTemps() {
        val updated = _uiState.value.records.map { record ->
            val randomCore = Random.nextInt(200, 211).toDouble() // 200 to 210 whole number
            record.copy(coreTemp = randomCore)
        }
        _uiState.value = _uiState.value.copy(
            records = updated,
            isSavedToCloud = false,
            userMessage = "Randomized Core temps (200°C - 210°C) for all ${updated.size} items"
        )
    }

    /**
     * Randomize all products T1 temperature between 81 and 100 °C
     */
    fun randomizeAllT1Temps() {
        val updated = _uiState.value.records.map { record ->
            val randomT1 = Random.nextInt(81, 101).toDouble() // 81 to 100 whole number
            record.copy(t1Temp = randomT1)
        }
        _uiState.value = _uiState.value.copy(
            records = updated,
            isSavedToCloud = false,
            userMessage = "Randomized T1 temps (81°C - 100°C) for all ${updated.size} items"
        )
    }

    /**
     * Randomize both Core and T1 temperatures for all items
     */
    fun randomizeBothAll() {
        val updated = _uiState.value.records.map { record ->
            val randomCore = Random.nextInt(200, 211).toDouble()
            val randomT1 = Random.nextInt(81, 101).toDouble()
            record.copy(coreTemp = randomCore, t1Temp = randomT1)
        }
        _uiState.value = _uiState.value.copy(
            records = updated,
            isSavedToCloud = false,
            userMessage = "Autofilled both Core (200-210°C) & T1 (81-100°C) for all items"
        )
    }

    fun randomizeSingleCore(productId: String) {
        val randomCore = Random.nextInt(200, 211).toDouble()
        updateProductCore(productId, randomCore)
    }

    fun randomizeSingleT1(productId: String) {
        val randomT1 = Random.nextInt(81, 101).toDouble()
        updateProductT1(productId, randomT1)
    }

    fun editProduct(productId: String, newName: String, newCategory: String) {
        val updated = _uiState.value.records.map {
            if (it.productId == productId) it.copy(productName = newName.trim(), category = newCategory.trim()) else it
        }
        _uiState.value = _uiState.value.copy(
            records = updated,
            isSavedToCloud = false,
            userMessage = "Updated \"$newName\""
        )
    }

    fun updateProductCore(productId: String, temp: Double?) {
        val updated = _uiState.value.records.map {
            if (it.productId == productId) it.copy(coreTemp = temp) else it
        }
        _uiState.value = _uiState.value.copy(records = updated, isSavedToCloud = false)
    }

    fun updateProductT1(productId: String, temp: Double?) {
        val updated = _uiState.value.records.map {
            if (it.productId == productId) it.copy(t1Temp = temp) else it
        }
        _uiState.value = _uiState.value.copy(records = updated, isSavedToCloud = false)
    }

    fun updateProductNotes(productId: String, notes: String) {
        val updated = _uiState.value.records.map {
            if (it.productId == productId) it.copy(notes = notes) else it
        }
        _uiState.value = _uiState.value.copy(records = updated, isSavedToCloud = false)
    }

    fun clearAllTemps() {
        val updated = _uiState.value.records.map {
            it.copy(coreTemp = null, t1Temp = null)
        }
        _uiState.value = _uiState.value.copy(
            records = updated,
            isSavedToCloud = false,
            userMessage = "Cleared temperatures for ${_uiState.value.selectedDate}"
        )
    }

    fun addCustomProduct(name: String, category: String) {
        val id = "custom_" + System.currentTimeMillis()
        val newRecord = ProductTempRecord(
            productId = id,
            productName = name.trim(),
            category = category.trim().ifEmpty { "Fry Food / Custom" }
        )
        val updated = _uiState.value.records + newRecord
        _uiState.value = _uiState.value.copy(
            records = updated,
            isSavedToCloud = false,
            userMessage = "Added \"$name\" to list"
        )
    }

    fun deleteProduct(productId: String) {
        val updated = _uiState.value.records.filterNot { it.productId == productId }
        _uiState.value = _uiState.value.copy(records = updated, isSavedToCloud = false)
    }

    fun saveCurrentLog() {
        val state = _uiState.value
        _uiState.value = state.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            val dailyLog = DailyLog.fromRecords(
                userId = userId,
                date = state.selectedDate,
                dayOfWeek = state.dayOfWeek,
                checkedBy = state.checkedBy,
                records = state.records,
                notes = state.generalNotes
            )

            val result = repository.saveDailyLog(dailyLog)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSavedToCloud = true,
                    userMessage = "Saved to Cloud Firestore: ${state.selectedDate}"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Save failed: ${result.exceptionOrNull()?.localizedMessage}"
                )
            }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null, errorMessage = null)
    }
}

class TempTrackerViewModelFactory(
    private val repository: TempTrackerRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TempTrackerViewModel::class.java)) {
            return TempTrackerViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
