package com.mobilesystems.feedme.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.FragmentSearchListItemBinding
import com.mobilesystems.feedme.domain.model.Product

class SearchResultAdapter (
    private val context: Context?,
    private val dataSet: List<Any>,
    private val itemClickListener: SearchResultAdapterClickListener
    ) : RecyclerView.Adapter<SearchResultAdapter.SearchListViewHolder>() {

    //view binding
    private var _itemBinding: FragmentSearchListItemBinding? = null
    private val itemBinding get() = _itemBinding!!

        inner class SearchListViewHolder(itemBinding: FragmentSearchListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

            val searchListItemName: TextView = itemBinding.searchTitle
            val searchListLocation: TextView = itemBinding.searchLocation

        }


        interface SearchResultAdapterClickListener {
            fun passData(product: Product, itemView: View)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultAdapter.SearchListViewHolder {
        // Create a view which defines the UI of the list item
        _itemBinding = FragmentSearchListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchListViewHolder(itemBinding)
    }

        override fun onBindViewHolder(searchListViewHolder: SearchResultAdapter.SearchListViewHolder, position: Int) {
            if(dataSet.isNotEmpty()){
                searchListViewHolder.searchListItemName.text
            }

        }

        override fun getItemCount(): Int {
            return dataSet.size ?: 0
        }

}