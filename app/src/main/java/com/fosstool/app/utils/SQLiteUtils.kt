package com.fosstool.app.utils

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

@Suppress("unused")
object SQLiteUtils {

    const val readOnly = SQLiteDatabase.OPEN_READONLY
    const val readWrite = SQLiteDatabase.OPEN_READWRITE

    fun openDataBase(dbPath: String, mode: Int): SQLiteDatabase? {
        return SQLiteDatabase.openDatabase(dbPath, null, mode)
    }


    fun openOrCreateDataBase(dbPath: String): SQLiteDatabase? {
        return SQLiteDatabase.openOrCreateDatabase(dbPath, null)
    }

    fun SQLiteDatabase?.getTableData(table: String): Cursor? {
        return this?.query(table, null, null, null, null, null, null)
    }

}
