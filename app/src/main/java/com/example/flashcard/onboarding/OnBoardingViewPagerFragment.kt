package com.example.flashcard.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.flashcard.R
import com.example.flashcard.onboarding.onBoardingFragments.OnboardingFragment1
import com.example.flashcard.onboarding.onBoardingFragments.OnboardingFragment2
import com.example.flashcard.onboarding.onBoardingFragments.OnboardingFragment3
import com.example.flashcard.onboarding.onBoardingFragments.OnboardingFragment4

class OnBoardingViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_on_boarding_view_pager, container, false)

        val fragmentList = arrayListOf<Fragment>(
            OnboardingFragment1(),
            OnboardingFragment2(),
            OnboardingFragment3(),
            OnboardingFragment4(),
        )

        val adapter = OnBoardingViewPagerFragmentAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        view.findViewById<ViewPager2>(R.id.vp_onBoarding).adapter = adapter

        return view
    }
}