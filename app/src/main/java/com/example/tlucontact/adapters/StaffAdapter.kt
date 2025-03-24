package com.example.tlucontact.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tlucontact.R
import com.example.tlucontact.model.Staff

class StaffAdapter(
    private val staffList: List<Staff>,
    private val context: Context,
    private val isAdmin: Boolean,
    private val loggedInStaffId: Int
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(staff: Staff)
        fun onEditClick(staff: Staff)
        fun onDeleteClick(staff: Staff)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStaffName: TextView = itemView.findViewById(R.id.tvStaffName)
        val tvStaffPosition: TextView = itemView.findViewById(R.id.tvStaffPosition)
        val tvStaffUnit: TextView = itemView.findViewById(R.id.tvStaffUnit)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]

        holder.tvStaffName.text = staff.name
        holder.tvStaffPosition.text = staff.position
        holder.tvStaffUnit.text = staff.unitName

        // Show edit button only for admin or the staff member's own record
        if (isAdmin || loggedInStaffId == staff.id) {
            holder.btnEdit.visibility = View.VISIBLE
        } else {
            holder.btnEdit.visibility = View.GONE
        }

        // Show delete button only for admin
        if (isAdmin) {
            holder.btnDelete.visibility = View.VISIBLE
        } else {
            holder.btnDelete.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(staff)
        }

        holder.btnEdit.setOnClickListener {
            listener?.onEditClick(staff)
        }

        holder.btnDelete.setOnClickListener {
            listener?.onDeleteClick(staff)
        }
    }

    override fun getItemCount(): Int {
        return staffList.size
    }
}