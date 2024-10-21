package com.example.flashcard.mainActivity

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import com.example.flashcard.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.MaterialColors

class HostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_host, container, false)
//        if (onBoardingFinished()) {
//            findNavController().navigate(R.id.action_hostFragment_to_deckFragment2)
//        } else {
//            findNavController().navigate(R.id.action_hostFragment_to_onBoardingViewPagerFragment)
//        }
        findNavController().navigate(R.id.action_hostFragment_to_onBoardingViewPagerFragment)

        return view
    }

    private fun onBoardingFinished(): Boolean {
        val sharedPreferences = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("Finished", false)
    }

}