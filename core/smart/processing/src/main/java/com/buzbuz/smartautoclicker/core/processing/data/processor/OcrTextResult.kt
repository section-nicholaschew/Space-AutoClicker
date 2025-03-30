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
package com.buzbuz.smartautoclicker.core.processing.data.processor

import android.graphics.Rect
import com.buzbuz.smartautoclicker.core.domain.model.condition.OcrTextCondition
import com.buzbuz.smartautoclicker.core.processing.domain.ConditionResult

/**
 * Result of a OCR text detection condition verification.
 *
 * @param isFulfilled true if the condition is fulfilled according to the OCR text detection and shouldBeDetected.
 * @param haveBeenDetected true if the text has been detected, false if not.
 * @param condition the condition that have been verified.
 * @param detectedText the text that was detected on screen.
 * @param confidenceRate the confidence rate of the OCR text detection (0-100).
 * @param boundingBox the bounding box of the detected text on screen.
 */
class   OcrTextResult(
    override val isFulfilled: Boolean,
    val haveBeenDetected: Boolean,
    val condition: OcrTextCondition,
    val detectedText: String,
    val confidenceRate: Int,
    val boundingBox: Rect,
) : ConditionResult
