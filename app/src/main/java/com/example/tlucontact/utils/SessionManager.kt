package com.example.tlucontact.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.tlucontact.model.User

class SessionManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    companion object {
        private const val PREF_NAME = "TLUContactPref"
        private const val IS_LOGIN = "IsLoggedIn"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_STAFF_ID = "staff_id"
        private const val KEY_IS_ADMIN = "is_admin"
    }

    fun createLoginSession(user: User) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putInt(KEY_USER_ID, user.id)
        editor.putString(KEY_USERNAME, user.username)
        editor.putInt(KEY_STAFF_ID, user.staffId)
        editor.putBoolean(KEY_IS_ADMIN, user.isAdmin)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(IS_LOGIN, false)
    }

    fun getUserDetails(): HashMap<String, Any> {
        val user = HashMap<String, Any>()
        user[KEY_USER_ID] = pref.getInt(KEY_USER_ID, 0)
        user[KEY_USERNAME] = pref.getString(KEY_USERNAME, "") ?: ""
        user[KEY_STAFF_ID] = pref.getInt(KEY_STAFF_ID, 0)
        user[KEY_IS_ADMIN] = pref.getBoolean(KEY_IS_ADMIN, false)
        return user
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }

    fun getLoggedInUserId(): Int {
        return pref.getInt(KEY_USER_ID, 0)
    }

    fun getLoggedInStaffId(): Int {
        return pref.getInt(KEY_STAFF_ID, 0)
    }

    fun isAdmin(): Boolean {
        return pref.getBoolean(KEY_IS_ADMIN, false)
    }
}