package com.pjm.cours.ui.launcher

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.pjm.cours.R
import com.pjm.cours.ui.login.LoginFragment
import com.pjm.cours.ui.main.MainFragment
import com.pjm.cours.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherFragment : Fragment() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setObserver()
    }

    private fun setObserver() {
        viewModel.localGoogleIdToken.observe(this, EventObserver { localGoogleIdToken ->
            if (localGoogleIdToken.isEmpty()) {
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<LoginFragment>(R.id.fragment_container_view)
                }
            } else {
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<MainFragment>(R.id.fragment_container_view)
                }
            }
        })
    }
}