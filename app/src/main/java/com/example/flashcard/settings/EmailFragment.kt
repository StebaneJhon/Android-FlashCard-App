package com.example.flashcard.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.flashcard.R
import com.example.flashcard.databinding.FragmentEmailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EmailFragment : Fragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: EmailFragmentArgs by navArgs()
        val subject = args.subject

        binding.emailTopAppBar.title = subject
        binding.emailTopAppBar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_emailFragment_to_settingsFragment)
        }

        isEmailSent(false)

        binding.btSendEmail.setOnClickListener {

            val userEmail = binding.tieEmail.text?.toString()?.trim()
            val userMessage = binding.tieMessage.text?.toString()?.trim()
            val userName = binding.tieName.text?.toString()?.trim()

            if(verifyEmailAndMessageField(userEmail, userMessage)) {
                sendEmail(userEmail, userMessage, subject, userName)
            }
        }
    }

    private fun sendEmail(
        userEmail: String?,
        userMessage: String?,
        userSubject: String?,
        userName: String?,
    ) {
        val addresses = arrayOf("ssoaharison@gmail.com", userEmail)
        val subject = "Recall App $userSubject $userName"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, userMessage)
        }

        try {
            startActivity(intent)
            isEmailSent(true)
        } catch (e: Exception) {
            isEmailSent(false)
            showError(getString(R.string.error_message_missing_mail_app))
        }
    }

    private fun showError(errorMessage: String) {
        MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(getString(R.string.error_message_email_not_sent))
            .setMessage(errorMessage)
            .setPositiveButton(getString(R.string.bt_text_dismiss)) { dialog, _ ->
                dialog.dismiss()
            }
    }

    private fun isEmailSent(isSent: Boolean) {
        binding.imvCheck.isVisible = isSent
        binding.tvSuccessMessage.isVisible = isSent
    }

    private fun verifyEmailAndMessageField(
        userEmail: String?,
        userMessage: String?,
    ): Boolean {
        if (userEmail.isNullOrBlank()) {
            binding.tilEmail.error = getString(R.string.error_message_missing_email)
            return false
        }
        if (userMessage.isNullOrBlank()) {
            binding.tilMessage.error = getString(R.string.error_message_missing_message)
            return false
        }
        return true
    }

}