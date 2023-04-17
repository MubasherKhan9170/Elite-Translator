package com.translate.translator.voice.translation.dictionary.all.language.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.translate.translator.voice.translation.dictionary.all.language.R

class FullScreenRect : View {
    private var mRectPaint: Paint? = null
    private var mStartX = 0
    private var mStartY = 0
     var mEndX = 200
     var mEndY = 200

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

    private fun init() {

        mRectPaint = Paint()
        mRectPaint!!.color = context.resources.getColor(R.color.white)
        mRectPaint!!.style = Paint.Style.STROKE
        mRectPaint!!.strokeWidth = 5f // TODO: should take from resources

        /* mTextPaint = new TextPaint();
        mTextPaint.setColor(getContext().getResources().getColor(android.R.color.holo_green_light));
        mTextPaint.setTextSize(20);*/
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRect(
            Math.min(mStartX, mEndX).toFloat(), Math.min(mStartY, mEndY).toFloat(),
            Math.max(mEndX, mStartX).toFloat(), Math.max(mEndY, mStartY).toFloat(), mRectPaint!!
        )

    }
}