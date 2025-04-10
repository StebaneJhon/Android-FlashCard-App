package com.ssoaharison.recall.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.ssoaharison.recall.R
import com.ssoaharison.recall.databinding.FragmentPrivacyPolicyBinding

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
            findNavController().navigate(R.id.action_privacyPolicyFragment_to_settingsFragment)
        }

        weViewSetup()

    }

    private fun weViewSetup() {
        binding.wvPrivacyPolicy.apply {
            webViewClient = WebViewClient()
            loadUrl("https://sites.google.com/view/recall-mobileapp/privacy-policy")
            settings.safeBrowsingEnabled = true
        }
    }
}