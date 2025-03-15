package com.example.gooverapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.FragmentMoreBinding
import com.example.gooverapplication.databinding.ItemRecyclerviewMoreBinding
import com.example.gooverapplication.databinding.ItemRecyclerviewMoreOnoffBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMoreBinding.inflate(inflater)
        val rc_more = binding.rcMore
        val itemList = ArrayList<MoreItem>()

        itemList.add(MoreItem(R.drawable.showmore_icon_24, "Goover 사용 가이드"))
        itemList.add(MoreItem(R.drawable.showmore_icon_24, "카테고리 관리"))
        itemList.add(MoreItem(R.drawable.showmore_icon_24, "복습주기 관리"))
        itemList.add(MoreItem(R.drawable.showmore_icon_24, "틀린문제 관리"))
        val moreAdapter = MoreAdapter(itemList)
        moreAdapter.notifyDataSetChanged()

        rc_more.adapter = moreAdapter
        rc_more.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        moreAdapter.itemClickListener = object : MoreAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                if(position == 0)
                {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/ssoonnwwoo/2023_KWU_Capstone2_Goover/blob/master/README.md#goover-%EC%82%AC%EC%9A%A9-%EA%B0%80%EC%9D%B4%EB%93%9C")
                    )
                    startActivity(browserIntent)
                }
                else if(position == 1)
                {
                    val intent = Intent(context, AddCategoryActivity::class.java)
                    startActivity(intent)
                }
                else if(position == 2)
                {
                    val intent = Intent(context, AddCycleActivity::class.java)
                    startActivity(intent)
                }
                else
                {
                    val isManage :Boolean? = true
                    val intent = Intent(context, AddWrongActivity::class.java)
                    intent.putExtra("isManageExtra", isManage)
                    startActivity(intent)
                    //틀린 문제 관련 코드 추가 필요
                }
            }
        }

        val rc_more_onoff = binding.rcMoreOnoff
        val itemList_onoff = ArrayList<MoreItemOnOff>()
        itemList_onoff.add(MoreItemOnOff(false, "복습 일정 알림"))
        val moreAdapterOnOff = MoreAdapterOnOff(itemList_onoff)
        moreAdapterOnOff.notifyDataSetChanged()

        rc_more_onoff.adapter = moreAdapterOnOff
        rc_more_onoff.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)


        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MoreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MoreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


data class MoreItem(val icon: Int, val content: String)
data class MoreItemOnOff(val onoff : Boolean, val content: String)
class MoreViewHolder(val binding11: ItemRecyclerviewMoreBinding) : RecyclerView.ViewHolder(binding11.root)
class MoreViewHolderOnOff(val binding12: ItemRecyclerviewMoreOnoffBinding) : RecyclerView.ViewHolder(binding12.root)
class MoreAdapter(val datas: MutableList<MoreItem>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    interface OnItemClickListener {
        fun onItemClick(position: Int) {}
    }

    var itemClickListener: OnItemClickListener? = null

    // 리스트의 사이즈 return
    override fun getItemCount(): Int {
        return datas.size;
    }
    //ViewHolder 생성
    // binding 객체 생성해서 매개변수로 넘겨줌  (XML inflate(팽창) - > 바인딩 객체)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MoreViewHolder(
            ItemRecyclerviewMoreBinding.inflate(LayoutInflater.from(parent.context), parent,
                false))

    // ViewHolder에 데이터 연결 및 표시
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listposition = datas[position]
        val binding11 = (holder as MoreViewHolder).binding11
        holder.binding11.recyclerViewImageItem.setImageResource(datas[position].icon)
        holder.binding11.tvContent.setText(datas[position].content)
        holder.binding11.root.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }

    }

}

class MoreAdapterOnOff(val datas: MutableList<MoreItemOnOff>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 리스트의 사이즈 return
    override fun getItemCount(): Int {
        return datas.size;
    }

    //ViewHolder 생성
    // binding 객체 생성해서 매개변수로 넘겨줌  (XML inflate(팽창) - > 바인딩 객체)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MoreViewHolderOnOff(
            ItemRecyclerviewMoreOnoffBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )
        )

    // ViewHolder에 데이터 연결 및 표시
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listposition = datas[position]
        val binding12 = (holder as MoreViewHolderOnOff).binding12
        //holder.binding12.pushAlarmOnoff.
        holder.binding12.tvContent.setText(datas[position].content)
    }
}
