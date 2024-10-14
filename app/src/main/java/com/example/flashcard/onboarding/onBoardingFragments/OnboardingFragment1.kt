package com.example.flashcard.onboarding.onBoardingFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.flashcard.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class OnboardingFragment1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding1, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.vp_onBoarding)
        view.findViewById<Button>(R.id.bt_next).setOnClickListener {
            viewPager?.currentItem = 1
        }
        return view
    }

}