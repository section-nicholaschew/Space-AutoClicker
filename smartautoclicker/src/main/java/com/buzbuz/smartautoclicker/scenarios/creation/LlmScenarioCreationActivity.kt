/*
 * Copyright (C) 2025 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.scenarios.creation

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.buzbuz.smartautoclicker.R
import com.buzbuz.smartautoclicker.core.domain.model.scenario.Scenario
import com.buzbuz.smartautoclicker.core.domain.IRepository

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity for creating scenarios using LLM-based generation.
 * This activity allows users to describe what they want to automate and generates a scenario using AI.
 */
@AndroidEntryPoint
class LlmScenarioCreationActivity : AppCompatActivity() {

    companion object {
        /** Key for the scenario name extra. */
        const val EXTRA_SCENARIO_NAME = "extra_scenario_name"
    }

    @Inject lateinit var repository: IRepository

    private lateinit var promptInput: EditText
    private lateinit var generateButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llm_scenario_creation)

        // Get the scenario name from the intent
        val scenarioName = intent.getStringExtra(EXTRA_SCENARIO_NAME) ?: getString(R.string.default_scenario_name)
        
        // Initialize UI components
        promptInput = findViewById(R.id.prompt_input)
        generateButton = findViewById(R.id.generate_button)
        progressBar = findViewById(R.id.progress_bar)
        resultText = findViewById(R.id.result_text)

        // Set up the generate button
        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString()
            if (prompt.isNotEmpty()) {
                generateScenario(scenarioName, prompt)
            }
        }
    }

    private fun generateScenario(scenarioName: String, prompt: String) {
        // Show progress and disable button
        progressBar.visibility = View.VISIBLE
        generateButton.isEnabled = false
        
        // TODO: Implement actual LLM integration
        // For now, we'll just simulate the generation with a delay
        
        lifecycleScope.launch {
            // Simulate LLM processing
            kotlinx.coroutines.delay(2000)
            
            // Create a basic scenario
            val scenario = Scenario(
                id = com.buzbuz.smartautoclicker.core.base.identifier.Identifier(
                    databaseId = com.buzbuz.smartautoclicker.core.base.identifier.DATABASE_ID_INSERTION,
                    tempId = 0L
                ),
                name = "$scenarioName (AI Generated)",
                detectionQuality = resources.getInteger(R.integer.default_detection_quality),
                randomize = false,
            )
            
            // Add the scenario to the repository
            repository.addScenario(scenario)
            
            // Update UI
            progressBar.visibility = View.GONE
            resultText.visibility = View.VISIBLE
            resultText.text = getString(R.string.llm_scenario_created_message)
            
            // Finish after a short delay
            kotlinx.coroutines.delay(1500)
            finish()
        }
    }
}
