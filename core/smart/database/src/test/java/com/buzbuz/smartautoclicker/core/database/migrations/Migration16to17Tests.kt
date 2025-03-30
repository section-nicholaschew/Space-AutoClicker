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
package com.buzbuz.smartautoclicker.core.database.migrations

import android.database.Cursor
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry

import com.buzbuz.smartautoclicker.core.database.ClickDatabase
import com.buzbuz.smartautoclicker.core.database.utils.getConditions

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for the [Migration16to17]. */
@RunWith(AndroidJUnit4::class)
class Migration16to17Tests {

    companion object {
        private const val TEST_DB = "migration-test"

        private const val CONDITION_ID = 42L
        private const val CONDITION_EVENT_ID = 24L
        private const val CONDITION_NAME = "Condition Name"
        private const val CONDITION_PATH = "/test/path"
        private const val CONDITION_AREA_LEFT = 0
        private const val CONDITION_AREA_TOP = 1
        private const val CONDITION_AREA_RIGHT = 2
        private const val CONDITION_AREA_BOTTOM = 3
        private const val CONDITION_BITMAP_POSITION = "0;0;10;10;0"
        private const val CONDITION_TYPE = "1" // ON_IMAGE_DETECTED
        private const val CONDITION_THRESHOLD = 10
        private const val CONDITION_DETECTION_TYPE = 1
        private const val CONDITION_SHOULD_BE_DETECTED = 1
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ClickDatabase::class.java,
    )

    @Test
    fun migrate_condition_ocrTextFields() {
        // Create the database with version 16
        val dbV16 = helper.createDatabase(TEST_DB, 16).apply {
            // Insert a condition
            execSQL(
                """
                INSERT INTO condition_table VALUES (
                    $CONDITION_ID, $CONDITION_EVENT_ID, '$CONDITION_NAME', $CONDITION_TYPE,
                    '$CONDITION_PATH',
                    $CONDITION_AREA_LEFT, $CONDITION_AREA_TOP, $CONDITION_AREA_RIGHT, $CONDITION_AREA_BOTTOM,
                    '$CONDITION_BITMAP_POSITION',
                    $CONDITION_THRESHOLD, $CONDITION_DETECTION_TYPE, $CONDITION_SHOULD_BE_DETECTED,
                    NULL, NULL, NULL, NULL,
                    NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0
                )
                """
            )
            close()
        }

        // Migrate to version 17
        val dbV17 = helper.runMigrationsAndValidate(TEST_DB, 17, true, Migration16to17)

        // Check OCR text columns were added and are NULL for existing records
        dbV17.query("SELECT text_to_find, exact_text_match, min_text_confidence FROM condition_table").apply {
            Assert.assertTrue(moveToFirst())
            Assert.assertTrue(isNull(getColumnIndex("text_to_find")))
            Assert.assertTrue(isNull(getColumnIndex("exact_text_match")))
            Assert.assertTrue(isNull(getColumnIndex("min_text_confidence")))
            close()
        }
    }

    @Test
    fun migrate_OCRCondition_insertion() {
        // Create the database with version 16
        val dbV16 = helper.createDatabase(TEST_DB, 16).apply {
            close()
        }

        // Migrate to version 17
        val dbV17 = helper.runMigrationsAndValidate(TEST_DB, 17, true, Migration16to17)

        // Insert an OCR condition to verify the new columns
        dbV17.execSQL(
            """
            INSERT INTO condition_table VALUES (
                $CONDITION_ID, $CONDITION_EVENT_ID, '$CONDITION_NAME', '5',
                NULL, 
                $CONDITION_AREA_LEFT, $CONDITION_AREA_TOP, $CONDITION_AREA_RIGHT, $CONDITION_AREA_BOTTOM,
                NULL,
                NULL, $CONDITION_DETECTION_TYPE, $CONDITION_SHOULD_BE_DETECTED,
                NULL, NULL, NULL, NULL,
                NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0,
                'Test OCR Text', 1, 75
            )
            """
        )

        // Verify the OCR condition data
        dbV17.query("SELECT text_to_find, exact_text_match, min_text_confidence FROM condition_table").apply {
            Assert.assertTrue(moveToFirst())
            Assert.assertEquals("Test OCR Text", getString(getColumnIndex("text_to_find")))
            Assert.assertEquals(1, getInt(getColumnIndex("exact_text_match")))
            Assert.assertEquals(75, getInt(getColumnIndex("min_text_confidence")))
            close()
        }
    }
}
