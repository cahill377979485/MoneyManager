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
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.my.moneymanager.m.MyRepository
import com.my.moneymanager.m.bean.Interest
import com.my.moneymanager.m.bean.Record
import com.my.moneymanager.m.bean.RecordEveryday
import com.my.moneymanager.m.bean.Records
import com.my.moneymanager.v.ChartActivity
import com.my.moneymanager.xutil.MyUtil
import com.my.moneymanager.xutil.ToastUtils
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author 文琳
 * @time 2021/1/5 10:45
 * @desc 主页面的ViewModel
 */
class MainVM(application: Application) : AndroidViewModel(application) {
    var dataList: MutableLiveData<List<Record>> = MutableLiveData()//记录列表数据
    var totalStr: MutableLiveData<String> = MutableLiveData()//总价值统计文本
    var totalInterestStr: MutableLiveData<String> = MutableLiveData()//总利息统计文本
    var etText: MutableLiveData<String> = MutableLiveData()//输入框的文本
    var searchFlag: MutableLiveData<Boolean> = MutableLiveData()//搜索模式开关
    var updateFlag: MutableLiveData<Boolean> = MutableLiveData()//更新模式开关
    var refreshFlag: MutableLiveData<Boolean> = MutableLiveData()//刷新开关
    private var updatePosition: Int = 0//更新的位置
    private val repository by lazy { MyRepository() }//本地存储工具类
    private var okHttpClient: OkHttpClient
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
        //通过拦截器的方式设置头文件参数
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        builder.addInterceptor {
            val request: Request = it.request()
                .newBuilder()
                .addHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.81 Safari/537.36 SE 2.X MetaSr 1.0"
                )
                .addHeader("Referer", "http://fundf10.eastmoney.com/jjjz_000198.html")
                .build()
            it.proceed(request)
        }
        okHttpClient = builder.build()
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
                val money = it[i].money.toFloat()
                total += money
                if (money < 0) totalOut += money
                else totalIn += money
            }
            dataList.postValue(it)//这句因为数据改变，在MainActivity中被观察到，所以会自动更新列表
        }
        val strTotal: String = (preString + total).replace(".0+$".toRegex(), "")
        val strIn: String = (" 收" + totalIn).replace(".0+$".toRegex(), "")
        val strOut: String = (" 支" + totalOut).replace(".0+$".toRegex(), "")
        totalStr.postValue(strTotal + strIn + strOut)
    }

    /**
     * 获取支付宝每日利率信息并计算利息
     */
    suspend fun calculateInterests() {
        //异步从网络获取支付宝每日利率数据
        val url =
            "http://api.fund.eastmoney.com/f10/lsjz?callback=jQuery183023336459069593007_1618390055881&fundCode=000198&pageIndex=1&pageSize=3000&startDate=2016-02-01&endDate=" + MyUtil.getFormatDateToday() + "&_=1618390082880"
//        LogUtils.e(url)
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        okHttpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtils.e(e.message)
                    totalInterestStr.postValue("支付宝利率数据下载失败")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.let {
                        val strGet = it.body()?.string()
//                        LogUtils.e(strGet)
                        totalInterestStr.postValue("已获取到支付宝利率数据")
                        //处理获取到的数据
                        val keyStart: Int = strGet!!.indexOf("(")
                        val keyEnd: Int = strGet.indexOf(")")
                        val keyStr: String = strGet.substring(keyStart + 1, keyEnd)
                        LogUtils.e(keyStr.substring(0, 100))
                        val gson = Gson()
                        val listRate: ArrayList<Interest> = ArrayList()
                        keyStr.let {
                            Regex("\"FSRQ\":\"\\d{4}-\\d{2}-\\d{2}\",\"DWJZ\":\"\\d.\\d{4}\"").findAll(
                                keyStr
                            ).let { result ->
                                for (m: MatchResult in result) {
                                    for (s: String in m.groupValues) {
//                                        LogUtils.e(s)
                                        val sNew = s.replace("-".toRegex(), "")
                                        listRate.add(
                                            0,
                                            gson.fromJson("{$sNew}", Interest::class.java)
                                        )
                                    }
                                }
                            }
                        }
//                        LogUtils.e("list.size=" + list.size)
//                        LogUtils.e(list.get(0).FSRQ)
//                        LogUtils.e(list.get(0).DWJZ)
                        //todo 可以考虑保存到本地
                        //计算利息
                        //1 先将同日期的记录总和
                        repository.recordList?.let { list ->
                            var tempDate: Int//日期可看成一串八位数的数字
                            var tempMoney: Float
                            var tempSum = 0f
                            var latestDate = 0
                            val listNew: ArrayList<RecordEveryday> = ArrayList()
                            for (record in list.reversed()) {
                                tempDate = record.date.toInt()
                                tempMoney = -record.money.toFloat()//记得取负数
                                tempSum += tempMoney
                                if (latestDate > 0 && tempDate == latestDate) {
                                    listNew.last().money += tempMoney
                                    listNew.last().sum += tempMoney
                                } else {
                                    listNew.add(RecordEveryday(tempDate, tempMoney, tempSum))
                                    latestDate = tempDate
                                }
                            }
                            //因为第一天存的，需要经过第二天的运作才能产生收益，所以将listNew的数据的日期进行改动，将money>0的日期加一，money<0的日期不变。
                            var resetDate = false
                            for (recordEveryday in listNew) {
                                if (recordEveryday.money > 0) {
                                    resetDate = false
                                    for (r in listRate) {
                                        if (resetDate) {
                                            recordEveryday.date = r.FSRQ.toInt()
                                            break
                                        }
                                        if (r.FSRQ.toInt() == recordEveryday.date) resetDate = true
                                    }
                                }
                            }
                            //根据最新的数据进行累计每天的本金加收益
                            var interests: Float
                            var money: Float
                            var oriSum = 0f
                            var sumWithInterest = 0f
                            var gain: Float
                            for (r in listRate) {
                                money = 0f
                                for (recordEveryday in listNew) {
                                    if (recordEveryday.date == r.FSRQ.toInt()) {
                                        money = recordEveryday.money
                                        oriSum = recordEveryday.sum
                                        break
                                    }
                                }
                                if (r.FSRQ.toInt() >= MyUtil.getStartDateInt()) {
                                    gain = (sumWithInterest + money) * (r.DWJZ / 10000f)
                                    sumWithInterest = (sumWithInterest + money) + gain
                                } else {
                                    sumWithInterest = oriSum
                                }
                            }
                            //将计算利息的总额-不算利息的总额=利息
                            interests = sumWithInterest - oriSum
                            val strInterest =
                                ("自" + MyUtil.getStartDateInt() + "起的总利息：" + interests).replace(
                                    ".0+$".toRegex(), ""
                                )
                            LogUtils.e(strInterest)
                            totalInterestStr.postValue(strInterest)
                        }
                    }
                }
            })
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