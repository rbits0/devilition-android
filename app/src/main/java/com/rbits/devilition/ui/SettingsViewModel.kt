package com.rbits.devilition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rbits.devilition.data.ISettingsRepository
import com.rbits.devilition.data.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ISettingsRepository
) : ViewModel() {
    val settingsState = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Settings(),
    )

    fun setTimeBonusEnabled(value: Boolean) {
        viewModelScope.launch {
            repository.setTimeBonusEnabled(value)
        }
    }
}

class SettingsViewModelFactory(private val repository: ISettingsRepository)
    : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Invalid ViewModel class")
        }
    }
}
