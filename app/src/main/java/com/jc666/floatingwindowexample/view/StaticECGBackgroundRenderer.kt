package com.jc666.floatingwindowexample.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import com.jc666.floatingwindowexample.view.ConstContent.KEY_ECG_VIEW_FOUR_THREE_FOCUS_TAG
import com.jc666.floatingwindowexample.view.ConstContent.KEY_ECG_VIEW_FOUR_THREE_TAG
import com.jc666.floatingwindowexample.view.ConstContent.KEY_ECG_VIEW_ONE_TWELVE_TAG
import com.jc666.floatingwindowexample.view.ConstContent.KEY_ECG_VIEW_TWO_SIX_TAG
import com.jc666.floatingwindowexample.view.ConstContent.ORIENTATION_LANDSCAPE
import com.jc666.floatingwindowexample.view.ConstContent.ORIENTATION_PORTRAIT


/**
 * @author JC666
 * @date 2022/03/01
 * @describe
 * 紀錄畫筆顏色Type，0:黑色(白色波形,全部都黑色) 1:白色(綠色波形)
 *
 *
 */
class StaticECGBackgroundRenderer(context: Context,
                                  private val width: Int,
                                  private val height: Int,
                                  private val colorType: Int,
                                  leadName: String,
                                  gain: Int,
                                  smallGridEnable: Int,
                                  textSp:Int,
                                  isTextPadding: Int,
                                  ecgFormatType: Int,
                                  orientation: Int) {
    private val TAG = this.javaClass.simpleName

    //行與行之間的畫筆(大網格)
    private var rowPaint : Paint? = null

    //網格畫筆(小網格)
    private var cellPaint  : Paint? = null

    //網格畫點畫筆
    private var pointPaint : Paint? = null

    //專門寫每個Lead名稱畫筆
    private var labelTextPaint : Paint? = null

    //專門寫每個Lead單位名稱畫筆
    private var labelUnitTextPaint : Paint? = null

    //專門畫每個Lead起始位置的柱狀條畫筆
    private var labelLeadPaint : Paint? = null

    private var leadName = "Error"

    private var gainValue = "Error"

    private var displaySmallGridStatus:Boolean = true

    private var leadNameTextSP:Int = 8

    private var leadUnitTextSP:Int = 8

    private var textPaddingEnable:Boolean = false

    protected var mDensity: Float
    protected var mScaleDensity: Float
    protected var mDisplayMetrics: DisplayMetrics

    /**
     * 小網格寬高
     */
    private var SMALL_GRID_WIDTH = 6

    /**
     * 大網格寬高
     */
    private var BIG_GRID_WIDTH = 30

    /**
     * 網隔名稱文字大小
     */
    private var gridLabelNameSp = 6F

    private var gridHorizontalNum = 0
    private var gridVerticalNum = 0
    private var gridHorizontalSmallNum = 0
    private var gridVerticalSmallNum = 0

    private var ecgFormatTypeTag = KEY_ECG_VIEW_ONE_TWELVE_TAG

    private var ORIENTATION: Int = 2

    fun draw(canvas: Canvas) {
        Log.d(TAG, "width: " + width + " height: " + height)
        initPaint(canvas, colorType)

        /**
         * 小尺寸平板需要額外修改
         * */
//        if(height > 50 && height < 100) {
//            BIG_GRID_WIDTH = 15
//            SMALL_GRID_WIDTH = 3
//        } else if(height < 50) {
//            BIG_GRID_WIDTH = 10
//            SMALL_GRID_WIDTH = 2
//        }

        gridHorizontalNum = (height / BIG_GRID_WIDTH)
        gridVerticalNum = (width / BIG_GRID_WIDTH)
        gridHorizontalSmallNum = (height / SMALL_GRID_WIDTH)
        gridVerticalSmallNum = (width / SMALL_GRID_WIDTH)

        var startBigX = if(gridHorizontalNum % 2 == 0) {
            (height - (gridHorizontalNum * BIG_GRID_WIDTH)) / 2
        } else {
            (height - (gridHorizontalNum * BIG_GRID_WIDTH)) * 2
        }

//        Log.d(TAG, "startBigX: " + startBigX)
//        Log.d(TAG, "gridHorizontalNum: " + gridHorizontalNum)
//        Log.d(TAG, "gridHorizontalNum/2: " + gridHorizontalNum/2)
//        Log.d(TAG, "gridHorizontalSmallNum: " + gridHorizontalSmallNum)
//        Log.d(TAG, "gridVerticalSmallNum: " + gridVerticalSmallNum)
//        Log.d(TAG, "leadNameTextSP: " + leadNameTextSP)
//        Log.d(TAG, "LeadUnitTextSP: " + leadUnitTextSP)

        when(ecgFormatTypeTag) {
            KEY_ECG_VIEW_ONE_TWELVE_TAG -> {
                drawBigGrid(canvas, startBigX)
                if(displaySmallGridStatus) {
                    drawSmallGrid(canvas, startBigX)
                }

                //起始點位置
                drawStartPoint(canvas,startBigX)

                if(ORIENTATION == ORIENTATION_LANDSCAPE) {
                    //Lead name
                    drawLabelText(canvas, ((BIG_GRID_WIDTH/5)*2).toFloat(), (startBigX + BIG_GRID_WIDTH/3).toFloat(), leadName)
                    //Unit
                    drawLabelUnit(canvas, (((BIG_GRID_WIDTH/5)*2 + 2) + BIG_GRID_WIDTH/5).toFloat(), (((gridHorizontalNum/2) + 2) * BIG_GRID_WIDTH).toFloat(), gainValue)
                } else if(ORIENTATION == ORIENTATION_PORTRAIT) {
                    startBigX = (startBigX/2)/2
                    //Lead name
                    drawLabelNoPaddingText(canvas, (BIG_GRID_WIDTH/5).toFloat(), (startBigX + BIG_GRID_WIDTH/4).toFloat(), leadName)
                    //Unit
                    drawLabelNoPaddingUnit(canvas, 0f, (((gridHorizontalNum/2) + 3) * BIG_GRID_WIDTH).toFloat(), gainValue)
                }
            }
            KEY_ECG_VIEW_TWO_SIX_TAG -> {
                drawBigGrid(canvas, startBigX)
                if(displaySmallGridStatus) {
                    drawSmallGrid(canvas, startBigX)
                }

                //起始點位置
                drawStartPoint(canvas,startBigX)

                if(ORIENTATION == ORIENTATION_LANDSCAPE) {
                    //Lead name
                    drawLabelNoPaddingText(canvas, (BIG_GRID_WIDTH/5).toFloat(), (startBigX + BIG_GRID_WIDTH/4).toFloat(), leadName)
                    //Unit
                    drawLabelNoPaddingUnit(canvas, 0f, ((((gridHorizontalNum/2) + 2) * BIG_GRID_WIDTH)).toFloat(), gainValue)
                } else if(ORIENTATION == ORIENTATION_PORTRAIT) {
                    startBigX = (startBigX/2)/2
                    //Lead name
                    drawLabelNoPaddingText(canvas, (BIG_GRID_WIDTH/5).toFloat(), (startBigX + BIG_GRID_WIDTH/4).toFloat(), leadName)
                    //Unit
                    drawLabelNoPaddingUnit(canvas, 0f, (((gridHorizontalNum/2) + 3) * BIG_GRID_WIDTH).toFloat(), gainValue)
                }
            }
            KEY_ECG_VIEW_FOUR_THREE_TAG -> {

                startBigX = (startBigX/2)/2

                drawBigGrid(canvas, startBigX)
                if(displaySmallGridStatus) {
                    drawSmallGrid(canvas, startBigX)
                }

                //起始點位置
                drawStartPoint(canvas,startBigX)

                if(ORIENTATION == ORIENTATION_LANDSCAPE) {
                    //Lead name
                    drawLabelNoPaddingText(canvas, (BIG_GRID_WIDTH/5).toFloat(), (startBigX + BIG_GRID_WIDTH/4).toFloat(), leadName)
                    //Unit
                    drawLabelNoPaddingUnit(canvas, 0f, (((gridHorizontalNum/2) + 2) * BIG_GRID_WIDTH).toFloat(), gainValue)
                } else if(ORIENTATION == ORIENTATION_PORTRAIT) {
                    //Lead name
                    drawLabelNoPaddingText(canvas, (BIG_GRID_WIDTH/5).toFloat(), (startBigX + BIG_GRID_WIDTH/4).toFloat(), leadName)
                    //Unit
                    drawLabelNoPaddingUnit(canvas, 0f, (((gridHorizontalNum/2) + 2) * BIG_GRID_WIDTH).toFloat(), gainValue)
                }
            }
            KEY_ECG_VIEW_FOUR_THREE_FOCUS_TAG -> {
                startBigX = (startBigX/2)/2

                drawBigGrid(canvas, startBigX)
                if(displaySmallGridStatus) {
                    drawSmallGrid(canvas, startBigX)
                }

                //起始點位置
                drawStartPoint(canvas,startBigX)

                if(ORIENTATION == ORIENTATION_LANDSCAPE) {
                    //Lead name
                    drawLabelNoPaddingText(canvas, (BIG_GRID_WIDTH/5).toFloat(), (startBigX + BIG_GRID_WIDTH/5).toFloat(), leadName)
                    //Unit
                    drawLabelNoPaddingUnit(canvas, 0f, (((gridHorizontalNum/2) + 2) * BIG_GRID_WIDTH + 2).toFloat(), gainValue)
                } else if(ORIENTATION == ORIENTATION_PORTRAIT) {
                    //Lead name
                    drawLabelNoPaddingText(canvas, (BIG_GRID_WIDTH/5).toFloat(), (startBigX + BIG_GRID_WIDTH/5).toFloat(), leadName)
                    //Unit
                    drawLabelNoPaddingUnit(canvas, 0f, (((gridHorizontalNum/2) + 2) * BIG_GRID_WIDTH + 2).toFloat(), gainValue)
                }
            }
        }

    }

    /**
     * 繪製大網格
     *
     * @param canvas
     */
    private fun drawBigGrid(canvas: Canvas, startX:Int) {
        /** 绘制横线  */
        for (i in 0..gridHorizontalNum + 1) {
            canvas.drawLine(
                0f, startX + (i * BIG_GRID_WIDTH).toFloat(),
                width.toFloat(), startX + (i * BIG_GRID_WIDTH).toFloat(), rowPaint!!
            )
        }
        /** 绘制竖线  */
        for (i in 0..gridVerticalNum + 1) {
            canvas.drawLine(
                (i * BIG_GRID_WIDTH).toFloat(),
                0f,
                (i * BIG_GRID_WIDTH).toFloat(),
                height.toFloat(),
                rowPaint!!
            )
        }
    }

    /**
     * 繪製小網格
     *
     * @param canvas
     */
    private fun drawSmallGrid(canvas: Canvas, startX:Int) {
        //畫橫線
        if(startX > 0) {
            var count = startX/SMALL_GRID_WIDTH
            for (j in 0..count) {
                canvas.drawLine(
                    0f,
                    (j * SMALL_GRID_WIDTH).toFloat(),
                    width.toFloat(),
                    (j * SMALL_GRID_WIDTH).toFloat(),
                    cellPaint!!
                )
            }
        }

        for (i in 0..gridHorizontalNum + 1) {
            for (j in 0..6) {
                canvas.drawLine(
                    0f,
                    ((startX + (i * BIG_GRID_WIDTH)) + (j * SMALL_GRID_WIDTH)).toFloat(),
                    width.toFloat(),
                    ((startX + (i * BIG_GRID_WIDTH)) + (j * SMALL_GRID_WIDTH)).toFloat(),
                    cellPaint!!
                )
            }
        }
//        for (i in 0..gridHorizontalSmallNum + 1) {
//            canvas.drawLine(
//                0f, (i * SMALL_GRID_WIDTH).toFloat(),
//                width.toFloat(), (i * SMALL_GRID_WIDTH).toFloat(), cellPaint!!
//            )
//        }
        //畫豎線
        for (i in 0..gridVerticalSmallNum + 1) {
            canvas.drawLine(
                (i * SMALL_GRID_WIDTH).toFloat(), 0f,
                (i * SMALL_GRID_WIDTH).toFloat(), height.toFloat(), cellPaint!!
            )
        }
    }

    /**
     * 繪製初始條
     *
     * @param canvas
     */
    private fun drawStartPoint(canvas: Canvas, startX:Int) {
        /** 繪製起始位置Lead(一條示) */
//        for (i in BIG_GRID_WIDTH/3 until (BIG_GRID_WIDTH/3)*2) {
//            canvas.drawLine(
//                i.toFloat(),
//                startX + (((gridHorizontalNum/2) - 1) * BIG_GRID_WIDTH).toFloat(),
//                i.toFloat(),
//                startX + (((gridHorizontalNum/2) + 1) * BIG_GRID_WIDTH).toFloat(),
//                labelLeadPaint!!
//            )
//        }

        /** 繪製起始位置Lead(ㄇ字型示) */
        for (i in 0 until BIG_GRID_WIDTH/5) {
            canvas.drawLine(
                i.toFloat(),
                (startX + (((gridHorizontalNum/2) + 1) * BIG_GRID_WIDTH).toFloat()) - 2,
                i.toFloat(),
                startX + (((gridHorizontalNum/2) + 1) * BIG_GRID_WIDTH).toFloat(),
                labelLeadPaint!!
            )
        }
        for (i in BIG_GRID_WIDTH/5 until ((BIG_GRID_WIDTH/5) + 2)) {
            canvas.drawLine(
                i.toFloat(),
                (startX + (((gridHorizontalNum/2) - 1) * BIG_GRID_WIDTH).toFloat()) - 2,
                i.toFloat(),
                startX + (((gridHorizontalNum/2) + 1) * BIG_GRID_WIDTH).toFloat(),
                labelLeadPaint!!
            )
        }
        for (i in ((BIG_GRID_WIDTH/5) + 2) until (BIG_GRID_WIDTH/5)*2) {
            canvas.drawLine(
                i.toFloat(),
                (startX + (((gridHorizontalNum/2) - 1) * BIG_GRID_WIDTH).toFloat()) - 2,
                i.toFloat(),
                startX + (((gridHorizontalNum/2) - 1) * BIG_GRID_WIDTH).toFloat(),
                labelLeadPaint!!
            )
        }
        for (i in (BIG_GRID_WIDTH/5)*2 until ((BIG_GRID_WIDTH/5)*2 + 2)) {
            canvas.drawLine(
                i.toFloat(),
                (startX + (((gridHorizontalNum/2) - 1) * BIG_GRID_WIDTH).toFloat()) - 2,
                i.toFloat(),
                startX + (((gridHorizontalNum/2) + 1) * BIG_GRID_WIDTH).toFloat(),
                labelLeadPaint!!
            )
        }
        for (i in ((BIG_GRID_WIDTH/5)*2 + 2) until ((BIG_GRID_WIDTH/5)*2 + 2) + BIG_GRID_WIDTH/5) {
            canvas.drawLine(
                i.toFloat(),
                (startX + (((gridHorizontalNum/2) + 1) * BIG_GRID_WIDTH).toFloat()) - 2,
                i.toFloat(),
                startX + (((gridHorizontalNum/2) + 1) * BIG_GRID_WIDTH).toFloat(),
                labelLeadPaint!!
            )
        }
    }

    /**
     * 绘制文字
     *
     * @param canvas
     */
    private fun drawLabelText(canvas: Canvas, left: Float, bottom: Float, text: String) {
        val padding: Int = ChartUtils.dp2px(mDensity, gridLabelNameSp)
        val fontMetrics: Paint.FontMetricsInt = labelTextPaint!!.getFontMetricsInt()
        val startX = left + padding
        val rectBottom = bottom - padding
        val rectTop: Float = rectBottom - ChartUtils.getTextHeight(labelTextPaint!!, text)
        val baseline = (rectBottom + rectTop - fontMetrics.bottom - fontMetrics.top) / 1f
        canvas.drawText(text, startX, baseline, labelTextPaint!!)
    }

    private fun drawLabelNoPaddingText(canvas: Canvas, left: Float, bottom: Float, text: String) {
        val fontMetrics: Paint.FontMetricsInt = labelTextPaint!!.getFontMetricsInt()
        val startX = left
        val rectBottom = bottom
        val rectTop: Float = rectBottom - ChartUtils.getTextHeight(labelTextPaint!!, text)
        val baseline = (rectBottom + rectTop - fontMetrics.bottom - fontMetrics.top) / 1f
        canvas.drawText(text, startX, baseline, labelTextPaint!!)
    }

    private fun drawLabelUnit(canvas: Canvas, left: Float, bottom: Float, text: String) {
        val padding: Int = ChartUtils.dp2px(mDensity, gridLabelNameSp)
        val fontMetrics: Paint.FontMetricsInt = labelUnitTextPaint!!.getFontMetricsInt()
        val startX = left + padding
        val rectBottom = bottom - padding
        val rectTop: Float = rectBottom - ChartUtils.getTextHeight(labelUnitTextPaint!!, text)
        val baseline = (rectBottom + rectTop - fontMetrics.bottom - fontMetrics.top) / 2f
        canvas.drawText(text, startX, baseline, labelUnitTextPaint!!)
    }

    private fun drawLabelNoPaddingUnit(canvas: Canvas, left: Float, bottom: Float, text: String) {
        val fontMetrics: Paint.FontMetricsInt = labelUnitTextPaint!!.getFontMetricsInt()
        val startX = left
        val rectBottom = bottom
        val rectTop: Float = rectBottom - ChartUtils.getTextHeight(labelUnitTextPaint!!, text)
        val baseline = (rectBottom + rectTop - fontMetrics.bottom - fontMetrics.top) / 2f
        canvas.drawText(text, startX, baseline, labelUnitTextPaint!!)
    }

    private fun initPaint(canvas: Canvas, type: Int) {
        /**
         * relation 設定線條也要跟著不同的屏幕分辨率上做調整，這樣轉出來輸出的畫面才會一致!!!
         * 參考文章 :
         * https://codejzy.com/posts-859534.html
         * https://stackoverflow.com/questions/11622773/android-line-width-pixel-size
         */
        var relation = Math.sqrt((canvas.width * canvas.height).toDouble())
        //        Log.d(TAG, "initPaint canvas relation: " + relation);
        relation = relation / 250
        //        Log.d(TAG, "initPaint canvas relation: " + relation);
        rowPaint = Paint()
        rowPaint!!.color = GRAY_ROW_COLOR
        rowPaint!!.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            0.9f,
            mDisplayMetrics
        )
        rowPaint!!.isAntiAlias = false
        rowPaint!!.style = Paint.Style.STROKE

        cellPaint = Paint()
        cellPaint!!.color = GRAY_CELL_COLOR
        cellPaint!!.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            0.5f,
            mDisplayMetrics
        )
        cellPaint!!.isAntiAlias = false
        cellPaint!!.style = Paint.Style.STROKE

        pointPaint = Paint()
        pointPaint!!.color = GRAY_POINT_COLOR
        pointPaint!!.strokeWidth = (0.2 * relation).toFloat()
        pointPaint!!.isAntiAlias = false
        pointPaint!!.style = Paint.Style.STROKE

        labelLeadPaint = Paint()
        labelLeadPaint!!.isAntiAlias = false
        if (type == 0) {
            labelLeadPaint!!.color = BLACK_LABEL_LEAD_COLOR
        } else {
            labelLeadPaint!!.color = GREEN_LABEL_LEAD_COLOR
        }
        labelLeadPaint!!.strokeWidth = ChartUtils.dip2px(mDisplayMetrics, 1f)

        when(ecgFormatTypeTag) {
            KEY_ECG_VIEW_ONE_TWELVE_TAG -> {
                leadNameTextSP = 8
                leadUnitTextSP = 8
            }
            KEY_ECG_VIEW_TWO_SIX_TAG -> {
                leadNameTextSP = 12
                leadUnitTextSP = 10
            }
            KEY_ECG_VIEW_FOUR_THREE_TAG -> {
                leadNameTextSP = 14
                leadUnitTextSP = 14
            }
            KEY_ECG_VIEW_FOUR_THREE_FOCUS_TAG -> {
                leadNameTextSP = 8
                leadUnitTextSP = 8
            }
        }

        labelTextPaint = Paint()
        labelTextPaint!!.isAntiAlias = false
        labelTextPaint!!.style = Paint.Style.FILL
        labelTextPaint!!.strokeCap = Paint.Cap.ROUND
        labelTextPaint!!.textSize = (leadNameTextSP * relation).toFloat()
        if (type == 0) {
            labelTextPaint!!.color = BLACK_LABEL_LEAD_TEXT_COLOR
        } else {
            labelTextPaint!!.color = WHITE_LABEL_LEAD_TEXT_COLOR
        }
        labelTextPaint!!.typeface = Typeface.create(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

        labelUnitTextPaint = Paint()
        labelUnitTextPaint!!.isAntiAlias = false
        labelUnitTextPaint!!.style = Paint.Style.FILL
        labelUnitTextPaint!!.strokeCap = Paint.Cap.ROUND
        labelUnitTextPaint!!.textSize = (leadUnitTextSP * relation).toFloat()
        if (type == 0) {
            labelUnitTextPaint!!.color = BLACK_LABEL_LEAD_UNIT_TEXT_COLOR
        } else {
            labelUnitTextPaint!!.color = GREEN_LABEL_LEAD_UNIT_TEXT_COLOR
        }
        labelUnitTextPaint!!.typeface = Typeface.create(
            Typeface.DEFAULT,
            Typeface.BOLD
        )
    }

    companion object {
        private val GRAY_ROW_COLOR = Color.parseColor("#40949497") //灰色
        private val GRAY_CELL_COLOR = Color.parseColor("#20949497") //灰色
        private val GRAY_POINT_COLOR = Color.parseColor("#25949497") //灰色

        //lead 文字顏色
        private val BLACK_LABEL_LEAD_TEXT_COLOR = Color.parseColor("#ff000000") //黑色
        private val WHITE_LABEL_LEAD_TEXT_COLOR = Color.parseColor("#ffffffff") //白色

        //lead 起始位置顏色
        private val BLACK_LABEL_LEAD_COLOR = Color.parseColor("#ff000000") //黑色
        private val GREEN_LABEL_LEAD_COLOR = Color.parseColor("#ff00c100") //暗綠色

        //lead 單位文字顏色
        private val BLACK_LABEL_LEAD_UNIT_TEXT_COLOR = Color.parseColor("#ff000000") //黑色
        private val GREEN_LABEL_LEAD_UNIT_TEXT_COLOR = Color.parseColor("#ff00c100") //暗綠色
    }

    init {
        this.leadName = leadName
        when (gain) {
            0 -> gainValue = "2mv"
            1 -> gainValue = "1mv"
            2 -> gainValue = "0.5mv"
            3 -> gainValue = "0.25mv"
        }
        when (smallGridEnable) {
            0 -> displaySmallGridStatus = false
            1 -> displaySmallGridStatus = true
        }
//        when (textSp) {
//            0 -> textSP = 8
//            1 -> textSP = 10
//            2 -> textSP = 12
//            3 -> textSP = 14
//        }
        when (isTextPadding) {
            0 -> textPaddingEnable = false
            1 -> textPaddingEnable = true
        }
        when (ecgFormatType) {
            0 -> ecgFormatTypeTag = KEY_ECG_VIEW_ONE_TWELVE_TAG
            1 -> ecgFormatTypeTag = KEY_ECG_VIEW_TWO_SIX_TAG
            2 -> ecgFormatTypeTag = KEY_ECG_VIEW_FOUR_THREE_TAG
            3 -> ecgFormatTypeTag = KEY_ECG_VIEW_FOUR_THREE_FOCUS_TAG
        }
        this.ORIENTATION = orientation
        mDisplayMetrics = context.resources.displayMetrics
        mDensity = mDisplayMetrics.density
        mScaleDensity = mDisplayMetrics.scaledDensity
    }
}