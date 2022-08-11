package com.mobilesystems.feedme.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.databinding.DashboardFragmentBinding
import com.mobilesystems.feedme.domain.model.User
import com.mobilesystems.feedme.ui.shoppinglist.AddProductToShoppingListFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val sharedViewModel: SharedDashboardViewModel by activityViewModels()

    // view binding
    private var _binding: DashboardFragmentBinding? = null
    private val binding get() = _binding!!

    // This property is only valid between onCreateView and onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //inflate layout for the fragment
        _binding = DashboardFragmentBinding.inflate(inflater, container, false)

        val userProfileName: TextView = binding.dashboardUserProfileName
        val moreButtonOne: TextView = binding.dashboardMoreButtonOne
        val moreButtonTwo: TextView = binding.dashboardMoreButtonTwo


        // Create the observer which updates the UI.
        val userObserver = Observer<User?> { user : User? ->
            if (user != null) {
                // use picasso to locally store image
                userProfileName.text = user.firstName
            } else {
                Log.d(TAG, "No user is logged in.")
            }
        }

        moreButtonOne.setOnClickListener {
            //navigate to inventory list
            val action = DashboardFragmentDirections.actionNavigationDashboardToNavigationInventorylist()
            findNavController().navigate(action)
        }

        moreButtonTwo.setOnClickListener {
            //navigate to recipe list
            val action = DashboardFragmentDirections.actionNavigationDashboardToNavigationRecipes()
            findNavController().navigate(action)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        sharedViewModel.loggedInUser.observe(viewLifecycleOwner, userObserver)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add ingredient list as child fragment
        val recipeListFragment = DashboardRecipeListFragment()
        val productListFragment = DashboardExpiringProductListFragment()

        addChildFragment(R.id.number_one_recipe_list_fragment, recipeListFragment)
        addChildFragment(R.id.expiring_products_list_fragment, productListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addChildFragment(viewId: Int, childFragment: Fragment){
        // nest child fragment into parent fragment
        // https://developer.android.com/about/versions/android-4.2#NestedFragments
        val child = childFragmentManager.findFragmentById(viewId)

        if(child == null){
            childFragmentManager.beginTransaction().apply {
                add(viewId, childFragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    companion object {
        const val TAG = "DashboardFragment"
        fun newInstance() = DashboardFragment()
    }

}