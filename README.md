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
	        implementation 'com.github.liuninglxn:FullVideoView:V1.0'
	}


USE

Step 1

     UUabcVideoView.setUp(VideoURL,"This is a title",UUabcVideoView.SCREEN_WINDOW_FULLSCREEN)

     UUabcVideoView.backButton.setOnClickListener {
            ...
            finish()
     }

     UUabcVideoView.startButton.performClick()


Step 2

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