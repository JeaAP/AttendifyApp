package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attendify.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var dbHelper: DatabaseHelperProfile

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)
        dbHelper = DatabaseHelperProfile(requireContext())

        // Load data from the database and update UI
        loadProfileData()

        // Set listeners for buttons and views
        binding.accountImage.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbcent.setOnClickListener {
            val intent = Intent(requireContext(), ScanActivity::class.java)
            startActivity(intent)
        }

        binding.btnIzin.setOnClickListener {
            val intent = Intent(requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.cardSchedule.setOnClickListener {
            val intent = Intent(requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.linkText.setOnClickListener {
            val intent = Intent(requireContext(), viewAllAbsensi::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun loadProfileData() {
        val profile = dbHelper.getProfile()
        if (profile != null) {
            binding.accountName.text = profile.nama
            binding.accountClass.text = profile.kelas
            binding.username.text = "Hi ${profile.username},"
            binding.greetings.text = "Selamat datang!"
            binding.motivations.text = "Tetap semangat belajar!"

            val bitmap = profile.foto?.let {
                DatabaseHelperProfile.byteArrayToBitmap(it)
            }
            if (bitmap != null) {
                binding.accountImage.setImageBitmap(bitmap)
            } else {
                binding.accountImage.setImageResource(R.drawable.account_circle)
            }

        } else {
            binding.accountName.text = "[Nama tidak ditemukan]"
            binding.accountClass.text = "[Kelas tidak ditemukan]"
            binding.username.text = "Hi [Nama],"
            binding.greetings.text = "[Greetings]"
            binding.motivations.text = "[Motivasi]"
        }
    }
}
