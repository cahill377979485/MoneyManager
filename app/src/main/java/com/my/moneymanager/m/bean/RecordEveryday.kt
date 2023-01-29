package com.my.moneymanager.m.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *
 * @author 文琳
 * @Email 377979485@qq.com
 * @desc
 * @time 2023-01-29 星期日 9:36
 *
 */
@Parcelize
data class RecordEveryday(
    var date: Int,
    var money: Float,
    var sum: Float
) : Parcelable