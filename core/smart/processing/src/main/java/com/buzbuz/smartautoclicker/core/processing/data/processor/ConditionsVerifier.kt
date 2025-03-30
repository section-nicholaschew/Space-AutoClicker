/*
 * Copyright (C) 2024 Kevin Buzeau
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

import android.graphics.Bitmap
import android.graphics.Point

import com.buzbuz.smartautoclicker.core.detection.ImageDetector
import com.buzbuz.smartautoclicker.core.detection.OcrTextDetector
import com.buzbuz.smartautoclicker.core.domain.model.AND
import com.buzbuz.smartautoclicker.core.domain.model.ConditionOperator
import com.buzbuz.smartautoclicker.core.domain.model.CounterOperationValue
import com.buzbuz.smartautoclicker.core.domain.model.EXACT
import com.buzbuz.smartautoclicker.core.domain.model.IN_AREA
import com.buzbuz.smartautoclicker.core.domain.model.OR
import com.buzbuz.smartautoclicker.core.domain.model.WHOLE_SCREEN
import com.buzbuz.smartautoclicker.core.domain.model.condition.Condition
import com.buzbuz.smartautoclicker.core.domain.model.condition.ImageCondition
import com.buzbuz.smartautoclicker.core.domain.model.condition.OcrTextCondition
import com.buzbuz.smartautoclicker.core.domain.model.condition.TriggerCondition
import com.buzbuz.smartautoclicker.core.processing.data.processor.state.ProcessingState
import com.buzbuz.smartautoclicker.core.processing.domain.ConditionResult
import com.buzbuz.smartautoclicker.core.processing.domain.ScenarioProcessingListener

import kotlinx.coroutines.yield

internal class ConditionsVerifier(
    private val state: ProcessingState,
    private val imageDetector: ImageDetector,
    private val ocrTextDetector: OcrTextDetector,
    private val bitmapSupplier: suspend (ImageCondition) -> Bitmap?,
    private val progressListener: ScenarioProcessingListener? = null,
) {

    private companion object {
        val POSITIVE_RESULT: DefaultResult = DefaultResult(true)
        val NEGATIVE_RESULT: DefaultResult = DefaultResult(false)
    }

    private val verificationResults: ConditionsResult = ConditionsResult()
    /**
     * Set only during a [verifyConditions], it contains the system time at verification start.
     * This allows to use the same reference time for all conditions during the same verification loop.
     */
    private var currentVerificationTsMs: Long? = null

    suspend fun verifyConditions(@ConditionOperator operator: Int, conditions: List<Condition>): ConditionsResult {
        verificationResults.reset()
        currentVerificationTsMs = System.currentTimeMillis()

        var verificationResult: ConditionResult

        for (condition in conditions) {
            verificationResult = verifyCondition(condition)
            verificationResults.addResult(condition.getValidId(), verificationResult)

            if (operator == OR && verificationResult.isFulfilled) {
                verificationResults.setFulfilledState(true)
                return verificationResults
            }
            if (operator == AND && !verificationResult.isFulfilled) {
                verificationResults.setFulfilledState(false)
                return verificationResults
            }

            yield()
        }

        verificationResults.setFulfilledState(operator == AND)
        return verificationResults
    }

    private suspend fun verifyCondition(condition: Condition): ConditionResult =
        when (condition) {
            is OcrTextCondition -> verifyOcrTextCondition(condition)
            is ImageCondition -> verifyImageCondition(condition)
            is TriggerCondition -> if (verifyTriggerCondition(condition)) POSITIVE_RESULT else NEGATIVE_RESULT
        }

    private fun verifyTriggerCondition(condition: TriggerCondition): Boolean =
        when (condition) {
            is TriggerCondition.OnBroadcastReceived -> verifyOnBroadcastReceived(condition)
            is TriggerCondition.OnCounterCountReached -> verifyOnCounterReached(condition)
            is TriggerCondition.OnTimerReached -> verifyOnTimerReached(condition)
        }

    private fun verifyOnBroadcastReceived(condition: TriggerCondition.OnBroadcastReceived): Boolean =
        state.isBroadcastReceived(condition)

    private fun verifyOnCounterReached(condition: TriggerCondition.OnCounterCountReached): Boolean =
        state.getCounterValue(condition.counterName)?.let { counterValue ->

            val operandValue = when (val operationValue = condition.counterValue) {
                is CounterOperationValue.Counter -> state.getCounterValue(operationValue.value) ?: 0
                is CounterOperationValue.Number -> operationValue.value
            }

            when (condition.comparisonOperation) {
                TriggerCondition.OnCounterCountReached.ComparisonOperation.GREATER ->
                    counterValue > operandValue

                TriggerCondition.OnCounterCountReached.ComparisonOperation.GREATER_OR_EQUALS ->
                    counterValue >= operandValue

                TriggerCondition.OnCounterCountReached.ComparisonOperation.EQUALS ->
                    counterValue == operandValue

                TriggerCondition.OnCounterCountReached.ComparisonOperation.LOWER_OR_EQUALS ->
                    counterValue <= operandValue

                TriggerCondition.OnCounterCountReached.ComparisonOperation.LOWER ->
                    counterValue < operandValue
            }
        } ?: false

    private fun verifyOnTimerReached(condition: TriggerCondition.OnTimerReached): Boolean {
        val currentTsMs = currentVerificationTsMs ?: return false
        val timerEndMs = state.getTimerEndMs(condition.getValidId()) ?: return false

        return if (currentTsMs > timerEndMs) {
            if (condition.restartWhenReached) state.setTimerStartToNow(condition)
            else state.setTimerToDisabled(condition.getValidId())
            true
        } else false
    }

    private suspend fun verifyImageCondition(condition: ImageCondition): ConditionResult {
        progressListener?.onImageConditionProcessingStarted(condition)

        val result = bitmapSupplier(condition)?.let { conditionBitmap ->
            val detectionResult = when (condition.detectionType) {
                EXACT ->
                    imageDetector.detectCondition(conditionBitmap, condition.area, condition.threshold)

                WHOLE_SCREEN ->
                    imageDetector.detectCondition(conditionBitmap, condition.threshold)

                IN_AREA ->
                    condition.detectionArea?.let { area ->
                        imageDetector.detectCondition(conditionBitmap, area, condition.threshold)
                    } ?: throw IllegalArgumentException("Invalid IN_AREA condition, no area defined")

                else -> throw IllegalArgumentException("Unexpected detection type")
            }

            ImageResult(
                isFulfilled = detectionResult.isDetected == condition.shouldBeDetected,
                haveBeenDetected = detectionResult.isDetected,
                condition = condition,
                position = Point(detectionResult.position.x, detectionResult.position.y),
                confidenceRate = detectionResult.confidenceRate,
            )
        } ?: NEGATIVE_RESULT

        progressListener?.onImageConditionProcessingCompleted(result)
        return result
    }
    
    private suspend fun verifyOcrTextCondition(condition: OcrTextCondition): ConditionResult {
        progressListener?.onOcrTextConditionProcessingStarted(condition)
        
        // Get current screen size and content from the image detector
        val screenSize = imageDetector.getScreenSize()
        val screenContent = imageDetector.getScreenContent()
        
        if (screenContent == null) {
            progressListener?.onConditionVerificationCompleted(condition, false, "No screen content available")
            return NEGATIVE_RESULT
        }
        
        // We need to convert the byte buffer to a bitmap for the OCR detection
        val bitmap = android.graphics.Bitmap.createBitmap(
            screenSize.x, screenSize.y, android.graphics.Bitmap.Config.ARGB_8888
        )
        
        // Copy the buffer content to the bitmap
        screenContent.rewind()
        bitmap.copyPixelsFromBuffer(screenContent)
        
        // Determine the region of interest based on detection type
        val roi = when (condition.detectionType) {
            EXACT -> condition.area
            WHOLE_SCREEN -> null
            IN_AREA -> condition.detectionArea
            else -> throw IllegalArgumentException("Unexpected detection type")
        }
        
        // Perform OCR text detection using our dedicated detector
        val ocrResult = ocrTextDetector.findText(
            bitmap = bitmap,
            textToFind = condition.textToFind,
            roi = roi,
            exactMatch = condition.exactTextMatch,
            minConfidence = condition.minTextConfidence / 100f
        )
        
        // Create our condition result
        val result = OcrTextResult(
            isFulfilled = ocrResult.recognized == condition.shouldBeDetected,
            haveBeenDetected = ocrResult.recognized,
            condition = condition,
            detectedText = ocrResult.text,
            confidenceRate = (ocrResult.confidence * 100).toInt(),
            boundingBox = ocrResult.boundingBox
        )
        
        // Clean up the bitmap
        bitmap.recycle()
        
        // Report results
        progressListener?.onOcrTextConditionProcessingCompleted(result)
        return result
    }
}