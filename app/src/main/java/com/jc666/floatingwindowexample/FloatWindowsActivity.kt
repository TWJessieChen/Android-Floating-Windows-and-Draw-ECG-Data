package com.jc666.floatingwindowexample

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.jc666.floatingwindowexample.data.DataValue.description
import com.jc666.floatingwindowexample.data.ECGDataBriteMEDParse
import com.jc666.floatingwindowexample.utils.Event
import com.jc666.floatingwindowexample.view.DynamicWaveEcgView
import com.jc666.floatingwindowexample.view.StaticECGBackgroundView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * FloatWindowsActivity 浮動式視窗 Service
 *
 */

class FloatWindowsActivity : Service() {
    private val TAG = FloatWindowsActivity::class.java.simpleName

    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var windowMag: WindowManager? = null
    private var btn_float_windows : Button? = null
    private var btn_save : Button? = null
    private var tv_title : TextView? = null
    private var etv_description : EditText? = null

    private var one_twelve_wave_view_i: DynamicWaveEcgView? = null
    private var iv_background_one_twelve_lead_i: StaticECGBackgroundView? = null

    private var dataParseResult: ECGDataBriteMEDParse? = null
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var isJob = false
    private var leadValue = 0
    private var leadValueIndex = 0

    //As FloatWindowsActivity inherits Service class, it actually overrides the onBind method
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()

        //The screen height and width are calculated, cause
        //the height and width of the floating window is set depending on this
        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        //To obtain a WindowManager of a different Display,
        //we need a Context for that display, so WINDOW_SERVICE is used
        windowMag = getSystemService(WINDOW_SERVICE) as WindowManager

        //A LayoutInflater instance is created to retrieve the LayoutInflater for the floating_layout xml

        //A LayoutInflater instance is created to retrieve the LayoutInflater for the floating_layout xml
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //inflate a new view hierarchy from the floating_layout xml
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup

        btn_float_windows = floatView!!.findViewById(R.id.btn_float_windows)
        btn_save = floatView!!.findViewById(R.id.btn_save)
        tv_title = floatView!!.findViewById(R.id.tv_title)
        etv_description = floatView!!.findViewById(R.id.etv_description)
        one_twelve_wave_view_i = floatView!!.findViewById(R.id.one_twelve_wave_view_i)
        iv_background_one_twelve_lead_i = floatView!!.findViewById(R.id.iv_background_one_twelve_lead_i)

        //Just like MainActivity, the text written in Maximized will stay
        etv_description!!.setText(description)
        etv_description!!.setSelection(description.length)
        etv_description!!.setCursorVisible(false)

