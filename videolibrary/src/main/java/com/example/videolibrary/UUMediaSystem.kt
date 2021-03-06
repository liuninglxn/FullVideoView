package com.example.videolibrary

import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.annotation.RequiresApi

/**
 * 实现系统的播放引擎
 */
class UUMediaSystem(uuVideoView: UUvideo) : UUMediaInterface(uuVideoView), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {
    override fun setSurface(surface: Surface?) {
        mediaPlayer?.setSurface(surface)
    }

    var mediaPlayer: MediaPlayer? = null

    override fun prepare() {
        release()
        mMediaHandlerThread = HandlerThread("UUVD")
        mMediaHandlerThread.start()
        mMediaHandler = Handler(mMediaHandlerThread.looper)//主线程还是非主线程，就在这里
        handler = Handler()

        mMediaHandler.post {
            try {
                mediaPlayer = MediaPlayer()
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer!!.isLooping = uuVideoView.jzDataSource.looping
                mediaPlayer!!.setOnPreparedListener(this@UUMediaSystem)
                mediaPlayer!!.setOnCompletionListener(this@UUMediaSystem)
                mediaPlayer!!.setOnBufferingUpdateListener(this@UUMediaSystem)
                mediaPlayer!!.setScreenOnWhilePlaying(true)
                mediaPlayer!!.setOnSeekCompleteListener(this@UUMediaSystem)
                mediaPlayer!!.setOnErrorListener(this@UUMediaSystem)
                mediaPlayer!!.setOnInfoListener(this@UUMediaSystem)
                mediaPlayer!!.setOnVideoSizeChangedListener(this@UUMediaSystem)
                val clazz = MediaPlayer::class.java
                val method = clazz.getDeclaredMethod("setDataSource", String::class.java, Map::class.java)
                method.invoke(mediaPlayer, uuVideoView.jzDataSource.currentUrl.toString(), uuVideoView.jzDataSource.headerMap)
                mediaPlayer!!.prepareAsync()
                mediaPlayer!!.setSurface(Surface(SAVED_SURFACE))
                //mediaPlayer!!.setSurface(Surface(uuVideoView.textureView.surfaceTexture))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun start() {
        mMediaHandler.post { mediaPlayer!!.start() }
    }

    override fun pause() {
        mMediaHandler.post { mediaPlayer!!.pause() }
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer!!.isPlaying
    }

    override fun seekTo(time: Long) {
        mMediaHandler.post {
            try {
                mediaPlayer!!.seekTo(time.toInt())
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    override fun release() {
        if (mMediaHandler != null && mMediaHandlerThread != null && mediaPlayer != null) {//不知道有没有妖孽
            val tmpHandlerThread = mMediaHandlerThread
            val tmpMediaPlayer = mediaPlayer
            mMediaHandler.post {
                tmpMediaPlayer?.setSurface(null)
                tmpMediaPlayer!!.release()//release就不能放到主线程里，界面会卡顿
                tmpHandlerThread.quit()
            }
            mediaPlayer = null
        }
    }

    //TODO 测试这种问题是否在threadHandler中是否正常，所有的操作mediaplayer是否不需要thread，挨个测试，是否有问题
    override fun getCurrentPosition(): Long {
        return if (mediaPlayer != null) {
            mediaPlayer!!.currentPosition.toLong()
        } else {
            0
        }
    }

    override fun getDuration(): Long {
        return if (mediaPlayer != null) {
            mediaPlayer!!.duration.toLong()
        } else {
            0
        }
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        if (mMediaHandler == null) return
        mMediaHandler.post { if (mediaPlayer != null) mediaPlayer!!.setVolume(leftVolume, rightVolume) }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun setSpeed(speed: Float) {
        val pp = mediaPlayer!!.playbackParams
        pp.speed = speed
        mediaPlayer!!.playbackParams = pp
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
        if (uuVideoView.jzDataSource.currentUrl.toString().toLowerCase().contains("mp3") || uuVideoView.jzDataSource.currentUrl.toString().toLowerCase().contains(
                "wav"
            )
        ) {
            handler.post { uuVideoView.onPrepared() }
        }
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        handler.post { uuVideoView.onAutoCompletion() }
    }

    override fun onBufferingUpdate(mediaPlayer: MediaPlayer, percent: Int) {
        handler.post { uuVideoView.setBufferProgress(percent) }
    }

    override fun onSeekComplete(mediaPlayer: MediaPlayer) {
        handler.post { uuVideoView.onSeekComplete() }
    }

    override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
        handler.post { uuVideoView.onError(what, extra) }
        return true
    }

    override fun onInfo(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
        handler.post {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                if (uuVideoView.currentState == UUvideo.CURRENT_STATE_PREPARING || uuVideoView.currentState == UUvideo.CURRENT_STATE_PREPARING_CHANGING_URL) {
                    uuVideoView.onPrepared()
                }
            } else {
                uuVideoView.onInfo(what, extra)
            }
        }
        return false
    }

    override fun onVideoSizeChanged(mediaPlayer: MediaPlayer, width: Int, height: Int) {
        handler.post { uuVideoView.onVideoSizeChanged(width, height) }
    }


    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (UUMediaInterface.SAVED_SURFACE == null) {
            UUMediaInterface.SAVED_SURFACE = surface
            prepare()
        } else {
            uuVideoView.textureView.surfaceTexture = UUMediaInterface.SAVED_SURFACE
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }
}
