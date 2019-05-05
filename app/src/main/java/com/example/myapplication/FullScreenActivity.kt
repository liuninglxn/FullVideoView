package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.videolibrary.UUabcVideoView
import com.example.videolibrary.UUvideo
import kotlinx.android.synthetic.main.activity_fullscreen.*


class FullScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
        setContentView(R.layout.activity_fullscreen)
        full_videoplayer.setUp(
            "http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4",
//            "https://sishu-qiniu.uuabc.com/o_1d4n2fghd1tn81sr9uc61glr1kcfi.mp4",//
            "This is a title",
            UUabcVideoView.SCREEN_WINDOW_FULLSCREEN
        )
        full_videoplayer.fullScreen.visibility=View.GONE
        full_videoplayer.backButton.setOnClickListener { finish() }
        full_videoplayer.startButton.performClick()
        full_videoplayer.batteryLevel.visibility=View.GONE
        full_videoplayer.batteryTimeLayout.visibility=View.GONE
    }

    override fun onBackPressed() {
        if (UUvideo.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        UUvideo.resetAllVideos()
    }
}
