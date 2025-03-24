package com.example.tlucontact.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.tlucontact.R
import com.example.tlucontact.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var btnUnitDirectory: Button
    private lateinit var btnStaffDirectory: Button
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        // Check if user is logged in, if not redirect to login
        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        btnUnitDirectory = findViewById(R.id.btnUnitDirectory)
        btnStaffDirectory = findViewById(R.id.btnStaffDirectory)

        btnUnitDirectory.setOnClickListener {
            val intent = Intent(this, UnitListActivity::class.java)
            startActivity(intent)
        }

        btnStaffDirectory.setOnClickListener {
            val intent = Intent(this, StaffListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                sessionManager.logout()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}