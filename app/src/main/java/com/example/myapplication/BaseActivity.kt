package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {

    private val TAG: String = javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

}
