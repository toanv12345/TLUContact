package com.example.tlucontact.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tlucontact.R
import com.example.tlucontact.adapters.UnitAdapter
import com.example.tlucontact.database.DatabaseHelper
import com.example.tlucontact.model.Unit
import com.example.tlucontact.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UnitListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UnitAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var searchView: SearchView
    private lateinit var fabAddUnit: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_list)

        // Set up toolbar
        supportActionBar?.title = "Danh bạ đơn vị"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        recyclerView = findViewById(R.id.recyclerViewUnits)
        searchView = findViewById(R.id.searchViewUnits)
        fabAddUnit = findViewById(R.id.fabAddUnit)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadUnits()

        // Set up search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText.isEmpty()) {
                        loadUnits()
                    } else {
                        searchUnits(newText)
                    }
                }
                return true
            }
        })

        // Add Unit button - only visible for admin users
        if (sessionManager.isAdmin()) {
            fabAddUnit.show()
            fabAddUnit.setOnClickListener {
                val intent = Intent(this, UnitFormActivity::class.java)
                startActivity(intent)
            }
        } else {
            fabAddUnit.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUnits()
    }

    private fun loadUnits() {
        val units = dbHelper.getAllUnits()
        adapter = UnitAdapter(units, this, sessionManager.isAdmin())
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : UnitAdapter.OnItemClickListener {
            override fun onItemClick(unit: Unit) {
                val intent = Intent(this@UnitListActivity, UnitDetailActivity::class.java)
                intent.putExtra("unitId", unit.id)
                startActivity(intent)
            }

            override fun onEditClick(unit: Unit) {
                val intent = Intent(this@UnitListActivity, UnitFormActivity::class.java)
                intent.putExtra("unitId", unit.id)
                startActivity(intent)
            }

            override fun onDeleteClick(unit: Unit) {
                dbHelper.deleteUnit(unit.id)
                loadUnits()
            }
        })
    }

    private fun searchUnits(query: String) {
        val units = dbHelper.searchUnits(query)
        adapter = UnitAdapter(units, this, sessionManager.isAdmin())
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : UnitAdapter.OnItemClickListener {
            override fun onItemClick(unit: Unit) {
                val intent = Intent(this@UnitListActivity, UnitDetailActivity::class.java)
                intent.putExtra("unitId", unit.id)
                startActivity(intent)
            }

            override fun onEditClick(unit: Unit) {
                val intent = Intent(this@UnitListActivity, UnitFormActivity::class.java)
                intent.putExtra("unitId", unit.id)
                startActivity(intent)
            }

            override fun onDeleteClick(unit: Unit) {
                dbHelper.deleteUnit(unit.id)
                loadUnits()
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