package com.my.moneymanager.m.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author 文琳
 * @time 2021/1/5 10:00
 * @desc 物品们
 */
@Parcelize
data class Records(var list: List<Record>?, var totalStart: Float = 0f) : Parcelable