package com.mobilesystems.feedme.ui.recipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import com.mobilesystems.feedme.databinding.RecipeListFragmentBinding
import com.mobilesystems.feedme.domain.model.Recipe
import dagger.hilt.android.AndroidEntryPoint

/**
 * A fragment representing a list of Items.
 */
@AndroidEntryPoint
class RecipeListFragment : Fragment() {

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedRecipesViewModel by activityViewModels()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var adapter: RecipeListAdapter

    //view binding
    private var _binding: RecipeListFragmentBinding? = null
    private val binding get() = _binding!!

    private val listener = object: RecipeListAdapter.RecipeAdapterClickListener {

        override fun passData(recipe: Recipe, itemView: View) {
            // pass data and navigate to recipe detail view
            sharedViewModel.selectedRecipe(recipe)
            val action = RecipeListFragmentDirections.actionNavigationRecipesToRecipeFragment(recipe)
            Navigation.findNavController(itemView).navigate(action)
        }
    }

    // Remove items of list via swipe
    private var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                sharedViewModel.removeRecipeByPosition(viewHolder.absoluteAdapterPosition)
                adapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
            }

            override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int,
                                 target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = RecipeListFragmentBinding.inflate(inflater, container, false)

        // Setup recycler view
        recipeRecyclerView = binding.recyclerviewRecipeList
        linearLayoutManager = LinearLayoutManager(context)
        recipeRecyclerView.layoutManager = linearLayoutManager
        recipeRecyclerView.setHasFixedSize(true)

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recipeRecyclerView)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val recipeListObserver = Observer<List<Recipe>?> { recipeList: List<Recipe>? ->
            // Update the UI and update the adapter for recycelerview
            if (recipeList != null) {
                adapter = RecipeListAdapter(recipeList, listener)
                recipeRecyclerView.adapter = adapter
            }
        }

        // update adapter after data is loaded
        sharedViewModel.recipeList.observe(viewLifecycleOwner, recipeListObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RecipeListFragment"
        fun newInstance() = RecipeListFragment()
    }
}