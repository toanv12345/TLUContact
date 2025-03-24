package com.example.tlucontact.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tlucontact.R
import com.example.tlucontact.database.DatabaseHelper
import com.example.tlucontact.model.Unit

class UnitFormActivity : AppCompatActivity() {

    private lateinit var etUnitName: EditText
    private lateinit var etUnitPhone: EditText
    private lateinit var etUnitAddress: EditText
    private lateinit var etUnitEmail: EditText
    private lateinit var btnSave: Button

    private lateinit var dbHelper: DatabaseHelper

    private var unitId: Int = 0
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_form)

        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)

        etUnitName = findViewById(R.id.etUnitName)
        etUnitPhone = findViewById(R.id.etUnitPhone)
        etUnitAddress = findViewById(R.id.etUnitAddress)
        etUnitEmail = findViewById(R.id.etUnitEmail)
        btnSave = findViewById(R.id.btnSave)

        // Check if we're editing an existing unit
        unitId = intent.getIntExtra("unitId", 0)
        isEditMode = unitId != 0

        if (isEditMode) {
            supportActionBar?.title = "Sửa đơn vị"
            loadUnitData()
        } else {
            supportActionBar?.title = "Thêm đơn vị"
        }

        btnSave.setOnClickListener {
            saveUnit()
        }
    }

    private fun loadUnitData() {
        val unit = dbHelper.getUnit(unitId)
        if (unit != null) {
            etUnitName.setText(unit.name)
            etUnitPhone.setText(unit.phone)
            etUnitAddress.setText(unit.address)
            etUnitEmail.setText(unit.email)
        }
    }

    private fun saveUnit() {
        val name = etUnitName.text.toString().trim()
        val phone = etUnitPhone.text.toString().trim()
        val address = etUnitAddress.text.toString().trim()
        val email = etUnitEmail.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode) {
            val unit = Unit(unitId, name, phone, address, email)
            val result = dbHelper.updateUnit(unit)
            if (result > 0) {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
        } else {
            val unit = Unit(0, name, phone, address, email)
            val result = dbHelper.addUnit(unit)
            if (result > 0) {
                Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show()
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