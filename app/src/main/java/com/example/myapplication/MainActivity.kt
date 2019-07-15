package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.myapplication.CustomMediaPlayer.JZMediaExo
import com.example.videolibrary.UUabcVideoView
import com.example.videolibrary.UUvideo
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoplayer.thumbImageView.setImageResource(R.drawable.ic_teacher_info_bg)
        videoplayer.batteryLevel.visibility= View.GONE
        videoplayer.setUp(
            "https://courseware.uuabc.com/courseware_centent_1561864858147"//mp4
            , "" , UUvideo.SCREEN_NORMAL, JZMediaExo::class.java)
//        mExoPlayerView?.setDataSource("https://courseware.uuabc.com/courseware_centent_1561864858147")
        button.setOnClickListener {
            val intent = Intent(this,FullScreenActivity::class.java)
            startActivity(intent)
        }
        button2.setOnClickListener {
            UUabcVideoView.startFullscreenDirectly(this, UUabcVideoView::class.java,
                "https://courseware.uuabc.com/courseware_centent_1561864858147"
                , "This is a title")
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
