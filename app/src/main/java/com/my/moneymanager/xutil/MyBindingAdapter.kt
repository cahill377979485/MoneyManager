package com.my.moneymanager.xutil

import android.text.TextWatcher
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * @author 文琳
 * @time 2021/1/5 10:52
 * @desc 用于MVVM架构在xml文件中直接使用指定的key="value"来设置和绑定数据
 * “对于 Kotlin 1.3，可以使用注解 @JvmStatic 与 @JvmField 标记接口的 companion 对象成员。在类文件中会将这些
 * 成员提升到相应接口中并标记为 static。”——《Kotlin 1.3 的新特性》（http://www.kotlincn.net/docs/reference/whatsnew13.html）
 */
object MyBindingAdapter {

    @JvmStatic//不加这个注解会报错说没有静态方法
    @BindingAdapter(value = ["smoothScrollToTopFlag"])
    fun smoothScrollToPosition(rv: RecyclerView, smoothScrollToTopFlag: Boolean = false) {
        if (smoothScrollToTopFlag) rv.smoothScrollToPosition(0)
    }

    @JvmStatic
    @BindingAdapter(value = ["refreshFlag", "itemViewCacheSize"])
    fun setItemViewCacheSize(rv: RecyclerView, refreshFlag: Boolean = false, size: Int = 5) {
        if (refreshFlag) rv.setItemViewCacheSize(size)
    }

    @JvmStatic
    @BindingAdapter(value = ["addTextChangedListener"])
    fun addTextChangedListener(et: EditText, watcher: TextWatcher) {
        et.addTextChangedListener(watcher)
    }

//    @JvmStatic
//    @BindingAdapter(value = ["app:gridLayoutManager"])
//    fun setLayoutManager(rv:RecyclerView, span:Int){
//        rv.apply {
//            layoutManager = GridLayoutManager()
//        }
//    }
}