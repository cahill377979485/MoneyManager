package com.my.moneymanager.xutil

import android.animation.ObjectAnimator
import android.view.View

/**
 * @author 文琳
 * @time 2021/1/5 17:00
 * @desc 列表条目的显示动画，由中间向左右两边展开
 */
private const val GOLDEN_RATIO = .618f

class InAnimation @JvmOverloads constructor(private val mFrom: Float = GOLDEN_RATIO) {

    fun getAnimators(view: View?): Array<ObjectAnimator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", mFrom, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", mFrom, 1f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", mFrom, 1f)
        return arrayOf(scaleX, scaleY, alpha)
    }
}