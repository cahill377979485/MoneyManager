package com.my.moneymanager.v.binder

import com.my.moneymanager.m.bean.Record

/**
 * @author 文琳
 * @time 2021/1/5 16:00
 * @desc 物品的点击事件
 */
interface ClickRecord {
    fun onClick(record: Record)
}