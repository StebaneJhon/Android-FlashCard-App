package com.ssoaharison.recall.onboarding.onBoardingFragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ssoaharison.recall.R
import com.google.android.material.color.MaterialColors

class OnboardingFragment4 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding4, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.vp_onBoarding)
        view.findViewById<Button>(R.id.bt_next_4).setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingViewPagerFragment_to_deckFragment2)
            onBoardingFinished()
        }
        view.findViewById<Button>(R.id.bt_previous_4).setOnClickListener {
            viewPager?.currentItem = 2
        }
        return view
    }

    private fun onBoardingFinished() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
        setThemedStatusBar()

    }

    private fun setThemedStatusBar() {
        val window = activity?.window
        window?.statusBarColor = MaterialColors.getColor(
            requireView(),
            com.google.android.material.R.attr.colorSurfaceContainerLow
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            val windowInsetController = ViewCompat.getWindowInsetsController(window?.decorView!!)
            windowInsetController?.isAppearanceLightStatusBars = true
        }
    }

}