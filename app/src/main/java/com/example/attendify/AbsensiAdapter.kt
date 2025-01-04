package com.example.attendify

import android.content.Intent
import android.media.RouteListingPreference
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AbsensiAdapter(private val absensiList: List<Absensi>) : RecyclerView.Adapter<AbsensiAdapter.AbsensiViewHolder>() {

    class AbsensiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.day)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val timeTextView: TextView = itemView.findViewById(R.id.time)
        val emoteImageView: ImageView = itemView.findViewById(R.id.emoteImage)

        fun bind(absensi: RouteListingPreference.Item, listener: (RouteListingPreference.Item) -> Unit) {
            // Update your itemView views here
            itemView.setOnClickListener { listener(absensi) }
        }
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

        // Set drawable based on keterangan and mood
        val drawableRes = when (absensi.keterangan) {
            "Hadir" -> when (absensi.mood) {
                "Happy" -> R.drawable.happyemote
                "Good" -> R.drawable.goodemote
                "Bad" -> R.drawable.bademote
                else -> R.drawable.goodemote // Default drawable for unknown mood
            }
            "Sakit" -> R.drawable.outline_sick_24
            else -> R.drawable.izin // Default drawable for other keterangan
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, viewAllAbsensi::class.java).apply {
                putExtra("absensi_id", absensi.id) // Menggunakan objek absensi
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.emoteImageView.setImageResource(drawableRes)
    }

    override fun getItemCount(): Int = absensiList.size
}