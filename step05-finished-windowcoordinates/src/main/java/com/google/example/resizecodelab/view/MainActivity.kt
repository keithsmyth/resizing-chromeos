/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.example.resizecodelab.view

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.SavedStateVMFactory
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.example.resizecodelab.R
import kotlinx.android.synthetic.main.activity_main_new.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        //Set up RecyclerView's on transition
        motionMain.setTransitionListener(createTransitionListener())

        // Normally you would receive this as an argument to this Activity.
        val dataId = 1

        //Retrieve the ViewModel with state data
        viewModel = ViewModelProviders.of(this, SavedStateVMFactory(this, Bundle().apply { putInt(KEY_ID, dataId) }))
            .get(MainViewModel::class.java)

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

        buttonPurchase.setOnClickListener { showPurchaseDialog() }

        //On first load, make sure we are showing the correct layout
        configurationUpdate(resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationUpdate(newConfig)
    }

    private fun createTransitionListener(): MotionLayout.TransitionListener {
        return object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                when (currentId) {
                    R.id.constraint_set_default -> {
                        Log.d("ResizeCodeLab", "onTransitionCompleted default")
                        recyclerReviews.layoutManager = LinearLayoutManager(this@MainActivity)
                        recyclerSuggested.layoutManager = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    }
                    R.id.constraint_set_default_land -> {
                        Log.d("ResizeCodeLab", "onTransitionCompleted default land")
                        recyclerReviews.layoutManager = GridLayoutManager(this@MainActivity, 2)
                        recyclerSuggested.layoutManager = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    }
                    R.id.constraint_set_large -> {
                        Log.d("ResizeCodeLab", "onTransitionCompleted large")
                        recyclerReviews.layoutManager = LinearLayoutManager(this@MainActivity)
                        recyclerSuggested.layoutManager = GridLayoutManager(this@MainActivity, 2)
                    }
                    R.id.constraint_set_large_land -> {
                        Log.d("ResizeCodeLab", "onTransitionCompleted large land")
                        recyclerReviews.layoutManager = GridLayoutManager(this@MainActivity, 2)
                        recyclerSuggested.layoutManager = GridLayoutManager(this@MainActivity, 3)
                    }
                }
            }
        }
    }

    private fun showPurchaseDialog() {
        val textPopupMessage = TextView(this)
        textPopupMessage.gravity = Gravity.CENTER
        textPopupMessage.text = getString(R.string.popup_purchase)
        TextViewCompat.setTextAppearance(textPopupMessage, R.style.TextAppearance_AppCompat_Title)

        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        val framePopup = FrameLayout(this)
        framePopup.layoutParams = layoutParams
        framePopup.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))

        framePopup.addView(textPopupMessage)

        //Get window size
        val displayMetrics = resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels
        val screenHeightPx = displayMetrics.heightPixels

        //Popup should be 50% of window size
        val popupWidthPx = screenWidthPx / 2
        val popupHeightPx = screenHeightPx / 2

        //Place it in the middle of the window
        val popupX = (screenWidthPx / 2) - (popupWidthPx / 2)
        val popupY = (screenHeightPx / 2) - (popupHeightPx / 2)

        //Show the window
        val popupWindow = PopupWindow(framePopup, popupWidthPx, popupHeightPx, true)
        popupWindow.elevation = 10f
        popupWindow.showAtLocation(scrollMain, Gravity.NO_GRAVITY, popupX, popupY)
    }

    private fun configurationUpdate(config: Configuration) {
        Log.d("ResizeCodeLab", config.screenWidthDp.toString())
        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
        val constraintSetResId = when {
            config.screenWidthDp >= 600 -> {
                if (isLandscape) R.id.constraint_set_large_land else R.id.constraint_set_large
            }
            else -> {
                if (isLandscape) R.id.constraint_set_default_land else R.id.constraint_set_default
            }
        }
        motionMain.transitionToState(constraintSetResId)
    }

    private fun updateControlVisibility(showControls: Boolean) {
        progressLoadingReviews.isVisible = !showControls
        buttonPurchase.isVisible = showControls
        buttonExpand.isVisible = showControls
    }
}
