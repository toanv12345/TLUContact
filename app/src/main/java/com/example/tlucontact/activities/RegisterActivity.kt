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

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPosition: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var spinnerUnit: Spinner
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var dbHelper: DatabaseHelper
    private var units = listOf<Unit>()
    private var selectedUnitId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Set up toolbar
        supportActionBar?.title = "Đăng ký tài khoản"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)

        etName = findViewById(R.id.etName)
        etPosition = findViewById(R.id.etPosition)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        spinnerUnit = findViewById(R.id.spinnerUnit)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)

        // Load units for spinner
        loadUnits()

        btnRegister.setOnClickListener {
            registerUser()
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

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val position = etPosition.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validate inputs
        if (name.isEmpty() || position.isEmpty() || phone.isEmpty() || email.isEmpty() ||
            username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedUnitId == 0) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if username already exists
        val existingUser = dbHelper.checkUsernameExists(username)
        if (existingUser) {
            Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        // Create new staff record
        val staff = Staff(0, name, position, phone, email, selectedUnitId)
        val staffId = dbHelper.addStaff(staff)

        if (staffId > 0) {
            // Create user account
            val user = User(0, username, password, staffId.toInt(), false)
            val userId = dbHelper.createUser(user)

            if (userId > 0) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // If user creation fails, delete the staff record
                dbHelper.deleteStaff(staffId.toInt())
                Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
        }

        progressBar.visibility = View.GONE
        btnRegister.isEnabled = true
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