package com.example.gooverapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import gun0912.tedimagepicker.util.ToastUtil.context
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "Database"
        private const val DATABASE_VERSION = 6
    }
    data class MyData
        (val id: Long, val category: String, val title: String, val memo: String, val cycle: String,
         val date: String, val answer: String, val bookmark: Int)

    override fun onCreate(db: SQLiteDatabase) {
        // 테이블 생성
        db.execSQL("CREATE TABLE IF NOT EXISTS MyTable(ID INTEGER PRIMARY KEY, category TEXT, " +
                "title TEXT, memo TEXT, cycle TEXT, date TEXT, answer TEXT, bookmark INTEGER DEFAULT 0)")

        db.execSQL("CREATE TABLE IF NOT EXISTS SecondTable(ID INTEGER PRIMARY KEY, category TEXT, " +
                "title TEXT, memo TEXT, cycle TEXT, date TEXT, answer TEXT, bookmark INTEGER DEFAULT 0, " +
                "review INTEGER DEFAULT 0)")

        db.execSQL("CREATE TABLE IF NOT EXISTS CategoryTable (ID INTEGER PRIMARY KEY, category TEXT)")

        db.execSQL("CREATE TABLE IF NOT EXISTS CycleTable (ID INTEGER PRIMARY KEY, cycle TEXT)")

        db.execSQL("CREATE TABLE IF NOT EXISTS ImageTable (ID INTEGER PRIMARY KEY, entry_id INTEGER, " +
                "image TEXT, FOREIGN KEY(entry_id) REFERENCES MyTable(ID))")

        db.execSQL("CREATE TABLE IF NOT EXISTS ImageTable2 (ID INTEGER PRIMARY KEY, second_id INTEGER, " +
                "image TEXT, FOREIGN KEY(second_id) REFERENCES SecondTable(ID))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 테이블을 삭제했다가 다시 만듬
        db.execSQL("DROP TABLE IF EXISTS MyTable")
        db.execSQL("DROP TABLE IF EXISTS SecondTable")
        db.execSQL("DROP TABLE IF EXISTS CategoryTable")
        db.execSQL("DROP TABLE IF EXISTS CycleTable")
        db.execSQL("DROP TABLE IF EXISTS ImageTable")
        db.execSQL("DROP TABLE IF EXISTS ImageTable2")
        // 테이블 다시 만들기
        onCreate(db)
    }

    fun getCycleCount(cycleString: String): List<Int> {
        return cycleString.split(" ").map { it.toInt() }
    }

    fun getDataForDate(selectedDate: String): MutableList<MyData> {
        val dataList = mutableListOf<MyData>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM SecondTable WHERE date = ?", arrayOf(selectedDate))

        if (cursor != null && cursor.moveToFirst()) {
            Log.d("this", "LOLOLOLOLO")
            do {
                val nid = cursor.getLong(cursor.getColumnIndex("ID"))
                val ncategory = cursor.getString(cursor.getColumnIndex("category"))
                val ntitle = cursor.getString(cursor.getColumnIndex("title"))
                val nmemo = cursor.getString(cursor.getColumnIndex("memo"))
                val ncycle = cursor.getString(cursor.getColumnIndex("cycle"))
                val ndate = cursor.getString(cursor.getColumnIndex("date"))
                val nanswer = cursor.getString(cursor.getColumnIndex("answer"))
                val nbookmark = cursor.getInt(cursor.getColumnIndex("bookmark"))
                Log.d("this", "bbbbbookmark: $nbookmark")
                if (nbookmark != -1) {
                    Log.d("this", "Bookmark Index:$ndate")
                    Log.d("this", "bbbbbookmark: $nbookmark")
                    Log.d("this", "id $nid, cate $ncategory, title $ntitle, memeo $nmemo, cycle $ncycle, date $ndate, answer $nanswer")
                    val myData = MyData(nid, ncategory, ntitle, nmemo, ncycle, ndate, nanswer, nbookmark)

                    dataList.add(myData)
                }

            }while (cursor.moveToNext())
        } else {
            cursor?.close()
            db.close()
        }
        return dataList
    }

    // 문제 테이블에서 날짜 아래 점 찍기, distinct로 문제 여러개여도 날짜 하나에 점 하나, 달력에 사용
    fun getAllDistinctDates(): List<String> {
        val dateList = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT date FROM SecondTable", null)
        if (cursor.moveToFirst()) {
            do {
                // Log the column count for debugging
                val columnCount = cursor.columnCount


                // Iterate through columns and log column names and values
                for (i in 0 until columnCount) {
                    val columnName = cursor.getColumnName(i)
                    val columnValue = cursor.getString(i)
                    Log.d("DatabaseHelper", "Column: $columnName, Value: $columnValue")
                }

                val date = cursor.getString(cursor.getColumnIndex("date"))
                Log.d("DatabaseHelper", "Read date: $date")

                dateList.add(date)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return dateList
    }

    fun getReviewValue(category: String?, title: String?, memo: String?, cycle: String?, date: String?, answer: String?): Int {
        val columns = arrayOf("review")
        val selection = "category = ? AND title = ? AND memo = ? AND cycle = ? AND date = ? AND answer = ?"
        val selectionArgs = arrayOf(category, title, memo, cycle, date, answer)

        val cursor = readableDatabase.query("SecondTable", columns, selection, selectionArgs, null, null, null)

        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndex("review"))
        } else {
            0
        }
    }

    fun updateReviewValue(category: String?, title: String?, memo: String?, cycle: String?, date: String?, answer: String?) {
        val values = ContentValues().apply {
            put("review", 1)
        }

        val whereClause = "category = ? AND title = ? AND memo = ? AND cycle = ? AND date = ? AND answer = ?"
        val whereArgs = arrayOf(category, title, memo, cycle, date, answer)

        this.writableDatabase.update("SecondTable", values, whereClause, whereArgs)
    }

    fun insertMyTable(category: String, title: String, memo: String, cycle: String, date: String, answer: String, bookmark: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("category", category)
        values.put("title", title)
        values.put("memo", memo)
        values.put("cycle", cycle)
        values.put("date", date)
        values.put("answer", answer)
        values.put("bookmark", bookmark)

        val result = db.insert("MyTable", null, values)

        db.close()
        return result
    }

    fun insertSecondTable(category: String, title: String, memo: String, cycle: String, date: String, answer: String, bookmark: Int): List<Long> {
        val db = this.writableDatabase
        val secondvalues = ContentValues()
        val cycleCounts = this.getCycleCount(cycle)
        // 날짜 계산을 위해 원래의 날짜를 파싱합니다.
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        val originalDate = LocalDate.parse(date, dateFormatter)

        val secondIds = mutableListOf<Long>()

        cycleCounts.forEachIndexed { index, cycleCount ->
            // 현재 항목의 값을 사용하여 조정된 날짜를 계산
            val adjustedDate = originalDate.plusDays(cycleCount.toLong())

            // 조정된 데이터를 SecondTable에 추가합니다.
            secondvalues.apply {
                put("category", category)
                put("title", title)
                put("memo", memo)
                put("date", adjustedDate.format(dateFormatter))
                put("answer", answer)
                put("bookmark", bookmark)
                put("cycle", cycle)
            }
            Log.d("this", "카피북 $bookmark")

            // SecondTable에 데이터 추가
            val secondId = db.insert("SecondTable", null, secondvalues)
            secondIds.add(secondId)
        }

        db.close()
        return secondIds
    }

    // 이미지 리스트에 있는 데이터를 이미지 테이블에 추가
    fun insertImage(entryId: Long, imagePath: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("entry_id", entryId)
        values.put("image", imagePath)

        db.insert("ImageTable", null, values)

        db.close()
    }

    fun insertImage2(secondIds: List<Long>, imagePath: String) {
        val db = this.writableDatabase

        secondIds.forEach { secondId ->
            val values = ContentValues()
            values.put("second_id", secondId)
            values.put("image", imagePath)

            db.insert("ImageTable2", null, values)
        }

        db.close()
    }

    // 문제에 대한 이미지를 ImageTable에서 찾기 위한 entry_id 찾기
    fun getEntryId(category: String?, title: String?, memo: String?, cycle: String?, date: String?, answer: String?): Long {
        val db = this.readableDatabase
        val selection = "category=? AND title=? AND memo=? AND cycle=? AND date=? AND answer=?"
        val selectionArgs = arrayOf(category, title, memo, cycle, date, answer)
        var entryId: Long = -1

        try {
            val cursor = db.query("MyTable", arrayOf("ID"), selection, selectionArgs, null, null, null)
            if (cursor.moveToFirst()) {
                entryId = cursor.getLong(cursor.getColumnIndex("ID"))
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("Database", "Error getting entry ID for category: $category, title: $title, etc.", e)
        } finally {
            db.close()
        }
        return entryId
    }

    fun getEntryId2(category: String?, title: String?, memo: String?, cycle: String?, date: String?, answer: String?): Long {
        val db = this.readableDatabase
        val selection = "category=? AND title=? AND memo=? AND cycle=? AND date=? AND answer=?"
        val selectionArgs = arrayOf(category, title, memo, cycle, date, answer)
        var entryId: Long = -1

        try {
            val cursor = db.query("SecondTable", arrayOf("ID"), selection, selectionArgs, null, null, null)
            if (cursor.moveToFirst()) {
                entryId = cursor.getLong(cursor.getColumnIndex("ID"))
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("Database", "Error getting entry ID for category: $category, title: $title, etc.", e)
        } finally {
            db.close()
        }
        return entryId
    }

    // entry_id 조건에 맞는 이미지를 ImageTable에서 가져오기, 이미지 리스트 중 첫번째 아이템 하나만
    fun getImagePaths(entryId: Long): List<String> {
        val db = this.readableDatabase
        val imagePaths = mutableListOf<String>()

        try {
            val cursor = db.query("ImageTable", arrayOf("image"), "entry_id = ?", arrayOf(entryId.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val imagePath = cursor.getString(cursor.getColumnIndex("image"))
                    imagePaths.add(imagePath)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("Database", "Error getting image paths for entryId: $entryId", e)
        } finally {
            db.close()
        }
        return imagePaths
    }

    fun getImagePaths2(secondId: Long): List<String> {
        val db = this.readableDatabase
        val imagePaths = mutableListOf<String>()

        try {
            val cursor = db.query("ImageTable2", arrayOf("image"), "second_id = ?", arrayOf(secondId.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val imagePath = cursor.getString(cursor.getColumnIndex("image"))
                    imagePaths.add(imagePath)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("Database", "Error getting image paths for entryId: $secondId", e)
        } finally {
            db.close()
        }
        return imagePaths
    }

    // 문제 테이블의 모든 열 가져와서 카테고리 조건에 따라 필터링
    fun getAllWrongByCategory(category: String): List<MyData> {
        val dataList = mutableListOf<MyData>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM MyTable WHERE category = ?", arrayOf(category))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("ID"))
                val title = cursor.getString(cursor.getColumnIndex("title"))
                val memo = cursor.getString(cursor.getColumnIndex("memo"))
                val cycle = cursor.getString(cursor.getColumnIndex("cycle"))
                val date = cursor.getString(cursor.getColumnIndex("date"))
                val answer = cursor.getString(cursor.getColumnIndex("answer"))
                val bookmark = cursor.getInt(cursor.getColumnIndex("bookmark"))
                val data = MyData(id.toLong(), category, title, memo, cycle, date, answer, bookmark)
                dataList.add(data)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return dataList
    }

    //문제 테이블에서 문제 제거, 모델에 맞춰서 모든 열의 조건이 일치한 데이터를 제거 (bookmark는 True든 false든 삭제해야 하므로 제외)
    fun deleteWrong(data: RecyclerInViewModel) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // MyTable에서 데이터 삭제
            val deleteResult = db.delete(
                "MyTable",
                "category = ? AND title = ? AND memo = ? AND cycle = ? AND date = ? AND answer = ?",
                arrayOf(data.category, data.title, data.memo, data.cycle, data.date, data.answer)
            )

            if (deleteResult > 0) {
                Log.d("Database", "Deleting data from MyTable: $data")

                val cycleCounts = this.getCycleCount(data.cycle)
                // 날짜 계산을 위해 원래의 날짜를 파싱합니다.
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
                val originalDate = LocalDate.parse(data.date, dateFormatter)

                cycleCounts.forEachIndexed { index, cycleCount ->
                    // 현재 항목의 값을 사용하여 조정된 날짜를 계산
                    val adjustedDate = originalDate.plusDays(cycleCount.toLong())

                    // 조정된 데이터를 SecondTable에서 삭제합니다.
                    db.delete(
                        "SecondTable",
                        "category = ? AND title = ? AND memo = ? AND cycle = ? AND date = ? AND answer = ?",
                        arrayOf(data.category, data.title, data.memo, data.cycle, adjustedDate.format(dateFormatter), data.answer)
                    )
                }
                // 모든 데이터가 삭제되었으므로 테이블을 다시 생성
                onCreate(db)
                db.setTransactionSuccessful()
            } else {
                Log.e("Database", "Error deleting data from MyTable: No rows affected for $data")
            }
        } catch (e: Exception) {
            Log.e("Database", "Error deleting data from MyTable: $data", e)
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // 주기 테이블에 대한 함수
    fun getAllCycle(): List<String> {
        val cycleStrings = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM CycleTable", null)
        cursor.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex("cycle")
                if (columnIndex != -1) {
                    do {
                        val cycleValue = it.getString(columnIndex)
                        cycleStrings.add(cycleValue)
                    } while (it.moveToNext())
                }
            }
        }
        return cycleStrings
    }
    fun insertCycle(cycle: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("cycle", cycle)

        val result = db.insert("CycleTable", null, values)

        if (result == -1L) {
            Toast.makeText(context, "Failed to add cycle.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "주기 $values 추가", Toast.LENGTH_SHORT).show()
        }
        db.close()
    }
    fun deleteCycle(cyclearray: String) {
        val db = this.writableDatabase
        db.delete("CycleTable", "cycle = ?", arrayOf(cyclearray))
        db.close()
    }

    // 카테고리 테이블에 대한 함수
    fun getAllCategories(): List<String> {
        val categoryList = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM CategoryTable", null)
        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(cursor.getColumnIndex("category"))
                categoryList.add(category)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return categoryList
    }
    fun insertCategory(category: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("category", category)
        db.insert("CategoryTable", null, values)
        db.close()
    }
    fun deleteCategory(category: String) {
        val db = this.writableDatabase
        db.delete("CategoryTable", "category = ?", arrayOf(category))
        try {
            val result = db.delete(
                "MyTable",
                "category = ?",
                arrayOf(category)
            )
            db.delete("SecondTable", "category = ?", arrayOf(category))
            if (result > 0) {
                Log.d("Database", "Deleting data from MyTable for category: $category")
            } else {
                Log.e("Database", "Error deleting data from MyTable: No rows affected for category $category")
            }
        } catch (e: Exception) {
            Log.e("Database", "Error deleting data from MyTable for category $category", e)
        } finally {
            db.close()
        }
    }

    // Home화면 틀린 문제 즐겨찾기
    fun getAllBookmarked(): List<MyData> {
        val dataList = mutableListOf<MyData>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM MyTable WHERE bookmark = 1", arrayOf())
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("ID"))
                val category = cursor.getString(cursor.getColumnIndex("category"))
                val title = cursor.getString(cursor.getColumnIndex("title"))
                val memo = cursor.getString(cursor.getColumnIndex("memo"))
                val cycle = cursor.getString(cursor.getColumnIndex("cycle"))
                val date = cursor.getString(cursor.getColumnIndex("date"))
                val answer = cursor.getString(cursor.getColumnIndex("answer"))
                val bookmark = cursor.getInt(cursor.getColumnIndex("bookmark"))
                val data = MyData(id.toLong(), category, title, memo, cycle, date, answer, bookmark)
                dataList.add(data)
            } while (cursor.moveToNext())
        }
        Log.d("DatabaseHelper", "Bookmarked Data List Size: ${dataList.size}")
        cursor.close()
        db.close()
        return dataList
    }

    // 오늘 날짜 TodoList
    fun getAllDataForDate(date: String): List<MyData> {
        val dataList = mutableListOf<MyData>()
        val db = this.readableDatabase
        val currentDate = getCurrentDate()

        val cursor = db.rawQuery("SELECT * FROM SecondTable WHERE date = ?", arrayOf(currentDate))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("ID"))
                val category = cursor.getString(cursor.getColumnIndex("category"))
                val title = cursor.getString(cursor.getColumnIndex("title"))
                val memo = cursor.getString(cursor.getColumnIndex("memo"))
                val cycle = cursor.getString(cursor.getColumnIndex("cycle"))
                val answer = cursor.getString(cursor.getColumnIndex("answer"))
                val bookmark = cursor.getInt(cursor.getColumnIndex("bookmark"))
                val data = MyData(id.toLong(), category, title, memo, cycle, currentDate, answer, bookmark)
                dataList.add(data)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return dataList
    }
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}