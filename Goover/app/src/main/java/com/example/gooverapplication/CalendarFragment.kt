package com.example.gooverapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gooverapplication.databinding.FragmentCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun extractFormattedDate(calendarDay: CalendarDay): String {
    val date: Date = calendarDay.date
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(date)
}

class CalendarFragment : Fragment() {
    lateinit var binding: FragmentCalendarBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())
        setupRecyclerView()
    }

    fun setupRecyclerView(){
        val selectedData = dbHelper.getDataForDate(extractFormattedDate(selectedDate))

        val groupedMap = mutableMapOf<String, MutableList<RecyclerInViewModel>>()

        for (myData in selectedData) {
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

        val recyclerOutList = mutableListOf<RecyclerOutViewModel>()

        for ((category, innerList) in groupedMap) {
            recyclerOutList.add(RecyclerOutViewModel(category, innerList))
        }

        val adapter = CalendarOutAdapter(requireContext(), recyclerOutList)
        binding.OuterRecyclerView.adapter = adapter
        binding.OuterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCalendarBinding.inflate(inflater)
        val calendarView = binding.calendarView
        calendarView.setTitleFormatter(object : TitleFormatter {
            override fun format(day: CalendarDay?): CharSequence {
                return "${day!!.year}년  ${day.month+1}월"
            }
        })

        val dbHelper = DatabaseHelper(requireContext())
        val dates = dbHelper.getAllDistinctDates()
        val selectedDates = mutableListOf<CalendarDay>()

        for (date in dates) {
            val trimmedDate = date.trim()
            try {
                val year = trimmedDate.substring(0, 4).toInt()
                val month = trimmedDate.substring(5, 7).toInt()
                val day = trimmedDate.substring(8, 10).toInt()

                val calendarDay = CalendarDay.from(year, month-1, day)
                selectedDates.add(calendarDay)
            } catch (e: NumberFormatException) {
                Log.e("CalendarFragment", "NumberFormatException: $e, Trimmed Date: $trimmedDate")
            }
        }

        Log.d("CalendarFragment", "Selected Dates: $selectedDates")

        val TodayDecorator = TodayDecorator(mainActivity)
        calendarView.setOnDateChangedListener(object: OnDateSelectedListener{
            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {
                selectedDate = calendarView.selectedDate
                setupRecyclerView()
                Log.d("this", "선택된 날짜: $selectedDate")

                val selectedMonth = selectedDate.month + 1
                val selectedDay = selectedDate.day
                binding.calendarTodo.text = "$selectedMonth 월 $selectedDay 일에 할 일"
            }

        })
        calendarView.addDecorators(
            TodayDecorator,
            SunDecorator(),
            SatDecorator(),
            EventDecorator(selectedDates, requireContext())
        )
        return binding.root
    }
    companion object {
    }
}

/* 오늘 날짜의 background를 설정하는 클래스 */
class TodayDecorator(context: Context): DayViewDecorator {
    private val drawable = ContextCompat.getDrawable(context,R.drawable.calendar_circle)
    private var date = CalendarDay.today()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!!
    }
    override fun decorate(view: DayViewFacade?) {
        view?.setBackgroundDrawable(drawable!!)
    }
}

// 토요일 데코레이터
class SatDecorator: DayViewDecorator {
    private val calendar = Calendar.getInstance()

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay = calendar[Calendar.DAY_OF_WEEK]
        return weekDay == Calendar.SATURDAY
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.BLUE))
    }
}
//일요일 데코레이터
class SunDecorator: DayViewDecorator {
    private val calendar = Calendar.getInstance()

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay = calendar[Calendar.DAY_OF_WEEK]
        return weekDay == Calendar.SUNDAY
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.RED))
    }
}

class EventDecorator(private val dates: MutableList<CalendarDay>, private val context: Context): DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(dayView: DayViewFacade?) {
        dayView?.addSpan(DotSpan(10F, Color.parseColor("#ED00CE")))
    }
}
