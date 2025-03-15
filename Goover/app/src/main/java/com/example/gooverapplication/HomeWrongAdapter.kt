package com.example.gooverapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ItemRecyclerviewHomeWrongBinding

class HomeWrongAdapter(val context: Context, val itemList: List<HomeWrongViewModel>) :
    RecyclerView.Adapter<HomeWrongAdapter.HomeWrongViewHolder>(){
    // 리스트의 사이즈 return
    override fun getItemCount(): Int {
        Log.d("HomeWrong", "데이터 갯수 ${itemList.size}")
        return itemList.size;
    }
    //ViewHolder 생성
    // binding 객체 생성해서 매개변수로 넘겨줌  (XML inflate(팽창) - > 바인딩 객체)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeWrongViewHolder{
        val binding = ItemRecyclerviewHomeWrongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeWrongViewHolder(binding)
    }
    // ViewHolder에 데이터 연결 및 표시
    override fun onBindViewHolder(holder: HomeWrongViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    inner class HomeWrongViewHolder(val binding11: ItemRecyclerviewHomeWrongBinding) :
        RecyclerView.ViewHolder(binding11.root) {
        init {
            binding11.itemRoot.setOnClickListener {
                val position = adapterPosition

                if(position != RecyclerView.NO_POSITION){
                    val selectedItem = itemList[position]

                    val intent = Intent(binding11.root.context, DetailWrongActivity::class.java)

                    intent.putExtra("category", selectedItem.category)
                    intent.putExtra("title", selectedItem.title)
                    intent.putExtra("memo", selectedItem.memo)
                    intent.putExtra("cycle", selectedItem.cycle)
                    intent.putExtra("date", selectedItem.date)
                    intent.putExtra("answer", selectedItem.answer)

                    binding11.root.context.startActivity(intent)
                }
            }
        }

        fun bind(item: HomeWrongViewModel) {
            binding11.model = item
        }

    }
}