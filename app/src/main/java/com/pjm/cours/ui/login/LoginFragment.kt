package com.pjm.cours.ui.login

import android.app.PendingIntent
import android.content.IntentSender
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pjm.cours.BuildConfig
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentLoginBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.settinguserinfo.SettingUserInfoFragment
import com.pjm.cours.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signInLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var signInLegacyLauncher: ActivityResultLauncher<IntentSenderRequest>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(requireContext())
        signInRequest = createSignInRequest()
        signInLauncher = getActivityResultLauncher(oneTapClient)
        signInLegacyLauncher = getActivityResultLauncher(oneTapClient)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLoginWithGoogle.setOnClickListener {
            beginSignIn()
        }
    }

    private fun getActivityResultLauncher(signInClient: SignInClient) =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                try {
                    val credential = signInClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        parentFragmentManager.commit {
                                            setReorderingAllowed(true)
                                            addToBackStack(null)
                                            val bundle = Bundle().apply {
                                                putString(Constants.KEY_GOOGLE_ID_TOKEN, idToken)
                                            }
                                            replace<SettingUserInfoFragment>(
                                                R.id.fragment_container_view,
                                                null,
                                                bundle
                                            )
                                        }
                                    } else {

                                    }
                                }
                        }
                        else -> {
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {

                        }
                        CommonStatusCodes.NETWORK_ERROR -> {

                        }
                        else -> {
                        }
                    }
                }
            }
        }

    private fun createSignInRequest() = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()

    private fun signInLegacy() {
        val request = GetSignInIntentRequest.builder()
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .build()

        oneTapClient
            .getSignInIntent(request)
            .addOnSuccessListener { result: PendingIntent ->
                try {
                    signInLegacyLauncher.launch(
                        IntentSenderRequest.Builder(result.intentSender).build()
                    )
                } catch (e: IntentSender.SendIntentException) {

                }
            }
            .addOnFailureListener { e: Exception? ->

            }
    }

    private fun beginSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    signInLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                            .build()
                    )
                } catch (e: IntentSender.SendIntentException) {

                }
            }
            .addOnFailureListener(requireActivity()) {
                signInLegacy()
            }
    }
}