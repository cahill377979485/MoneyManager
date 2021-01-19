package com.my.moneymanager.v.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.my.moneymanager.databinding.RecordBinding
import com.my.moneymanager.m.bean.Record
import com.my.moneymanager.m.bean.TypeData
import com.my.moneymanager.xutil.InAnimation
import com.my.moneymanager.xutil.TypeDataNames
import org.greenrobot.eventbus.EventBus

/**
 * @author 文琳
 * @time 2021/1/5 17:27
 * @desc 物品的样式
 */
class RecordBinder : ItemViewBinder<Record, RecordBinder.RecordHolder>() {
    private val mAnimation = InAnimation()

    inner class RecordHolder(var binding: RecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): RecordHolder {
        return RecordHolder(RecordBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecordHolder, item: Record) {
        holder.binding.item = item
        holder.binding.click = object : ClickRecord {
            override fun onClick(record: Record) {
                EventBus.getDefault().post(TypeData(TypeDataNames.clickRecord, record))
            }
        }
        holder.binding.executePendingBindings()
        for (anim in mAnimation.getAnimators(holder.itemView)) {
            anim.interpolator = DecelerateInterpolator()
            anim.setDuration(300).start()
        }
    }
}