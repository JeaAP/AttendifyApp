package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attendify.databinding.FragmentHomeBinding

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)

        binding.accountImage.setOnClickListener{
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbcent.setOnClickListener{
            val intent = Intent(requireContext(), ScanActivity::class.java)
            startActivity(intent)
        }

        binding.btnIzin.setOnClickListener{
            val intent = Intent(requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.cardSchedule.setOnClickListener{
            val intent = Intent(requireContext(), coomingSoon::class.java)
            startActivity(intent)
        }

        binding.linkText.setOnClickListener{
            val intent = Intent(requireContext(), viewAllAbsensi::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}