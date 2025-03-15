package com.example.gooverapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gooverapplication.databinding.FragmentTodoBinding

class TodoFragment : Fragment() {
    lateinit var binding: FragmentTodoBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val currentDate = dbHelper.getCurrentDate()
        val itemList = dbHelper.getAllDataForDate(currentDate)

        // 카테고리별로 아이템을 그룹화하기 위한 맵 생성
        val groupedMap = mutableMapOf<String, MutableList<RecyclerInViewModel>>()

        // 각 MyData 아이템을 반복하면서 RecyclerInViewModel 아이템을 맵에 추가
        for (myData in itemList) {
            val category = myData.category
            val recyclerItem = RecyclerInViewModel(
                myData.category,
                myData.title,
                myData.memo,
                myData.cycle,
                myData.date,
                myData.answer
            )

            // 맵에 RecyclerInViewModel 아이템 추가
            if (groupedMap.containsKey(category)) {
                // 이미 맵에 해당 카테고리가 있는 경우 항목을 추가
                groupedMap[category]?.add(recyclerItem)
            } else {
                // 맵에 해당 카테고리가 없는 경우 새로운 목록 생성
                groupedMap[category] = mutableListOf(recyclerItem)
            }
        }

        // RecyclerOutViewModel 아이템을 보관할 목록 생성
        val recyclerOutList = mutableListOf<RecyclerOutViewModel>()

        // 그룹화된 맵을 반복하면서 RecyclerOutViewModel 아이템 생성
        for ((category, innerList) in groupedMap) {
            recyclerOutList.add(RecyclerOutViewModel(category, innerList))
        }

        // 생성된 목록을 사용하여 어댑터 설정
        val adapter = TodoOutAdapter(requireContext(), recyclerOutList)
        binding.OuterRecyclerView.adapter = adapter
        binding.OuterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    companion object {
    }
}