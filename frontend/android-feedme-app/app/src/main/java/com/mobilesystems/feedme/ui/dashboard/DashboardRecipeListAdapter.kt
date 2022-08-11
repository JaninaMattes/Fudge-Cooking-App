package com.mobilesystems.feedme.ui.dashboard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.DashboardNumberOneRecipeItemBinding
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.ui.common.utils.abbreviateString
import com.squareup.picasso.Picasso

class DashboardRecipeListAdapter (
    private val dataSet: List<Recipe>?,
    private val itemClickListener: RecipeAdapterClickListener
) : RecyclerView.Adapter<DashboardRecipeListAdapter.RecipeListViewHolder>() {

    private lateinit var imageUrl: String

    //view binding
    private var _itemBinding: DashboardNumberOneRecipeItemBinding? = null
    private val itemBinding get() = _itemBinding!!

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class RecipeListViewHolder(itemBinding: DashboardNumberOneRecipeItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        val cardView: CardView = itemBinding.noOneRecipeItemCardView
        val recipeImageView: ImageView
        val recipeTypeLabelTextView: TextView
        val recipeNameTextView: TextView
        val recipeRatingBar: RatingBar
        val recipeCookingDifficulty: TextView

        init {
            // bind views by view id
            recipeImageView = itemBinding.noOneRecipeImage
            recipeTypeLabelTextView = itemBinding.noOneRecipeLabel
            recipeNameTextView = itemBinding.noOneRecipeName
            recipeRatingBar = itemBinding.noOneRecipeRatingBar
            recipeCookingDifficulty = itemBinding.noOneRecipeCookingDifficulty

            // initialize clicklistener and pass clicked product for listitem position
            cardView.setOnClickListener{ v ->
                if (dataSet != null) {
                    itemClickListener.passData(dataSet[bindingAdapterPosition], v)
                }
            }
        }
    }

    interface RecipeAdapterClickListener {
        fun passData(recipe: Recipe, itemView: View)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeListViewHolder {
        // Create a view which defines the UI of the list item
        _itemBinding = DashboardNumberOneRecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(recipeViewHolder: RecipeListViewHolder, position: Int) {
        if (dataSet != null) {
            // get selected product
            val currentItem = dataSet[position]
            // pass values to view items
            imageUrl = currentItem.imageUrl
            if(imageUrl.isNotEmpty()) {
                // fit image
                Picasso.get().load(imageUrl).fit().centerCrop().into(recipeViewHolder.recipeImageView)
            }
            recipeViewHolder.recipeCookingDifficulty.text = currentItem.difficulty
            recipeViewHolder.recipeRatingBar.rating = currentItem.cummulativeRating
            recipeViewHolder.recipeNameTextView.text = abbreviateString(currentItem.recipeName, 35)
            recipeViewHolder.recipeTypeLabelTextView.text = abbreviateString(currentItem.recipeLabel, 20)
        }
    }

    override fun getItemCount(): Int {
        return dataSet?.size ?: 0
    }
}