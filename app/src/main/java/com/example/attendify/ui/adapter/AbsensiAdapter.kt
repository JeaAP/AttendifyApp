package com.example.attendify.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendify.R
import com.example.attendify.model.Absensi

class AbsensiAdapter(
    private var absensiList: List<Absensi>,
    private val onItemClicked: (Absensi) -> Unit
) : RecyclerView.Adapter<AbsensiAdapter.AbsensiViewHolder>() {

    inner class AbsensiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.day)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val timeTextView: TextView = itemView.findViewById(R.id.time)
        val emoteImageView: ImageView = itemView.findViewById(R.id.emoteImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsensiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item, parent, false)
        return AbsensiViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsensiViewHolder, position: Int) {
        val absensi = absensiList[position]
        holder.dayTextView.text = absensi.hari
        holder.dateTextView.text = absensi.tanggal
        holder.timeTextView.text = absensi.jam

        val drawableRes = when (absensi.mood) {
            "Happy" -> R.drawable.salutingemote
            "Good" -> R.drawable.smileemote
            "Sad" -> R.drawable.sademote
            "Angry" -> R.drawable.angryemote
            "Not Good" -> R.drawable.confusedemote
            else -> R.drawable.smileemote
        }
        holder.emoteImageView.setImageResource(drawableRes)

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClicked(absensi)
        }
    }

    override fun getItemCount(): Int = absensiList.size

    fun updateData(newData: List<Absensi>) {
        absensiList = newData
        notifyDataSetChanged()
    }
}