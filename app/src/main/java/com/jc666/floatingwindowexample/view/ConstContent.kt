package com.jc666.floatingwindowexample.view

import android.util.Log
import java.io.*

/**
 *
 * Contants
 * const val : 當定義常量時，出於效率考慮，我們應該使用const val方式，避免頻繁函式呼叫。
 *
 * @author JC666
 */

object ConstContent {

    const val KEY_ECG_VIEW_ONE_TWELVE_TAG = "key_ecg_view_one_twelve_tag"
    const val KEY_ECG_VIEW_TWO_SIX_TAG = "key_ecg_view_two_six_tag"
    const val KEY_ECG_VIEW_FOUR_THREE_TAG = "key_ecg_view_four_three_tag"
    const val KEY_ECG_VIEW_FOUR_THREE_FOCUS_TAG = "key_ecg_view_four_three_focus_tag"

    const val KEY_ECG_DATA_VIEW_FOUR_THREE_I_TAG = "key_ecg_data_view_four_three_i_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_II_TAG = "key_ecg_data_view_four_three_ii_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_III_TAG = "key_ecg_data_view_four_three_iii_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_AVR_TAG = "key_ecg_data_view_four_three_avr_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_AVL_TAG = "key_ecg_data_view_four_three_avl_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_AVF_TAG = "key_ecg_data_view_four_three_avf_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_V1_TAG = "key_ecg_data_view_four_three_v1_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_V2_TAG = "key_ecg_data_view_four_three_v2_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_V3_TAG = "key_ecg_data_view_four_three_v3_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_V4_TAG = "key_ecg_data_view_four_three_v4_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_V5_TAG = "key_ecg_data_view_four_three_v5_tag"
    const val KEY_ECG_DATA_VIEW_FOUR_THREE_V6_TAG = "key_ecg_data_view_four_three_v6_tag"

    const val ORIENTATION_LANDSCAPE = 2
    const val ORIENTATION_PORTRAIT = 1

    const val GAIN_I_DESCRIPTION = "25mm/1S   10mm/0.5mv"
    const val GAIN_II_DESCRIPTION = "25mm/1S   10mm/1mv"
    const val GAIN_III_DESCRIPTION = "25mm/1S   10mm/2mv"
    const val GAIN_IV_DESCRIPTION = "25mm/1S   10mm/4mv"

    const val GAIN_I_UNIT = "0.5mv"
    const val GAIN_II_UNIT = "1mv"
    const val GAIN_III_UNIT = "2mv"
    const val GAIN_IV_UNIT = "4mv"

    fun writeFileIsAppend(data: String?) {
        try {
            val outputFile = File(
                "/data/user/0/tw.com.britemed.ecg/files/AnalECGData/",
                "DynamicWaveEcgView.txt"
            )
            val writer: Writer = BufferedWriter(FileWriter(outputFile, true))
            writer.write(data)
            writer.close()
        } catch (e: IOException) {
            Log.w("", e.message, e)
        }
    }
}