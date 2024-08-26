package com.example.flashcard.mainActivity

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.flashcard.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (onBoardingFinished()) {
            findNavController().navigate(R.id.action_hostFragment_to_deckFragment2)
        } else {
            findNavController().navigate(R.id.action_hostFragment_to_onBoardingViewPagerFragment)
        }
        return inflater.inflate(R.layout.fragment_host, container, false)
    }

    private fun onBoardingFinished(): Boolean {
        val sharedPreferences = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("Finished", false)
    }

}