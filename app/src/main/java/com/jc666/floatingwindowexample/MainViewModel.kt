package com.jc666.floatingwindowexample

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.jc666.floatingwindowexample.data.ECGDataBriteMEDParse
import com.jc666.floatingwindowexample.utils.Event
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean


/**
 *
 * ViewModel架構
 * 使用LiveData
 * 使用CoroutineScope
 *
 *  setting Low Pass Filter value(40, 150, 200, OFF = 0)
 *  setting High Pass Filter value(0.05F, 0.15F)
 *  setting AC Filter value(50, 60, OFF = 0)
 *  setting Pacemaker Detection value(ON = 1, OFF = 0)
 *  setting gain value Gain(0.5F, 1F, 2F, 4F)
 *
 *  紀錄此帳號心電圖形格式(1x12, 2x6, 4x3+R), 對應的0, 1, 2
 *  紀錄此帳號電子報告顯示色彩( 背景(白)-波形線條(黑) = 0, 背景(黑)-波形線條(綠) = 1 )
 *
 */

class MainViewModel()  : ViewModel() {
    private val TAG = MainViewModel::class.java.simpleName

    /**
     * LiveData
     * */
    val errorMessage: LiveData<Event<String>> get() = _errorMessage
    private val _errorMessage = MutableLiveData<Event<String>>()

    val loading: LiveData<Event<Boolean>> get() = _loading
    private val _loading = MutableLiveData<Event<Boolean>>()

    val leadECGFormatOneTwelve: LiveData<Event<Int>> get() = _leadECGFormatOneTwelve
    private val _leadECGFormatOneTwelve = MutableLiveData<Event<Int>>()


    /**
     * 內部使用變數
     * */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    private val ioScope = CoroutineScope(Dispatchers.IO + exceptionHandler)

    private var dataParseResult: ECGDataBriteMEDParse? = null

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var isJob = false
    private var leadValue = 0
    private var leadValueIndex = 0

    /**
     * Function
     * */
    fun initECGData(context: Context) {
        dataParseResult = ECGDataBriteMEDParse(context)
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
                    _leadECGFormatOneTwelve.postValue(Event(dataParseResult!!.valuesOneLeadTest.get(leadValueIndex).ecg[leadValue]))
                    leadValueIndex++
                    if(leadValueIndex >= dataParseResult!!.valuesOneLeadTest.size) {
                        leadValueIndex = 0
                    }
                }
            }
        }
        timer!!.schedule(timerTask, 500, 50)
    }

    fun stopSimulatorECG() {
        isJob = false
        leadValueIndex = 0
        if(timer != null) {
            timerTask!!.cancel()
            timer!!.cancel()
        }
        timerTask = null
        timer = null
    }

    private fun onError(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _errorMessage.value = Event(message)
            _loading.value = Event(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ioScope.cancel()
        viewModelScope.cancel()
    }

}