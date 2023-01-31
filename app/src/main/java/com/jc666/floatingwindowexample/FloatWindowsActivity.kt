package com.jc666.floatingwindowexample

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.jc666.floatingwindowexample.data.DataValue.description
import com.jc666.floatingwindowexample.data.ECGDataBriteMEDParse
import com.jc666.floatingwindowexample.view.DynamicWaveEcgView
import com.jc666.floatingwindowexample.view.StaticECGBackgroundView
import java.util.*

/**
 * FloatWindowsActivity 浮動式視窗 Service
 *
 */

class FloatWindowsActivity : Service() {
    private val TAG = FloatWindowsActivity::class.java.simpleName

    private lateinit var floatView: ViewGroup
    private var layoutType = 0
    private lateinit var floatWindowLayoutParam: WindowManager.LayoutParams
    private lateinit var windowMag: WindowManager
    private lateinit var btnFloatWindows : Button
    private lateinit var btnSave : Button
    private lateinit var tvTitle : TextView
    private lateinit var etvDescription : EditText

    private lateinit var oneTwelveWaveViewI: DynamicWaveEcgView
    private lateinit var ivBackgroundOneTwelveLeadI: StaticECGBackgroundView

    private lateinit var dataParseResult: ECGDataBriteMEDParse
    private lateinit var timer: Timer
    private lateinit var timerTask: TimerTask
    private var isJob = false
    private var leadValue = 0
    private var leadValueIndex = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initComponent()
        initFuncAndListener()
        initSimulatorECG()
    }

    private fun initComponent() {
        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        windowMag = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup

        btnFloatWindows = floatView.findViewById(R.id.btn_float_windows)
        btnSave = floatView.findViewById(R.id.btn_save)
        tvTitle = floatView.findViewById(R.id.tv_title)
        etvDescription = floatView.findViewById(R.id.etv_description)
        oneTwelveWaveViewI = floatView.findViewById(R.id.one_twelve_wave_view_i)
        ivBackgroundOneTwelveLeadI = floatView.findViewById(R.id.iv_background_one_twelve_lead_i)

        etvDescription.setText(description)
        etvDescription.setSelection(description.length)
        etvDescription.setCursorVisible(false)

        layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_TOAST
        }

        floatWindowLayoutParam = WindowManager.LayoutParams(
            (width * 0.55f).toInt(), (height * 0.58f).toInt(),
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        floatWindowLayoutParam.gravity = Gravity.CENTER
        floatWindowLayoutParam.x = 0
        floatWindowLayoutParam.y = 0

        windowMag.addView(floatView, floatWindowLayoutParam)

        dataParseResult = ECGDataBriteMEDParse(this@FloatWindowsActivity)
        oneTwelveWaveViewI.setDrawLineColorType(1)
        ivBackgroundOneTwelveLeadI.setBackgroundParams(1, 1F)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFuncAndListener() {
        etvDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                description = s.toString()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        etvDescription.setOnTouchListener(OnTouchListener { v, event ->
            etvDescription.setCursorVisible(true)
            val floatWindowLayoutParamUpdateFlag: WindowManager.LayoutParams =
                floatWindowLayoutParam
            floatWindowLayoutParamUpdateFlag.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            windowMag.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag)
            false
        })

        btnSave.setOnClickListener {
            description = etvDescription.text.toString()
            etvDescription.setCursorVisible(false)
            etvDescription.clearFocus()
            Toast.makeText(this@FloatWindowsActivity, "Text Saved!!!\n" + description, Toast.LENGTH_SHORT).show()
        }

        btnFloatWindows.setOnClickListener {
            stopSelf()
            windowMag.removeView(floatView)
            val backToHome = Intent(this@FloatWindowsActivity, MainActivity::class.java)
            backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(backToHome)
        }

        floatView.setOnTouchListener(object : OnTouchListener {
            val floatWindowLayoutUpdateParam: WindowManager.LayoutParams = floatWindowLayoutParam as WindowManager.LayoutParams
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = floatWindowLayoutUpdateParam.x.toDouble()
                        y = floatWindowLayoutUpdateParam.y.toDouble()
                        px = event.rawX.toDouble()
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        floatWindowLayoutUpdateParam.x = (x + event.rawX - px).toInt()
                        floatWindowLayoutUpdateParam.y = (y + event.rawY - py).toInt()
                        windowMag.updateViewLayout(floatView, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })
    }

    fun initSimulatorECG() {
        isJob = true
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                while (isJob) {
                    try {
                        Thread.sleep(1)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    oneTwelveWaveViewI.updateECGData(dataParseResult.valuesOneLeadTest.get(leadValueIndex).ecg[leadValue].toDouble()/13981f, false, 1f, false)
                    leadValueIndex++
                    if(leadValueIndex >= dataParseResult.valuesOneLeadTest.size) {
                        leadValueIndex = 0
                    }
                }
            }
        }
        timer.schedule(timerTask, 500, 50)
        oneTwelveWaveViewI.startRefreshDrawUI()
    }

    fun stopSimulatorECG() {
        oneTwelveWaveViewI.stopRefreshDrawUI()
        isJob = false
        leadValueIndex = 0
        timerTask.cancel()
        timer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSimulatorECG()
        stopSelf()
        windowMag!!.removeView(floatView)
    }

}