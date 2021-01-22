package com.my.moneymanager.xutil

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.drakeet.multitype.MultiTypeAdapter
import com.my.moneymanager.m.MyRepository
import com.my.moneymanager.m.bean.Record
import com.my.moneymanager.vm.MainVM
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author 文琳
 * @time 2021/1/5 9:51
 * @desc 封装一些代码很多或者常用的方法
 */
object MyUtil {

    /**
     * 设置长按拖动
     */
    fun setHelper(
        rv: RecyclerView?,
        list: ArrayList<Record>?,
        vm: MainVM,
        adapter: MultiTypeAdapter
    ) {
        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder
            ): Int {
                return if (recyclerView.layoutManager is GridLayoutManager) {
                    val dragFlags =
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    val swipeFlags = 0
                    makeMovementFlags(dragFlags, swipeFlags)
                } else {
                    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    val swipeFlags = 0
                    makeMovementFlags(dragFlags, swipeFlags)
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                target: ViewHolder
            ): Boolean {
                if (vm.searchFlag.value == true) {
                    return true
                }
                val fromPosition = viewHolder.adapterPosition //得到拖动ViewHolder的position
                val toPosition = target.adapterPosition //得到目标ViewHolder的position
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(list as List<Record>, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(list as List<Record>, i, i - 1)
                    }
                }
                adapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}

            override fun onSelectedChanged(
                viewHolder: ViewHolder?,
                actionState: Int
            ) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder?.itemView?.setBackgroundColor(Color.LTGRAY)
                    if (vm.searchFlag.value == true) {
                        ToastUtils.instance?.showInfo("请先取消实时搜索")
                        return
                    }
                }
                super.onSelectedChanged(viewHolder, actionState)
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.setBackgroundColor(0)
                //保证后面的list是所有的数据，而不是部分数据
                if (vm.searchFlag.value == true) {
                    return
                }
                if (list != null && list.size > 0) {
                    val newList: MutableList<Record> = ArrayList()
                    for (i in list.indices) {
                        val record = list[i]
                        record.position = i
                        newList.add(record)
                    }
                    MyRepository().save(newList)
                    vm.dataList.postValue(newList)
                }
            }
        })
        helper.attachToRecyclerView(rv)
    }

    /**
     * 解析物品和价格
     */
    fun getDateDescAndMoneyArrayByRegex(str: String): Array<String> {
        val arr = arrayOf("", str, "")
        str.let {
            Regex("^(\\d{8})?(.*?)?(-?\\d*\\.?\\d*)?$").find(str)?.let {
                arr[0] = it.groupValues[1]//第一个括号匹配到的内容
                arr[1] = it.groupValues[2]//第二个括号匹配到的内容
                arr[2] = it.groupValues[3]//第三个括号匹配到的内容
                //针对几种特殊情况进行处理，包括：直接以“.”结尾(需改成0)、类似“123.”(需去掉小数点)、类似“.132”(需在最前面加上“0”)
                if (arr[2].contains(".")) {
                    arr[2] = arr[2].replace(Regex("0+?$"), "")//后面的问号表示非贪婪匹配
                    arr[2] = arr[2].replace(Regex("\\.$"), "")//经过上面的去除尾部的0之后，如果直接以“.”结尾则去掉“.”
                }
            }
            //如果金额为空则默认为0
            if (arr[2] == "") arr[2] = "0"
            if (arr[2].startsWith(".")) arr[2] = "0" + arr[2]
        }
        return arr
    }

    @SuppressLint("SimpleDateFormat")
    fun getFormatCreateTime(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(Date(System.currentTimeMillis()))
}