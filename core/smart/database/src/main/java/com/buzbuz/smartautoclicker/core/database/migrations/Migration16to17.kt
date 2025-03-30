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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.buzbuz.smartautoclicker.core.base.migrations.SQLiteColumn
import com.buzbuz.smartautoclicker.core.base.migrations.getSQLiteTableReference
import com.buzbuz.smartautoclicker.core.database.CONDITION_TABLE

/**
 * Migration from database v16 to v17.
 * 
 * Changes:
 * - Add new columns to condition_table for text detection conditions
 */
object Migration16to17 : Migration(16, 17) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new columns for text detection in condition_table
        db.getSQLiteTableReference(CONDITION_TABLE)
            .alterTableAddColumn(SQLiteColumn.Text("text_to_detect", isNotNull = false))
        db.getSQLiteTableReference(CONDITION_TABLE)
            .alterTableAddColumn(SQLiteColumn.Boolean("text_case_sensitive", isNotNull = false))
        db.getSQLiteTableReference(CONDITION_TABLE)
            .alterTableAddColumn(SQLiteColumn.Boolean("text_match_whole_words", isNotNull = false))
        db.getSQLiteTableReference(CONDITION_TABLE)
            .alterTableAddColumn(SQLiteColumn.Int("text_confidence_threshold", isNotNull = false))
        db.getSQLiteTableReference(CONDITION_TABLE)
            .alterTableAddColumn(SQLiteColumn.Int("text_detection_area_left", isNotNull = false))
        db.getSQLiteTableReference(CONDITION_TABLE)
            .alterTableAddColumn(SQLiteColumn.Int("text_detection_area_top", isNotNull = false))
        db.getSQLiteTableReference(CONDITION_TABLE)
            .alterTableAddColumn(SQLiteColumn.Int("text_detection_area_right", isNotNull = false))
        db.getSQLiteTableReference(CONDITION_TABLE)
            .alterTableAddColumn(SQLiteColumn.Int("text_detection_area_bottom", isNotNull = false))
    }
}
