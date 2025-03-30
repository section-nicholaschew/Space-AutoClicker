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
package com.buzbuz.smartautoclicker.core.database.utils

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** Create an OCR text condition in the database for tests. */
fun createOcrTextCondition(
    db: SupportSQLiteDatabase,
    id: Long,
    eventId: Long,
    name: String,
    textToFind: String,
    exactTextMatch: Boolean,
    minTextConfidence: Int
) {
    db.execSQL(
        """
        INSERT INTO condition_table VALUES (
            $id, $eventId, '$name', '5',
            NULL, 
            0, 0, 100, 100,
            NULL,
            NULL, 1, 1,
            NULL, NULL, NULL, NULL,
            NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0,
            '$textToFind', ${if (exactTextMatch) 1 else 0}, $minTextConfidence
        )
        """
    )
}

/** Get all OCR text conditions from the database. */
fun SupportSQLiteDatabase.getOcrTextConditions(): List<OcrTextConditionInfo> {
    val conditions = mutableListOf<OcrTextConditionInfo>()
    
    query(
        """
        SELECT id, eventId, name, text_to_find, exact_text_match, min_text_confidence
        FROM condition_table 
        WHERE type = '5'
        """
    ).use { cursor ->
        while (cursor.moveToNext()) {
            conditions.add(
                OcrTextConditionInfo(
                    id = cursor.getLong(cursor.getColumnIndex("id")),
                    eventId = cursor.getLong(cursor.getColumnIndex("eventId")),
                    name = cursor.getString(cursor.getColumnIndex("name")),
                    textToFind = cursor.getString(cursor.getColumnIndex("text_to_find")),
                    exactTextMatch = cursor.getInt(cursor.getColumnIndex("exact_text_match")) == 1,
                    minTextConfidence = cursor.getInt(cursor.getColumnIndex("min_text_confidence"))
                )
            )
        }
    }
    
    return conditions
}

/** Insert a V16 condition into the database (before migration). */
internal fun SupportSQLiteDatabase.insertV16Condition(
    id: Long,
    eventId: Long,
    name: String,
    type: Int = 5, // 5 = ConditionType.ON_OCR_TEXT_DETECTED
    priority: Int = 0
) {
    execSQL("""
        INSERT INTO condition_table (id, eventId, name, type, priority) 
        VALUES ($id, $eventId, '$name', $type, $priority)
    """)
}

/** Assert that a cursor row contains the expected OCR text condition. */
internal fun Cursor.assertRowIsOcrTextCondition(expectedCondition: OcrTextConditionInfo) {
    assertColumnEquals(expectedCondition.id, "id")
    assertColumnEquals(expectedCondition.eventId, "eventId")
    assertColumnEquals(expectedCondition.name, "name")
    assertColumnEquals(expectedCondition.textToFind, "text_to_find")
    assertColumnEquals(if (expectedCondition.exactTextMatch) 1 else 0, "exact_text_match")
    assertColumnEquals(expectedCondition.minTextConfidence, "min_text_confidence")
}

/** Information about a OCR text condition in the database. */
data class OcrTextConditionInfo(
    val id: Long,
    val eventId: Long,
    val name: String,
    val textToFind: String,
    val exactTextMatch: Boolean,
    val minTextConfidence: Int
)
