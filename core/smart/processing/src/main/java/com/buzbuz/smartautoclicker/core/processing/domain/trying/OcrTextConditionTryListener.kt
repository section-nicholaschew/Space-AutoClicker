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
package com.buzbuz.smartautoclicker.core.processing.domain.trying

import android.graphics.Rect
import com.buzbuz.smartautoclicker.core.domain.model.condition.OcrTextCondition

/**
 * Listener for ocr text condition detection attempts.
 */
interface OcrTextConditionTryListener {

    /**
     * Called when the try on the condition detection starts.
     *
     * @param textToFind the text to find on screen
     * @param exactMatch true if the detected text must match exactly, false for partial matching
     * @param area the area of the screen to look for the text
     */
    fun onTrialStarted(textToFind: String, exactMatch: Boolean, area: Rect?)

    /**
     * Called when the try is a success.
     *
     * @param text the detected text
     * @param confidence the confidence level of the detection in percent (0-100)
     * @param boundingBox the area where the text was found
     */
    fun onTrialSuccess(text: String, confidence: Int, boundingBox: Rect)

    /**
     * Called when the try is a failure.
     */
    fun onTrialFailure()

    /**
     * Reset the listener to its initial state.
     */
    fun clear()
}

/**
 * Information about an ocr text condition detection trial.
 *
 * @param condition the condition to try
 * @param minConfidence the minimum confidence level required for the OCR text detection
 * @param listener the object notified about the trial events
 */
data class OcrTextConditionTrial(
    val condition: OcrTextCondition,
    val minConfidence: Int = 70,
    val listener: OcrTextConditionTryListener,
)
