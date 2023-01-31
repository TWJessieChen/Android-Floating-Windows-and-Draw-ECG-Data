package com.jc666.floatingwindowexample.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.jc666.floatingwindowexample.R

/**
 * @author JC666
 * @date 2022/03/01
 * @describe
 *
 *
 */
class StaticECGBackgroundView : View {
    private val TAG = StaticECGBackgroundView::class.java.simpleName

    //紀錄畫筆顏色Type，0:白色(黑色波形) 1:黑色(綠色波形)
    private val backgroundBlackColor = Color.parseColor("#ff202123") //黑色
    private val backgroundWhiteColor = Color.parseColor("#ffffffff") //白色

    private lateinit var mStaticECGBackgroundRenderer: StaticECGBackgroundRenderer

    private var backgroundTextPaddingStatus = 0 //預設不需要字串位移

    private var backgroundTextSP = 2 //預設8sp字體大小

    private var backgroundSmallGridStatus = 1 //預設開啟小網格

    private var backgroundDrawMode = 0

    private var gainValue = 0

    private var leadName = ""

    private lateinit var ecgBackgroundBitmap: Bitmap

    private lateinit var ecgBackgroundCanvas: Canvas

    private lateinit var mRenderPaint: Paint

    private var ecgFormatType = 0

    private var orientation: Int = 2

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
                backgroundTextPaddingStatus = a.getInt(R.styleable.StaticEcgView_background_text_padding, 0)
            }
            if (a.hasValue(R.styleable.StaticEcgView_background_text_sp)) {
                backgroundTextSP = a.getInt(R.styleable.StaticEcgView_background_text_sp, 2)
            }
            if (a.hasValue(R.styleable.StaticEcgView_background_small_grid_enable)) {
                backgroundSmallGridStatus = a.getInt(R.styleable.StaticEcgView_background_small_grid_enable, 1)
            }
            if (a.hasValue(R.styleable.StaticEcgView_background_draw_mode)) {
                backgroundDrawMode = a.getInt(R.styleable.StaticEcgView_background_draw_mode, 0)
            }
            if (a.hasValue(R.styleable.StaticEcgView_gain)) {
                gainValue = a.getInt(R.styleable.StaticEcgView_gain, 0)
            }
            if (a.hasValue(R.styleable.StaticEcgView_lead_name)) {
                leadName = a.getString(R.styleable.StaticEcgView_lead_name).toString()
            }
            if (a.hasValue(R.styleable.StaticEcgView_wave_ecg_format)) {
                ecgFormatType = a.getInt(R.styleable.StaticEcgView_wave_ecg_format, 0)
            }
            if (a.hasValue(R.styleable.StaticEcgView_wave_ecg_orientation)) {
                orientation = a.getInt(R.styleable.StaticEcgView_wave_ecg_orientation, 2)
            }
        }
        ecgBackgroundCanvas = Canvas()
        mRenderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mRenderPaint.setStyle(Paint.Style.FILL)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        this.mStaticECGBackgroundRenderer = StaticECGBackgroundRenderer(context,
            measuredWidth,
            measuredHeight,
            backgroundDrawMode,
            leadName,
            gainValue,
            backgroundSmallGridStatus,
            backgroundTextSP,
            backgroundTextPaddingStatus,
            ecgFormatType,
            orientation)

        ecgBackgroundBitmap = Bitmap.createBitmap(
            measuredWidth,
            measuredHeight, Bitmap.Config.ARGB_8888
        )

        ecgBackgroundBitmap.eraseColor(Color.TRANSPARENT)

        ecgBackgroundCanvas.setBitmap(ecgBackgroundBitmap)
        ecgBackgroundCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        if(backgroundDrawMode == 0) {
            ecgBackgroundCanvas.drawColor(backgroundWhiteColor)
        } else {
            ecgBackgroundCanvas.drawColor(backgroundBlackColor)
        }

        mStaticECGBackgroundRenderer.draw(ecgBackgroundCanvas)

        canvas.drawBitmap(ecgBackgroundBitmap, 0f, 0f, mRenderPaint)
    }

    override fun onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow")
        // releases the bitmap in the renderer to avoid oom error
        if (mStaticECGBackgroundRenderer is StaticECGBackgroundRenderer) {
            ecgBackgroundCanvas.setBitmap(null)
            ecgBackgroundBitmap.recycle()
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
        this.backgroundDrawMode = background
        when (_gain) {
            0.5F -> {
                this.gainValue = 0
            }
            1F -> {
                this.gainValue = 1
            }
            2F -> {
                this.gainValue = 2
            }
            4F -> {
                this.gainValue = 3
            }
        }
        invalidate()
    }

    fun updateLeadName(leadName: String) {
        this.leadName = leadName
        invalidate()
    }

    fun updateLeadNameAndBackgroundParams(background: Int, _gain: Float, leadName: String) {
        this.backgroundDrawMode = background
        when (_gain) {
            0.5F -> {
                this.gainValue = 0
            }
            1F -> {
                this.gainValue = 1
            }
            2F -> {
                this.gainValue = 2
            }
            4F -> {
                this.gainValue = 3
            }
        }
        this.leadName = leadName
        invalidate()
    }
}