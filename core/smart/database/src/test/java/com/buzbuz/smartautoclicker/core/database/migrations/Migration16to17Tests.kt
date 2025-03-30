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

import android.os.Build

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry

import com.buzbuz.smartautoclicker.core.database.ClickDatabase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Tests the [Migration16to17]. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33]) // Using a supported SDK version
class Migration16to17Tests {

    private companion object {
        private const val TEST_DB = "migration-test"

        private const val OLD_DB_VERSION = 16
        private const val NEW_DB_VERSION = 17

        private const val CONDITION_TABLE = "condition_table"
        
        private const val TEST_CONDITION_ID = 42L
        private const val TEST_EVENT_ID = 24L
        private const val TEST_CONDITION_NAME = "Test Condition"
        private const val TEST_PRIORITY = 1
        private const val TEST_TYPE = "ON_IMAGE_DETECTED"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ClickDatabase::class.java,
    )

    /**
     * Creates a v16 condition in the database.
     *
     * @param db the database to insert the condition into.
     */
    private fun createV16Condition(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO $CONDITION_TABLE (
                id, eventId, name, priority, type, path, area_left, area_top, area_right, area_bottom, 
                threshold, detection_type, shouldBeDetected
            ) VALUES (
                $TEST_CONDITION_ID, $TEST_EVENT_ID, '$TEST_CONDITION_NAME', $TEST_PRIORITY, '$TEST_TYPE', 
                'test_path', 10, 20, 30, 40, 80, 1, 1
            )
            """.trimIndent()
        )
    }

    /**
     * Gets the names of columns in a table.
     * 
     * @param db the database to query.
     * @param tableName the name of the table to get columns from.
     * @return a list of column names in the table.
     */
    private fun getTableColumns(db: SupportSQLiteDatabase, tableName: String): List<String> {
        val columns = mutableListOf<String>()
        db.query("PRAGMA table_info($tableName)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(nameIndex))
            }
        }
        return columns
    }

    /**
     * Test that all the OCR text detection columns are added to the condition table.
     */
    @Test
    fun migrate_condition_table_adds_ocr_columns() {
        // Create v16 database and insert a condition
        helper.createDatabase(TEST_DB, OLD_DB_VERSION).use { dbV16 ->
            createV16Condition(dbV16)
        }

        // Migrate to v17 and verify the new columns were added
        helper.runMigrationsAndValidate(TEST_DB, NEW_DB_VERSION, true, Migration16to17).use { dbV17 ->
            val columns = getTableColumns(dbV17, CONDITION_TABLE)
            
            // Verify OCR text detection columns exist
            assertTrue("text_to_detect column should exist", columns.contains("text_to_detect"))
            assertTrue("text_case_sensitive column should exist", columns.contains("text_case_sensitive"))
            assertTrue("text_match_whole_words column should exist", columns.contains("text_match_whole_words"))
            assertTrue("text_confidence_threshold column should exist", columns.contains("text_confidence_threshold"))
            assertTrue("text_detection_area_left column should exist", columns.contains("text_detection_area_left"))
            assertTrue("text_detection_area_top column should exist", columns.contains("text_detection_area_top"))
            assertTrue("text_detection_area_right column should exist", columns.contains("text_detection_area_right"))
            assertTrue("text_detection_area_bottom column should exist", columns.contains("text_detection_area_bottom"))
        }
    }

    /**
     * Test that existing data in the condition table is preserved after migration.
     */
    @Test
    fun migrate_condition_table_preserves_existing_data() {
        // Create v16 database and insert a condition
        helper.createDatabase(TEST_DB, OLD_DB_VERSION).use { dbV16 ->
            createV16Condition(dbV16)
        }

        // Migrate to v17 and verify the existing data was preserved
        helper.runMigrationsAndValidate(TEST_DB, NEW_DB_VERSION, true, Migration16to17).use { dbV17 ->
            val query = """
                SELECT id, eventId, name, priority, type, path
                FROM $CONDITION_TABLE
                WHERE id = $TEST_CONDITION_ID
            """.trimIndent()
            
            dbV17.query(query).use { cursor ->

                assertTrue("Cursor should have at least one row", cursor.moveToFirst())
                
                assertEquals(TEST_CONDITION_ID, cursor.getLong(cursor.getColumnIndex("id")))
                assertEquals(TEST_EVENT_ID, cursor.getLong(cursor.getColumnIndex("eventId")))
                assertEquals(TEST_CONDITION_NAME, cursor.getString(cursor.getColumnIndex("name")))
                assertEquals(TEST_PRIORITY, cursor.getInt(cursor.getColumnIndex("priority")))
                assertEquals(TEST_TYPE, cursor.getString(cursor.getColumnIndex("type")))
                assertEquals("test_path", cursor.getString(cursor.getColumnIndex("path")))
            }
        }
    }
}
