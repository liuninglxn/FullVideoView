package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.videolibrary.UUabcVideoView
import com.example.videolibrary.UUvideo
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videoplayer.thumbImageView.setImageResource(R.drawable.testimg)
        videoplayer.batteryLevel.visibility= View.GONE
        videoplayer.setUp("http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4"
            , "This is a title" , UUvideo.SCREEN_NORMAL)

        button.setOnClickListener {
            val intent = Intent(this,FullScreenActivity::class.java)
            startActivity(intent)
        }
        button2.setOnClickListener {
            UUabcVideoView.startFullscreenDirectly(this, UUabcVideoView::class.java, "http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4", "This is a title")
        }
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
