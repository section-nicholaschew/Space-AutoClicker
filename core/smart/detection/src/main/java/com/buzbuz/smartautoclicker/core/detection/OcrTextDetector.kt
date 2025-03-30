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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log

import androidx.annotation.VisibleForTesting
import androidx.core.graphics.toRect

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Handles OCR text detection using ML Kit's TextRecognition.
 *
 * This class provides methods for detecting text within a bitmap or specific area of a bitmap and
 * comparing it against expected text patterns.
 */
@Singleton
class OcrTextDetector @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        /** Tag for logs */
        private const val TAG = "OcrTextDetector"
    }

    /** The ML Kit text recognizer used for OCR detection */
    private val textRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Detects text in the provided bitmap.
     *
     * @param bitmap the bitmap to analyze for text
     * @param roi optional region of interest to limit text detection area
     * @return the detected text and confidence
     */
    suspend fun detectText(
        bitmap: Bitmap,
        roi: Rect? = null
    ): OcrResult = suspendCancellableCoroutine { continuation ->
        try {
            val processedBitmap = if (roi != null) {
                // Crop the bitmap to the ROI if specified
                Bitmap.createBitmap(
                    bitmap, 
                    roi.left, 
                    roi.top, 
                    roi.width(), 
                    roi.height()
                )
            } else {
                bitmap
            }
            
            val image = InputImage.fromBitmap(processedBitmap, 0)
            
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val result = processVisionTextResults(visionText, roi)
                    continuation.resume(result)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Text recognition failed: ${e.message}")
                    continuation.resume(OcrResult(false, "", 0f, Rect()))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in text detection: ${e.message}")
            continuation.resume(OcrResult(false, "", 0f, Rect()))
        }
            
        continuation.invokeOnCancellation {
            // Nothing to cancel in ML Kit
        }
    }

    /**
     * Searches for specific text in the provided bitmap.
     *
     * @param bitmap the bitmap to analyze for text
     * @param textToFind the text to search for
     * @param roi optional region of interest to limit text detection area
     * @param exactMatch if true, requires exact text match; if false, searches for textToFind within detected text
     * @param minConfidence minimum confidence level (0-1) for text to be considered valid
     * @return an OcrResult with the detection results
     */
    suspend fun findText(
        bitmap: Bitmap,
        textToFind: String,
        roi: Rect? = null,
        exactMatch: Boolean = false,
        minConfidence: Float = 0f
    ): OcrResult {
        val ocrResult = detectText(bitmap, roi)
        
        // If no text was detected or confidence is too low, return negative result
        if (!ocrResult.recognized || ocrResult.confidence < minConfidence) {
            return OcrResult(false, "", ocrResult.confidence, Rect())
        }
        
        // Check if detected text matches search criteria
        val isMatch = if (exactMatch) {
            ocrResult.text.equals(textToFind, ignoreCase = true)
        } else {
            ocrResult.text.contains(textToFind, ignoreCase = true)
        }
        
        return if (isMatch) {
            ocrResult
        } else {
            OcrResult(false, ocrResult.text, ocrResult.confidence, ocrResult.boundingBox)
        }
    }

    /**
     * Processes the vision text results into our OcrResult format.
     */
    private fun processVisionTextResults(
        visionText: Text,
        roi: Rect?
    ): OcrResult {
        // If no text was detected, return empty result
        if (visionText.textBlocks.isEmpty()) {
            return OcrResult(false, "", 0f, Rect())
        }
        
        // Combine all text blocks into a single string
        val fullText = visionText.textBlocks.joinToString(" ") { it.text }
        
        // Calculate average confidence across all blocks
        val avgConfidence = visionText.textBlocks
            .flatMap { it.lines }
            .mapNotNull { it.confidence }
            .average()
            .let { if (it.isNaN()) 0f else it.toFloat() }
        
        // Calculate bounding box encompassing all text blocks
        val boundingBox = getBoundingBoxForText(visionText, roi)
        
        return OcrResult(true, fullText, avgConfidence, boundingBox)
    }
    
    /**
     * Calculates a bounding box that encompasses all detected text blocks.
     */
    private fun getBoundingBoxForText(visionText: Text, roi: Rect?): Rect {
        if (visionText.textBlocks.isEmpty()) {
            return Rect()
        }
        
        var left = Int.MAX_VALUE
        var top = Int.MAX_VALUE
        var right = 0
        var bottom = 0
        
        for (block in visionText.textBlocks) {
            block.boundingBox?.let { box ->
                left = minOf(left, box.left)
                top = minOf(top, box.top)
                right = maxOf(right, box.right)
                bottom = maxOf(bottom, box.bottom)
            }
        }
        
        // Adjust coordinates if ROI was specified
        if (roi != null) {
            left += roi.left
            top += roi.top
            right += roi.left
            bottom += roi.top
        }
        
        return if (left < right && top < bottom) {
            Rect(left, top, right, bottom)
        } else {
            Rect()
        }
    }
}
