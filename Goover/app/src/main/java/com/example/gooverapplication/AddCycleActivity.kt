package com.example.gooverapplication

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ActivityAddCycleBinding
import com.example.gooverapplication.databinding.ItemRecyclerviewCategoryBinding

class AddCycleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCycleBinding
    private var CycleList:MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddCycleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = DatabaseHelper(this)
        CycleList.addAll(dbHelper.getAllCycle())

        binding.backButton.setOnClickListener {finish()}
        binding.cancelButton.setOnClickListener {finish()}

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.CycleRecyclerView.layoutManager = layoutManager
        binding.CycleRecyclerView.addItemDecoration(CycleDecoration(this))

        val adapter = CycleAdapter(CycleList) { position ->
            deleteTask(position)
        }

        binding.CycleRecyclerView.adapter = adapter

        // 등록버튼 이벤트 처리 부분
        binding.cycleRegister.setOnClickListener {
            // EditText에서 문자열 가져오기
            val text = binding.cycleText.text.toString().trimEnd()
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 올리고 내리고 관리
            // 문자열 비어있는지 검사
            // 비었을때 : else, 안비었을때 : if
            if(!TextUtils.isEmpty(text))
            {
                if(isNumericWithSpaces(text))
                {
                    // "1 4 7 14" --> ['1', '4', '7', '14']
                    val itemList = text.split(" ").map { it.toInt() }
                    // 중복 제거
                    val distinctItemList = itemList.distinct()
                    // 정렬
                    val sortedList = distinctItemList.sorted()
                    val resultString = sortedList.joinToString(" ")
                    // 리스트에 추가 => 리스트는 리사이클러 뷰에 등록되어있고, 리스트 요소들이 화면에 표시됩니다
                    CycleList.add(resultString)

                    dbHelper.insertCycle(resultString)

                    binding.CycleRecyclerView.adapter?.notifyDataSetChanged()// 리스트가 업데이트 됐음을 알려줌
                    imm.hideSoftInputFromWindow(binding.cycleText.windowToken, 0)// 키보드 내리기
                    binding.cycleText.setText(null)// EditText 초기화
                }
                else{
                    Toast.makeText(this, "복습주기는 숫자와 공백으로 이루어져야 합니다", Toast.LENGTH_SHORT).show()
                }

            }
            else{
                Toast.makeText(this, "복습 주기를 입력해 주세요!", Toast.LENGTH_SHORT).show()
                binding.cycleText.requestFocus()

                imm.showSoftInput(binding.cycleText, 0)// 키보드 올리기
            }
        }
        binding.cycleText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                if(text.isEmpty()){
                    binding.inputResult.visibility = View.INVISIBLE
                }
                else if(isNumericWithSpaces(text))
                {
                    binding.inputResult.setText("잘 입력하셨습니다! 추가 버튼을 눌러주세요 :)")
                    binding.inputResult.setTextColor(Color.BLUE)
                }

                else{
                    binding.inputResult.setText("숫자, 띄어쓰기 이외의 문자가 있습니다!")
                    binding.inputResult.setTextColor(Color.RED)
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }
    fun deleteTask(position: Int) {
        val dbHelper = DatabaseHelper(this)
        dbHelper.deleteCycle(CycleList[position])
        CycleList.removeAt(position)
        binding.CycleRecyclerView.adapter?.notifyItemRemoved(position)
    }


    fun isNumericWithSpaces(text: String): Boolean {
        // 정규 표현식을 사용하여 숫자와 공백으로만 이루어진지 확인
        val regex = Regex("^[0-9]+(\\s[0-9]+)*\\s*$")
        return regex.matches(text)
    }
}

class CycleViewHolder(val binding11: ItemRecyclerviewCategoryBinding) : RecyclerView.ViewHolder(binding11.root)

class CycleAdapter(
    private val datas: MutableList<String>,
    val onClickDeleteIcon: (position: Int) -> Unit
) : RecyclerView.Adapter<CycleViewHolder>(){
    // 리스트의 사이즈 return
    override fun getItemCount(): Int = datas.size
    //ViewHolder 생성
    // binding 객체 생성해서 매개변수로 넘겨줌  (XML inflate(팽창) - > 바인딩 객체)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CycleViewHolder {
        val viewHolder = CycleViewHolder(
            ItemRecyclerviewCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
        viewHolder.binding11.trashcanIcon.setOnClickListener {
            val position = viewHolder.adapterPosition
            onClickDeleteIcon(position)
        }
        return viewHolder
    }

    // ViewHolder에 데이터 연결 및 표시
    override fun onBindViewHolder(holder: CycleViewHolder, position: Int) {
        val listposition = datas[position]
        holder.binding11.categoryTv.text = listposition
        val binding11 = holder.binding11
        binding11.categoryTv.text = "주기: $listposition" // 주기를 문자열로 표시
    }

}

class CycleDecoration(val context: Context): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 0, 0)
        ViewCompat.setElevation(view, 8.0f)
        view.clipToOutline = true

    }
}