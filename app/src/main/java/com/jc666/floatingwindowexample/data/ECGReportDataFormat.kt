package com.jc666.floatingwindowexample.data

data class ECGReportDataFormat(
    val ecg: List<Int>,
    val isPacemaker: Int,
    val leadOff: List<Int>
)