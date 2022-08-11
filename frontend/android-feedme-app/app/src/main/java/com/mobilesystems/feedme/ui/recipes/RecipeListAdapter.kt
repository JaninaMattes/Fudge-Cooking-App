package com.mobilesystems.feedme.ui.recipes

import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.mobilesystems.feedme.databinding.RecipeItemBinding
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.ui.common.utils.abbreviateString
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Tutorial: https://developer.android.com/guide/topics/ui/layout/recyclerview
 *          https://developersbreach.com/navigation-with-architecture-components-android/
 */
class RecipeListAdapter(
    private val dataSet: List<Recipe>?,
    private val itemClickListener: RecipeAdapterClickListener
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    //view binding
    private var _itemBinding: RecipeItemBinding? = null
    private val itemBinding get() = _itemBinding!!

    interface RecipeAdapterClickListener {
        fun passData(recipe: Recipe, itemView: View)
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    inner class RecipeViewHolder(itemBinding: RecipeItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        val cardView: CardView = itemBinding.recipeCardView
        val recipeImageView: CircleImageView = itemBinding.recipeImage
        val recipeLabel: TextView = itemBinding.recipeLabel
        val recipeName: TextView = itemBinding.recipeName
        val ratingBar: RatingBar = itemBinding.ratingBar
        val recipeRating: TextView = itemBinding.textRating
        val cookingDifficulty: TextView = itemBinding.cookingDifficulty

        init {
            // initialize clicklistener and pass clicked product for listitem position
            cardView.setOnClickListener{ v ->
                if (dataSet != null) {
                    itemClickListener.passData(dataSet[bindingAdapterPosition], v)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        // Create a view which defines the UI of the list item
         _itemBinding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: RecipeViewHolder, position: Int) {
        if (dataSet != null) {
            try {
                // get selected product
                val currentItem = dataSet[position]
                // center crop and fit images
                var imgUrl = currentItem.imageUrl
                if (imgUrl.isEmpty()) {
                    imgUrl =
                        "https://cdn5.vectorstock.com/i/1000x1000/07/09/chef-hat-sign-icon-cooking-symbol-vector-3750709.jpg"
                }
                Picasso.get().load(imgUrl).fit().centerCrop().into(viewHolder.recipeImageView)
                viewHolder.recipeName.text = abbreviateString(currentItem.recipeName, 30)
                viewHolder.recipeLabel.text = abbreviateString(currentItem.recipeLabel, 20)
                viewHolder.cookingDifficulty.text = currentItem.difficulty
                viewHolder.ratingBar.rating = currentItem.cummulativeRating
                viewHolder.recipeRating.text = "${currentItem.cummulativeRating}"
            }catch (e: Exception){
                Log.d("Recipe", "Error occured $e")
                e.stackTrace
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSet?.size ?: 0
    }
    
}