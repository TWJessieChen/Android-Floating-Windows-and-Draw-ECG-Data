package com.jc666.floatingwindowexample

import androidx.activity.viewModels
import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jc666.floatingwindowexample.data.DataValue.description
import com.jc666.floatingwindowexample.view.DynamicWaveEcgView
import com.jc666.floatingwindowexample.view.StaticECGBackgroundView
import kotlinx.coroutines.*

/**
 * MainActivity 整版畫面
 */

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    private val mainViewModel: MainViewModel by viewModels()

    private var btn_float_windows : Button? = null
    private var btn_save : Button? = null
    private var tv_title : TextView? = null
    private var etv_description : EditText? = null
    private var dialog: AlertDialog? = null
    private var one_twelve_wave_view_i: DynamicWaveEcgView? = null
    private var iv_background_one_twelve_lead_i: StaticECGBackgroundView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_float_windows = findViewById(R.id.btn_float_windows)
        btn_save = findViewById(R.id.btn_save)
        tv_title = findViewById(R.id.tv_title)
        etv_description = findViewById(R.id.etv_description)
        one_twelve_wave_view_i = findViewById(R.id.one_twelve_wave_view_i)
        iv_background_one_twelve_lead_i = findViewById(R.id.iv_background_one_twelve_lead_i)

        mainViewModel.initECGData(this@MainActivity)

        //If the app is started again while the floating window service is running
        //then the floating window service will stop
        if (isMyServiceRunning()) {
            //onDestroy() method in FloatingWindowGFG class will be called here
            stopService(Intent(this@MainActivity, FloatWindowsActivity::class.java))
        }

        //currentDesc String will be empty at first time launch
        //but the text written in floating window will not gone

        //currentDesc String will be empty at first time launch
        //but the text written in floating window will not gone
        etv_description!!.setText(description)
        etv_description!!.setSelection(description.length)

        etv_description!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                description = s.toString()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        btn_save!!.setOnClickListener {
            Log.d(TAG,"btn_save")
            lifecycleScope.launch {
                description = etv_description!!.text.toString()
                etv_description!!.setCursorVisible(false)
                etv_description!!.clearFocus()
                Toast.makeText(this@MainActivity, "Text Saved!!!\n" + description, Toast.LENGTH_SHORT).show()
            }
        }

        btn_float_windows!!.setOnClickListener {
            lifecycleScope.launch {

                //First it confirms whether the 'Display over other apps' permission in given
                if (checkOverlayDisplayPermission()) {
                    //FloatingWindowGFG service is started
                    startService(Intent(this@MainActivity, FloatWindowsActivity::class.java))
                    //The MainActivity closes here
                    finish()
                } else {
                    //If permission is not given, it shows the AlertDialog box and
                    //redirects to the Settings
                    requestOverlayDisplayPermission()
                }
            }
        }

        /**
         * ECG init
         * */
        one_twelve_wave_view_i!!.setDrawLineColorType(0)
        iv_background_one_twelve_lead_i!!.setBackgroundParams(0, 1F)

        mainViewModel.leadECGFormatOneTwelve.observe(this, {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                lifecycleScope.launch(Dispatchers.IO) {
                    one_twelve_wave_view_i!!.updateECGData(it.toDouble()/13981f, false, 1f, false)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.initSimulatorECG()
        one_twelve_wave_view_i!!.startRefreshDrawUI()
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.stopSimulatorECG()
        one_twelve_wave_view_i!!.stopRefreshDrawUI()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    private fun isMyServiceRunning(): Boolean {
        //The ACTIVITY_SERVICE is needed to retrieve a ActivityManager for interacting with the global system
        //It has a constant String value "activity".
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        //A loop is needed to get Service information that are currently running in the System.
        //So ActivityManager.RunningServiceInfo is used. It helps to retrieve a
        //particular service information, here its this service.
        //getRunningServices() method returns a list of the services that are currently running
        //and MAX_VALUE is 2147483647. So at most this many services can be returned by this method.
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            //If this service is found as a running, it will return true or else false.
            if (FloatWindowsActivity::class.java.getName() == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun requestOverlayDisplayPermission() {
        //An AlertDialog is created
        val builder = AlertDialog.Builder(this)
        //This dialog can be closed, just by taping anywhere outside the dialog-box
        builder.setCancelable(true)
        //The title of the Dialog-box is set
        builder.setTitle("Screen Overlay Permission Needed")
        //The message of the Dialog-box is set
        builder.setMessage("Enable 'Display over other apps' from System Settings.")
        //The event of the Positive-Button is set
        builder.setPositiveButton(
            "Open Settings"
        ) { dialog, which -> //The app will redirect to the 'Display over other apps' in Settings.
            //This is an Implicit Intent. This is needed when any Action is needed to perform, here it is
            //redirecting to an other app(Settings).
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            //This method will start the intent. It takes two parameter, one is the Intent and the other is
            //an requestCode Integer. Here it is -1.
            startActivityForResult(intent, RESULT_OK)
        }
        dialog = builder.create()
        //The Dialog will show in the screen
        dialog!!.show()
    }

    private fun checkOverlayDisplayPermission(): Boolean {
        //Android Version is lesser than Marshmallow or the API is lesser than 23
        //doesn't need 'Display over other apps' permission enabling.
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //If 'Display over other apps' is not enabled it will return false or else true
            if (!Settings.canDrawOverlays(this)) {
                false
            } else {
                true
            }
        } else {
            true
        }
    }

}