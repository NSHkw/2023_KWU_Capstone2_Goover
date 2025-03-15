package com.example.gooverapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ItemRecyclerviewCalendarOuterBinding

class CalendarOutAdapter(val context: Context, val itemList: MutableList<RecyclerOutViewModel>)
    : RecyclerView.Adapter<CalendarOutAdapter.ViewHolder>() {
    lateinit var detailLayout: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecyclerviewCalendarOuterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        detailLayout = holder.binding.innerRecyclerView
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(var binding: ItemRecyclerviewCalendarOuterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecyclerOutViewModel) {
            binding.model = item
            binding.innerRecyclerView.adapter = CalendarInAdapter(context, item.innerList)
            binding.innerRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.innerRecyclerView.addItemDecoration(CalendarDecoration(context))
        }
    }
}