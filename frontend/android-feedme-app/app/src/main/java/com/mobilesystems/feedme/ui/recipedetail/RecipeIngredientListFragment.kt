package com.mobilesystems.feedme.ui.recipedetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.RecipeIngredientListFragmentBinding
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.ui.recipes.SharedRecipesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment to display the ingredient RecyclerView with ingredient list.
 */

@AndroidEntryPoint
class RecipeIngredientListFragment : Fragment() {

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedRecipesViewModel by activityViewModels()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var ingredientRecyclerView: RecyclerView
    private lateinit var adapterRecipe: RecipeIngredientListAdapter

    //view binding
    private var _binding: RecipeIngredientListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = RecipeIngredientListFragmentBinding.inflate(inflater, container, false)

        // Setup recycler view
        ingredientRecyclerView = binding.recyclerviewIngredientList
        linearLayoutManager = LinearLayoutManager(context)
        ingredientRecyclerView.layoutManager = linearLayoutManager
        ingredientRecyclerView.isNestedScrollingEnabled = false // no scrolling of list
        ingredientRecyclerView.setHasFixedSize(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val ingredientListObserver = Observer<List<Product>?> { ingredientList: List<Product>? ->
            // Update the UI and update the adapter for recyclerview
            if (ingredientList != null) {
                val context = activity?.applicationContext
                if(context != null){
                    adapterRecipe = RecipeIngredientListAdapter(context, sharedViewModel, ingredientList)
                    ingredientRecyclerView.adapter = adapterRecipe
                }
            }
        }

        // update adapter after data is loaded
        sharedViewModel.selectedRecipeIngredients.observe(viewLifecycleOwner, ingredientListObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RecipeIngredientListFragment"
        fun newInstance() = RecipeIngredientListFragment()
    }
}