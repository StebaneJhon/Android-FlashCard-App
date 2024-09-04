package com.example.flashcard.onboarding.onBoardingFragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.google.android.material.bottomnavigation.BottomNavigationView

class OnboardingFragment4 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_onboarding4, container, false)

        view.findViewById<Button>(R.id.bt_next_4).setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingViewPagerFragment_to_deckFragment2)
            onBoardingFinished()
        }

        return view
    }

    private fun onBoardingFinished() {
        val sharedPreferences = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }

}