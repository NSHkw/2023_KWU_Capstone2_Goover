package com.example.gooverapplication

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.FragmentHomeBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private lateinit var dbHelper: DatabaseHelper

    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    lateinit var mainActivity: MainActivity
    private var selectedDate = CalendarDay.today()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentHomeBinding.inflate(inflater)
        val calendarView = binding.calendarView
        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader)
        calendarView.setTitleFormatter(object : TitleFormatter {
            override fun format(day: CalendarDay?): CharSequence {
                return "${day!!.year}년  ${day.month+1}월"
            }
        })

        val dbHelper = DatabaseHelper(requireContext())
        val dates = dbHelper.getAllDistinctDates()
        val selectedDates = mutableListOf<CalendarDay>()

        Log.d("this", "datesss $dates")

        for (date in dates) {
            val trimmedDate = date.trim()
            try {
                // 여기서 date 형식이 맞는지 확인하고, 맞지 않으면 수정이 필요합니다.
                val year = trimmedDate.substring(0, 4).toInt()
                val month = trimmedDate.substring(5, 7).toInt()
                val day = trimmedDate.substring(8, 10).toInt()

                val calendarDay = CalendarDay.from(year, month - 1, day)
                selectedDates.add(calendarDay)
            } catch (e: NumberFormatException) {
                Log.e("CalendarFragment", "NumberFormatException: $e, Trimmed Date: $trimmedDate")
            }
        }

        val TodayDecorator = TodayDecorator(mainActivity)
        calendarView.setOnDateChangedListener(object: OnDateSelectedListener {
            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {
                selectedDate = calendarView.selectedDate
                Log.d("selectedDate", selectedDate.toString())
            }

        })
        calendarView.addDecorators(
            TodayDecorator,
            SunDecorator(),
            SatDecorator(),
            EventDecorator(selectedDates, requireContext())
        )

        binding.showmoreCalendar.setOnClickListener{
            val navCont = findNavController()
            navCont.popBackStack()
            navCont.navigate(R.id.calendarFragment)
        }
        binding.showmoreWrong.setOnClickListener{
            val navCont = findNavController()
            navCont.popBackStack()
            navCont.navigate(R.id.wrongQuestionFragment)
        }
        binding.showmoreTodo.setOnClickListener{
            val navCont = findNavController()
            navCont.popBackStack()
            navCont.navigate(R.id.todoFragment)
        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())
        setUpWrongRecyclerView()
        setUpTodoRecyclerView()
    }
    fun getDrawableUri(context: Context, drawableId: Int): Uri {
        // 리소스 ID를 사용하여 URI 생성
        val uri = Uri.parse("android.resource://" + context.packageName + "/" + drawableId)
        return uri
    }
    private fun setUpWrongRecyclerView() {
        // getAllBookmarked 함수를 사용하여 즐겨찾기된 데이터 가져오기
        val bookmarkedDataList = dbHelper.getAllBookmarked()

        // 가져온 데이터를 기반으로 HomeWrongViewModel 항목의 목록 생성
        val itemList = bookmarkedDataList.map { myData ->
            val imagePaths = dbHelper.getImagePaths(myData.id)
            HomeWrongViewModel(
                myData.category, myData.title,
                myData.memo, myData.cycle,
                myData.date, myData.answer, imagePaths)
        }

        val adapter = HomeWrongAdapter(requireContext(), itemList.toList())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.recyclerView.addItemDecoration(HomeWrongDecoration(requireContext()))
    }

    private fun setUpTodoRecyclerView() {
        val currentDate = dbHelper.getCurrentDate()
        Log.d("TodoFragment", "Current Date: $currentDate")
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
        val adapter = HomeTodoOutAdapter(requireContext(), recyclerOutList)
        binding.OuterRecyclerView.adapter = adapter
        binding.OuterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
    companion object {
    }
}

class HomeWrongDecoration(val context: Context): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 32, 0)
        ViewCompat.setElevation(view, 10.0f)
        view.clipToOutline = true
    }
}