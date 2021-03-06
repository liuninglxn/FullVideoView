package com.example.videolibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import java.util.*

/**
 * Created by ln
 * On 2019/04/08 12:25
 */
object UUUtils {
    private const val TAG = "UUvideo"

    var SYSTEM_UI = 0

    fun stringForTime(timeMs: Long): String {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00"
        }
        val totalSeconds = timeMs / 1000
        val seconds = (totalSeconds % 60).toInt()
        val minutes = (totalSeconds / 60 % 60).toInt()
        val hours = (totalSeconds / 3600).toInt()
        val stringBuilder = StringBuilder()
        val mFormatter = Formatter(stringBuilder, Locale.getDefault())
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    /**
     * This method requires the caller to hold the permission ACCESS_NETWORK_STATE.
     * @param context context
     * @return if wifi is connected,return true
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * Get activity from context object
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    fun scanForActivity(context: Context?): Activity? {
        if (context == null) return null

        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            return scanForActivity(context.baseContext)
        }

        return null
    }

    /**
     * Get AppCompatActivity from context
     * @param context context
     * @return AppCompatActivity if it's not null
     */
    fun getAppCompActivity(context: Context?): AppCompatActivity? {
        if (context == null) return null
        if (context is AppCompatActivity) {
            return context
        } else if (context is ContextThemeWrapper) {
            return getAppCompActivity(context.baseContext)
        }
        return null
    }

    fun setRequestedOrientation(context: Context, orientation: Int) {
        if (UUUtils.getAppCompActivity(context) != null) {
            UUUtils.getAppCompActivity(context)!!.requestedOrientation = orientation
        } else {
            UUUtils.scanForActivity(context)!!.requestedOrientation = orientation
        }
    }

    fun getWindow(context: Context): Window {
        return if (UUUtils.getAppCompActivity(context) != null) {
            UUUtils.getAppCompActivity(context)!!.window
        } else {
            UUUtils.scanForActivity(context)!!.window
        }
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun saveProgress(context: Context, url: Any, progress: Long) {
        var progress = progress
        if (!UUvideo.SAVE_PROGRESS) return
        Log.i(TAG, "saveProgress: $progress")
        if (progress < 5000) {
            progress = 0
        }
        val spn = context.getSharedPreferences(
            "JZVD_PROGRESS",
            Context.MODE_PRIVATE
        )
        val editor = spn.edit()
        editor.putLong("newVersion:$url", progress).apply()
    }

    fun getSavedProgress(context: Context, url: Any): Long {
        if (!UUvideo.SAVE_PROGRESS) return 0
        val spn = context.getSharedPreferences(
            "JZVD_PROGRESS",
            Context.MODE_PRIVATE
        )
        return spn.getLong("newVersion:$url", 0)
    }

    /**
     * if url == null, clear all progress
     * @param context context
     * @param url     if url!=null clear this url progress
     */
    fun clearSavedProgress(context: Context, url: Any?) {
        if (url == null) {
            val spn = context.getSharedPreferences(
                "JZVD_PROGRESS",
                Context.MODE_PRIVATE
            )
            spn.edit().clear().apply()
        } else {
            val spn = context.getSharedPreferences(
                "JZVD_PROGRESS",
                Context.MODE_PRIVATE
            )
            spn.edit().putLong("newVersion:$url", 0).apply()
        }
    }

    @SuppressLint("RestrictedApi")
    fun showStatusBar(context: Context) {
        if (UUvideo.TOOL_BAR_EXIST) {
            UUUtils.getWindow(context).clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    //如果是沉浸式的，全屏前就没有状态栏
    @SuppressLint("RestrictedApi")
    fun hideStatusBar(context: Context) {
        if (UUvideo.TOOL_BAR_EXIST) {
            UUUtils.getWindow(context)
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    @SuppressLint("NewApi")
    fun hideSystemUI(context: Context) {
        var uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        SYSTEM_UI = UUUtils.getWindow(context).decorView.systemUiVisibility
        UUUtils.getWindow(context).decorView.systemUiVisibility = uiOptions

    }

    @SuppressLint("NewApi")
    fun showSystemUI(context: Context) {
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        //Toast.makeText(context, "fjdkslfdska $SYSTEM_UI", Toast.LENGTH_SHORT).show()
        UUUtils.getWindow(context).decorView.systemUiVisibility = SYSTEM_UI
    }

    fun verifyStoragePermissions(activity: Activity) {
        try {
            val permission = ActivityCompat.checkSelfPermission(
                activity,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"),
                    1
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
