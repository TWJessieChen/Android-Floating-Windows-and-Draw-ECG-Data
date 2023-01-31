package com.jc666.floatingwindowexample

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jc666.floatingwindowexample.data.ECGDataBriteMEDParse
import com.jc666.floatingwindowexample.utils.Event
import kotlinx.coroutines.*
import java.util.*

/**
 *
 * ViewModel架構
 * 使用LiveData
 * 使用CoroutineScope
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
    private lateinit var dataParseResult: ECGDataBriteMEDParse
    private lateinit var timer: Timer
    private lateinit var timerTask: TimerTask
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
                    _leadECGFormatOneTwelve.postValue(Event(dataParseResult.valuesOneLeadTest.get(leadValueIndex).ecg[leadValue]))
                    leadValueIndex++
                    if(leadValueIndex >= dataParseResult.valuesOneLeadTest.size) {
                        leadValueIndex = 0
                    }
                }
            }
        }
        timer.schedule(timerTask, 500, 50)
    }

    fun stopSimulatorECG() {
        isJob = false
        leadValueIndex = 0
        timerTask.cancel()
        timer.cancel()
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