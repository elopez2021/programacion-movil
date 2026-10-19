package com.example.login.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "users_db.sqlite"
        private const val DATABASE_VERSION = 3

        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NAME = "name"

        /**
         * Hash password using SHA-256
         */
        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL UNIQUE,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    /**
     * Register a new user with username, email, password and name.
     */
    fun registerUser(username: String, email: String, password: String, name: String): Result<Long> {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()

        if (trimmedUsername.isBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be empty"))
        }
        if (trimmedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }

        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, trimmedUsername.lowercase())
                put(COLUMN_EMAIL, trimmedEmail.lowercase())
                put(COLUMN_PASSWORD, hashPassword(password))
                put(COLUMN_NAME, trimmedName)
            }
            val id = db.insertOrThrow(TABLE_USERS, null, values)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(Exception("Username or Email already exists: ${e.localizedMessage}"))
        }
    }

    /**
     * Authenticate a user by username (or email) and password.
     */
    fun loginUser(identifier: String, password: String): User? {
        val db = readableDatabase
        val query = """
            SELECT $COLUMN_ID, $COLUMN_USERNAME, $COLUMN_EMAIL, $COLUMN_NAME 
            FROM $TABLE_USERS 
            WHERE (LOWER($COLUMN_USERNAME) = LOWER(?) OR LOWER($COLUMN_EMAIL) = LOWER(?)) AND $COLUMN_PASSWORD = ?
        """.trimIndent()

        val hashedPassword = hashPassword(password)
        val trimmed = identifier.trim()
        val cursor = db.rawQuery(query, arrayOf(trimmed, trimmed, hashedPassword))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
            val usernameIndex = cursor.getColumnIndexOrThrow(COLUMN_USERNAME)
            val emailIndex = cursor.getColumnIndexOrThrow(COLUMN_EMAIL)
            val nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME)

            user = User(
                id = cursor.getLong(idIndex),
                username = cursor.getString(usernameIndex),
                email = cursor.getString(emailIndex),
                name = cursor.getString(nameIndex)
            )
        }
        cursor.close()
        return user
    }

    /**
     * Check if an email exists in the database
     */
    fun emailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_USERS WHERE LOWER($COLUMN_EMAIL) = LOWER(?)",
            arrayOf(email.trim())
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    /**
     * Check if a username already exists in the database
     */
    fun userExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_USERS WHERE LOWER($COLUMN_USERNAME) = LOWER(?)",
            arrayOf(username.trim())
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    /**
     * Update user's full name in their profile
     */
    fun updateUserName(userId: Long, newName: String): Result<Boolean> {
        val trimmedName = newName.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }

        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_NAME, trimmedName)
            }
            val rowsAffected = db.update(
                TABLE_USERS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(userId.toString())
            )
            if (rowsAffected > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update name"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user profile name and email
     */
    fun updateUserProfile(userId: Long, newName: String, newEmail: String): Result<Boolean> {
        val trimmedName = newName.trim()
        val trimmedEmail = newEmail.trim()
        if (trimmedName.isBlank() || trimmedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Name and Email cannot be empty"))
        }

        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_NAME, trimmedName)
                put(COLUMN_EMAIL, trimmedEmail.lowercase())
            }
            val rowsAffected = db.update(
                TABLE_USERS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(userId.toString())
            )
            if (rowsAffected > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Change password for logged-in user, verifying their old password first
     */
    fun changePassword(userId: Long, oldPassword: String, newPassword: String): Result<Boolean> {
        if (oldPassword.isBlank() || newPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Passwords cannot be empty"))
        }

        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_PASSWORD FROM $TABLE_USERS WHERE $COLUMN_ID = ?",
            arrayOf(userId.toString())
        )

        var currentHash: String? = null
        if (cursor.moveToFirst()) {
            currentHash = cursor.getString(0)
        }
        cursor.close()

        if (currentHash == null || currentHash != hashPassword(oldPassword)) {
            return Result.failure(Exception("WRONG_OLD_PASSWORD"))
        }

        return try {
            val values = ContentValues().apply {
                put(COLUMN_PASSWORD, hashPassword(newPassword))
            }
            val rowsAffected = db.update(
                TABLE_USERS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(userId.toString())
            )
            if (rowsAffected > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check how many users exist in the SQLite database
     */
    fun getUserCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USERS", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
}
