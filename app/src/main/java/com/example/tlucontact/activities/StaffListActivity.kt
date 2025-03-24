package com.example.tlucontact.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tlucontact.R
import com.example.tlucontact.adapters.StaffAdapter
import com.example.tlucontact.database.DatabaseHelper
import com.example.tlucontact.model.Staff
import com.example.tlucontact.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StaffListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StaffAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var searchView: SearchView
    private lateinit var fabAddStaff: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_list)

        // Set up toolbar
        supportActionBar?.title = "Danh bạ CBNV"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        recyclerView = findViewById(R.id.recyclerViewStaff)
        searchView = findViewById(R.id.searchViewStaff)
        fabAddStaff = findViewById(R.id.fabAddStaff)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadStaff()

        // Set up search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText.isEmpty()) {
                        loadStaff()
                    } else {
                        searchStaff(newText)
                    }
                }
                return true
            }
        })

        // Add Staff button - only visible for admin users
        if (sessionManager.isAdmin()) {
            fabAddStaff.show()
            fabAddStaff.setOnClickListener {
                val intent = Intent(this, StaffFormActivity::class.java)
                startActivity(intent)
            }
        } else {
            fabAddStaff.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStaff()
    }

    private fun loadStaff() {
        val staff = dbHelper.getAllStaff()
        adapter = StaffAdapter(staff, this, sessionManager.isAdmin(), sessionManager.getLoggedInStaffId())
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : StaffAdapter.OnItemClickListener {
            override fun onItemClick(staff: Staff) {
                val intent = Intent(this@StaffListActivity, StaffDetailActivity::class.java)
                intent.putExtra("staffId", staff.id)
                startActivity(intent)
            }

            override fun onEditClick(staff: Staff) {
                val intent = Intent(this@StaffListActivity, StaffFormActivity::class.java)
                intent.putExtra("staffId", staff.id)
                startActivity(intent)
            }

            override fun onDeleteClick(staff: Staff) {
                dbHelper.deleteStaff(staff.id)
                loadStaff()
            }
        })
    }

    private fun searchStaff(query: String) {
        val staff = dbHelper.searchStaff(query)
        adapter = StaffAdapter(staff, this, sessionManager.isAdmin(), sessionManager.getLoggedInStaffId())
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : StaffAdapter.OnItemClickListener {
            override fun onItemClick(staff: Staff) {
                val intent = Intent(this@StaffListActivity, StaffDetailActivity::class.java)
                intent.putExtra("staffId", staff.id)
                startActivity(intent)
            }

            override fun onEditClick(staff: Staff) {
                val intent = Intent(this@StaffListActivity, StaffFormActivity::class.java)
                intent.putExtra("staffId", staff.id)
                startActivity(intent)
            }

            override fun onDeleteClick(staff: Staff) {
                dbHelper.deleteStaff(staff.id)
                loadStaff()
            }
        })
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