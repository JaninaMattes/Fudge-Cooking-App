package com.mobilesystems.feedme.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.DashboardNumberOneRecipesFragmentBinding
import com.mobilesystems.feedme.domain.model.Recipe
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardRecipeListFragment: Fragment() {

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedDashboardViewModel by activityViewModels()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recipeListRecyclerView: RecyclerView
    private lateinit var adapter: DashboardRecipeListAdapter

    //view binding
    private var _binding: DashboardNumberOneRecipesFragmentBinding? = null
    private val binding get() = _binding!!

    private val listener = object: DashboardRecipeListAdapter.RecipeAdapterClickListener {
        override fun passData(recipe: Recipe, itemView: View) {
            // pass data and navigate to product detail view
            sharedViewModel.selectedRecipe(recipe)
            val action = DashboardFragmentDirections.actionNavigationDashboardToRecipeFragment(recipe)
            Navigation.findNavController(itemView).navigate(action)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = DashboardNumberOneRecipesFragmentBinding.inflate(inflater, container, false)

        // Setup recycler view
        recipeListRecyclerView = binding.recyclerviewNoOneRecipes
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recipeListRecyclerView.layoutManager = linearLayoutManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val recipeListObserver = Observer<List<Recipe>?> { recipeList: List<Recipe>? ->
            // Update the UI and update the adapter for recycelerview
            if (recipeList != null) {
                adapter = DashboardRecipeListAdapter(recipeList, listener)
                recipeListRecyclerView.adapter = adapter
            }else{
                Log.d(TAG, "Recipe list is empty.")
            }
        }

        // update adapter after data is loaded
        sharedViewModel.noOneRecipesList.observe(viewLifecycleOwner, recipeListObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DashboardRecipeListFragment"
        fun newInstance() = DashboardRecipeListFragment()
    }
}