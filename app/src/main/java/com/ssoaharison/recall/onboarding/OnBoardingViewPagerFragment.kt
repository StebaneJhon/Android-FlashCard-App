package com.ssoaharison.recall.onboarding

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.ssoaharison.recall.R
import com.ssoaharison.recall.onboarding.onBoardingFragments.OnboardingFragment1
import com.ssoaharison.recall.onboarding.onBoardingFragments.OnboardingFragment2
import com.ssoaharison.recall.onboarding.onBoardingFragments.OnboardingFragment3
import com.ssoaharison.recall.onboarding.onBoardingFragments.OnboardingFragment4

class OnBoardingViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_on_boarding_view_pager, container, false)

        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.royal_blue)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            val windowInsetController = ViewCompat.getWindowInsetsController(window?.decorView!!)
            windowInsetController?.isAppearanceLightStatusBars = false
        }


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