        //WindowManager.LayoutParams takes a lot of parameters to set the
        //the parameters of the layout. One of them is Layout_type.
        LAYOUT_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //If API Level is more than 26, we need TYPE_APPLICATION_OVERLAY
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            //If API Level is lesser than 26, then we can use TYPE_SYSTEM_ERROR,
            //TYPE_SYSTEM_OVERLAY, TYPE_PHONE, TYPE_PRIORITY_PHONE. But these are all
            //deprecated in API 26 and later. Here TYPE_TOAST works best.
            WindowManager.LayoutParams.TYPE_TOAST
        }

        //Now the Parameter of the floating-window layout is set.
        //1) The Width of the window will be 55% of the phone width.
        //2) The Height of the window will be 58% of the phone height.
        //3) Layout_Type is already set.
        //4) Next Parameter is Window_Flag. Here FLAG_NOT_FOCUSABLE is used. But
        //problem with this flag is key inputs can't be given to the EditText.
        //This problem is solved later.
        //5) Next parameter is Layout_Format. System chooses a format that supports translucency by PixelFormat.TRANSLUCENT
        floatWindowLayoutParam = WindowManager.LayoutParams(
            (width * 0.55f).toInt(), (height * 0.58f).toInt(),
            LAYOUT_TYPE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //The Gravity of the Floating Window is set. The Window will appear in the center of the screen
        floatWindowLayoutParam!!.gravity = Gravity.CENTER
        //X and Y value of the window is set
        floatWindowLayoutParam!!.x = 0
        floatWindowLayoutParam!!.y = 0

        //The ViewGroup that inflates the floating_layout.xml is
        //added to the WindowManager with all the parameters
        windowMag!!.addView(floatView, floatWindowLayoutParam)

        etv_description!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                description = s.toString()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        //Floating Window Layout Flag is set to FLAG_NOT_FOCUSABLE, so no input is possible to the EditText. But that's a problem.
        //So, the problem is solved here. The Layout Flag is changed when the EditText is touched.
        etv_description!!.setOnTouchListener(OnTouchListener { v, event ->
            etv_description!!.setCursorVisible(true)
            val floatWindowLayoutParamUpdateFlag: WindowManager.LayoutParams =
                floatWindowLayoutParam as WindowManager.LayoutParams
            //Layout Flag is changed to FLAG_NOT_TOUCH_MODAL which helps to take inputs inside floating window, but
            //while in EditText the back button won't work and FLAG_LAYOUT_IN_SCREEN flag helps to keep the window
            //always over the keyboard
            floatWindowLayoutParamUpdateFlag.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            //WindowManager is updated with the Updated Parameters
            windowMag!!.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag)
            false
        })

        btn_save!!.setOnClickListener {
            Log.d(TAG,"btn_save")

            description = etv_description!!.text.toString()
            etv_description!!.setCursorVisible(false)
            etv_description!!.clearFocus()
            Toast.makeText(this@FloatWindowsActivity, "Text Saved!!!\n" + description, Toast.LENGTH_SHORT).show()
        }

        btn_float_windows!!.setOnClickListener {

            //stopSelf() method is used to stop the service if
            //it was previously started
            stopSelf()
            //The window is removed from the screen
            windowMag!!.removeView(floatView)
            //The app will maximize again. So the MainActivity class will be called again.
            val backToHome = Intent(this@FloatWindowsActivity, MainActivity::class.java)
            //1) FLAG_ACTIVITY_NEW_TASK flag helps activity to start a new task on the history stack.
            //If a task is already running like the floating window service, a new activity will not be started.
            //Instead the task will be brought back to the front just like the MainActivity here
            //2) FLAG_ACTIVITY_CLEAR_TASK can be used in the conjunction with FLAG_ACTIVITY_NEW_TASK. This flag will
            //kill the existing task first and then new activity is started.
            backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(backToHome)

        }

        //Another feature of the floating window is, the window is movable.
        //The window can be moved at any position on the screen.
        floatView!!.setOnTouchListener(object : OnTouchListener {
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
                        //returns the original raw X coordinate of this event
                        px = event.rawX.toDouble()
                        //returns the original raw Y coordinate of this event
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        floatWindowLayoutUpdateParam.x = (x + event.rawX - px).toInt()
                        floatWindowLayoutUpdateParam.y = (y + event.rawY - py).toInt()

                        //updated parameter is applied to the WindowManager
                        windowMag!!.updateViewLayout(floatView, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })


        /**
         * ECG init
         * */
        dataParseResult = ECGDataBriteMEDParse(this@FloatWindowsActivity)
        one_twelve_wave_view_i!!.setDrawLineColorType(1)
        iv_background_one_twelve_lead_i!!.setBackgroundParams(1, 1F)

        initSimulatorECG()
    }

    fun initSimulatorECG() {
        isJob = true
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                while (isJob) {
//                    Log.d(TAG,"run")
                    try {
                        Thread.sleep(1)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    one_twelve_wave_view_i!!.updateECGData(dataParseResult!!.valuesOneLeadTest.get(leadValueIndex).ecg[leadValue].toDouble()/13981f, false, 1f, false)
                    leadValueIndex++
                    if(leadValueIndex >= dataParseResult!!.valuesOneLeadTest.size) {
                        leadValueIndex = 0
                    }
                }
            }
        }
        timer!!.schedule(timerTask, 500, 50)
        one_twelve_wave_view_i!!.startRefreshDrawUI()
    }

    fun stopSimulatorECG() {
        one_twelve_wave_view_i!!.stopRefreshDrawUI()
        isJob = false
        leadValueIndex = 0
        if(timer != null) {
            timerTask!!.cancel()
            timer!!.cancel()
        }
        timerTask = null
        timer = null
    }

    //It is called when stopService() method is called in MainActivity
    override fun onDestroy() {
        super.onDestroy()
        stopSimulatorECG()
        stopSelf()
        //Window is removed from the screen
        windowMag!!.removeView(floatView)
    }

}