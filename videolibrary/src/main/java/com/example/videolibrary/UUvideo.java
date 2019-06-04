package com.example.videolibrary;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public abstract class UUvideo extends FrameLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {

    public static final String TAG = "UUVideo";
    public static final int THRESHOLD = 80;

    public static final int SCREEN_NORMAL = 0;
    public static final int SCREEN_WINDOW_LIST = 1;
    public static final int SCREEN_WINDOW_FULLSCREEN = 2;
    public static final int SCREEN_WINDOW_TINY = 3;

    public static final int STATE_IDLE = -1;
    public static final int CURRENT_STATE_NORMAL = 0;
    public static final int CURRENT_STATE_PREPARING = 1;
    public static final int CURRENT_STATE_PREPARING_CHANGING_URL = 2;
    public static final int CURRENT_STATE_PLAYING = 3;
    public static final int CURRENT_STATE_PAUSE = 5;
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    public static final int CURRENT_STATE_ERROR = 7;

    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER = 0;//DEFAULT
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT = 1;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP = 2;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL = 3;

    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 10;
    public static final int CURRENT_STATE_PLAYING_BUFFERING_END = 11;
    public static int BACKUP_PLAYING_BUFFERING_STATE = -1;


    public static boolean TOOL_BAR_EXIST = true;
    public static int FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static boolean SAVE_PROGRESS = false;//是否从上次观看点继续观看
    public static boolean WIFI_TIP_DIALOG_SHOWED = false;
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public static long lastAutoFullscreenTime = 0;
    public static UUvideo CURRENT_JZVD;
    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//是否新建个class，代码更规矩，并且变量的位置也很尴尬
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    resetAllVideos();
                    Log.d(TAG, "AUDIOFOCUS_LOSS [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };
    public static LinkedList<ViewGroup> CONTAINER_LIST = new LinkedList<ViewGroup>();
    public static int ON_PLAY_PAUSE_TMP_STATE = 0;
    public int currentState = -1;
    public int currentScreen = -1;
    public long seekToInAdvance = 0;
    public ImageView startButton;
    public SeekBar progressBar;
    public ImageView fullscreenButton;
    public TextView currentTimeTextView, totalTimeTextView;
    public ViewGroup textureViewContainer;
    public ViewGroup topContainer, bottomContainer;
    public int widthRatio = 0;
    public int heightRatio = 0;
    public UUDataSource jzDataSource;
    public int positionInList = -1;
    public int videoRotation = 0;
    public UUMediaInterface mediaInterface;
    public UUTextureView textureView;
    public int seekToManulPosition = -1;
    protected Timer UPDATE_PROGRESS_TIMER;
    protected int mScreenWidth;
    protected int mScreenHeight;
    protected AudioManager mAudioManager;
    protected ProgressTimerTask mProgressTimerTask;
    protected boolean mTouchingProgressBar;
    protected float mDownX;
    protected float mDownY;
    protected boolean mChangeVolume;
    protected boolean mChangePosition;
    protected boolean mChangeBrightness;
    protected long mGestureDownPosition;
    protected int mGestureDownVolume;
    protected float mGestureDownBrightness;
    protected long mSeekTimePosition;
    protected long gobakFullscreenTime = 0;
    private VideoStateListener mVideoStateListener;

    public UUvideo(Context context) {
        super(context);
        init(context);
    }

    public UUvideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public static void resetAllVideos() {
        Log.d(TAG, "resetAllVideos");
        if (CURRENT_JZVD != null) {
            CURRENT_JZVD.reset();
            CURRENT_JZVD = null;
        }
    }

    public static void clearSavedProgress(Context context, String url) {
        UUUtils.INSTANCE.clearSavedProgress(context, url);
    }

    public static void goOnPlayOnResume() {
        if (CURRENT_JZVD != null) {
            if (CURRENT_JZVD.currentState == UUvideo.CURRENT_STATE_PAUSE) {
                if (ON_PLAY_PAUSE_TMP_STATE == CURRENT_STATE_PAUSE) {
                    CURRENT_JZVD.onStatePause();
                    CURRENT_JZVD.mediaInterface.pause();
                } else {
                    CURRENT_JZVD.onStatePlaying();
                    CURRENT_JZVD.mediaInterface.start();
                }
                ON_PLAY_PAUSE_TMP_STATE = 0;
            }
        }
    }


    public static void onScrollReleaseAllVideos(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //以后这种功能不要写在jzvd里了，让用户看看详细的也能更了解

    }

    public static void goOnPlayOnPause() {
        if (CURRENT_JZVD != null) {
            if (CURRENT_JZVD.currentState == UUvideo.CURRENT_STATE_AUTO_COMPLETE ||
                    CURRENT_JZVD.currentState == UUvideo.CURRENT_STATE_NORMAL ||
                    CURRENT_JZVD.currentState == UUvideo.CURRENT_STATE_ERROR) {
                UUvideo.resetAllVideos();
            } else {
                ON_PLAY_PAUSE_TMP_STATE = CURRENT_JZVD.currentState;
                CURRENT_JZVD.onStatePause();
                CURRENT_JZVD.mediaInterface.pause();
            }
        }
    }

    public static void onChildViewAttachedToWindow(View view, int jzvdId) {

    }

    public static void onChildViewDetachedFromWindow(View view, int jzvdId) {

    }

    public static void setTextureViewRotation(int rotation) {
        if (CURRENT_JZVD != null && CURRENT_JZVD.textureView != null) {
            CURRENT_JZVD.textureView.setRotation(rotation);
        }
    }

    public static void setVideoImageDisplayType(int type) {
        UUvideo.VIDEO_IMAGE_DISPLAY_TYPE = type;
        if (CURRENT_JZVD != null && CURRENT_JZVD.textureView != null) {
            CURRENT_JZVD.textureView.requestLayout();
        }
    }

    public static void setCurrentJzvd(UUvideo uuvideo) {
        if (CURRENT_JZVD != null) CURRENT_JZVD.reset();
        CURRENT_JZVD = uuvideo;
    }

    public static boolean backPress() {
        Log.i(TAG, "backPress");
        if (CONTAINER_LIST.size() != 0 && CURRENT_JZVD != null) {//回到普通窗口
            CURRENT_JZVD.gotoScreenNormal();
            return true;
        }
        return false;
    }

    public static void startFullscreenDirectly(Context context, Class _class, String url, String title) {
        startFullscreenDirectly(context, _class, new UUDataSource(url, title));
    }

    public static void startFullscreenDirectly(Context context, Class _class, UUDataSource uuDataSource) {
        UUUtils.INSTANCE.hideStatusBar(context);
        UUUtils.INSTANCE.setRequestedOrientation(context, FULLSCREEN_ORIENTATION);
        ViewGroup vp = (ViewGroup) UUUtils.INSTANCE.scanForActivity(context).getWindow().getDecorView();
        try {
            Constructor<UUvideo> constructor = _class.getConstructor(Context.class);
            final UUvideo uuvd = constructor.newInstance(context);
            LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.addView(uuvd, lp);
            uuvd.setUp(uuDataSource, UUabcVideoView.SCREEN_WINDOW_FULLSCREEN);
            uuvd.startButton.performClick();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param mediaInterface
     */
    public void setMediaInterface(UUMediaInterface mediaInterface) {
        reset();
        this.mediaInterface = mediaInterface;
    }

    public abstract int getLayoutId();

    public void init(Context context) {
        View.inflate(context, getLayoutId(), this);
        startButton = findViewById(R.id.start);
        fullscreenButton = findViewById(R.id.fullscreen);
        progressBar = findViewById(R.id.bottom_seek_progress);
        currentTimeTextView = findViewById(R.id.current);
        totalTimeTextView = findViewById(R.id.total);
        bottomContainer = findViewById(R.id.layout_bottom);
        textureViewContainer = findViewById(R.id.surface_container);
        topContainer = findViewById(R.id.layout_top);

        startButton.setOnClickListener(this);
        fullscreenButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);
        bottomContainer.setOnClickListener(this);
        textureViewContainer.setOnClickListener(this);
        textureViewContainer.setOnTouchListener(this);

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        currentState = STATE_IDLE;
    }

    public void setUp(String url, String title, int screen) {
        setUp(new UUDataSource(url, title), screen);
    }

    public void setUp(String url, String title, int screen, UUMediaInterface jzMediaInterface) {
        setUp(new UUDataSource(url, title), screen, jzMediaInterface);
    }

    public void setUp(UUDataSource jzDataSource, int screen) {
        setUp(jzDataSource, screen, new UUMediaSystem(this));
    }

    public void setUp(UUDataSource jzDataSource, int screen, UUMediaInterface jzMediaInterface) {
        if ((System.currentTimeMillis() - gobakFullscreenTime) < 200) return;

        this.jzDataSource = jzDataSource;
        this.currentScreen = screen;
        onStateNormal();
        mediaInterface = jzMediaInterface;//这个位置可能需要调整
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            Log.i(TAG, "onClick start [" + this.hashCode() + "] ");
            if (jzDataSource == null || jzDataSource.getUrlsMap().isEmpty() || jzDataSource.getCurrentUrl() == null) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentState == CURRENT_STATE_NORMAL) {
                if (!jzDataSource.getCurrentUrl().toString().startsWith("file") && !
                        jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                        !UUUtils.INSTANCE.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {//这个可以放到std中
                    showWifiDialog();
                    return;
                }
                startVideo();
                if (mVideoStateListener!=null){
                    mVideoStateListener.onStart();
                }
            } else if (currentState == CURRENT_STATE_PLAYING) {
                Log.d(TAG, "pauseVideo [" + this.hashCode() + "] ");
                mediaInterface.pause();
                if (mVideoStateListener!=null){
                    mVideoStateListener.onPause();
                }
                onStatePause();
            } else if (currentState == CURRENT_STATE_PAUSE) {
                mediaInterface.start();
                onStatePlaying();
                if (mVideoStateListener!=null){
                    mVideoStateListener.onPauseToStart();
                }
            } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
                startVideo();
                if (mVideoStateListener!=null){
                    mVideoStateListener.onStart();
                }
            }
        } else if (i == R.id.fullscreen) {
            Log.i(TAG, "onClick fullscreen [" + this.hashCode() + "] ");
            if (currentState == CURRENT_STATE_AUTO_COMPLETE) return;
            if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                //quit fullscreen
                backPress();
            } else {
                Log.d(TAG, "toFullscreenActivity [" + this.hashCode() + "] ");
                gotoScreenFullscreen();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int id = v.getId();
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "onTouch surfaceContainer actionDown [" + this.hashCode() + "] ");
                    mTouchingProgressBar = true;

                    mDownX = x;
                    mDownY = y;
                    mChangeVolume = false;
                    mChangePosition = false;
                    mChangeBrightness = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);
                    if (currentScreen == SCREEN_WINDOW_FULLSCREEN) {
                        if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                            if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                                cancelProgressTimer();
                                if (absDeltaX >= THRESHOLD) {
                                    // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                                    // 否则会因为mediaplayer的状态非法导致App Crash
                                    if (currentState != CURRENT_STATE_ERROR) {
                                        mChangePosition = true;
                                        mGestureDownPosition = getCurrentPositionWhenPlaying();
                                    }
                                } else {
                                    //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                                    if (mDownX < mScreenWidth * 0.5f) {//左侧改变亮度
                                        mChangeBrightness = true;
                                        WindowManager.LayoutParams lp = UUUtils.INSTANCE.getWindow(getContext()).getAttributes();
                                        if (lp.screenBrightness < 0) {
                                            try {
                                                mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                                Log.i(TAG, "current system brightness: " + mGestureDownBrightness);
                                            } catch (Settings.SettingNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            mGestureDownBrightness = lp.screenBrightness * 255;
                                            Log.i(TAG, "current activity brightness: " + mGestureDownBrightness);
                                        }
                                    } else {//右侧改变声音
                                        mChangeVolume = true;
                                        mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                    }
                                }
                            }
                        }
                    }
                    if (mChangePosition) {
                        long totalTimeDuration = getDuration();
                        mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / mScreenWidth);
                        if (mSeekTimePosition > totalTimeDuration)
                            mSeekTimePosition = totalTimeDuration;
                        String seekTime = UUUtils.INSTANCE.stringForTime(mSeekTimePosition);
                        String totalTime = UUUtils.INSTANCE.stringForTime(totalTimeDuration);

                        showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
                    }
                    if (mChangeVolume) {
                        deltaY = -deltaY;
                        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
                        //dialog中显示百分比
                        int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
                        showVolumeDialog(-deltaY, volumePercent);
                    }

                    if (mChangeBrightness) {
                        deltaY = -deltaY;
                        int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
                        WindowManager.LayoutParams params = UUUtils.INSTANCE.getWindow(getContext()).getAttributes();
                        if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                            params.screenBrightness = 1;
                        } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                            params.screenBrightness = 0.01f;
                        } else {
                            params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
                        }
                        UUUtils.INSTANCE.getWindow(getContext()).setAttributes(params);
                        //dialog中显示百分比
                        int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
                        showBrightnessDialog(brightnessPercent);
