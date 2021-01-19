package com.my.moneymanager.m.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

/**
 * @author 文琳
 * @time 2021/1/5 11:01
 * @desc 传递消息对象
 */
@Parcelize
class TypeData(val name: String, val data: @RawValue Any) : Parcelable