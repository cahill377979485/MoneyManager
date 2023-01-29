package com.my.moneymanager.m.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *
 * @author 文琳
 * @Email 377979485@qq.com
 * @desc
 * @time 2023-01-28 星期六 18:16
 *
 */
@Parcelize
data class Interest(
    val FSRQ: String,
    val DWJZ: Float
) : Parcelable