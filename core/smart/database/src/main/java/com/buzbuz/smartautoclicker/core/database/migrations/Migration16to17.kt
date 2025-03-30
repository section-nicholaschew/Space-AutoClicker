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

/**
 * Migration from database v16 to v17.
 * Changes:
 * - Add OCR text detection columns to the condition table
 */
object Migration16to17 : Migration(16, 17) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.getSQLiteTableReference("condition_table")
            .alterTableAddColumn(SQLiteColumn.Text("text_to_find", isNotNull = false))
        db.getSQLiteTableReference("condition_table")
            .alterTableAddColumn(SQLiteColumn.Int("exact_text_match", isNotNull = false))
        db.getSQLiteTableReference("condition_table")
            .alterTableAddColumn(SQLiteColumn.Int("min_text_confidence", isNotNull = false))

    }
}
