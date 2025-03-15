package com.example.gooverapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ItemRecyclerviewHometodoOuterBinding

class HomeTodoOutAdapter(val context: Context, val itemList: MutableList<RecyclerOutViewModel>)
    : RecyclerView.Adapter<HomeTodoOutAdapter.Holder>()
{
    lateinit var detailLayout: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecyclerviewHometodoOuterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        detailLayout = holder.binding.innerRecyclerView
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class Holder(var binding: ItemRecyclerviewHometodoOuterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecyclerOutViewModel) {
            binding.model = item
            binding.innerRecyclerView.adapter = HomeTodoInAdapter(context, item.innerList)
            binding.innerRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.innerRecyclerView.addItemDecoration(HomeTodoDecoration(context))
        }
    }
}