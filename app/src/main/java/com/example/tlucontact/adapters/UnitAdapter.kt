package com.example.tlucontact.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tlucontact.R
import com.example.tlucontact.model.Unit

class UnitAdapter(
    private val units: List<Unit>,
    private val context: Context,
    private val isAdmin: Boolean
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(unit: Unit)
        fun onEditClick(unit: Unit)
        fun onDeleteClick(unit: Unit)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUnitName: TextView = itemView.findViewById(R.id.tvUnitName)
        val tvUnitPhone: TextView = itemView.findViewById(R.id.tvUnitPhone)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_unit, parent, false)
        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unit = units[position]

        holder.tvUnitName.text = unit.name
        holder.tvUnitPhone.text = unit.phone

        // Show or hide edit/delete buttons based on admin status
        if (isAdmin) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
        } else {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(unit)
        }

        holder.btnEdit.setOnClickListener {
            listener?.onEditClick(unit)
        }

        holder.btnDelete.setOnClickListener {
            listener?.onDeleteClick(unit)
        }
    }

    override fun getItemCount(): Int {
        return units.size
    }
}