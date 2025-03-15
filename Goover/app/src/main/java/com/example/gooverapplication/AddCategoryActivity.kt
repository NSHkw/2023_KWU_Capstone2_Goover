package com.example.gooverapplication

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gooverapplication.databinding.ActivityAddCategoryBinding
import com.example.gooverapplication.databinding.ItemRecyclerviewCategoryBinding

class AddCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding
    private var categoryList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = DatabaseHelper(this)
        categoryList.addAll(dbHelper.getAllCategories())

        binding.backButton.setOnClickListener { finish() }
        binding.cancelButton.setOnClickListener { finish() }

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.CategoryRecyclerView.layoutManager = layoutManager
        binding.CategoryRecyclerView.addItemDecoration(CategoryDecoration(this))

        val adapter = CategoryAdapter(categoryList) { position ->
            deleteTask(position)
        }

        binding.CategoryRecyclerView.adapter = adapter

        binding.categoryRegister.setOnClickListener {
            val text = binding.categoryText.text.toString()
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 올리고 내리고 관리
            // 문자열 비어있는지 검사
            // 비었을때 : else, 안비었을때 : if
            if(!TextUtils.isEmpty(text))
            {
                // 리스트에 추가 => 리스트는 리사이클러 뷰에 등록되어있고, 리스트 요소들이 화면에 표시됩니다
                categoryList.add(text)
                binding.CategoryRecyclerView.adapter?.notifyDataSetChanged()// 리스트가 업데이트 됐음을 알려줌

                dbHelper.insertCategory(text) //categoryTable에 category 추가한다
                imm.hideSoftInputFromWindow(binding.categoryText.windowToken, 0)// 키보드 내리기
                binding.categoryText.setText(null)// EditText 초기화
            }
            else{
                Toast.makeText(this, "카테고리 제목을 입력해 주세요!", Toast.LENGTH_SHORT).show()
                binding.categoryText.requestFocus()

                imm.showSoftInput(binding.categoryText, 0)// 키보드 올리기
            }
        }
    }

    fun deleteTask(position: Int) {
        val dbHelper = DatabaseHelper(this)
        val deletedCategory = categoryList[position]

        val confirmDeleteDialog = ConfirmDeleteFragment(deletedCategory) {
            //삭제에 대한 사용자의 확인
            dbHelper.deleteCategory(deletedCategory)
            categoryList.removeAt(position)
            binding.CategoryRecyclerView.adapter?.notifyItemRemoved(position)
        }
        // 창모드 보여주기
        confirmDeleteDialog.show(supportFragmentManager, "confirmDeleteDialog")
    }
}

class CategoryViewHolder(val binding: ItemRecyclerviewCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.trashcanIcon.setOnClickListener {
            val position = adapterPosition
            (binding.root.context as? AddCategoryActivity)?.deleteTask(position)
        }
    }
}

class CategoryAdapter(private val datas: MutableList<String>, val onClickDeleteIcon: (position: Int) -> Unit)
    : RecyclerView.Adapter<CategoryViewHolder>() {

    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val viewHolder = CategoryViewHolder(
            ItemRecyclerviewCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
        viewHolder.binding.trashcanIcon.setOnClickListener {
            val position = viewHolder.adapterPosition
            onClickDeleteIcon(position)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val listposition = datas[position]
        holder.binding.categoryTv.text = listposition
    }
}

class CategoryDecoration(val context: Context) : RecyclerView.ItemDecoration() {
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
