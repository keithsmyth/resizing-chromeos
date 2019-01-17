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
import androidx.lifecycle.ViewModelProviders
import com.google.example.resizecodelab.R
import kotlinx.android.synthetic.main.activity_main.*

private const val KEY_EXPANDED = "KEY_EXPANDED"

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Retrieve the ViewModel with state data
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        //Restore savedInstanceState variables
        savedInstanceState?.getBoolean(KEY_EXPANDED)?.let(viewModel::restoreDescriptionExpanded)

        //Set up recycler view for reviews
        val reviewAdapter = ReviewAdapter()
        recyclerReviews.adapter = reviewAdapter
        viewModel.reviews.observe(this, NullFilteringObserver(reviewAdapter::onReviewsLoaded))

        //Set up recycler view for suggested products
        val suggestionAdapter = SuggestionAdapter()
        recyclerSuggested.adapter = suggestionAdapter
        viewModel.suggestions.observe(this, NullFilteringObserver(suggestionAdapter::updateSuggestions))

        viewModel.showControls.observe(this, NullFilteringObserver(::updateControlVisibility))

        viewModel.expandButtonTextResId.observe(this, NullFilteringObserver<Int> { resId ->
            buttonExpand.text = getString(resId)
        })

        viewModel.productName.observe(this, NullFilteringObserver(textProductName::setText))
        viewModel.productCompany.observe(this, NullFilteringObserver(textProductCompany::setText))
        viewModel.descriptionText.observe(this, NullFilteringObserver(textProductDescription::setText))

        //Expand/collapse button for product description
        buttonExpand.setOnClickListener { viewModel.toggleDescriptionExpanded() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.isDescriptionExpanded.value?.let { (outState.putBoolean(KEY_EXPANDED, it)) }
    }

    private fun updateControlVisibility(showControls: Boolean) {
        progressLoadingReviews.isVisible = !showControls
        buttonPurchase.isVisible = showControls
        buttonExpand.isVisible = showControls
    }
}
