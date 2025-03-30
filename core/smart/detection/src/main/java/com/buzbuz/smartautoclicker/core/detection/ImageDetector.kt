/*
 * Copyright (C) 2023 Kevin Buzeau
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

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * Detects bitmaps within other bitmaps for conditions detection on the screen.
 * Also provides OCR (Optical Character Recognition) capabilities for text detection.
 * All calls should be made on the same thread.
 */
interface ImageDetector : AutoCloseable {

    /** Initialize the detector. Must be called on the same thread as the detection. */
    fun init()

    /**
     * Set the current metrics of the screen.
     * This MUST be called before the first detection, with the first bitmap provided by the screen. All orientation
     * changes must also trigger a call to this method.
     * All following calls to [detectCondition] methods will be verified against the metrics of this bitmap.
     *
     * @param screenBitmap the content of the screen as a bitmap.
     * @param detectionQuality the quality of the detection. The higher the preciser, the lower the faster. Must be
     *                         contained in [DETECTION_QUALITY_MIN] and [DETECTION_QUALITY_MAX].
     */
    fun setScreenMetrics(metricsKey: String, screenBitmap: Bitmap, detectionQuality: Double)

    /**
     * Set the bitmap for the screen.
     * All following calls to [detectCondition] methods will be verified against this bitmap.
     *
     * @param screenBitmap the content of the screen as a bitmap.
     */
    fun setupDetection(screenBitmap: Bitmap)

    /**
     * Detect if the bitmap is in the whole current screen bitmap.
     * [setupDetection] must have been called first with the content of the screen.
     *
     * @param conditionBitmap the condition to detect in the screen.
     * @param threshold the allowed error threshold allowed for the condition.
     *
     * @return the results of the detection.
     */
    fun detectCondition(conditionBitmap: Bitmap, threshold: Int): DetectionResult

    /**
     * Detect if the bitmap is at a specific position in the current screen bitmap.
     * [setupDetection] must have been called first with the content of the screen.
     *
     * @param conditionBitmap the condition to detect in the screen.
     * @param position the position on the screen where the condition should be detected.
     * @param threshold the allowed error threshold allowed for the condition.
     *
     * @return the results of the detection.
     */
    fun detectCondition(conditionBitmap: Bitmap, position: Rect, threshold: Int): DetectionResult
    
    /**
     * Initialize OCR capabilities with a specific language.
     * @param dataPath the path to the tessdata directory containing language data files.
     * @param language the language to use for OCR detection (e.g., "eng" for English).
     * @return true if OCR was initialized successfully, false otherwise.
     */
    fun initOcr(dataPath: String, language: String): Boolean
    
    /**
     * Detect text in the current screen bitmap.
     * [setupDetection] must have been called first with the content of the screen.
     *
     * @param roi optional region of interest to limit text detection area.
     * @param minConfidence minimum confidence level (0-100) for text to be considered valid.
     * @return the OCR detection result.
     */
    fun detectText(roi: Rect? = null, minConfidence: Float = 0f): OcrResult
    
    /**
     * Find specific text in the current screen bitmap.
     * [setupDetection] must have been called first with the content of the screen.
     *
     * @param textToFind the text to search for.
     * @param roi optional region of interest to limit text detection area.
     * @param exactMatch if true, requires exact text match; if false, searches for textToFind within detected text.
     * @param minConfidence minimum confidence level (0-100) for text to be considered valid.
     * @return the OCR detection result. isRecognized() will be true if the text was found.
     */
    fun findText(textToFind: String, roi: Rect? = null, exactMatch: Boolean = false, minConfidence: Float = 0f): OcrResult
}

/** The minimum detection quality for the algorithm. */
const val DETECTION_QUALITY_MIN = 400L