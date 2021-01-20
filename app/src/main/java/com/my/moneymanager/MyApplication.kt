package com.my.moneymanager

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.Utils
import com.squareup.leakcanary.LeakCanary

/**
 * @author 文琳
 * @time 2021/1/5 9:38
 * @desc 全局设置
 */
class MyApplication : Application() {
    companion object{
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        //常用工具类的初始化
        Utils.init(this)
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}