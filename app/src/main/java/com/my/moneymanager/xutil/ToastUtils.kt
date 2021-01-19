package com.my.moneymanager.xutil

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.my.moneymanager.MyApplication
import com.my.moneymanager.R

/**
 * 带背景颜色和图标的简单单例吐司提示，当新的提示出现时旧的提示当即被替换
 */
class ToastUtils  //单例
private constructor() {
    fun showToast(strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showToast(null, str)
    }

    fun showInfo(strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showInfo(null, str)
    }

    fun showSuccess(strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showSuccess(null, str)
    }

    fun showError(strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showError(null, str)
    }

    fun showWarning(strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showWarning(null, str)
    }

    fun showToast(str: String?) {
        showToast(null, str)
    }

    fun showInfo(str: String?) {
        showInfo(null, str)
    }

    fun showSuccess(str: String?) {
        showSuccess(null, str)
    }

    fun showError(str: String?) {
        showError(null, str)
    }

    fun showWarning(str: String?) {
        showWarning(null, str)
    }

    fun showToast(parent: ViewGroup?, strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showToast(null, str)
    }

    fun showInfo(parent: ViewGroup?, strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showInfo(parent, str)
    }

    fun showSuccess(parent: ViewGroup?, strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showSuccess(parent, str)
    }

    fun showError(parent: ViewGroup?, strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showError(parent, str)
    }

    fun showWarning(parent: ViewGroup?, strId: Int) {
        val str = MyApplication.context.resources.getString(strId)
        showWarning(parent, str)
    }

    /**
     * 显示普通的吐司提示
     *
     * @param parent 父控件
     * @param text   显示文本
     */
    fun showToast(parent: ViewGroup?, str: String?) {
        handler.post {
            if (toast != null) toast = null
            toast = Toast(MyApplication.context)
            val view = getView(parent)
            toast?.view = view
            toast!!.view?.setBackgroundResource(R.drawable.toast_bg_round_black)
            val tv = toast!!.view?.findViewById<TextView>(R.id.tv_toast)
            tv?.apply {
                text = str
                setTextColor(ContextCompat.getColor(MyApplication.context, R.color.myWhite))
                textSize = 16f
            }
            val iv = toast!!.view?.findViewById<ImageView>(R.id.iv_toast)
            iv?.visibility = View.GONE
            toast!!.show()
        }
    }

    /**
     * 显示普通的吐司提示
     *
     * @param parent 父控件
     * @param text   显示文本
     */
    fun showInfo(parent: ViewGroup?, text: String?) {
        handler.post {
            if (toast != null) toast = null
            toast = Toast(MyApplication.context)
            val view = getView(parent)
            toast!!.view = view
            toast!!.view?.setBackgroundResource(R.drawable.toast_bg_round_black)
            val tv = toast!!.view?.findViewById<TextView>(R.id.tv_toast)
            tv?.text = text
            tv?.setTextColor(ContextCompat.getColor(MyApplication.context, R.color.myWhite))
            tv?.textSize = 16f
            val iv = toast!!.view?.findViewById<ImageView>(R.id.iv_toast)
            iv?.setImageResource(R.mipmap.common_sign_toast_info)
            iv?.visibility = View.VISIBLE
            toast!!.show()
        }
    }

    /**
     * 显示成功的吐司提示
     *
     * @param parent 父控件
     * @param text   显示文本
     */
    fun showSuccess(parent: ViewGroup?, text: String?) {
        handler.post {
            if (toast != null) toast = null
            toast = Toast(MyApplication.context)
            val view = getView(parent)
            toast!!.view = view
            toast!!.view?.setBackgroundResource(R.drawable.toast_bg_round_green)
            val tv = toast!!.view?.findViewById<TextView>(R.id.tv_toast)
            tv?.text = text
            tv?.setTextColor(ContextCompat.getColor(MyApplication.context, R.color.myWhite))
            tv?.textSize = 16f
            val iv = toast!!.view?.findViewById<ImageView>(R.id.iv_toast)
            iv?.setImageResource(R.mipmap.common_sign_toast_success)
            iv?.visibility = View.VISIBLE
            toast!!.show()
        }
    }

    /**
     * 显示错误的吐司提示
     *
     * @param parent 父控件
     * @param text   显示文本
     */
    fun showError(parent: ViewGroup?, text: String?) {
        handler.post {
            if (toast != null) toast = null
            toast = Toast(MyApplication.context)
            val view = getView(parent)
            toast!!.view = view
            toast!!.view?.setBackgroundResource(R.drawable.toast_bg_round_red)
            val tv = toast!!.view?.findViewById<TextView>(R.id.tv_toast)
            tv?.text = text
            tv?.setTextColor(ContextCompat.getColor(MyApplication.context, R.color.myWhite))
            tv?.textSize = 16f
            val iv = toast!!.view?.findViewById<ImageView>(R.id.iv_toast)
            iv?.setImageResource(R.mipmap.common_sign_toast_error)
            iv?.visibility = View.VISIBLE
            toast!!.show()
        }
    }

    /**
     * 显示错误的吐司提示
     *
     * @param parent 父控件
     * @param text   显示文本
     */
    fun showWarning(parent: ViewGroup?, text: String?) {
        handler.post {
            if (toast != null) toast = null
            toast = Toast(
                MyApplication.context
            )
            val view = getView(parent)
            toast!!.view = view
            toast!!.view?.setBackgroundResource(R.drawable.toast_bg_round_yellow)
            val tv = toast!!.view?.findViewById<TextView>(R.id.tv_toast)
            tv?.text = text
            tv?.setTextColor(ContextCompat.getColor(MyApplication.context, R.color.myWhite))
            tv?.textSize = 16f
            val iv = toast!!.view?.findViewById<ImageView>(R.id.iv_toast)
            iv?.setImageResource(R.mipmap.common_sign_toast_warning)
            iv?.visibility = View.VISIBLE
            toast!!.show()
        }
    }

    companion object {
        private var toast: Toast? = null
        private val handler = Handler(Looper.getMainLooper())
        private var toastUtils: ToastUtils? = null

        val instance: ToastUtils?
            get() {
                if (toastUtils == null) {
                    toastUtils = ToastUtils()
                }
                return toastUtils
            }

        private fun getView(parent: ViewGroup?): View {
            return if (parent == null) {
                LayoutInflater.from(MyApplication.context)
                    .inflate(R.layout.common_toast_layout, null)
            } else {
                LayoutInflater.from(MyApplication.context)
                    .inflate(R.layout.common_toast_layout, parent, false)
            }
        }
    }
}