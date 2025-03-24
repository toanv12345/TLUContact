package com.example.tlucontact.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tlucontact.R
import com.example.tlucontact.database.DatabaseHelper
import com.example.tlucontact.utils.SessionManager

class StaffDetailActivity : AppCompatActivity() {

    private lateinit var tvStaffName: TextView
    private lateinit var tvStaffPosition: TextView
    private lateinit var tvStaffPhone: TextView
    private lateinit var tvStaffEmail: TextView
    private lateinit var tvStaffUnit: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnCall: ImageButton

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var staffId: Int = 0
    private var phoneNumber: String = ""

    companion object {
        private const val CALL_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_detail)

        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        tvStaffName = findViewById(R.id.tvStaffName)
        tvStaffPosition = findViewById(R.id.tvStaffPosition)
        tvStaffPhone = findViewById(R.id.tvStaffPhone)
        tvStaffEmail = findViewById(R.id.tvStaffEmail)
        tvStaffUnit = findViewById(R.id.tvStaffUnit)
        btnEdit = findViewById(R.id.btnEdit)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnCall = findViewById(R.id.btnCall)

        // Get staff ID from intent
        staffId = intent.getIntExtra("staffId", 0)

        if (staffId == 0) {
            finish()
            return
        }

        // Load staff details
        loadStaffDetails()

        // Show or hide edit button based on permissions
        if (sessionManager.isAdmin() || sessionManager.getLoggedInStaffId() == staffId) {
            btnEdit.visibility = View.VISIBLE
        } else {
            btnEdit.visibility = View.GONE
        }

        // Show or hide change password button
        if (sessionManager.getLoggedInStaffId() == staffId) {
            btnChangePassword.visibility = View.VISIBLE
        } else {
            btnChangePassword.visibility = View.GONE
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, StaffFormActivity::class.java)
            intent.putExtra("staffId", staffId)
            startActivity(intent)
        }

        btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        // Set up call button click listener
        btnCall.setOnClickListener {
            makePhoneCall()
        }

        // Make phone number clickable to call
        tvStaffPhone.setOnClickListener {
            makePhoneCall()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStaffDetails()
    }

    private fun loadStaffDetails() {
        val staff = dbHelper.getStaff(staffId)
        if (staff != null) {
            supportActionBar?.title = staff.name
            tvStaffName.text = staff.name
            tvStaffPosition.text = staff.position
            tvStaffPhone.text = staff.phone
            phoneNumber = staff.phone
            tvStaffEmail.text = staff.email
            tvStaffUnit.text = staff.unitName
        }
    }

    private fun makePhoneCall() {
        if (phoneNumber.isNotEmpty()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), CALL_PERMISSION_REQUEST_CODE)
            } else {
                startCall()
            }
        } else {
            Toast.makeText(this, "Không có số điện thoại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCall() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCall()
            } else {
                Toast.makeText(this, "Quyền gọi điện bị từ chối", Toast.LENGTH_SHORT).show()
            }
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