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
 * 一開始起始畫面會先判斷是否有啟動過Floating Windows功能
 * 還有切換畫面的時候，會判斷是否有開啟Permission
 *
 *
 * 2023/01/31 JC666
 */

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var btnFloatWindows : Button
    private lateinit var btnSave : Button
    private lateinit var tvTitle : TextView
    private lateinit var etvDescription : EditText
    private lateinit var oneTwelveWaveViewI: DynamicWaveEcgView
    private lateinit var ivBackgroundOneTwelveLeadI: StaticECGBackgroundView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponent()

        if (isMyServiceRunning()) {
            stopService(Intent(this@MainActivity, FloatWindowsActivity::class.java))
        }

        initFuncAndListener()
    }

    private fun initComponent() {
        btnFloatWindows = findViewById(R.id.btn_float_windows)
        btnSave = findViewById(R.id.btn_save)
        tvTitle = findViewById(R.id.tv_title)
        etvDescription = findViewById(R.id.etv_description)
        oneTwelveWaveViewI = findViewById(R.id.one_twelve_wave_view_i)
        ivBackgroundOneTwelveLeadI = findViewById(R.id.iv_background_one_twelve_lead_i)
    }

    private fun initFuncAndListener() {
        mainViewModel.initECGData(this@MainActivity)
        etvDescription.setText(description)
        etvDescription.setSelection(description.length)
        oneTwelveWaveViewI.setDrawLineColorType(0)
        ivBackgroundOneTwelveLeadI.setBackgroundParams(0, 1F)

        etvDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                description = s.toString()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        btnSave.setOnClickListener {
            lifecycleScope.launch {
                description = etvDescription.text.toString()
                etvDescription.setCursorVisible(false)
                etvDescription.clearFocus()
                Toast.makeText(this@MainActivity, "Text Saved!!!\n" + description, Toast.LENGTH_SHORT).show()
            }
        }

        btnFloatWindows.setOnClickListener {
            lifecycleScope.launch {
                if (checkOverlayDisplayPermission()) {
                    startService(Intent(this@MainActivity, FloatWindowsActivity::class.java))
                    finish()
                } else {
                    requestOverlayDisplayPermission()
                }
            }
        }

        mainViewModel.leadECGFormatOneTwelve.observe(this) {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                lifecycleScope.launch(Dispatchers.IO) {
                    oneTwelveWaveViewI.updateECGData(it.toDouble() / 13981f, false, 1f, false)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.initSimulatorECG()
        oneTwelveWaveViewI!!.startRefreshDrawUI()
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.stopSimulatorECG()
        oneTwelveWaveViewI!!.stopRefreshDrawUI()
    }

    private fun isMyServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (FloatWindowsActivity::class.java.getName() == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun requestOverlayDisplayPermission() {
        AlertDialog.Builder(this)
            .setCancelable(true)
            .setTitle("Screen Overlay Permission Needed")
            .setMessage("Enable 'Display over other apps' from System Settings.")
            .setPositiveButton(
                    "Open Settings"
                    ) { dialog, which ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, RESULT_OK)
            }.create().show()
    }

    private fun checkOverlayDisplayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

}