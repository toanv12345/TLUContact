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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tlucontact.R
import com.example.tlucontact.adapters.StaffAdapter
import com.example.tlucontact.database.DatabaseHelper
import com.example.tlucontact.model.Staff
import com.example.tlucontact.utils.SessionManager

class UnitDetailActivity : AppCompatActivity() {

    private lateinit var tvUnitName: TextView
    private lateinit var tvUnitPhone: TextView
    private lateinit var tvUnitAddress: TextView
    private lateinit var tvUnitEmail: TextView
    private lateinit var btnEdit: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StaffAdapter
    private lateinit var tvStaffTitle: TextView
    private lateinit var btnCall: ImageButton

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var unitId: Int = 0
    private var phoneNumber: String = ""

    companion object {
        private const val CALL_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_detail)

        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        tvUnitName = findViewById(R.id.tvUnitName)
        tvUnitPhone = findViewById(R.id.tvUnitPhone)
        tvUnitAddress = findViewById(R.id.tvUnitAddress)
        tvUnitEmail = findViewById(R.id.tvUnitEmail)
        btnEdit = findViewById(R.id.btnEdit)
        btnCall = findViewById(R.id.btnCall)
        recyclerView = findViewById(R.id.recyclerViewStaff)
        tvStaffTitle = findViewById(R.id.tvStaffTitle)

        // Get unit ID from intent
        unitId = intent.getIntExtra("unitId", 0)

        if (unitId == 0) {
            finish()
            return
        }

        // Show or hide edit button based on admin status
        if (sessionManager.isAdmin()) {
            btnEdit.visibility = View.VISIBLE
        } else {
            btnEdit.visibility = View.GONE
        }

        // Load unit details
        loadUnitDetails()

        // Set up RecyclerView for staff in this unit
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadStaffInUnit()

        btnEdit.setOnClickListener {
            val intent = Intent(this, UnitFormActivity::class.java)
            intent.putExtra("unitId", unitId)
            startActivity(intent)
        }

        btnCall.setOnClickListener {
            makePhoneCall()
        }

        // Make phone number clickable to call
        tvUnitPhone.setOnClickListener {
            makePhoneCall()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUnitDetails()
        loadStaffInUnit()
    }

    private fun loadUnitDetails() {
        val unit = dbHelper.getUnit(unitId)
        if (unit != null) {
            supportActionBar?.title = unit.name
            tvUnitName.text = unit.name
            tvUnitPhone.text = unit.phone
            phoneNumber = unit.phone
            tvUnitAddress.text = unit.address
            tvUnitEmail.text = unit.email
        }
    }

    private fun loadStaffInUnit() {
        val staffList = dbHelper.getStaffByUnitId(unitId)

        if (staffList.isEmpty()) {
            tvStaffTitle.text = "Không có CBNV thuộc đơn vị này"
        } else {
            tvStaffTitle.text = "Danh sách CBNV thuộc đơn vị này"
            adapter = StaffAdapter(staffList, this, sessionManager.isAdmin(), sessionManager.getLoggedInStaffId())
            recyclerView.adapter = adapter

            adapter.setOnItemClickListener(object : StaffAdapter.OnItemClickListener {
                override fun onItemClick(staff: Staff) {
                    val intent = Intent(this@UnitDetailActivity, StaffDetailActivity::class.java)
                    intent.putExtra("staffId", staff.id)
                    startActivity(intent)
                }

                override fun onEditClick(staff: Staff) {
                    val intent = Intent(this@UnitDetailActivity, StaffFormActivity::class.java)
                    intent.putExtra("staffId", staff.id)
                    startActivity(intent)
                }

                override fun onDeleteClick(staff: Staff) {
                    dbHelper.deleteStaff(staff.id)
                    loadStaffInUnit()
                }
            })
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