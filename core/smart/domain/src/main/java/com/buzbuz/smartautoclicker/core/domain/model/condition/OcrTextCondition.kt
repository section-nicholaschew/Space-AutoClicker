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
package com.buzbuz.smartautoclicker.core.domain.model.condition

import android.graphics.Rect

import com.buzbuz.smartautoclicker.core.domain.model.DetectionType
import com.buzbuz.smartautoclicker.core.domain.model.IN_AREA
import com.buzbuz.smartautoclicker.core.base.identifier.Identifier
import com.buzbuz.smartautoclicker.core.base.interfaces.Prioritizable

/**
 * OCR text detection condition for an Event.
 *
 * @param id the unique identifier for the condition.
 * @param eventId the identifier of the event for this condition.
 * @param name the name of the condition.
 * @param priority the priority for this condition when multiple conditions are checked.
 * @param textToFind the text that should be detected on screen.
 * @param exactTextMatch if true, the detected text must match exactly. If false, it does a contains check.
 * @param minTextConfidence the minimum confidence level (0-100%) for text detection to be considered valid.
 * @param area the area of the screen to detect text in.
 * @param detectionType the type of detection for this condition. Must be one of [DetectionType].
 * @param shouldBeDetected true if this text should be detected to validate the condition, false if it should not be found.
 * @param detectionArea the area to detect the condition in if [detectionType] is IN_AREA.
 */
data class OcrTextCondition(
    override val id: Identifier,
    override val eventId: Identifier,
    override val name: String,
    override var priority: Int,
    val textToFind: String,
    val exactTextMatch: Boolean,
    val minTextConfidence: Int,
    val area: Rect,
    @DetectionType val detectionType: Int,
    val shouldBeDetected: Boolean,
    val detectionArea: Rect? = null,
) : Condition(), Prioritizable {

    /**
     * Creates a deep copy of this condition.
     * @return a new instance of OcrTextCondition with the same values but new Rect instances.
     */
    fun deepCopy(): OcrTextCondition = OcrTextCondition(
        id = id,
        eventId = eventId,
        name = name,
        priority = priority,
        textToFind = textToFind,
        exactTextMatch = exactTextMatch,
        minTextConfidence = minTextConfidence,
        area = Rect(area),
        detectionType = detectionType,
        shouldBeDetected = shouldBeDetected,
        detectionArea = detectionArea?.let { Rect(it) }
    )

    /** Tells if this condition is complete and valid to be saved. */
    override fun isComplete(): Boolean =
        super.isComplete() && textToFind.isNotEmpty() &&
                (detectionType == IN_AREA && detectionArea != null || detectionType != IN_AREA)
                
    override fun hashCodeNoIds(): Int =
        name.hashCode() + textToFind.hashCode() + exactTextMatch.hashCode() + minTextConfidence.hashCode() +
                area.hashCode() + detectionType.hashCode() + shouldBeDetected.hashCode() + 
                (detectionArea?.hashCode() ?: 0) + priority.hashCode()
}
