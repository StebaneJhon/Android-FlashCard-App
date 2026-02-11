package com.soaharisonstebane.mneme.privacyPolicy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.drawerlayout.widget.DrawerLayout
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {

    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.PrivacyPolicyTopAppBar.setNavigationOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.mainActivityRoot)?.open()
        }

        weViewSetup()

    }

    private fun weViewSetup() {
        binding.wvPrivacyPolicy.apply {
            webViewClient = WebViewClient()
            loadUrl("https://stebanejhon.github.io/mneme_privacy_policy/")
            settings.safeBrowsingEnabled = true
        }
    }
}