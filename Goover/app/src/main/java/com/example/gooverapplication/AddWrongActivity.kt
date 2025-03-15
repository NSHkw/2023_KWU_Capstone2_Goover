package com.example.gooverapplication

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ActivityAddWrongBinding
import com.example.gooverapplication.databinding.ItemRecyclerviewBinding
import gun0912.tedimagepicker.builder.TedImagePicker
import java.text.SimpleDateFormat
import java.util.Locale

class AddWrongActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddWrongBinding
    private lateinit var MutableUriList:MutableList<Uri>
    private var scannedUri:Uri? = null
    private lateinit var startDate: Calendar
    val dbHelper = DatabaseHelper(this)
    var dateString = ""
    val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result?.data != null) {
            val str = result.data?.getStringExtra("str")
            val returnPosition = result.data?.getIntExtra("returnPosition", 0)
            if (str != null) {
                scannedUri = str.toUri()
                scannedUri?.let {
                    MutableUriList[returnPosition!!] = it
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                }
            } else {
                Log.e("scanned", "str is null")
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddWrongBinding.inflate(layoutInflater)

        // 뒤로가기 & 취소 버튼
        binding.backButton.setOnClickListener { finish() }
        binding.cancelButton.setOnClickListener { finish() }

        //카테고리 테이블 가져와 spinner로 출력
        val spinner = binding.spinner
        val categoryList = dbHelper.getAllCategories()
        val adapter: ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.spinner_layout, categoryList)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        //주기 테이블 가져와 spinner로 출력
        val CycleList = dbHelper.getAllCycle()
        val CycleSpinner = binding.cycleSpinner
        val CycleAdapter: ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.spinner_layout, CycleList)

        CycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        CycleSpinner.adapter = CycleAdapter

        //Date Picker
        val date = binding.date
        date.setOnClickListener {
            val cal = Calendar.getInstance() //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                dateString = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                date.text = dateString
                date.setTextColor(ContextCompat.getColor(applicationContext!!, R.color.black))
                startDate = intToCalendar(year, month, dayOfMonth)
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(
                Calendar.DAY_OF_MONTH)).show()
        }

        // Add Image
        binding.addImage.setOnClickListener{
            TedImagePicker.with(this).max(5,"문제당 최대 5장 까지 선택 할 수 있습니다")
                .startMultiImage { uriList ->
                    MutableUriList = uriList.toMutableList()
                    var uriListSize : Int = MutableUriList.size
                    binding.uriListSize.setTextColor(ContextCompat.getColor(applicationContext!!,R.color.mainColor1))
                    binding.uriListSize.setText("$uriListSize")
                    val adapter = MyAdapter(MutableUriList,
                        onClickDeleteIcon = {deleteTask(it)},
                        onClickImage = {listposition, position -> showImage(listposition, position) })
                    val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

                    binding.recyclerView.layoutManager = layoutManager
                    binding.recyclerView.adapter = adapter
                    binding.recyclerView.addItemDecoration(MyDecoration(this))
                }
        }

        // Add or Change
        val isManage :Boolean? = intent.getBooleanExtra("isManageExtra", false)
        if(isManage!!)
        {
            binding.btnOk.setText("변경")
            binding.chuga.setText("틀린문제 수정")
        }

        binding.btnOk.setOnClickListener {
            val category = binding.spinner.selectedItem.toString()
            val cycle = binding.cycleSpinner.selectedItem.toString()
            val title = binding.titleEditText.text.toString()
            val memo = binding.memoEditText.text.toString()
            val answer = binding.answer.text.toString()
            val date = dateString
            val isChecked = binding.bookmark.isChecked
            var checkInt: Int
            if(isChecked ==true){
                checkInt=1
            } else{
                checkInt=0
            }

            // startDate:Calendar -> DatesList:List<String>로 만드는 함수 필요
            // cycleList:List<Int>를 만들고
            // fun addDays
            // cycleList:List<Int>를 순회 하면서, startDate:Calendar에 .add() 적용 => 결과는 Calendar type
            // 결과로 나온 Calendar 객체를 "%d-%02d-%02d" 형식에 맞추어서 String type으로 변환해서 list에 추가
            // list를 반환
            val gooverDates:List<String> = getGooverDates(startDate, cycle)
            Log.d("gooverDates", "$gooverDates")

            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (!TextUtils.isEmpty(title)) {
                // MyTable에 항목 삽입하고 entryId를 가져옴
                val entryId = dbHelper.insertMyTable(category, title, memo, cycle, date, answer, checkInt)
                val secondIds = dbHelper.insertSecondTable(category, title, memo, cycle, date, answer, checkInt)
                for (secondId in secondIds) {
                    // Do something with each secondId
                    Log.d("SecondId", "$secondId")
                }
                Log.d("entry", "$entryId 출력")

                for (uri in MutableUriList) {
                    dbHelper.insertImage(entryId, uri.toString())
                    for (secondId in secondIds) {
                        dbHelper.insertImage2(listOf(secondId), uri.toString())
                        Log.d("이미지 크기", "내부 for 문이 실행됨 - entryId: $entryId, secondId: $secondId, uri: $uri")
                    }
                }

                binding.recyclerView.adapter?.notifyDataSetChanged() // 리스트가 업데이트 됐음을 알림
                imm.hideSoftInputFromWindow(binding.titleEditText.windowToken, 0)
                Toast.makeText(this, "데이터가 성공적으로 추가되었습니다!", Toast.LENGTH_SHORT).show()
                val intent = intent
                setResult(RESULT_OK, intent)
                if(!isFinishing) finish()
            } else {
                Toast.makeText(this, "제목을 입력해 주세요!", Toast.LENGTH_SHORT).show()
                binding.titleEditText.requestFocus()
                imm.showSoftInput(binding.titleEditText, 0)
            }
            MutableUriList.clear()
        }
        setContentView(binding.root)
    }

    fun getGooverDates(startDate: Calendar, cycleString: String):List<String>{
        val cycleList = cycleToList(cycleString)
        val gooverDates = addDays(cycleList, startDate)
        return gooverDates
    }
    fun addDays(cycleList: List<Int>, startDate: Calendar): List<String> {
        val resultDates = mutableListOf<String>()

        for (cycle in cycleList) {
            val clonedCalendar = startDate.clone() as Calendar // 기존 Calendar 객체를 변경하지 않기 위해 복제

            // 주어진 cycle을 더한 날짜를 얻음
            clonedCalendar.add(Calendar.DAY_OF_YEAR, cycle)

            // SimpleDateFormat을 사용하여 "%d-%02d-%02d" 형식에 맞게 문자열로 변환
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(clonedCalendar.time)

            // 결과 리스트에 추가
            resultDates.add(formattedDate)
        }
        return resultDates
    }
    fun cycleToList(cycleString:String):List<Int>{
        return cycleString.split(" ").map { it.toInt() }
    }
    fun intToCalendar(year:Int, month:Int, day:Int) : Calendar{
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.set(year, month, day)
        return selectedCalendar
    }

    fun deleteTask(uri: Uri) {
        MutableUriList.remove(uri)
        var size = MutableUriList.size
        binding.recyclerView.adapter?.notifyDataSetChanged()
        binding.uriListSize.setText("$size")
    }

    //Start ImageViewer
    fun showImage(uri: Uri, position: Int){
        val intent = Intent(this, ImageViewerActivity::class.java)
        val stringUri:String = uri.toString()
        intent.putExtra("scannedUri", scannedUri)
        intent.putExtra("data1", stringUri)
        intent.putExtra("position", position)

        activityResultLauncher.launch(intent)
    }
}
// MyViewHolder(), RecyclerView.ViewHolder 상속
// MyViewHolder = ItemRecyvlerView의 root인 linearLayout
class MyViewHolder(val binding11: ItemRecyclerviewBinding) : RecyclerView.ViewHolder(binding11.root)

// URI 리스트를 매개변수로 받고, RecyclerView.Adapter를 상속
// RecyclerView.Adapter는 RecyclerView.ViewHolder 사용
class MyAdapter(val datas: MutableList<Uri>, val onClickDeleteIcon:(uri:Uri) -> Unit,val onClickImage:(uri:Uri, position:Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // 리스트의 사이즈 return
    override fun getItemCount(): Int {
        return datas.size;
    }
    //ViewHolder 생성, binding 객체 생성해서 매개변수로 넘겨줌  (XML inflate(팽창) - > 바인딩 객체)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(
            ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent,
                false))

    // ViewHolder에 데이터 연결 및 표시
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listposition = datas[position]
        val binding11 = (holder as MyViewHolder).binding11
        binding11.recyclerViewItem.setImageURI(datas[position])
        // 항목 삭제 리스너
        binding11.fabDelete.setOnClickListener {
            onClickDeleteIcon.invoke(listposition)
        }
        // 선택된 이미지 뷰 리스너
        binding11.recyclerViewItem.setOnClickListener{
            onClickImage.invoke(listposition, position)
        }
    }
}

class MyDecoration(val context: Context): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 24, 0)
        ViewCompat.setElevation(view, 10.0f)
        view.clipToOutline = true
    }
}