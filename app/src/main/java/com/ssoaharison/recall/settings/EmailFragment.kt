package com.ssoaharison.recall.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ssoaharison.recall.R
import com.ssoaharison.recall.databinding.FragmentEmailBinding
import com.ssoaharison.recall.util.Credential
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

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

            if (verifyEmailAndMessageField(userEmail, userMessage)) {
                sendEmailSMTP(userEmail, userMessage, subject, userName)
            }
        }
    }

    private fun sendEmailSMTP(
        userEmail: String?,
        userMessage: String?,
        userSubject: String,
        userName: String?
    ) {
        try {
            val stringHost = "smtp.gmail.com"
            val properties = System.getProperties()
            properties["mail.smtp.host"] = stringHost
            properties["mail.smtp.port"] = "465"
            properties["mail.smtp.ssl.enable"] = "true"
            properties["mail.smtp.auth"] = "true"

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        Credential.RECALL_EMAIL,
                        Credential.RECALL_MAIL_APP_PASSWORD
                    )
                }
            })

            val mimeMessage = MimeMessage(session)
            mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(Credential.RECALL_EMAIL))
            mimeMessage.subject = getString(R.string.email_object, getString(R.string.app_name),userSubject)
            mimeMessage.setText(getString(R.string.email_user_mail, userName, userEmail, userMessage))

            val t = Thread {
                try {
                    Transport.send(mimeMessage)
                } catch (e: MessagingException) {
                    isEmailSent(false)
                    showError(getString(R.string.error_message_email_not_sent))
                }
            }
            t.start()
        } catch (e: AddressException) {
            isEmailSent(false)
            showError(getString(R.string.error_message_missing_mail_app))
        } catch (e: MessagingException) {
            isEmailSent(false)
            showError(getString(R.string.error_message_email_not_sent))
        }
        isEmailSent(true)
    }

    private fun showError(errorMessage: String) {
        MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(getString(R.string.error_message_email_not_sent))
            .setMessage(errorMessage)
            .setPositiveButton(getString(R.string.bt_text_dismiss)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun isEmailSent(isSent: Boolean) {
        binding.imvCheck.isVisible = isSent
        binding.tvSuccessMessage.isVisible = isSent
        binding.tieEmail.text?.clear()
        binding.tieName.text?.clear()
        binding.tieMessage.text?.clear()
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