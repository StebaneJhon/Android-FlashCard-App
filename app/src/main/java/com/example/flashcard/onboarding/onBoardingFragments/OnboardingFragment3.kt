package com.example.flashcard.onboarding.onBoardingFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.flashcard.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class OnboardingFragment3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding3, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.vp_onBoarding)
        view.findViewById<Button>(R.id.bt_next_3).setOnClickListener {
            viewPager?.currentItem = 3
        }
        view.findViewById<Button>(R.id.bt_previous_3).setOnClickListener {
            viewPager?.currentItem = 1
        }
        return view
    }

}