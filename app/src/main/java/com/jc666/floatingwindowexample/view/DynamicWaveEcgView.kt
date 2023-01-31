package com.jc666.floatingwindowexample.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import com.jc666.floatingwindowexample.R
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author JC666
 * @date 2022/03/01
 * @describe
 *
 *
 */
open class DynamicWaveEcgView : View {
    private val TAG = DynamicWaveEcgView::class.java.simpleName

    private var refreshUITask: TimerTask? = null

    //隨意給個超大值，當作定義值
    private val DEFAULT_INITIAL_VALUE = 9999999999999999.99999999999999999

    private var mDensity = 0f

    private var mScaleDensity = 0f

    private lateinit var mDisplayMetrics: DisplayMetrics

    /**
     * 宽高
     */
    private var mWidth = 0f
    private var mHeight = 0f

    /**
     * 数据线画笔
     */
    private lateinit var mWavePaint: Paint

    private lateinit var peakPaint: Paint

    /**
     * 線條的路徑
     */
    private lateinit var mPathHead: Path

    private lateinit var mPathEnd: Path

    /**
     * 保存已绘制的数据坐标
     */
    private lateinit var dataArray: DoubleArray

    private lateinit var pacemakerDataArray: BooleanArray

    private lateinit var drawArray: DoubleArray

    private lateinit var needPacemakerDataArray: BooleanArray

    private var gainValue:Float = 1f

    /**
     * 线条粗细
     */
    private var waveLineStrokeWidth = 1f

    /**
     * 当前的x，y坐标
     */
    private var startY = 0f

    /**
     * 线条的长度，可用于控制横坐标4.9F
     */
    private var waveLineWidthRate = 0.0F

    private var ecgFormat = 0

    /**
     * ECG 數據的数量
     */
    private var row = 0

    private var draw_index = 0

    /**
     * 第一次不畫尾吧資料，畫到第二Run時，就需要開始畫了!!!
     */
    private var isFirstRun = true

    private var baseLine = 0.0f

    /**
     * 大網格寬高(這個需要跟繪圖背景網格大小同步才行)，換算GAIN值很重要的數據
     */
    private var bigGridWidth = 30

    private var twoGridLength = 0f

    private var startECGData = 0.0F

    private var isLeadOFF = false

    //修改紀錄是否有leadOff發生
    private var recordLeadOffHistory  = false

    private var recordLeadOffHistoryIndex  = 0

    //每次取資料的比數
    private var drawIntervalIndexValue = 50

    private var drawIndexValue = 0

    private var drawRefresh = 100

    private var drawRecordIndexValue = 0

    private var isCircleRun = false

    private var isDrawRefreshJob = false

    //空白距離
    private var drawHeadIntervalTailValue = 100

    private var readyDrawECGData = AtomicBoolean(false)

    private var recordCount = 0

