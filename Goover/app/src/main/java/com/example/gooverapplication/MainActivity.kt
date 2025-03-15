package com.example.gooverapplication

import android.R.attr
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.gooverapplication.databinding.ActivityMainBinding
import com.example.gooverapplication.databinding.FragmentWrongQuestionBinding


class MainActivity : AppCompatActivity() {
    private var isFabOpen = false // Fab 버튼 default는 닫혀있음
    private lateinit var binding:ActivityMainBinding
    private lateinit var wqbinding: FragmentWrongQuestionBinding
    lateinit var getResult: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        wqbinding = FragmentWrongQuestionBinding.inflate(layoutInflater)
        setFABClickEvent()
        setContentView(binding.root)
        initNavigation()
        binding.activityMain.setOnClickListener {
            isFabOpen.takeIf { it }?.let { toggleFab() }
        }
    }

    private fun setFABClickEvent() {
        binding.addCycle.shrink()
        binding.addWrong.shrink()
        binding.addCategory.shrink()
        // 플로팅 버튼 클릭
        binding.fabMain.setOnClickListener {
            toggleFab()
        }

        // 플로팅 버튼 클릭 이벤트 - 복습주기 추가
        binding.addCycle.setOnClickListener {
            toggleFab()
            val intent = Intent(this, AddCycleActivity::class.java)
            startActivity(intent)

        }

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if(result.resultCode == RESULT_OK){
                refreshCurrentNavigation()
            }
        }
        // 플로팅 버튼 클릭 이벤트 - 문제추가
        binding.addWrong.setOnClickListener {
            toggleFab()
            val intent = Intent(this, AddWrongActivity::class.java)
            getResult.launch(intent)
        }

        // 플로팅 버튼 클릭 이벤트 - 카테고리 추가
        binding.addCategory.setOnClickListener {
            toggleFab()
            val intent = Intent(this, AddCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun toggleFab() {
        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션
        if (isFabOpen) {
            ObjectAnimator.ofFloat(binding.addCycle, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.addWrong, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.addCategory, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.addWrong, "translationX", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.addCycle, "translationX", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.addCategory, "translationX", 0f).apply { start() }
            binding.addCycle.shrink()
            binding.addWrong.shrink()
            binding.addCategory.shrink()
            ObjectAnimator.ofFloat(binding.fabMain, View.ROTATION, 45f, 0f).apply { start() }
        } else { // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션
            ObjectAnimator.ofFloat(binding.addCycle, "translationX", -90f).apply { start() }
            ObjectAnimator.ofFloat(binding.addCycle, "translationY", -360f).apply { start() }
            ObjectAnimator.ofFloat(binding.addWrong, "translationX", -90f).apply { start() }
            ObjectAnimator.ofFloat(binding.addWrong, "translationY", -180f).apply { start() }
            ObjectAnimator.ofFloat(binding.addCategory, "translationX", -90f).apply { start() }
            ObjectAnimator.ofFloat(binding.addCategory, "translationY", -540f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabMain, View.ROTATION, 0f, 45f).apply { start() }
            binding.addCycle.extend()
            binding.addWrong.extend()
            binding.addCategory.extend()
        }

        isFabOpen = !isFabOpen

    }

    fun initNavigation() {
        NavigationUI.setupWithNavController(binding.btNav, findNavController(R.id.nav_host))
    }
    fun refreshCurrentNavigation(){
        val navController = findNavController(R.id.nav_host)
        val id = navController.currentDestination?.id
        Log.d("jsw", "id : $id")
        navController.popBackStack(id!!,true)
        navController.navigate(id)
        Log.d("jsw", "refresh end")
    }
}
