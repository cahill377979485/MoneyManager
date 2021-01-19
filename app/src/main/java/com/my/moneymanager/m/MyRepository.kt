package com.my.moneymanager.m

import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.my.moneymanager.m.bean.Record
import com.my.moneymanager.m.bean.Records
import com.my.moneymanager.xutil.MyUtil
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

private const val DATA: String = "DATA"

/**
 * @author 文琳
 * @time 2021/1/5 11:23
 * @desc 资料库，用于MVVM作为Model来提供数据，只在ViewModel中使用，不在View中使用，换句话说，不要在Activity或者Fragment中创建此实例
 */
class MyRepository {

    val recordList: ArrayList<Record>?
        get() {
            var list: ArrayList<Record> = ArrayList()
            val recordsStr: String = SPUtils.getInstance().getString(DATA)
            if (recordsStr.isNotEmpty()) {
                try {
                    val records: Records? = Gson().fromJson(recordsStr, Records::class.java)
                    records?.let { list = records.list as ArrayList<Record> }
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
            return list
        }

    /**
     * 保存到本地缓存中
     */
    fun save(list: List<Record>?) {
        try {
            val str = Gson().toJson(Records(list), Records::class.java)
            if (str.isNotEmpty()) SPUtils.getInstance().put(DATA, str)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
    }

    /**
     * 添加数据并保存
     */
    fun add(record: Record) {
        recordList?.let {
            it.add(0, record)
            save(it)
        }
    }

    /**
     * 删除
     */
    fun delete(createTime: String) {
        recordList?.let {
            for (i in it.indices) {
                val t: Record = it[i]
                if (t.createTime.equals(createTime, true)) {
                    it.remove(t)
                    break
                }
            }
            save(it)
        }
    }

    /**
     * 更新
     */
    fun update(updatePosition: Int, str: String) {
        recordList?.let {
            val arr: Array<String> = MyUtil.getDateDescAndMoneyArrayByRegex(str)
            it[updatePosition].date = arr[0]
            it[updatePosition].desc = arr[1]
            it[updatePosition].money = arr[2]
            save(it)
        }
    }
}