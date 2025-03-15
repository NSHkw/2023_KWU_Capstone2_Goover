package com.example.gooverapplication

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ItemRecyclerviewCalendarInnerBinding

class CalendarInAdapter(context: Context, val innerList: MutableList<RecyclerInViewModel>) :
    RecyclerView.Adapter<CalendarInAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecyclerviewCalendarInnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.clickwrong.setOnClickListener {  }
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = innerList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return innerList.size
    }

    inner class Holder(var binding: ItemRecyclerviewCalendarInnerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.clickwrong.setOnClickListener{
                val position = adapterPosition

                if(position != RecyclerView.NO_POSITION){
                    val selectedItem = innerList[position]

                    val intent = Intent(binding.root.context, DetailSecondActivity::class.java)

                    intent.putExtra("category", selectedItem.category)
                    intent.putExtra("title", selectedItem.title)
                    intent.putExtra("memo", selectedItem.memo)
                    intent.putExtra("cycle", selectedItem.cycle)
                    intent.putExtra("date", selectedItem.date)
                    intent.putExtra("answer", selectedItem.answer)

                    binding.root.context.startActivity(intent)
                }
            }
        }

        fun bind(item: RecyclerInViewModel) {
            binding.model = item
        }
    }
}


class CalendarDecoration(val context: Context) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        // Optionally set item offsets if needed
        outRect.set(0, 0, 0, 0)
    }
}