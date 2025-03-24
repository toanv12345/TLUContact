package com.example.tlucontact.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.tlucontact.model.Unit
import com.example.tlucontact.model.Staff
import com.example.tlucontact.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "TLUContact.db"

        // Table names
        private const val TABLE_UNITS = "units"
        private const val TABLE_STAFF = "staff"
        private const val TABLE_USERS = "users"

        // Common columns
        private const val COLUMN_ID = "id"

        // Units Table columns
        private const val COLUMN_UNIT_NAME = "name"
        private const val COLUMN_UNIT_PHONE = "phone"
        private const val COLUMN_UNIT_ADDRESS = "address"
        private const val COLUMN_UNIT_EMAIL = "email"

        // Staff Table columns
        private const val COLUMN_STAFF_NAME = "name"
        private const val COLUMN_STAFF_POSITION = "position"
        private const val COLUMN_STAFF_PHONE = "phone"
        private const val COLUMN_STAFF_EMAIL = "email"
        private const val COLUMN_STAFF_UNIT_ID = "unit_id"

        // Users Table columns
        private const val COLUMN_USER_USERNAME = "username"
        private const val COLUMN_USER_PASSWORD = "password"
        private const val COLUMN_USER_STAFF_ID = "staff_id"
        private const val COLUMN_USER_IS_ADMIN = "is_admin"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Units table
        val createUnitsTable = "CREATE TABLE $TABLE_UNITS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_UNIT_NAME TEXT, " +
                "$COLUMN_UNIT_PHONE TEXT, " +
                "$COLUMN_UNIT_ADDRESS TEXT, " +
                "$COLUMN_UNIT_EMAIL TEXT)"
        db.execSQL(createUnitsTable)

        // Create Staff table
        val createStaffTable = "CREATE TABLE $TABLE_STAFF (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_STAFF_NAME TEXT, " +
                "$COLUMN_STAFF_POSITION TEXT, " +
                "$COLUMN_STAFF_PHONE TEXT, " +
                "$COLUMN_STAFF_EMAIL TEXT, " +
                "$COLUMN_STAFF_UNIT_ID INTEGER, " +
                "FOREIGN KEY($COLUMN_STAFF_UNIT_ID) REFERENCES $TABLE_UNITS($COLUMN_ID))"
        db.execSQL(createStaffTable)

        // Create Users table
        val createUsersTable = "CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_USERNAME TEXT UNIQUE, " +
                "$COLUMN_USER_PASSWORD TEXT, " +
                "$COLUMN_USER_STAFF_ID INTEGER, " +
                "$COLUMN_USER_IS_ADMIN INTEGER DEFAULT 0, " +
                "FOREIGN KEY($COLUMN_USER_STAFF_ID) REFERENCES $TABLE_STAFF($COLUMN_ID))"
        db.execSQL(createUsersTable)

        // Insert admin user
        insertDefaultAdmin(db)

        // Insert sample data
        insertSampleData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STAFF")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_UNITS")
        onCreate(db)
    }

    private fun insertDefaultAdmin(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COLUMN_USER_USERNAME, "admin")
            put(COLUMN_USER_PASSWORD, "admin123")
            put(COLUMN_USER_IS_ADMIN, 1)
        }
        db.insert(TABLE_USERS, null, values)
    }

    private fun insertSampleData(db: SQLiteDatabase) {
        // Insert sample units
        val unitIds = mutableListOf<Long>()

        val units = arrayOf(
            arrayOf("Khoa Công nghệ thông tin", "024 3852 4078", "Nhà C1, TLU", "cntt@tlu.edu.vn"),
            arrayOf("Khoa Kinh tế và Quản lý", "024 3563 3125", "Nhà B1, TLU", "ktql@tlu.edu.vn"),
            arrayOf("Phòng Đào tạo", "024 3852 2028", "Nhà A1, TLU", "daotao@tlu.edu.vn")
        )

        for (unit in units) {
            val values = ContentValues().apply {
                put(COLUMN_UNIT_NAME, unit[0])
                put(COLUMN_UNIT_PHONE, unit[1])
                put(COLUMN_UNIT_ADDRESS, unit[2])
                put(COLUMN_UNIT_EMAIL, unit[3])
            }
            val id = db.insert(TABLE_UNITS, null, values)
            unitIds.add(id)
        }

        // Insert sample staff
        val staffData = arrayOf(
            arrayOf("Nguyễn Văn A", "Trưởng khoa", "0987654321", "nguyenvana@tlu.edu.vn", unitIds[0]),
            arrayOf("Trần Thị B", "Giảng viên", "0912345678", "tranthib@tlu.edu.vn", unitIds[0]),
            arrayOf("Lê Văn C", "Trưởng khoa", "0976543210", "levanc@tlu.edu.vn", unitIds[1]),
            arrayOf("Phạm Thị D", "Chuyên viên", "0932145678", "phamthid@tlu.edu.vn", unitIds[2])
        )

        for (staff in staffData) {
            val values = ContentValues().apply {
                put(COLUMN_STAFF_NAME, staff[0].toString())
                put(COLUMN_STAFF_POSITION, staff[1].toString())
                put(COLUMN_STAFF_PHONE, staff[2].toString())
                put(COLUMN_STAFF_EMAIL, staff[3].toString())
                put(COLUMN_STAFF_UNIT_ID, staff[4].toString().toLong())
            }
            val staffId = db.insert(TABLE_STAFF, null, values)

            // Create user accounts for staff
            val userValues = ContentValues().apply {
                put(COLUMN_USER_USERNAME, staff[3].toString().split("@")[0])
                put(COLUMN_USER_PASSWORD, "password123")
                put(COLUMN_USER_STAFF_ID, staffId)
                put(COLUMN_USER_IS_ADMIN, 0)
            }
            db.insert(TABLE_USERS, null, userValues)
        }
    }

    fun checkUsernameExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USER_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Unit operations
    fun getAllUnits(): List<Unit> {
        val units = mutableListOf<Unit>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_UNITS ORDER BY $COLUMN_UNIT_NAME", null)

        with(cursor) {
            while (moveToNext()) {
                val unit = Unit(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_UNIT_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_UNIT_PHONE)),
                    getString(getColumnIndexOrThrow(COLUMN_UNIT_ADDRESS)),
                    getString(getColumnIndexOrThrow(COLUMN_UNIT_EMAIL))
                )
                units.add(unit)
            }
            close()
        }

        return units
    }

    fun getUnit(id: Int): Unit? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_UNITS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val unit = Unit(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT_ADDRESS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT_EMAIL))
            )
            cursor.close()
            unit
        } else {
            cursor.close()
            null
        }
    }

    fun addUnit(unit: Unit): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_UNIT_NAME, unit.name)
            put(COLUMN_UNIT_PHONE, unit.phone)
            put(COLUMN_UNIT_ADDRESS, unit.address)
            put(COLUMN_UNIT_EMAIL, unit.email)
        }

        return db.insert(TABLE_UNITS, null, values)
    }

    fun updateUnit(unit: Unit): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_UNIT_NAME, unit.name)
            put(COLUMN_UNIT_PHONE, unit.phone)
            put(COLUMN_UNIT_ADDRESS, unit.address)
            put(COLUMN_UNIT_EMAIL, unit.email)
        }

        return db.update(
            TABLE_UNITS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(unit.id.toString())
        )
    }

    fun deleteUnit(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(
            TABLE_UNITS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun searchUnits(query: String): List<Unit> {
        val units = mutableListOf<Unit>()
        val db = this.readableDatabase
        val searchQuery = "%$query%"

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_UNITS WHERE " +
                    "$COLUMN_UNIT_NAME LIKE ? OR " +
                    "$COLUMN_UNIT_PHONE LIKE ? OR " +
                    "$COLUMN_UNIT_EMAIL LIKE ? " +
                    "ORDER BY $COLUMN_UNIT_NAME",
            arrayOf(searchQuery, searchQuery, searchQuery)
        )

        with(cursor) {
            while (moveToNext()) {
                val unit = Unit(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_UNIT_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_UNIT_PHONE)),
                    getString(getColumnIndexOrThrow(COLUMN_UNIT_ADDRESS)),
                    getString(getColumnIndexOrThrow(COLUMN_UNIT_EMAIL))
                )
                units.add(unit)
            }
            close()
        }

        return units
    }

    // Staff operations
    fun getAllStaff(): List<Staff> {
        val staffList = mutableListOf<Staff>()
        val db = this.readableDatabase
        val query = """
            SELECT s.*, u.$COLUMN_UNIT_NAME as unit_name
            FROM $TABLE_STAFF s
            JOIN $TABLE_UNITS u ON s.$COLUMN_STAFF_UNIT_ID = u.$COLUMN_ID
            ORDER BY s.$COLUMN_STAFF_NAME
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        with(cursor) {
            while (moveToNext()) {
                val staff = Staff(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_POSITION)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_PHONE)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_EMAIL)),
                    getInt(getColumnIndexOrThrow(COLUMN_STAFF_UNIT_ID)),
                    getString(getColumnIndexOrThrow("unit_name"))
                )
                staffList.add(staff)
            }
            close()
        }

        return staffList
    }

    fun getStaff(id: Int): Staff? {
        val db = this.readableDatabase
        val query = """
            SELECT s.*, u.$COLUMN_UNIT_NAME as unit_name
            FROM $TABLE_STAFF s
            JOIN $TABLE_UNITS u ON s.$COLUMN_STAFF_UNIT_ID = u.$COLUMN_ID
            WHERE s.$COLUMN_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val staff = Staff(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STAFF_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STAFF_POSITION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STAFF_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STAFF_EMAIL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STAFF_UNIT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow("unit_name"))
            )
            cursor.close()
            staff
        } else {
            cursor.close()
            null
        }
    }

    fun getStaffByUnitId(unitId: Int): List<Staff> {
        val staffList = mutableListOf<Staff>()
        val db = this.readableDatabase
        val query = """
            SELECT s.*, u.$COLUMN_UNIT_NAME as unit_name
            FROM $TABLE_STAFF s
            JOIN $TABLE_UNITS u ON s.$COLUMN_STAFF_UNIT_ID = u.$COLUMN_ID
            WHERE s.$COLUMN_STAFF_UNIT_ID = ?
            ORDER BY s.$COLUMN_STAFF_NAME
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(unitId.toString()))

        with(cursor) {
            while (moveToNext()) {
                val staff = Staff(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_POSITION)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_PHONE)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_EMAIL)),
                    getInt(getColumnIndexOrThrow(COLUMN_STAFF_UNIT_ID)),
                    getString(getColumnIndexOrThrow("unit_name"))
                )
                staffList.add(staff)
            }
            close()
        }

        return staffList
    }

    fun addStaff(staff: Staff): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STAFF_NAME, staff.name)
            put(COLUMN_STAFF_POSITION, staff.position)
            put(COLUMN_STAFF_PHONE, staff.phone)
            put(COLUMN_STAFF_EMAIL, staff.email)
            put(COLUMN_STAFF_UNIT_ID, staff.unitId)
        }

        return db.insert(TABLE_STAFF, null, values)
    }

    fun updateStaff(staff: Staff): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STAFF_NAME, staff.name)
            put(COLUMN_STAFF_POSITION, staff.position)
            put(COLUMN_STAFF_PHONE, staff.phone)
            put(COLUMN_STAFF_EMAIL, staff.email)
            put(COLUMN_STAFF_UNIT_ID, staff.unitId)
        }

        return db.update(
            TABLE_STAFF,
            values,
            "$COLUMN_ID = ?",
            arrayOf(staff.id.toString())
        )
    }

    fun deleteStaff(id: Int): Int {
        val db = this.writableDatabase

        // First delete any user associated with this staff
        db.delete(
            TABLE_USERS,
            "$COLUMN_USER_STAFF_ID = ?",
            arrayOf(id.toString())
        )

        // Then delete the staff
        return db.delete(
            TABLE_STAFF,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun searchStaff(query: String): List<Staff> {
        val staffList = mutableListOf<Staff>()
        val db = this.readableDatabase
        val searchQuery = "%$query%"

        val queryStr = """
            SELECT s.*, u.$COLUMN_UNIT_NAME as unit_name
            FROM $TABLE_STAFF s
            JOIN $TABLE_UNITS u ON s.$COLUMN_STAFF_UNIT_ID = u.$COLUMN_ID
            WHERE s.$COLUMN_STAFF_NAME LIKE ? OR 
                  s.$COLUMN_STAFF_POSITION LIKE ? OR 
                  s.$COLUMN_STAFF_PHONE LIKE ? OR 
                  s.$COLUMN_STAFF_EMAIL LIKE ? OR
                  u.$COLUMN_UNIT_NAME LIKE ?
            ORDER BY s.$COLUMN_STAFF_NAME
        """.trimIndent()

        val cursor = db.rawQuery(queryStr,
            arrayOf(searchQuery, searchQuery, searchQuery, searchQuery, searchQuery))

        with(cursor) {
            while (moveToNext()) {
                val staff = Staff(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_POSITION)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_PHONE)),
                    getString(getColumnIndexOrThrow(COLUMN_STAFF_EMAIL)),
                    getInt(getColumnIndexOrThrow(COLUMN_STAFF_UNIT_ID)),
                    getString(getColumnIndexOrThrow("unit_name"))
                )
                staffList.add(staff)
            }
            close()
        }

        return staffList
    }

    // User operations
    fun loginUser(username: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_USERNAME = ? AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val staffId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_STAFF_ID))
            val isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_IS_ADMIN)) == 1

            val user = User(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                staffId,
                isAdmin
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun getUserByStaffId(staffId: Int): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_STAFF_ID = ?",
            arrayOf(staffId.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_STAFF_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_IS_ADMIN)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun createUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_USERNAME, user.username)
            put(COLUMN_USER_PASSWORD, user.password)
            put(COLUMN_USER_STAFF_ID, user.staffId)
            put(COLUMN_USER_IS_ADMIN, if (user.isAdmin) 1 else 0)
        }

        return db.insert(TABLE_USERS, null, values)
    }

    fun updateUserPassword(userId: Int, newPassword: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_PASSWORD, newPassword)
        }

        return db.update(
            TABLE_USERS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(userId.toString())
        )
    }
}