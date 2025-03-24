package com.example.tlucontact.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tlucontact.R
import com.example.tlucontact.database.DatabaseHelper
import com.example.tlucontact.model.Staff
import com.example.tlucontact.model.Unit
import com.example.tlucontact.model.User
import com.example.tlucontact.utils.SessionManager

class StaffFormActivity : AppCompatActivity() {

    private lateinit var etStaffName: EditText
    private lateinit var etStaffPosition: EditText
    private lateinit var etStaffPhone: EditText
    private lateinit var etStaffEmail: EditText
    private lateinit var spinnerUnit: Spinner
    private lateinit var btnSave: Button

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var staffId: Int = 0
    private var isEditMode = false
    private var units = listOf<Unit>()
    private var selectedUnitId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_form)

        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        etStaffName = findViewById(R.id.etStaffName)
        etStaffPosition = findViewById(R.id.etStaffPosition)
        etStaffPhone = findViewById(R.id.etStaffPhone)
        etStaffEmail = findViewById(R.id.etStaffEmail)
        spinnerUnit = findViewById(R.id.spinnerUnit)
        btnSave = findViewById(R.id.btnSave)

        // Load units for the spinner
        loadUnits()

        // Check if we're editing an existing staff
        staffId = intent.getIntExtra("staffId", 0)
        isEditMode = staffId != 0

        if (isEditMode) {
            supportActionBar?.title = "Sửa thông tin CBNV"
            loadStaffData()

            // Check if user has permission to edit this staff
            if (!sessionManager.isAdmin() && sessionManager.getLoggedInStaffId() != staffId) {
                Toast.makeText(this, "Bạn không có quyền chỉnh sửa thông tin này", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            supportActionBar?.title = "Thêm CBNV"
            // Only admin can add new staff
            if (!sessionManager.isAdmin()) {
                Toast.makeText(this, "Bạn không có quyền thêm CBNV", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnSave.setOnClickListener {
            saveStaff()
        }
    }

    private fun loadUnits() {
        units = dbHelper.getAllUnits()
        val unitNames = units.map { it.name }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = adapter

        spinnerUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUnitId = units[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun loadStaffData() {
        val staff = dbHelper.getStaff(staffId)
        if (staff != null) {
            etStaffName.setText(staff.name)
            etStaffPosition.setText(staff.position)
            etStaffPhone.setText(staff.phone)
            etStaffEmail.setText(staff.email)

            // Select the correct unit in the spinner
            val unitIndex = units.indexOfFirst { it.id == staff.unitId }
            if (unitIndex >= 0) {
                spinnerUnit.setSelection(unitIndex)
            }
        }
    }

    private fun saveStaff() {
        val name = etStaffName.text.toString().trim()
        val position = etStaffPosition.text.toString().trim()
        val phone = etStaffPhone.text.toString().trim()
        val email = etStaffEmail.text.toString().trim()

        if (name.isEmpty() || position.isEmpty() || phone.isEmpty() || email.isEmpty() || selectedUnitId == 0) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode) {
            val staff = Staff(staffId, name, position, phone, email, selectedUnitId)
            val result = dbHelper.updateStaff(staff)
            if (result > 0) {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
        } else {
            val staff = Staff(0, name, position, phone, email, selectedUnitId)
            val staffId = dbHelper.addStaff(staff)
            if (staffId > 0) {
                Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show()

                // Check if we want to create a user account for this staff
                // For simplicity, we'll automatically create one with default credentials
                val username = email.split("@")[0]
                val password = "password123"
                val user = User(
                    0,
                    username,
                    password,
                    staffId.toInt(),
                    false
                )
                dbHelper.createUser(user)

                finish()
            } else {
                Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show()
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