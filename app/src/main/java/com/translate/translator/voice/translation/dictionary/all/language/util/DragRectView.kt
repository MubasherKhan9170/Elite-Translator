package com.translate.translator.voice.translation.dictionary.all.language.util

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class DragRectView : View {
    private var mRectPaint: Paint? = null
    private var mStartX = 0
    private var mStartY = 0
    private var mEndX = 4
    private var mEndY = 4
    private var mDrawRect = false
    private val mTextPaint: TextPaint? = null
    private var mCallback: OnUpCallback? = null

    interface OnUpCallback {
        fun onRectFinished(rect: Rect?)
        fun onTextClicked(text: String?)
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    /**
     * Sets callback for up
     *
     * @param callback [OnUpCallback]
     */
    fun setOnUpCallback(callback: OnUpCallback?) {
        mCallback = callback
    }

    /**
     * Inits internal data
     */
    private fun init() {
        mRectPaint = Paint()
        mRectPaint!!.color = context.resources.getColor(R.color.holo_green_light)
        mRectPaint!!.style = Paint.Style.STROKE
        mRectPaint!!.strokeWidth = 5f // TODO: should take from resources

        /* mTextPaint = new TextPaint();
        mTextPaint.setColor(getContext().getResources().getColor(android.R.color.holo_green_light));
        mTextPaint.setTextSize(20);*/
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // TODO: be aware of multi-touches
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawRect = false
                mStartX = event.x.toInt()
                mStartY = event.y.toInt()
                invalidate()
                if(!mDrawRect){
                    TextGraphic.rectangles.forEachIndexed { index, rectF ->
                        if(rectF.contains(mStartX.toFloat(), mStartY.toFloat())){
                            Log.i(TAG, "onTouchEvent: contained ${TextGraphic.texts[index]}")

                            if (mCallback != null) {
                                mCallback!!.onTextClicked(TextGraphic.texts[index])
                            }

                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                if (Math.abs(x - mEndX) > 10 || Math.abs(y - mEndY) > 10) {
                    mEndX = x
                    mEndY = y
                    invalidate()
                }else{
                   // mDrawRect = true
                    mDrawRect = false
                }

            }
            MotionEvent.ACTION_UP -> {
                if (mCallback != null) {
                    mCallback!!.onRectFinished(
                        Rect(
                            Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                            Math.max(mEndX, mStartX), Math.max(mStartY, mEndY)
                        )
                    )
                }
                invalidate()
                mDrawRect = false
            }
            else -> {
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mDrawRect) {
            canvas.drawRect(
                Math.min(mStartX, mEndX).toFloat(), Math.min(mStartY, mEndY).toFloat(),
                Math.max(mEndX, mStartX).toFloat(), Math.max(mEndY, mStartY).toFloat(), mRectPaint!!
            )

            /* canvas.drawText("  (" + Math.abs(mStartX - mEndX) + ", " + Math.abs(mStartY - mEndY) + ")",
                    Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mTextPaint);*/
        }
    }
    companion object{
        private const val TAG = "DragRectView"
    }

}