    /**
     * Debug 用
     */
    private var writeEcgDataToText = ""

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.StaticEcgView)
            if (a.hasValue(R.styleable.StaticEcgView_wave_ecg_format)) {
                ecgFormat = a.getInt(R.styleable.StaticEcgView_wave_ecg_format, 0)
            }
        }

        mDisplayMetrics = context.getResources().getDisplayMetrics()
        mDensity = mDisplayMetrics.density
        mScaleDensity = mDisplayMetrics.scaledDensity
        //Log.d(TAG,"mScaleDensity: " + mScaleDensity + " mDensity: " + mDensity + " mDisplayMetrics: " + mDisplayMetrics)

        /** ECG Wave paint */
        mWavePaint = Paint()
        mWavePaint.style = Paint.Style.STROKE
        mWavePaint.strokeCap = Paint.Cap.ROUND
        mWavePaint.color = GREEN_LEAD_LINE_COLOR
        mWavePaint.strokeWidth = waveLineStrokeWidth
        /** 抗锯齿效果 */
        mWavePaint.isAntiAlias = true

        /** Pacemaker peak  paint */
        peakPaint = Paint()
        peakPaint.isAntiAlias = true
        peakPaint.style = Paint.Style.FILL
        peakPaint.strokeCap = Paint.Cap.ROUND
        peakPaint.textSize = ChartUtils.sp2px(mScaleDensity, 20f).toFloat()
        peakPaint.color = Color.parseColor("#ffea5243")

        /** ECG 軌跡座標  paint */
        mPathHead = Path()

        mPathEnd = Path()

    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        /** 获取控件的宽高 */
        mWidth = measuredWidth.toFloat()
        mHeight = measuredHeight.toFloat()
        baseLine = mHeight/2
        twoGridLength = (bigGridWidth*2).toFloat()
        //Log.d(TAG,"twoGridLength: " + twoGridLength + " mHeight: " +  mHeight)
        //Log.d(TAG,"mHeight/mScaleDensity: " + (mHeight/mScaleDensity))

        /** 設定view 需要畫多少個點，5000(10s ECG資料), 2500(5s ECG資料), 1250(2.5s ECG資料) */
        when(ecgFormat) {
            0 -> {
                dataArray = DoubleArray(5000)
                pacemakerDataArray = BooleanArray(5000)
                needPacemakerDataArray = BooleanArray(5000)
                drawArray = DoubleArray(5000)
                waveLineWidthRate = mWidth/5000
                row = 5000
            }
            1 -> {
                dataArray = DoubleArray(2500)
                pacemakerDataArray = BooleanArray(2500)
                needPacemakerDataArray = BooleanArray(2500)
                drawArray = DoubleArray(2500)
                waveLineWidthRate = mWidth/2500
                row = 2500
            }
            2 -> {
                dataArray = DoubleArray(1250)
                pacemakerDataArray = BooleanArray(1250)
                needPacemakerDataArray = BooleanArray(1250)
                drawArray = DoubleArray(1250)
                waveLineWidthRate = mWidth/1250
                row = 1250
            }
        }

    }

    /**
     * onDraw
     * a. 频繁的GC
     *      原因：内存抖动，瞬间产生大量的对象。
     *      1. 尽可能减少在for循环中new对象或在onDraw中创建对象等
     *      2. 尽量不要在循环中大量使用局部变量
     * b. 过度绘制
     *      1. 去除不必要的背景色
     *      2. 布局的扁平化处理
     *      3. 减少透明色的使用
     * c. UI线程的复杂运算
     *      1. 减少UI线程中的数据运算
     *
     * 參考文章 :
     * https://www.zhouhaoh.com/2021/07/03/Choreographer/
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        Log.d(TAG,"onDraw")
        if(!isLeadOFF) {
            canvas.drawPath(mPathHead, mWavePaint)
            if(!isFirstRun) {
                canvas.drawPath(mPathEnd, mWavePaint)
            }
            for(i in 0 until needPacemakerDataArray.size) {
                if(needPacemakerDataArray[i] == true) {
                    canvas.drawLine(
                        (i * waveLineWidthRate), 0f,
                        ((i + 1) * waveLineWidthRate), baseLine, peakPaint
                    )
                }
            }
        }
    }

    /**
     * 預先準備畫出Path 路徑(End)
     *
     * @param start
     * @param end
     * @param path
     */
    private fun drawPathFromData(start: Int, end: Int, path: Path) {
        path.reset()
        startECGData = (drawArray[start] * gainValue).toFloat()
        startY = (baseLine - (startECGData * twoGridLength))
        path.moveTo((start * waveLineWidthRate), startY)
        //Log.d(TAG,"baseLine: " + baseLine + " startY: " + startY)
        for (i in (start + 1) until (end)) {
            path.lineTo(
                (i * waveLineWidthRate),
                baseLine - ((drawArray[i] * gainValue) * twoGridLength).toFloat()
            )
        }

    }

    /**
     * 預先準備畫出Path 路徑(Head)，差別在於有沒有把Path進行reset的動作
     *
     * @param start
     * @param end
     * @param path
     */
    private fun drawPathFromDataHead(start: Int, end: Int, path: Path) {
        startECGData = (drawArray[start] * gainValue).toFloat()
        startY = (baseLine - (startECGData * twoGridLength))
        path.lineTo((start * waveLineWidthRate), startY)
        //Log.d(TAG,"baseLine: " + baseLine + " startY: " + startY)
        for (i in (start + 1) until (end)) {
            path.lineTo(
                (i * waveLineWidthRate),
                baseLine - ((drawArray[i] * gainValue) * twoGridLength).toFloat()
            )
        }
    }

    private fun refreshECGDataUI() {
        /**
         * 繪圖動態ECG 頭
         * */
        drawPathFromData(
            if (row - 1 - draw_index > 100) 102 else 100 - (row - 1 - draw_index),
            draw_index,
            mPathHead
        )

        /**
         * 繪圖動態ECG 尾
         * */
        if(!isFirstRun) {
            drawPathFromData(Math.min(draw_index + 100, row - 1), row - 1,
                mPathEnd)
        }

        for(i in pacemakerDataArray.indices) {
            needPacemakerDataArray[i] = pacemakerDataArray[i]
        }
    }

    private fun refreshECGDataUIWithTimerFourThreeR() {
        if(isCircleRun) {
            for(i in drawRecordIndexValue until dataArray.size){
                drawArray[i] = dataArray[i]
            }
            for(i in 0 until drawIndexValue){
                drawArray[i] = dataArray[i]
            }
            for(i in drawRecordIndexValue until dataArray.size) {
                needPacemakerDataArray[i] = pacemakerDataArray[i]
            }
            for(i in 0 until drawIndexValue) {
                needPacemakerDataArray[i] = pacemakerDataArray[i]
            }
        } else {
            for(i in drawRecordIndexValue until drawIndexValue){
                drawArray[i] = dataArray[i]
            }
            for(i in drawRecordIndexValue until drawIndexValue) {
                needPacemakerDataArray[i] = pacemakerDataArray[i]
            }
        }

        /**
         * 繪圖動態ECG 頭
         * */

        if(isCircleRun) {
            drawPathFromData(
                0,
                drawIndexValue,
                mPathHead
            )
            isCircleRun = false
        } else {
            drawPathFromDataHead(
                drawRecordIndexValue,
                drawIndexValue,
                mPathHead
            )
        }

        /**
         * 繪圖動態ECG 尾
         * */
        if(!isFirstRun) {
            drawPathFromData(Math.min(drawIndexValue + drawHeadIntervalTailValue, row - 1), row - 1,
                mPathEnd)
        }

    }

    private fun refreshECGDataUIWithTimer() {
        if(isCircleRun) {
            for(i in drawRecordIndexValue until dataArray.size){
                drawArray[i] = dataArray[i]
            }
            for(i in 0 until drawIndexValue){
                drawArray[i] = dataArray[i]
            }
            for(i in drawRecordIndexValue until dataArray.size) {
                needPacemakerDataArray[i] = pacemakerDataArray[i]
            }
            for(i in 0 until drawIndexValue) {
                needPacemakerDataArray[i] = pacemakerDataArray[i]
            }
        } else {
            for(i in drawRecordIndexValue until drawIndexValue){
                drawArray[i] = dataArray[i]
            }
            for(i in drawRecordIndexValue until drawIndexValue) {
                needPacemakerDataArray[i] = pacemakerDataArray[i]
            }
        }

        /**
         * 繪圖動態ECG 頭
         * */
        if(isCircleRun) {
            drawPathFromData(
                0,
                drawIndexValue,
                mPathHead
            )
            isCircleRun = false
        } else {
            drawPathFromDataHead(
                drawRecordIndexValue,
                drawIndexValue,
                mPathHead
            )
        }

        /**
         * 繪圖動態ECG 尾
         * */
        if(!isFirstRun) {
            drawPathFromData(Math.min(drawIndexValue + drawHeadIntervalTailValue, row - 1), row - 1,
                mPathEnd)
        }

    }

    /**
     * 添加新的数据
     */
    fun updateECGData(line: Double, isPacemaker: Boolean, gainValue:Float, isLeadOFF:Boolean) {
//        Log.d(TAG,"showLine: " + line)
        if(readyDrawECGData.compareAndSet(false,true)) {
            mPathHead.reset()
            mPathEnd.reset()
        }
        this.isLeadOFF = isLeadOFF
        if(this.gainValue != gainValue) {
            this.gainValue = gainValue
        }
        if(isLeadOFF) {
            if(!recordLeadOffHistory) {
                recordLeadOffHistory = true
                for (i in dataArray.indices) {
                    dataArray[i] = DEFAULT_INITIAL_VALUE
                }
                for (i in pacemakerDataArray.indices) {
                    pacemakerDataArray[i] = false
                }
                mPathHead.reset()
                mPathEnd.reset()
            }

            recordLeadOffHistoryIndex = draw_index

        } else {
            if(recordLeadOffHistory) {
                if(recordLeadOffHistoryIndex == draw_index) {
                    recordLeadOffHistory = false
                }
            }
            /** 循环模式数据添加至当前绘制的位 */
//        Log.d(TAG,"showLine: " + line + " draw_index: " + draw_index)
            dataArray[draw_index] = line
            pacemakerDataArray[draw_index] = isPacemaker
        }

        draw_index++
        if (draw_index >= row) {
//            Log.e(TAG,"Thread id: " + Thread.currentThread().id)
            draw_index = 0
        }

    }

    fun startRefreshDrawUI() {
        startRefreshUITask()
    }

    fun stopRefreshDrawUI() {
        stopRefreshUITask()
        isDrawRefreshJob = false
        readyDrawECGData.compareAndSet(true,false)
    }

    fun reset() {
        stopRefreshUITask()
        readyDrawECGData.compareAndSet(true,false)
        isDrawRefreshJob = false
        recordLeadOffHistory  = false
        recordLeadOffHistoryIndex = 0
        isFirstRun = true
        draw_index = 0
        drawIndexValue = 0
        drawRecordIndexValue = 0
        for (i in dataArray.indices) {
            dataArray[i] = DEFAULT_INITIAL_VALUE
        }
        for (i in drawArray.indices) {
            drawArray[i] = DEFAULT_INITIAL_VALUE
        }
        for (i in pacemakerDataArray.indices) {
            pacemakerDataArray[i] = false
        }
        refreshECGDataUI()
        //invalidate()
        postInvalidate()
    }

    private fun refreshUIThread() {
        Thread {
            while (isDrawRefreshJob) {
                if(readyDrawECGData.get()) {
                    when(ecgFormat) {
                        0,1 -> {
                            refreshECGDataUIWithTimer()
                        }
                        2 -> {
                            refreshECGDataUIWithTimerFourThreeR()
                        }
                    }
                    drawRecordIndexValue = drawIndexValue
                    drawIndexValue += drawIntervalIndexValue
                    if (drawIndexValue >= row) {
                        drawIndexValue = 0
                        isFirstRun = false
                        isCircleRun = true
                    }
                    //invalidate()
                    postInvalidate()
                    try {
                        Thread.sleep(drawRefresh.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        Thread.sleep(5)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
//                    Log.e(TAG,"refreshUIThread: " + readyDrawECGData.get())
                }
            }
        }.start()
    }

    fun setDrawLineColorType(type: Int) {
        if (type == 1) {
            mWavePaint.color = GREEN_LEAD_LINE_COLOR
        } else {
            mWavePaint.color = BLACK_LEAD_LINE_COLOR
        }
    }

    @Synchronized
    private fun startRefreshUITask() {
        if(refreshUITask == null) {
            val timerInitData = Timer()
            initialDrawECGDataTimerTask()
            timerInitData.schedule(refreshUITask, drawRefresh.toLong())
        }
    }

    @Synchronized
    private fun stopRefreshUITask() {
        if (refreshUITask != null) {
            refreshUITask!!.cancel()
            refreshUITask = null
        }
    }

    fun initialDrawECGDataTimerTask() {
        refreshUITask = object : TimerTask() {
            override fun run() {
                isDrawRefreshJob = true
                refreshUIThread()
            }
        }
    }

    companion object {
        /**
         * 波形颜色
         */
        private val BLACK_LEAD_LINE_COLOR = Color.parseColor("#ff000000") //黑色
        private val GREEN_LEAD_LINE_COLOR = Color.parseColor("#ff00fb00") //亮綠色
    }

}