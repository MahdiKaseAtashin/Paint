package com.mahdikaseatashin.paint

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.log

class DrawingView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mBrushSize: Float = 0.toFloat()
    private var mCanvasBitmap: Bitmap? = null
    private var canvas: Canvas = Canvas()
    private var mDrawPaint: Paint? = null
    private var mDrawPath: CustomPath? = null
    private var mCanvasPaint: Paint? = null
    private var color = Color.BLACK
    private val mPaths = ArrayList<CustomPath>()
    private var mRemovePath: CustomPath? = null

    init {
        initializationSetup()
    }

    fun undo() {
        if (mPaths.isNotEmpty()) {
            mRemovePath = mPaths.removeAt(mPaths.lastIndex)
            mPaths.remove(mRemovePath)
            invalidate()
        }
    }

    private fun initializationSetup() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushSize
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushSize
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x
        val y = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushSize = mBrushSize
                mDrawPath!!.reset()
                if (x != null) {
                    if (y != null) {
                        mDrawPath!!.moveTo(x, y)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (x != null) {
                    if (y != null) {
                        mDrawPath!!.lineTo(x, y)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    fun getBrushSize() : Float{
        return mBrushSize
    }

    fun setBrushSize(size: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            size, resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setBrushColor(c: String) {
        color = Color.parseColor(c)
        mDrawPaint!!.color = color
    }
    fun setBrushColor(c: Int) {
        val hexColor = java.lang.String.format("#%06X", 0xFFFFFF and c)
        Log.e("TAG", "setBrushColor: $hexColor" )
        color = Color.parseColor(hexColor)
        mDrawPaint!!.color = color
    }


    private class CustomPath(var color: Int, var brushSize: Float) : Path()


}
