package com.example.videolibrary

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.View

/**
 * 参照Android系统的VideoView的onMeasure方法
 * relativelayout中无法全屏，嵌套一个linearlayout
 */
open class UUTextureView : TextureView {

    var currentVideoWidth = 0
    var currentVideoHeight = 0

    constructor(context: Context) : super(context) {
        currentVideoWidth = 0
        currentVideoHeight = 0
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        currentVideoWidth = 0
        currentVideoHeight = 0
    }

    fun setVideoSize(currentVideoWidth: Int, currentVideoHeight: Int) {
        if (this.currentVideoWidth != currentVideoWidth || this.currentVideoHeight != currentVideoHeight) {
            this.currentVideoWidth = currentVideoWidth
            this.currentVideoHeight = currentVideoHeight
            requestLayout()
        }
    }

    override fun setRotation(rotation: Float) {
        if (rotation != getRotation()) {
            super.setRotation(rotation)
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        Log.i(TAG, "onMeasure " + " [" + this.hashCode() + "] ")
        val viewRotation = rotation.toInt()
        val videoWidth = currentVideoWidth
        var videoHeight = currentVideoHeight


        var parentHeight = (parent as View).measuredHeight
        var parentWidth = (parent as View).measuredWidth
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            if (UUvideo.VIDEO_IMAGE_DISPLAY_TYPE == UUvideo.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT) {
                if (viewRotation == 90 || viewRotation == 270) {
                    val tempSize = parentWidth
                    parentWidth = parentHeight
                    parentHeight = tempSize
                }
                /**强制充满 */
                videoHeight = videoWidth * parentHeight / parentWidth
            }
        }

        // 如果判断成立，则说明显示的TextureView和本身的位置是有90度的旋转的，所以需要交换宽高参数。
        if (viewRotation == 90 || viewRotation == 270) {
            val tempMeasureSpec = widthMeasureSpec
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = tempMeasureSpec
        }

        var width = View.getDefaultSize(videoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(videoHeight, heightMeasureSpec)
        if (videoWidth > 0 && videoHeight > 0) {

            val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)

            Log.i(TAG, "widthMeasureSpec  [" + View.MeasureSpec.toString(widthMeasureSpec) + "]")
            Log.i(TAG, "heightMeasureSpec [" + View.MeasureSpec.toString(heightMeasureSpec) + "]")

            if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                width = widthSpecSize
                height = heightSpecSize
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                width = widthSpecSize
                height = width * videoHeight / videoWidth
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                height = heightSpecSize
                width = height * videoWidth / videoHeight
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            } else {
                width = videoWidth
                height = videoHeight
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            }
        } else {
        }
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            if (UUvideo.VIDEO_IMAGE_DISPLAY_TYPE == UUvideo.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL) {
                /**原图 */
                height = videoHeight
                width = videoWidth
            } else if (UUvideo.VIDEO_IMAGE_DISPLAY_TYPE == UUvideo.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP) {
                if (viewRotation == 90 || viewRotation == 270) {
                    val tempSize = parentWidth
                    parentWidth = parentHeight
                    parentHeight = tempSize
                }
                /**充满剪切 */
                if (videoHeight.toDouble() / videoWidth > parentHeight.toDouble() / parentWidth) {
                    height = (parentWidth.toDouble() / width.toDouble() * height.toDouble()).toInt()
                    width = parentWidth
                } else if (videoHeight.toDouble() / videoWidth < parentHeight.toDouble() / parentWidth) {
                    width = (parentHeight.toDouble() / height.toDouble() * width.toDouble()).toInt()
                    height = parentHeight
                }
            }
        }
        setMeasuredDimension(width, height)
    }

    companion object {
        protected const val TAG = "UUResizeTextureView"
    }
}
