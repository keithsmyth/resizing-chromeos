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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.example.resizecodelab.R
import com.google.example.resizecodelab.data.AppData
import com.google.example.resizecodelab.data.DataProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var suggestionAdapter: SuggestionAdapter
    private var isDescriptionExpanded: Boolean = false
    private lateinit var description: String
    private lateinit var shortDescription: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set up recycler view for reviews
        reviewAdapter = ReviewAdapter()
        recyclerReviews.adapter = reviewAdapter

        //Set up recycler view for suggested products
        suggestionAdapter = SuggestionAdapter()
        recyclerSuggested.adapter = suggestionAdapter

        //Expand/collapse button for product description
        buttonExpand.setOnClickListener {
            toggleExpandButton()
            updateDescription()
        }

        // Default state.
        handleReviewsUpdate(null)

        val reviewProvider = DataProvider()

        // Observe main data object
        reviewProvider.fetchData().observe(this, Observer(::handleReviewsUpdate))

        // Observe secondary data object
        reviewProvider.fetchSuggestions().observe(
            this,
            NullFilteringObserver(suggestionAdapter::updateSuggestions)
        )
    }

    private fun handleReviewsUpdate(appData: AppData?) {
        updateControlVisibility(appData != null)
        appData?.let {
            textProductName.text = it.title
            textProductCompany.text = it.developer

            reviewAdapter.onReviewsLoaded(it.reviews)

            description = it.description
            shortDescription = it.shortDescription
            updateDescription()
        }
    }

    private fun determineDescriptionText(): String {
        return if (isDescriptionExpanded) {
            description
        } else {
            shortDescription
        }
    }

    private fun updateControlVisibility(showControls: Boolean) {
        progressLoadingReviews.isVisible = !showControls
        buttonPurchase.isVisible = showControls
        buttonExpand.isVisible = showControls
    }

    private fun toggleExpandButton() {
        isDescriptionExpanded = !isDescriptionExpanded

        if (isDescriptionExpanded) {
            buttonExpand.setText(R.string.button_collapse)
        } else {
            buttonExpand.setText(R.string.button_expand)
        }
    }

    private fun updateDescription() {
        textProductDescription.text = determineDescriptionText()
    }
}
