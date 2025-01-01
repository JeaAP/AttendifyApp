package com.example.attendify

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attendify.databinding.FragmentPeopleBinding

/**
 * A simple [Fragment] subclass.
 * Use the [PeopleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PeopleFragment : Fragment() {

    private lateinit var binding: FragmentPeopleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPeopleBinding.inflate(inflater)

        binding.back.setOnClickListener{
            // Memeriksa dan kembali ke fragment sebelumnya
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                // Opsional: Jika tidak ada fragment di back stack, tutup activity
                activity?.finish()
            }
        }

        binding.ceoMore.setOnClickListener{
            val intent = Intent(requireContext(), aboutCEO::class.java)
            startActivity(intent)
        }

//        binding.ctoMore.setOnClickListener{
//            val intent = Intent(requireContext(), aboutCTO::class.java)
//            startActivity(intent)
//        }
//
//        binding.cmoMore.setOnClickListener{
//            val intent = Intent(requireContext(), aboutCMO::class.java)
//            startActivity(intent)
//        }

        return binding.root
    }
}