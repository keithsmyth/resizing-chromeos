/*      Copyright 2018 Google LLC

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.google.example.resizecodelab.view

import androidx.lifecycle.*
import com.google.example.resizecodelab.R
import com.google.example.resizecodelab.data.DataProvider
import com.google.example.resizecodelab.data.Review

/**
 * Facilitates fetching required data and exposes View related data back to view
 * Survives config changes (e.g. rotation), good place to store data that takes time to recover
 */
class MainViewModel : ViewModel() {

    private val dataProvider = DataProvider()

    private val appData = dataProvider.fetchData()

    val suggestions = dataProvider.fetchSuggestions()

    val showControls: LiveData<Boolean> = Transformations.map(appData) { it != null }

    val productName: LiveData<String> = Transformations.map(appData) { it?.title }

    val productCompany: LiveData<String> = Transformations.map(appData) { it?.developer }

    private val _isDescriptionExpanded = MutableLiveData<Boolean>()
    val isDescriptionExpanded: LiveData<Boolean>
        get() = _isDescriptionExpanded

    private val _descriptionText = MediatorLiveData<String>().apply {
        addSource(appData) { value = determineDescriptionText() }
        addSource(_isDescriptionExpanded) { value = determineDescriptionText() }
    }
    val descriptionText: LiveData<String>
        get() = _descriptionText

    val expandButtonTextResId: LiveData<Int> = Transformations.map(_isDescriptionExpanded) {
        if (it == true) {
            R.string.button_collapse
        } else {
            R.string.button_expand
        }
    }

    val reviews: LiveData<List<Review>> = Transformations.map(appData) { it?.reviews }

    /**
     * Handle toggle button presses
     */
    fun toggleDescriptionExpanded() {
        _isDescriptionExpanded.value = _isDescriptionExpanded.value != true
    }

    /**
     * onSaveInstanceState can outlive a ViewModel
     * Save data that cannot be retrieved again in onSaveInstanceState, and restore it when recreated
     */
    fun restoreDescriptionExpanded(isExpanded: Boolean) {
        _isDescriptionExpanded.value = isExpanded
    }

    private fun determineDescriptionText(): String? {
        return appData.value?.let { appData ->
            if (_isDescriptionExpanded.value == true) {
                appData.description
            } else {
                appData.shortDescription
            }
        }
    }
}
