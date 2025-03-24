package com.example.tlucontact.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tlucontact.R
import com.example.tlucontact.database.DatabaseHelper
import com.example.tlucontact.utils.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvPosition: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUnit: TextView
    private lateinit var tvUsername: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnChangePassword: Button

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Set up toolbar
        supportActionBar?.title = "Thông tin cá nhân"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Initialize views
        tvName = findViewById(R.id.tvName)
        tvPosition = findViewById(R.id.tvPosition)
        tvPhone = findViewById(R.id.tvPhone)
        tvEmail = findViewById(R.id.tvEmail)
        tvUnit = findViewById(R.id.tvUnit)
        tvUsername = findViewById(R.id.tvUsername)
        btnEdit = findViewById(R.id.btnEdit)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        // Get staff ID of logged-in user
        val staffId = sessionManager.getLoggedInStaffId()

        if (staffId <= 0) {
            // If user is admin with no associated staff record
            if (sessionManager.isAdmin()) {
                tvName.text = "Admin"
                tvUsername.text = sessionManager.getUserDetails()["username"] as String

                // Hide staff-specific fields
                findViewById<View>(R.id.llPosition).visibility = View.GONE
                findViewById<View>(R.id.llPhone).visibility = View.GONE
                findViewById<View>(R.id.llEmail).visibility = View.GONE
                findViewById<View>(R.id.llUnit).visibility = View.GONE
                btnEdit.visibility = View.GONE
            } else {
                finish()
                return
            }
        } else {
            // Load staff details
            loadStaffDetails(staffId)
        }

        // Set button click listeners
        btnEdit.setOnClickListener {
            val intent = Intent(this, StaffFormActivity::class.java)
            intent.putExtra("staffId", staffId)
            startActivity(intent)
        }

        btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val staffId = sessionManager.getLoggedInStaffId()
        if (staffId > 0) {
            loadStaffDetails(staffId)
        }
    }

    private fun loadStaffDetails(staffId: Int) {
        val staff = dbHelper.getStaff(staffId)
        if (staff != null) {
            tvName.text = staff.name
            tvPosition.text = staff.position
            tvPhone.text = staff.phone
            tvEmail.text = staff.email
            tvUnit.text = staff.unitName

            // Get username
            val userDetails = sessionManager.getUserDetails()
            tvUsername.text = userDetails["username"] as String
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}