package com.mobilesystems.feedme.ui.recipedetail

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.mobilesystems.feedme.domain.model.Recipe
import com.squareup.picasso.Picasso
import android.widget.*
import androidx.navigation.fragment.navArgs
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.ui.recipes.SharedRecipesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeDetailFragment : Fragment() {

    // shared view model
    private val sharedViewModel: SharedRecipesViewModel by activityViewModels()

    //safeargs
    private val args: RecipeDetailFragmentArgs by navArgs()

    // content on view
    private var recipeId: Int? = null
    private  var recipeImg: String? = null
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.loadShoppingList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // inflate rootview
        val rootView = inflater.inflate(R.layout.recipe_detail_fragment, container, false)

        // Elements
        val recipeImageview: ImageView = rootView.findViewById(R.id.recipe_detail_image)
        val recipeNameTextView: TextView = rootView.findViewById(R.id.text_recipe_name)
        val recipeSubtitleTextView: TextView = rootView.findViewById(R.id.text_recipe_subtitle)
        val recipeCardRating: TextView = rootView.findViewById(R.id.text_recipe_card_rating)
        val recipeRatingAmount: TextView = rootView.findViewById(R.id.text_recipe_detail_amount)
        val recipeCardNutritionTextView: TextView = rootView.findViewById(R.id.text_recipe_card_nutrition)
        val recipeCardCookingTime: TextView = rootView.findViewById(R.id.text_recipe_card_cooking_time)

        // Elements in Scrollview
        //val scrollViewContainer: ScrollView = rootView.findViewById(R.id.nested_scrollview_container)
        val recipeRatingTextView: TextView = rootView.findViewById(R.id.text_recipe_detail_rating)
        val recipeRatingBar: RatingBar = rootView.findViewById(R.id.recipe_detail_rating_bar)
        val recipePreparationTextView: TextView = rootView.findViewById(R.id.text_recipe_preparation)

        // Button
        val buttonExport: Button = rootView.findViewById(R.id.button_export_ingredients_to_shoppinglist)

        //safeargs updates the UI
        val recipe: Recipe = args.recipe
        var nutritionVal: String = recipe.recipeNutrition
        if(nutritionVal.isEmpty()) nutritionVal = " kcal"
        recipeId = recipe.recipeId
        recipeImg = recipe.imageUrl
        Picasso.get().load(recipeImg).into(recipeImageview)
        recipeNameTextView.text = recipe.recipeName
        recipeSubtitleTextView.text = recipe.recipeLabel
        recipeCardNutritionTextView.text = nutritionVal
        recipeCardCookingTime.text = recipe.cookingTime
        recipeCardRating.text = "${recipe.cummulativeRating}"
        recipePreparationTextView.text = recipe.instructions
        recipeRatingTextView.text = "${recipe.cummulativeRating}"
        recipeRatingBar.rating = recipe.cummulativeRating
        recipeRatingAmount.text = "${recipe.amountOfRatings}"

        sharedViewModel.selectedRecipe(recipe)

        buttonExport.setOnClickListener{
            val unavIng = sharedViewModel.notAvailableIngredients
            // Alert Dialog
            val alertBuilder = createAlert(unavIng.value)
            // Show dialog
            alertDialog = alertBuilder.create()
            alertDialog.show()
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Add ingredient list as child fragment
        addChildFragment()
    }

    private fun addChildFragment(){
        // nest child fragment into parent fragment
        // https://developer.android.com/about/versions/android-4.2#NestedFragments
        val ingredientListFragment = RecipeIngredientListFragment()

        childFragmentManager.beginTransaction().apply {
            add(R.id.ingredient_list_fragment, ingredientListFragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun createAlert(list: List<Any>?): AlertDialog.Builder {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val amount = list?.size ?: 0

        // Use custom view
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_layout, null)
        builder.setView(dialogView)
        // views
        val editText = dialogView.findViewById<View>(R.id.dialog_alert_text) as TextView
        val okButton = dialogView.findViewById<View>(R.id.dialog_button_export) as Button
        val cancelButton = dialogView.findViewById<View>(R.id.dialog_button_cancel) as Button

        editText.text = "Want to add $amount ingredients to your shopping list?"
        // confirm and cancel button
        builder.setCancelable(true)

        okButton.setOnClickListener {
            // Export the products to shoppinglist
            sharedViewModel.exportUnavailableIngredientsToShoppingList()
            alertDialog.cancel()
        }

        cancelButton.setOnClickListener {
            // Cancel export
            alertDialog.cancel()
        }

        return builder
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedViewModel.saveCurrentShoppingState()
    }

    companion object {
        const val TAG = "RecipeDetailFragment"
        fun newInstance() = RecipeDetailFragment()
    }

}