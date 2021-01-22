package com.my.moneymanager.vm

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.my.moneymanager.m.MyRepository
import com.my.moneymanager.m.bean.Record
import com.my.moneymanager.m.bean.Records
import com.my.moneymanager.v.ChartActivity
import com.my.moneymanager.xutil.MyUtil
import com.my.moneymanager.xutil.ToastUtils
import kotlinx.coroutines.*
import java.util.*

/**
 * @author 文琳
 * @time 2021/1/5 10:45
 * @desc 主页面的ViewModel
 */
class MainVM(application: Application) : AndroidViewModel(application) {
    var dataList: MutableLiveData<List<Record>> = MutableLiveData()//记录列表数据
    var totalStr: MutableLiveData<String> = MutableLiveData()//总价值统计文本
    var etText: MutableLiveData<String> = MutableLiveData()//输入框的文本
    var searchFlag: MutableLiveData<Boolean> = MutableLiveData()//搜索模式开关
    var updateFlag: MutableLiveData<Boolean> = MutableLiveData()//更新模式开关
    var refreshFlag: MutableLiveData<Boolean> = MutableLiveData()//刷新开关
    private var updatePosition: Int = 0//更新的位置
    private val repository by lazy { MyRepository() }//本地存储工具类
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (searchFlag.value != true) return
            etText.value?.let {
                if (it.isEmpty()) refreshData() else afterTextChanged(s.toString())
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    init {
        searchFlag.value = false
        updateFlag.value = false
    }

    fun getData() {
        runBlocking {

        }
        GlobalScope.launch {
            delay(1000)
        }
    }

    /**
     * 从本地获取并刷新数据
     */
    fun refreshData() {
        repository.recordList?.let { updateDataAndTotalStr(it, "总额") }
        refreshFlag.postValue(true)
    }

    /**
     * 添加记录
     */
    private fun addRecord(str: String) {
        val arr: Array<String> = MyUtil.getDateDescAndMoneyArrayByRegex(str)
        repository.add(Record(0, MyUtil.getFormatCreateTime(), arr[0], arr[1], arr[2]))
        searchFlag.postValue(false)
        etText.postValue("")
        refreshData()
    }

    /**
     * 删除记录
     */
    fun deleteRecord(createTime: String) {
        repository.delete(createTime)
        refreshData()
    }

    /**
     * 更新记录
     */
    private fun updateRecord(str: String) {
        repository.update(updatePosition, str)
        refreshData()
        etText.postValue("")
        updateFlag.postValue(false)
    }

    /**
     * 为更新做准备
     */
    fun tryUpdateRecord(record: Record) {
        updateFlag.postValue(true)
        updatePosition = -1
        repository.recordList?.let { list ->
            for (i in list.indices) {
                if (list[i].createTime.equals(record.createTime, ignoreCase = true)) {
                    updatePosition = i
                    break
                }
            }
        }
    }

    /**
     * 更新数据并且更新价值文本
     */
    private fun updateDataAndTotalStr(list: ArrayList<Record>, preString: String) {
        var total = 0f
        var totalIn = 0f
        var totalOut = 0f
        list.let {
            for (i in it.indices) {
                it[i].position = it.size - 1 - i
                val moneyWith100Times = it[i].money.toFloat().times(100)
                total += moneyWith100Times
                if (moneyWith100Times < 0) totalOut += moneyWith100Times
                else totalIn += moneyWith100Times
            }
            dataList.postValue(it)//这句因为数据改变，在MainActivity中被观察到，所以会自动更新列表
        }
        val strTotal: String = (preString + total / 100).replace(".0+$".toRegex(), "")
        val strIn: String = (" 收" + totalIn / 100).replace(".0+$".toRegex(), "")
        val strOut: String = (" 支" + totalOut / 100).replace(".0+$".toRegex(), "")
        totalStr.postValue(strTotal + strIn + strOut)
    }

    /**
     * 监听输入框文本，筛选内容显示在列表中并计算价值小计同步更新UI
     */
    private fun afterTextChanged(s: String) {
        repository.recordList?.let { list ->
            list.filter {
                val str =
                    "^" + it.date + it.desc + if (it.money.contains("-")) it.money else "+" + it.money + "$"
                str.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))
            }.apply {
                updateDataAndTotalStr(this as ArrayList<Record>, "总额小计")
            }
        }
    }

    /**
     * 点击了录入按钮，在更新状态下是更新按钮
     */
    fun clickInput(v: View) {
        etText.value?.let {
            if (it.isEmpty()) {
                ToastUtils.instance?.apply { showInfo("请先输入") }
                refreshData()
            } else {
                if (updateFlag.value == true) updateRecord(it) else addRecord(it)
            }
        }
    }

    /**
     * 点击了总额
     */
    fun clickChart(v: View) {
        if (dataList.value == null || (dataList.value as List<Record>).isEmpty()) {
            ToastUtils.instance?.showInfo("数据不够")
            return
        }
        val first = dataList.value!![dataList.value!!.size - 1].createTime
        val localList: ArrayList<Record>? = repository.recordList
        val list = localList?.asReversed()//逆序
        list?.let {
            var totalStart = 0f
            for (i in it.indices) {
                if (it[i].createTime == first) break
                val money = it[i].money.toFloat()
                totalStart -= money
            }
            val records = Records(dataList.value!!.asReversed(), totalStart)
            val bundle = Bundle()
            bundle.putParcelable("DATA", records)
            val context: Context = v.context
            val intent = Intent(context, ChartActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    /**
     * 点击了实时搜索复选框
     */
    fun clickCheckBox(v: View) {
        searchFlag.value?.let {
            searchFlag.postValue(!it)
        }
    }

    /**
     * 点击了清除按钮，清除输入框中已输入的文本
     */
    fun clickClear(v: View) {
        etText.postValue("")
    }
}