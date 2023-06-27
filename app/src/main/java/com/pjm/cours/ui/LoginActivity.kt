package com.pjm.cours.ui

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.pjm.cours.CoursApplication
import com.pjm.cours.databinding.ActivityLoginBinding
import com.pjm.cours.util.Constants.KEY_GOOGLE_ID_TOKEN

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signInLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var signInLegacyLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = createSignInRequest()
        signInLauncher = getActivityResultLauncher(oneTapClient)
        signInLegacyLauncher =
            getActivityResultLauncher(oneTapClient)

        binding.btnLoginWithGoogle.setOnClickListener {
            beginSignIn()
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

    private fun getActivityResultLauncher(signInClient: SignInClient) =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = signInClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        CoursApplication.preferencesManager.setGoogleIdToken(KEY_GOOGLE_ID_TOKEN, idToken)
                                        startActivity(Intent(this, MapActivity::class.java))
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

    private fun beginSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    signInLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                            .build()
                    )
                } catch (e: IntentSender.SendIntentException) {

                }
            }
            .addOnFailureListener(this) {
                signInLegacy()
            }
    }

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
}