//                        mDownY = y;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "onTouch surfaceContainer actionUp [" + this.hashCode() + "] ");
                    mTouchingProgressBar = false;
                    dismissProgressDialog();
                    dismissVolumeDialog();
                    dismissBrightnessDialog();
                    if (mChangePosition) {
                        mediaInterface.seekTo(mSeekTimePosition);
                        long duration = getDuration();
                        int progress = (int) (mSeekTimePosition * 100 / (duration == 0 ? 1 : duration));
                        progressBar.setProgress(progress);
                        Log.e("UUVideoSeekBar","SeekTime:"+mSeekTimePosition+"progress:"+progress);
                    }
                    if (mChangeVolume) {
                        //change volume event
                    }
                    startProgressTimer();
                    break;
            }
        }
        return false;
    }

    public void startVideo() {
        Log.d(TAG, "startVideo [" + this.hashCode() + "] ");
        setCurrentJzvd(this);
        addTextureView();
        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        UUUtils.INSTANCE.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        onStatePreparing();
    }

    public void onPrepared() {
        Log.i(TAG, "onPrepared " + " [" + this.hashCode() + "] ");
        if (currentState != CURRENT_STATE_PREPARING && currentState != CURRENT_STATE_PLAYING_BUFFERING_START)
            return;
        onStatePrepared();
        onStatePlaying();
    }

    public void setState(int state) {
        setState(state, 0, 0);
    }

    public void setState(int state, int urlMapIndex, int seekToInAdvance) {//后面两个参数干嘛的
        switch (state) {
            case CURRENT_STATE_NORMAL:
                onStateNormal();
                break;
            case CURRENT_STATE_PREPARING:
                onStatePreparing();
                break;
            case CURRENT_STATE_PREPARING_CHANGING_URL:
                changeUrl(urlMapIndex, seekToInAdvance);
                break;
            case CURRENT_STATE_PLAYING:
                onStatePlaying();
                break;
            case CURRENT_STATE_PAUSE:
                onStatePause();
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                onStatePlaybackBufferingStart();
                break;
            case CURRENT_STATE_ERROR:
                onStateError();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                onStateAutoComplete();
                break;
        }
    }

    public void setScreen(int screen) {//特殊的个别的进入全屏的按钮在这里设置  只有setup的时候能用上
        switch (screen) {
            case SCREEN_NORMAL:
                setScreenNormal();
                break;
            case SCREEN_WINDOW_FULLSCREEN:
                setScreenFullscreen();
                break;
            case SCREEN_WINDOW_TINY:
                setScreenTiny();
                break;
        }
    }

    public void onStateNormal() {
        Log.i(TAG, "onStateNormal " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_NORMAL;
        cancelProgressTimer();
        if (mediaInterface != null) mediaInterface.release();
    }

    public void onStatePreparing() {
        Log.i(TAG, "onStatePreparing " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_PREPARING;
        resetProgressAndTime();
        Log.e("UUVideoSeekBar","Preparing");
    }

    public void changeUrl(int urlMapIndex, long seekToInAdvance) {
        currentState = CURRENT_STATE_PREPARING_CHANGING_URL;
        this.seekToInAdvance = seekToInAdvance;
        jzDataSource.setCurrentUrlIndex(urlMapIndex);
        mediaInterface.prepare();
    }

    public void changeUrl(UUDataSource jzDataSource, long seekToInAdvance) {
        currentState = CURRENT_STATE_PREPARING_CHANGING_URL;
        this.seekToInAdvance = seekToInAdvance;
        this.jzDataSource = jzDataSource;
        mediaInterface.prepare();
    }

    public void changeUrl(String url, String title, long seekToInAdvance) {
        changeUrl(new UUDataSource(url, title), seekToInAdvance);
    }

    public void onStatePrepared() {//因为这个紧接着就会进入播放状态，所以不设置state
        if (seekToInAdvance != 0) {
            mediaInterface.seekTo(seekToInAdvance);
            seekToInAdvance = 0;
        } else {
            long position = UUUtils.INSTANCE.getSavedProgress(getContext(), Objects.requireNonNull(jzDataSource.getCurrentUrl()));
            if (position != 0) {
                mediaInterface.seekTo(position);
            }
        }
        Log.e("UUVideoSeekBar","Prepared");
    }

    public void onStatePlaying() {
        Log.i(TAG, "onStatePlaying " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_PLAYING;
        startProgressTimer();
        Log.e("UUVideoSeekBar","Playing");
    }

    public void onStatePause() {
        Log.i(TAG, "onStatePause " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_PAUSE;
        startProgressTimer();
    }

    public void onStateError() {
        Log.i(TAG, "onStateError " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_ERROR;
        cancelProgressTimer();
    }

    public void onStateAutoComplete() {
        Log.i(TAG, "onStateAutoComplete " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_AUTO_COMPLETE;
        cancelProgressTimer();
        progressBar.setProgress(100);
        currentTimeTextView.setText(totalTimeTextView.getText());
    }

    public void onInfo(int what, int extra) {
        Log.d(TAG, "onInfo what - " + what + " extra - " + extra);
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            if (currentState == CURRENT_STATE_PLAYING_BUFFERING_START) return;
            BACKUP_PLAYING_BUFFERING_STATE = currentState;
            onStatePlaybackBufferingStart();
            Log.d(TAG, "MEDIA_INFO_BUFFERING_START");
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (BACKUP_PLAYING_BUFFERING_STATE != -1) {
                if (currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
                    setState(BACKUP_PLAYING_BUFFERING_STATE);
                }
                BACKUP_PLAYING_BUFFERING_STATE = -1;
            }
            onStatePlaybackBufferingEnd();
            Log.d(TAG, "MEDIA_INFO_BUFFERING_END");
        }
    }
    public void onStatePlaybackBufferingStart() {
        Log.i(TAG, "onStatePlaybackBufferingStart " + " [" + this.hashCode() + "] ");
        currentState = CURRENT_STATE_PLAYING_BUFFERING_START;
        startProgressTimer();
    }
    public void onStatePlaybackBufferingEnd() {
        Log.i(TAG, "onStatePlaybackBufferingEnd " + " [" + this.hashCode() + "] ");
    }
    public void onError(int what, int extra) {
        Log.e(TAG, "onError " + what + " - " + extra + " [" + this.hashCode() + "] ");
        if (what != 38 && extra != -38 && what != -38 && extra != 38 && extra != -19) {
            onStateError();
            mediaInterface.release();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentScreen == SCREEN_WINDOW_FULLSCREEN || currentScreen == SCREEN_WINDOW_TINY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            getChildAt(0).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    public void onAutoCompletion() {
        Runtime.getRuntime().gc();
        Log.i(TAG, "onAutoCompletion " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        dismissBrightnessDialog();
        dismissProgressDialog();
        dismissVolumeDialog();
        onStateAutoComplete();
        mediaInterface.release();
        UUUtils.INSTANCE.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        UUUtils.INSTANCE.saveProgress(getContext(), jzDataSource.getCurrentUrl(), 0);
    }

    public void reset() {
        Log.i(TAG, "reset " + " [" + this.hashCode() + "] ");
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            long position = getCurrentPositionWhenPlaying();
            UUUtils.INSTANCE.saveProgress(getContext(), jzDataSource.getCurrentUrl(), position);
        }
        cancelProgressTimer();
        dismissBrightnessDialog();
        dismissProgressDialog();
        dismissVolumeDialog();
        onStateNormal();
        textureViewContainer.removeAllViews();
        UUMediaInterface.SAVED_SURFACE = null;

        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        UUUtils.INSTANCE.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mediaInterface.release();
    }

    public void addTextureView() {
        Log.d(TAG, "addTextureView [" + this.hashCode() + "] ");
        if (textureView != null) textureViewContainer.removeView(textureView);
        textureView = new UUTextureView(getContext().getApplicationContext());
        textureView.setSurfaceTextureListener(mediaInterface);

        LayoutParams layoutParams =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(textureView, layoutParams);
    }

    public void clearFloatScreen() {

    }

    public void onVideoSizeChanged(int width, int height) {
        Log.i(TAG, "onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        if (textureView != null) {
            if (videoRotation != 0) {
                textureView.setRotation(videoRotation);
            }
            textureView.setVideoSize(width, height);
        }
    }

    public void startProgressTimer() {
        Log.i(TAG, "startProgressTimer: " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 300);
    }

    public void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    public void onProgress(int progress, long position, long duration) {
        if (!mTouchingProgressBar) {
            if (seekToManulPosition != -1) {
                if (seekToManulPosition > progress) {
                    return;
                } else {
                    seekToManulPosition = -1;//这个关键帧有没有必要做
                }
            } else {
                if (progress != 0) progressBar.setProgress(progress);
            }
        }
        if (position != 0) currentTimeTextView.setText(UUUtils.INSTANCE.stringForTime(position));
        totalTimeTextView.setText(UUUtils.INSTANCE.stringForTime(duration));
    }

    public void setBufferProgress(int bufferProgress) {
        if (bufferProgress != 0) progressBar.setSecondaryProgress(bufferProgress);
    }

    public void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(UUUtils.INSTANCE.stringForTime(0));
        totalTimeTextView.setText(UUUtils.INSTANCE.stringForTime(0));
    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        //TODO 这块的判断应该根据MediaPlayer来
        if (currentState == CURRENT_STATE_PLAYING ||
                currentState == CURRENT_STATE_PAUSE) {
            try {
                position = mediaInterface.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public long getDuration() {
        long duration = 0;
        //TODO MediaPlayer 判空的问题
//        if (JZMediaPlayer.instance().mediaPlayer == null) return duration;
        try {
            duration = mediaInterface.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "bottomProgress onStartTrackingTouch [" + this.hashCode() + "] ");
        cancelProgressTimer();
        ViewParent vpdown = getParent();
        while (vpdown != null) {
            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getParent();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "bottomProgress onStopTrackingTouch [" + this.hashCode() + "] ");
        startProgressTimer();
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (currentState != CURRENT_STATE_PLAYING &&
                currentState != CURRENT_STATE_PAUSE) return;
        long time = seekBar.getProgress() * getDuration() / 100;
        seekToManulPosition = seekBar.getProgress();
        mediaInterface.seekTo(time);
        Log.i(TAG, "seekTo " + time + " [" + this.hashCode() + "] ");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            //设置这个progres对应的时间，给textview
            long duration = getDuration();
            currentTimeTextView.setText(UUUtils.INSTANCE.stringForTime(progress * duration / 100));
        }
    }

    public void gotoScreenFullscreen() {
        ViewGroup vg = (ViewGroup) getParent();
        vg.removeView(this);
        CONTAINER_LIST.add(vg);
        vg = (ViewGroup) (UUUtils.INSTANCE.scanForActivity(getContext())).getWindow().getDecorView();//和他也没有关系
        vg.addView(this, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setScreenFullscreen();
        UUUtils.INSTANCE.hideStatusBar(getContext());
        UUUtils.INSTANCE.setRequestedOrientation(getContext(), FULLSCREEN_ORIENTATION);
        UUUtils.INSTANCE.hideSystemUI(getContext());//华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326

    }

    public void gotoScreenNormal() {//goback本质上是goto
        gobakFullscreenTime = System.currentTimeMillis();//退出全屏
        ViewGroup vg = (ViewGroup) (UUUtils.INSTANCE.scanForActivity(getContext())).getWindow().getDecorView();
        vg.removeView(this);
        CONTAINER_LIST.getLast().removeAllViews();
        CONTAINER_LIST.getLast().addView(this, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        CONTAINER_LIST.pop();

        setScreenNormal();
        UUUtils.INSTANCE.showStatusBar(getContext());
        UUUtils.INSTANCE.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
        UUUtils.INSTANCE.showSystemUI(getContext());
    }

    public void setScreenNormal() {
        currentScreen = SCREEN_NORMAL;
    }

    public void setScreenFullscreen() {
        currentScreen = SCREEN_WINDOW_FULLSCREEN;
    }

    public void setScreenTiny() {
        currentScreen = SCREEN_WINDOW_TINY;
    }

    //    //重力感应的时候调用的函数，、、这里有重力感应的参数，暂时不能删除
    public void autoFullscreen(float x) {
        if (CURRENT_JZVD != null
                && (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE)
                && currentScreen != SCREEN_WINDOW_FULLSCREEN
                && currentScreen != SCREEN_WINDOW_TINY) {
            if (x > 0) {
                UUUtils.INSTANCE.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                UUUtils.INSTANCE.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
            gotoScreenFullscreen();
        }
    }

    public void autoQuitFullscreen() {
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
//                && CURRENT_JZVD != null
                && currentState == CURRENT_STATE_PLAYING
                && currentScreen == SCREEN_WINDOW_FULLSCREEN) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            backPress();
        }
    }

    //TODO 是否有用
    public void onSeekComplete() {

    }

    public void showWifiDialog() {
    }

    public void showProgressDialog(float deltaX,
                                   String seekTime, long seekTimePosition,
                                   String totalTime, long totalTimeDuration) {
    }

    public void dismissProgressDialog() {

    }

    public void showVolumeDialog(float deltaY, int volumePercent) {

    }

    public void dismissVolumeDialog() {

    }

    public void showBrightnessDialog(int brightnessPercent) {

    }

    public void dismissBrightnessDialog() {

    }

    public Context getApplicationContext() {//这个函数必要吗
        Context context = getContext();
        if (context != null) {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext != null) {
                return applicationContext;
            }
        }
        return context;
    }

    public static class JZAutoFullscreenListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {//可以得到传感器实时测量出来的变化值
            final float x = event.values[SensorManager.DATA_X];
            float y = event.values[SensorManager.DATA_Y];
            float z = event.values[SensorManager.DATA_Z];
            //过滤掉用力过猛会有一个反向的大数值
            if (x < -12 || x > 12) {
                if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000) {
                    if (UUvideo.CURRENT_JZVD != null) UUvideo.CURRENT_JZVD.autoFullscreen(x);
                    lastAutoFullscreenTime = System.currentTimeMillis();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
//                Log.v(TAG, "onProgressUpdate " + "[" + this.hashCode() + "] ");
                post(() -> {
                    long position = getCurrentPositionWhenPlaying();
                    long duration = getDuration();
                    int progress = (int) (position * 100 / (duration == 0 ? 1 : duration));
                    onProgress(progress, position, duration);
                });
            }
        }
    }
}
