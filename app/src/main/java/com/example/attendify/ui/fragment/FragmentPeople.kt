package com.example.attendify.ui.fragment

import com.example.attendify.ui.*
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attendify.databinding.FragmentPeopleBinding

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPeople.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPeople : Fragment() {

    private lateinit var binding: FragmentPeopleBinding
    private lateinit var activityMain: ActivityMain

    fun onBackPressed() {
//        super.onBackPressed()
//        Toast.makeText(this, "Back button is disabled on this screen.", Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        if (context is ActivityMain) {
            activityMain = context
        } else {
            throw IllegalStateException("PeopleFragment must be attached to MainActivity")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPeopleBinding.inflate(inflater)

        binding.back.setOnClickListener {
            activityMain.replaceFragment(FragmentHome())
        }
        binding.ceoMore.setOnClickListener {
            val intent = Intent(requireContext(), aboutCEO::class.java)
            startActivity(intent)
        }
        binding.ctoMore.setOnClickListener {
            val intent = Intent(requireContext(), aboutCTO::class.java)
            startActivity(intent)
        }
        binding.cmoMore.setOnClickListener {
            val intent = Intent(requireContext(), aboutCMO::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}
