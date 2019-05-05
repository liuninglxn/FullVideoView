# FullvideoView
How to
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.liuninglxn:FullVideoView:V1.2.2'
	}


USE

Step 1

    <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="200dp"
            tools:ignore="MissingConstraints">
        <com.example.videolibrary.UUabcVideoView
                android:id="@+id/UUabcVideoView"
                android:layout_width="match_parent"
                android:layout_height="200dp"/>
    </LinearLayout>

Step 2

1)  直接跳转到全屏

    button.setOnClickListener { UUabcVideoView.startFullscreenDirectly(this, UUabcVideoView::class.java,"This is a URL","This is a title")}

2） 横屏全屏自动播放且无法还原小屏

     UUabcVideoView.setUp("This is a URL","This is a title",UUabcVideoView.SCREEN_WINDOW_FULLSCREEN)

     UUabcVideoView.fullScreen.visibility=View.GONE

     UUabcVideoView.backButton.setOnClickListener {
            ...
            finish()
     }

     UUabcVideoView.startButton.performClick()

3） 普通状态，可全屏、小屏，可设置视频封面

     UUabcVideoView.batteryLevel.visibility= View.GONE//电池电量图标

     UUabcVideoView.thumbImageView.setImageResource(R.drawable.testimg)

     UUabcVideoView.setUp("This is a URL" , "This is a title" , UUvideo.SCREEN_NORMAL)

Step 3

     资源释放

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
