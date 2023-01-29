package com.my.moneymanager.m.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author 文琳
 * @time 2021/1/5 9:57
 * @desc 物品
 */
@Parcelize
data class Record(
    var position: Int = 0,
    var createTime: String,
    var date: String,
    var desc: String = "",
    var money: String,
    var sum: Int = 0
) : Parcelable
