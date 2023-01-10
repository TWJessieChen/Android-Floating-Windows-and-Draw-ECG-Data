package com.jc666.floatingwindowexample.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.jc666.floatingwindowexample.R


class StaticECGBackgroundView : View {
    private val TAG = StaticECGBackgroundView::class.java.simpleName

    //紀錄畫筆顏色Type，0:白色(黑色波形) 1:黑色(綠色波形)
    private val BACKGROUND_BLACK_COLOR = Color.parseColor("#ff202123") //黑色
    private val BACKGROUND_WHITE_COLOR = Color.parseColor("#ffffffff") //白色

    private var mStaticECGBackgroundRenderer: StaticECGBackgroundRenderer? = null

    private var BACKGROUND_TEXT_PADDING_STATUS = 0 //預設不需要字串位移

    private var BACKGROUND_TEXT_SP = 2 //預設8sp字體大小

    private var BACKGROUND_SMALL_GRID_STATUS = 1 //預設開啟小網格

    private var BACKGROUND_DRAW_MODE = 0

    private var GAIN = 0

    private var LEAD_NMAE = ""

    private var ecgBackgroundBitmap: Bitmap? = null

    private var ecgBackgroundCanvas: Canvas? = null

    private var mRenderPaint: Paint? = null

    private var ECG_FORMAT_TYPE = 0

    private var ORIENTATION: Int = 2

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.StaticEcgView)
            if (a.hasValue(R.styleable.StaticEcgView_background_text_padding)) {
                BACKGROUND_TEXT_PADDING_STATUS = a.getInt(R.styleable.StaticEcgView_background_text_padding, 0)
            }
            if (a.hasValue(R.styleable.StaticEcgView_background_text_sp)) {
                BACKGROUND_TEXT_SP = a.getInt(R.styleable.StaticEcgView_background_text_sp, 2)
            }
            if (a.hasValue(R.styleable.StaticEcgView_background_small_grid_enable)) {
                BACKGROUND_SMALL_GRID_STATUS = a.getInt(R.styleable.StaticEcgView_background_small_grid_enable, 1)
            }
            if (a.hasValue(R.styleable.StaticEcgView_background_draw_mode)) {
                BACKGROUND_DRAW_MODE = a.getInt(R.styleable.StaticEcgView_background_draw_mode, 0)
            }
            if (a.hasValue(R.styleable.StaticEcgView_gain)) {
                GAIN = a.getInt(R.styleable.StaticEcgView_gain, 0)
            }
            if (a.hasValue(R.styleable.StaticEcgView_lead_name)) {
                LEAD_NMAE = a.getString(R.styleable.StaticEcgView_lead_name).toString()
            }
            if (a.hasValue(R.styleable.StaticEcgView_wave_ecg_format)) {
                ECG_FORMAT_TYPE = a.getInt(R.styleable.StaticEcgView_wave_ecg_format, 0)
            }
            if (a.hasValue(R.styleable.StaticEcgView_wave_ecg_orientation)) {
                ORIENTATION = a.getInt(R.styleable.StaticEcgView_wave_ecg_orientation, 2)
            }
        }
        ecgBackgroundCanvas = Canvas()
        mRenderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mRenderPaint!!.setStyle(Paint.Style.FILL)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(this.mStaticECGBackgroundRenderer != null) {
            this.mStaticECGBackgroundRenderer = null
        }
        this.mStaticECGBackgroundRenderer = StaticECGBackgroundRenderer(context,
            measuredWidth,
            measuredHeight,
            BACKGROUND_DRAW_MODE,
            LEAD_NMAE,
            GAIN,
            BACKGROUND_SMALL_GRID_STATUS,
            BACKGROUND_TEXT_SP,
            BACKGROUND_TEXT_PADDING_STATUS,
            ECG_FORMAT_TYPE,
            ORIENTATION)

        if(ecgBackgroundBitmap == null) {
            ecgBackgroundBitmap = Bitmap.createBitmap(
                measuredWidth,
                measuredHeight, Bitmap.Config.ARGB_8888
            )
        }

        ecgBackgroundBitmap!!.eraseColor(Color.TRANSPARENT)

        ecgBackgroundCanvas!!.setBitmap(ecgBackgroundBitmap)
        ecgBackgroundCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        if(BACKGROUND_DRAW_MODE == 0) {
            ecgBackgroundCanvas!!.drawColor(BACKGROUND_WHITE_COLOR)
        } else {
            ecgBackgroundCanvas!!.drawColor(BACKGROUND_BLACK_COLOR)
        }

        mStaticECGBackgroundRenderer!!.draw(ecgBackgroundCanvas!!)

        canvas.drawBitmap(ecgBackgroundBitmap!!, 0f, 0f, mRenderPaint)
    }

    override fun onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow")
        // releases the bitmap in the renderer to avoid oom error
        if (mStaticECGBackgroundRenderer != null && mStaticECGBackgroundRenderer is StaticECGBackgroundRenderer) {
            if (ecgBackgroundCanvas != null) {
                ecgBackgroundCanvas!!.setBitmap(null)
                ecgBackgroundCanvas = null
            }
            if (ecgBackgroundBitmap != null) {
                if (ecgBackgroundBitmap != null) {
                    ecgBackgroundBitmap!!.recycle()
                }
            }
        }
        super.onDetachedFromWindow()
    }

    /**
     * 設定心電圖繪圖背景相關設定
     *
     * @param
     * background : 設定 WHITE:0, BLACK:1
     * _gain : 設定 0.5F -> GAIN_I:0, 1F -> GAIN_II:1, 2F -> GAIN_III:2, 4F -> GAIN_IV:3
     */
    fun setBackgroundParams(background: Int, _gain: Float) {
        this.BACKGROUND_DRAW_MODE = background
        when (_gain) {
            0.5F -> {
                this.GAIN = 0
            }
            1F -> {
                this.GAIN = 1
            }
            2F -> {
                this.GAIN = 2
            }
            4F -> {
                this.GAIN = 3
            }
        }
        invalidate()
    }

    fun updateLeadName(leadName: String) {
        this.LEAD_NMAE = leadName
        invalidate()
    }

    fun updateLeadNameAndBackgroundParams(background: Int, _gain: Float, leadName: String) {
        this.BACKGROUND_DRAW_MODE = background
        when (_gain) {
            0.5F -> {
                this.GAIN = 0
            }
            1F -> {
                this.GAIN = 1
            }
            2F -> {
                this.GAIN = 2
            }
            4F -> {
                this.GAIN = 3
            }
        }
        this.LEAD_NMAE = leadName
        invalidate()
    }
}