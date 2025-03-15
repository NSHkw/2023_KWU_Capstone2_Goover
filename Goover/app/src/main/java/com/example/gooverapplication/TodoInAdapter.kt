package com.example.gooverapplication

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ItemRecyclerviewHometodoInnerBinding

class TodoInAdapter(context: Context, val itemList: MutableList<RecyclerInViewModel>) :
    RecyclerView.Adapter<TodoInAdapter.Holder>() {
    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecyclerviewHometodoInnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.clickwrong.setOnClickListener {  }
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class Holder(var binding: ItemRecyclerviewHometodoInnerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.clickwrong.setOnClickListener{
                val position = adapterPosition

                if(position != RecyclerView.NO_POSITION){
                    val selectedItem = itemList[position]

                    // Create an Intent to start a new activity (replace YourNewActivity::class.java with your actual activity)
                    val intent = Intent(binding.root.context, DetailWrongActivity::class.java)

                    // Pass relevant data to the new activity using Intent extras
                    intent.putExtra("category", selectedItem.category)
                    intent.putExtra("title", selectedItem.title)
                    intent.putExtra("memo", selectedItem.memo)
                    intent.putExtra("cycle", selectedItem.cycle)
                    intent.putExtra("date", selectedItem.date)
                    intent.putExtra("answer", selectedItem.answer)

                    // Start the new activity
                    binding.root.context.startActivity(intent)
                }
            }
        }

        fun bind(item: RecyclerInViewModel) {
            binding.model = item
        }
    }
}
class TodoDecoration(val context: Context): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 0, 0)
        ViewCompat.setElevation(view, 5.0f)
        view.clipToOutline = true

    }
}