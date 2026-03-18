package com.greencoins.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencoins.app.data.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            _categories.value = ShopRepository.getCategories()
        }
    }
}
