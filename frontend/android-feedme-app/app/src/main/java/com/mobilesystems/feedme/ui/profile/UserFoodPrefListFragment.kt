package com.mobilesystems.feedme.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.UserProfileListFragmentBinding
import com.mobilesystems.feedme.domain.model.FoodType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserFoodPrefListFragment : Fragment() {

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedUserProfileViewModel by activityViewModels() // TODO: SharedRecipeViewModel von Recipe Detail verwenden
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var foodTagListRecyclerView: RecyclerView
    private lateinit var adapter: UserFoodPrefListAdapter

    //view binding
    private var _binding: UserProfileListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = UserProfileListFragmentBinding.inflate(inflater, container, false)

        // Setup recycler view
        foodTagListRecyclerView = binding.recyclerviewFoodTagList
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        foodTagListRecyclerView.layoutManager = linearLayoutManager
        foodTagListRecyclerView.setHasFixedSize(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val tagListObserver = Observer<List<FoodType>?> { foodTypesList: List<FoodType>? ->
            // Update the UI and update the adapter for recycelerview
            if (foodTypesList != null) {
                adapter = UserFoodPrefListAdapter(foodTypesList)
                foodTagListRecyclerView.adapter = adapter
            }
        }

        // update adapter after data is loaded
        sharedViewModel.loggedInUserFoodTypeList.observe(viewLifecycleOwner, tagListObserver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.loadLoggedInUser()
        sharedViewModel.loggedInUserFoodTypeList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "UserFoodPrefListFragment"
        fun newInstance() = UserFoodPrefListFragment()
    }
}