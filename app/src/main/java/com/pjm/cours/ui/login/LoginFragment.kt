package com.pjm.cours.ui.login

import android.app.PendingIntent
import android.content.IntentSender
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pjm.cours.BuildConfig
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentLoginBinding
import com.pjm.cours.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signInLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var signInLegacyLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var idToken: String? = null

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
        setObserver()
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isBeforeUser.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect { isBeforeUser ->
                if (isBeforeUser) {
                    viewModel.saveGoogleIdToken(auth.currentUser?.uid ?: "")
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToChatListFragment(),
                    )
                } else {
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToSettingUserInfoFragment(
                            auth.currentUser?.uid ?: "",
                        ),
                    )
                }
            }
        }
    }

    private fun getActivityResultLauncher(signInClient: SignInClient) =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                try {
                    val credential = signInClient.getSignInCredentialFromIntent(result.data)
                    idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        viewModel.getUserInfo()
                                    } else {
                                        Snackbar.make(
                                            binding.root,
                                            getString(R.string.error_message),
                                            Snackbar.LENGTH_SHORT,
                                        )
                                            .show()
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
            } else {
            }
        }

    private fun createSignInRequest() = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build(),
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
                        IntentSenderRequest.Builder(result.intentSender).build(),
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
                            .build(),
                    )
                } catch (e: IntentSender.SendIntentException) {
                }
            }
            .addOnFailureListener(requireActivity()) {
                signInLegacy()
            }
    }
}
