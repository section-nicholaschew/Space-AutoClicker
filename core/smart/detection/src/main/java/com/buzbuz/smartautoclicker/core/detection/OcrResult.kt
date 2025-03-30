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
package com.buzbuz.smartautoclicker.core.detection

import android.graphics.Rect

/**
 * Result of an OCR text detection operation.
 *
 * @param recognized true if text was recognized, false otherwise.
 * @param text the detected text, or empty string if nothing was detected.
 * @param confidence the confidence level (0-100) of the detection.
 * @param boundingBox the bounding box of the detected text.
 */
data class OcrResult(
    val recognized: Boolean,
    val text: String,
    val confidence: Float,
    val boundingBox: Rect
) {
    /**
     * Check if text was recognized.
     * 
     * @return true if text was recognized, false otherwise.
     */
    fun isRecognized(): Boolean = recognized
    
    override fun toString(): String {
        return "OcrResult(recognized=$recognized, text='$text', confidence=$confidence, boundingBox=$boundingBox)"
    }
}
