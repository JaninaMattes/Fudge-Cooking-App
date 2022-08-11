package com.mobilesystems.feedme.ui.logout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mobilesystems.feedme.LoginActivity
import com.mobilesystems.feedme.domain.model.User
import androidx.lifecycle.Observer
import com.mobilesystems.feedme.databinding.LogoutFragmentBinding
import com.mobilesystems.feedme.ui.dashboard.SharedDashboardViewModel


class LogoutFragment : Fragment() {

    private val sharedViewModel: SharedDashboardViewModel by activityViewModels()
    private lateinit var username: String
    private lateinit var password: String

    //view binding
    private var _binding: LogoutFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  LogoutFragmentBinding.inflate(inflater, container, false)

        //elements
        val cancelButton: Button = binding.cancelLogout
        val logoutButton: Button = binding.logoutButton

        val userObserver = Observer<User?>{ user: User? ->
            if(user != null){
                username = user.firstName
                password = user.password
            }else {
                sharedViewModel.loadLoggedInUser()
            }
        }

        sharedViewModel.loggedInUser.observe(viewLifecycleOwner, userObserver)

        cancelButton.setOnClickListener {
            val action = LogoutFragmentDirections.actionLogoutFragmentToNavigationDashboard()
            findNavController().navigate(action)
        }

        logoutButton.setOnClickListener{
            sharedViewModel.logout()
            val intent = Intent(activity?.applicationContext, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            Log.d("Logout", username)
        }

        return binding.root
    }

    companion object {
        const val TAG = "LogoutFragment"
        fun newInstance() = LogoutFragment()
    }

}