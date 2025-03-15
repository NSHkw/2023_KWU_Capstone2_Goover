package com.example.gooverapplication

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ItemRecyclerviewWrongOuterBinding

class OutRecyclerViewAdapter(val context: Context, val itemList: MutableList<RecyclerOutViewModel>)
    : RecyclerView.Adapter<OutRecyclerViewAdapter.Holder>()
{
    var selectedItems: SparseBooleanArray = SparseBooleanArray()
    var prePosition: Int = -1
    lateinit var detailLayout: RecyclerView
    lateinit var tv: TextView
    lateinit var iv: ImageView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecyclerviewWrongOuterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        detailLayout = holder.binding.innerRecyclerView
        tv = holder.binding.viewWrong
        iv = holder.binding.expandIcon
        val item = itemList[position]
        holder.bind(item)
        holder.binding.expand.setOnClickListener{
            if (selectedItems.get(position)) {
                // 펼쳐진 Item을 클릭 시
                selectedItems.delete(position)
            } else {
                // 직전의 클릭됐던 Item의 클릭상태를 지움
                //selectedItems.delete(prePosition)
                // 클릭한 Item의 position을 저장
                selectedItems.put(position, true)
            }
            if (prePosition != -1) notifyItemChanged(prePosition)
            notifyItemChanged(position)
            prePosition = position
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    inner class Holder(var binding: ItemRecyclerviewWrongOuterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecyclerOutViewModel) {
            binding.model = item
            binding.innerRecyclerView.adapter = InRecyclerViewAdapter(context, item.innerList)
            binding.innerRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.innerRecyclerView.addItemDecoration(WrongDecoration(context))
            changeVisibility(selectedItems.get(position))
            val category = item.category
        }
    }


    private fun changeVisibility(isExpanded: Boolean) {
        detailLayout.setVisibility(if (isExpanded) View.VISIBLE else View.GONE)
        tv.text =  if (isExpanded) "접기" else "펼치기"
        iv.setImageResource(if(isExpanded) R.drawable.shrink_icon_24 else R.drawable.expand_icon_24)
    }

}