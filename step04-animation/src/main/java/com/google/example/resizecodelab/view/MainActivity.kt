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

import android.content.res.Configuration
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.animation.AnticipateOvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintsChangedListener
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.example.resizecodelab.R
import kotlinx.android.synthetic.main.activity_main.*

private const val KEY_EXPANDED = "KEY_EXPANDED"

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_shell)

        //Set up constraint layout animations
        constraintMain.loadLayoutDescription(R.xml.constraint_states)
        constraintMain.setOnConstraintsChanged(object : ConstraintsChangedListener() {
            private val changeBounds = ChangeBounds().apply {
                duration = 600
                interpolator = AnticipateOvershootInterpolator(0.2f)
            }

            override fun preLayoutChange(state: Int, layoutId: Int) {
                TransitionManager.beginDelayedTransition(constraintMain, changeBounds)

                when (layoutId) {
                    R.layout.activity_main -> {
                        val reviewLayoutManager = LinearLayoutManager(
                            baseContext,
                            RecyclerView.VERTICAL,
                            false
                        )
                        recyclerReviews.layoutManager = reviewLayoutManager

                        val suggestionLayoutManager = LinearLayoutManager(
                            baseContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        recyclerSuggested.layoutManager = suggestionLayoutManager
                    }

                    R.layout.activity_main_land -> {
                        val reviewLayoutManager = GridLayoutManager(baseContext, 2)
                        recyclerReviews.layoutManager = reviewLayoutManager

                        val suggestionLayoutManager = LinearLayoutManager(
                            baseContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        recyclerSuggested.layoutManager = suggestionLayoutManager
                    }

                    R.layout.activity_main_w400 -> {
                        val reviewLayoutManager = LinearLayoutManager(
                            baseContext,
                            RecyclerView.VERTICAL,
                            false
                        )
                        recyclerReviews.layoutManager = reviewLayoutManager

                        val suggestionLayoutManager = GridLayoutManager(baseContext, 2)
                        recyclerSuggested.layoutManager = suggestionLayoutManager
                    }

                    R.layout.activity_main_w600_land -> {
                        val reviewLayoutManager = GridLayoutManager(baseContext, 2)
                        recyclerReviews.layoutManager = reviewLayoutManager

                        val suggestionLayoutManager = GridLayoutManager(baseContext, 3)
                        recyclerSuggested.layoutManager = suggestionLayoutManager
                    }
                }
            }

            override fun postLayoutChange(stateId: Int, layoutId: Int) {
                //Request all layout elements be redrawn
                constraintMain.requestLayout()
                //Visibility is part of constraint set, so rebinding is necessary
                viewModel.showControls.value?.let { updateControlVisibility(it) }
            }
        })

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
            buttonExpand.setText(resId)
        })

        viewModel.productName.observe(this, NullFilteringObserver(textProductName::setText))
        viewModel.productCompany.observe(this, NullFilteringObserver(textProductCompany::setText))
        viewModel.descriptionText.observe(this, NullFilteringObserver(textProductDescription::setText))

        //Expand/collapse button for product description
        buttonExpand.setOnClickListener { viewModel.toggleDescriptionExpanded() }

        //On first load, make sure we are showing the correct layout
        configurationUpdate(resources.configuration)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.isDescriptionExpanded.value?.let { (outState.putBoolean(KEY_EXPANDED, it)) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationUpdate(newConfig)
    }

    private fun configurationUpdate(configuration: Configuration) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            constraintMain.setState(
                R.id.constraintStateLandscape,
                configuration.screenWidthDp,
                configuration.screenHeightDp
            )
        else
            constraintMain.setState(
                R.id.constraintStatePortrait,
                configuration.screenWidthDp,
                configuration.screenHeightDp
            )
    }

    private fun updateControlVisibility(showControls: Boolean) {
        progressLoadingReviews.isVisible = !showControls
        buttonPurchase.isVisible = showControls
        buttonExpand.isVisible = showControls
    }
}
