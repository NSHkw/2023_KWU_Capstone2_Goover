package com.example.gooverapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.gooverapplication.databinding.ActivityDetailWrongBinding

class DetailWrongActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailWrongBinding
    private lateinit var dbHelper: DatabaseHelper
    private var imagePathsUri: List<Uri> = listOf()

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up View Binding
        binding = ActivityDetailWrongBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val category = intent.getStringExtra("category")
        val title = intent.getStringExtra("title")
        val memo = intent.getStringExtra("memo")
        val cycle = intent.getStringExtra("cycle")
        val date = intent.getStringExtra("date")
        val answer = intent.getStringExtra("answer")

        binding.categoryVeiw.text = "Category: $category"
        binding.titleVeiw.text = "Title: $title"
        binding.memoVeiw.text = "Memo: $memo"
        binding.cycleVeiw.text = "Cycle: $cycle"
        binding.dateVeiw.text = "Date: $date"
        binding.answer.text = "Answer: $answer"


        binding.submitAnswer.setOnClickListener {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 올리고 내리고 관리
            val inputAnswer :String? = binding.answerInput.text.toString()
            if(!TextUtils.isEmpty(inputAnswer))
            {
                if(inputAnswer == answer){
                    Toast.makeText(this, "정답입니다!!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else{
                    Toast.makeText(this, "오답입니다", Toast.LENGTH_SHORT).show()
                    binding.seeAnswer.visibility = View.VISIBLE
                }
            }
            else
            {
                Toast.makeText(this, "정답을 입력해 주세요", Toast.LENGTH_SHORT).show()
                binding.answerInput.requestFocus()
                imm.showSoftInput(binding.answerInput, 0)// 키보드 올리기
            }
        }
        binding.seeAnswer.setOnClickListener {
            binding.answer.visibility = View.VISIBLE
            binding.answerInput.setText(answer)
        }

        dbHelper = DatabaseHelper(this)

        val entryId = dbHelper.getEntryId(category, title, memo, cycle, date, answer)

        val viewPager = binding.viewPager

        fun mainViewChangeEvent(){
            viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    //content_text.text = textList[position]
                }
            })
        }
        fun mainInitViewPager2(){
            viewPager.apply {
                clipToPadding= false
                clipChildren= false
                offscreenPageLimit = 1
                adapter = ViewPager2Adapter(context, imagePathsUri)
            }
            viewPager.setPageTransformer(MarginPageTransformer(25))
            mainViewChangeEvent()
        }

        if (entryId != null) {
            val imagePathsString = dbHelper.getImagePaths(entryId)
            imagePathsUri = imagePathsString.map {it.toUri()}
            viewPager.setPadding(50,100,50,100)
            mainInitViewPager2()
        }
    }
}