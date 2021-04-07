package com.my.moneymanager.v

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.my.moneymanager.m.bean.Record
import kotlin.math.max

/**
 * 保证净剩数据是>0的。其中借本来为负数，这里取反，还（huan）同理。以期看到出借金额增减情况
 */
class ChartView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val paint: Paint = Paint()
    private val textPaint: TextPaint = TextPaint()
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private lateinit var list: List<Record>
    private var max: Float = 0f
    private val rect: Rect = Rect()
    private val path = Path()
    private var colorText = Color.parseColor("#ff383838")
    private var colorDash = Color.parseColor("#ff999999")
    private var valueWidth = 0f
    private var valueHeight = 0f
    private var totalStart = 0f
    private var firstEndY = 0f
    private var firstTotalTemp = 0f
    private var lastEndY = 0f
    private var lastTotalTemp = 0f
    private var maxEndY = 0f
    private var maxEndX = 0f
    private var textLength = 0f
    private var textDateStart: String = ""
    private var textDateEnd: String = ""
    private var textDateMax: String = ""

    companion object {
        private const val MARGIN_START: Float = 30f
        private const val MARGIN_END: Float = 30f
        private const val MARGIN_BOTTOM: Float = 80f
        private const val MARGIN_TOP: Float = 80f
        private const val DASH_WIDTH: Float = 20f
        private const val TEXT_MARGIN_END: Float = 5f
    }

    init {
        paint.apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            isDither = true
            strokeWidth = 4f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        textPaint.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            isDither = true
            color = colorText
            strokeWidth = 2f
            textSize = 24f
            strokeCap = Paint.Cap.ROUND
        }
    }

    /**
     * 设置数据和初始值
     */
    fun setData(newList: List<Record>, totalStart: Float = 0f) {
        if (newList.isEmpty()) return
        this.list = newList
        //得到最大值
        this.totalStart = totalStart
        var total = totalStart
        //判断是否全都是还款，如果是就全都取反。让其看起来跟全是借款差不多的样式。
        var allIn = true
        for (i in list.indices) {
            if (list[i].money.toFloat() < 0) {
                allIn = false
                break
            }
        }
        if (allIn) {
            this.totalStart = 0f
            for (i in list.indices) {
                list[i].money = "-" + list[i].money
            }
        }
        max = 0f
        for (i in list.indices) {
            val money = list[i].money.toFloat()
            total -= money
            max = max(max, total)
        }
        textDateStart = list.first().date
        textDateEnd = list.last().date
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        paint.apply {
            shader = LinearGradient(
                0f,
                0f,
                viewWidth.toFloat(),
                0f,
                Color.parseColor("#ff1dc9e3"),//蓝色
                Color.parseColor("#ff1de6ba"),//绿色
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (viewWidth <= 0 || viewHeight <= 0) return
        valueWidth = viewWidth.toFloat() - MARGIN_START - MARGIN_END
        valueHeight = viewHeight.toFloat() - MARGIN_BOTTOM - MARGIN_TOP
        canvas?.let {

            /**
             * 函数内部嵌套函数——画虚线
             */
            fun drawDash(y: Float) {
                textPaint.color = colorDash
                var startX = MARGIN_START
                while (startX < (viewWidth - MARGIN_END)) {
                    it.drawLine(startX, y, startX + DASH_WIDTH / 2f, y, textPaint)
                    startX += DASH_WIDTH
                    it.drawLine(startX, y, startX + DASH_WIDTH / 2f, y, textPaint)
                    startX += DASH_WIDTH / 2f
                }
                textPaint.color = colorText
            }

            /**
             * 函数内部嵌套函数——画文字
             */
            fun drawText(str: String, x: Float, y: Float) {
                textPaint.getTextBounds(str, 0, str.length, rect)
                it.drawText(
                    str,
                    x,
                    y + rect.height().toFloat() / 2f,
                    textPaint
                )
            }

            /**
             * 函数内部嵌套函数——画文字
             */
            fun drawText(value: Float, x: Float, y: Float) {
                drawText(value.toString(), x, y)
            }

            //画x坐标轴
            it.drawLine(
                MARGIN_START,
                viewHeight - MARGIN_BOTTOM,
                viewWidth.toFloat() - MARGIN_END,
                viewHeight - MARGIN_BOTTOM,
                textPaint
            )
            //画y坐标轴
            it.drawLine(
                MARGIN_START,
                MARGIN_TOP,
                MARGIN_START,
                viewHeight - MARGIN_BOTTOM,
                textPaint
            )
            //画净剩
            if (list.isNotEmpty()) {
                var totalTemp = totalStart
                path.reset()
                var lastX = MARGIN_START
                var lastY = viewHeight - MARGIN_BOTTOM
                path.moveTo(lastX, lastY)
                val chartWidth = valueWidth / list.size
                for (i in list.indices) {
                    if (i == 0) {
                        lastY = viewHeight - MARGIN_BOTTOM - totalTemp / max * valueHeight
                        path.lineTo(lastX, lastY)
                        firstEndY = lastY
                        firstTotalTemp = totalTemp
                    }
                    val money = list[i].money.toFloat()
                    totalTemp -= money
                    val endX = MARGIN_START + (i + 1) * chartWidth
                    val endY = viewHeight - MARGIN_BOTTOM - totalTemp / max * valueHeight
                    val midX = (endX + lastX) / 2f
                    path.cubicTo(midX, lastY, midX, endY, endX, endY)
                    val textWidth = textPaint.measureText((i + 1).toString())
                    val start = endX - textWidth / 2f
                    if (i == list.size - 1) {
                        val s = if (endX + textWidth / 2f > viewWidth) {//如果数字多，依然要显示完全
                            viewWidth - textWidth
                        } else {
                            endX - textWidth / 2f
                        }
                        it.drawText(
                            (i + 1).toString(),
                            s,
                            MARGIN_TOP / 2f,
                            textPaint
                        )
                    } else if (start > textLength
                        && start < (viewWidth - MARGIN_END - 2 * textPaint.measureText(list.size.toString()))
                    ) {//为最后一个值留出空间
                        it.drawText(
                            (i + 1).toString(),
                            endX - textWidth / 2f,
                            MARGIN_TOP / 2f,
                            textPaint
                        )
                        textLength = endX + textWidth / 2f
                    }
                    lastX = endX
                    lastY = endY
                    if (i == list.size - 1) {
                        lastEndY = endY
                        lastTotalTemp = totalTemp
                    }
                    if (totalTemp == max) {
                        maxEndY = endY
                        maxEndX = endX
                        textDateMax = list[i].date
                    }
                }
            }
            path.lineTo(viewWidth.toFloat() - MARGIN_END, viewHeight - MARGIN_BOTTOM)
            path.lineTo(MARGIN_START, viewHeight - MARGIN_BOTTOM)
            path.close()
            it.drawPath(path, paint)
            //画0
            drawText(
                0f,
                MARGIN_START + TEXT_MARGIN_END,
                viewHeight - MARGIN_BOTTOM
            )
            //画最后一个值的虚线和字
            drawDash(lastEndY)
            drawText(
                lastTotalTemp,
                viewWidth - MARGIN_END - textPaint.measureText(lastTotalTemp.toString()) - TEXT_MARGIN_END,
                lastEndY
            )
            if (maxEndY != lastEndY && max != 0f && maxEndY > 0) {//如果最后的值就是最大值，就不需要再画了
                //画最大值的虚线和字
                drawDash(maxEndY)
                drawText(
                    max, maxEndX - textPaint.measureText(max.toString()) / 2f,
                    maxEndY
                )
            }
            if (firstEndY != lastEndY && totalStart != 0f) {//如果第一个值就是最后的值，就不需要再画了
                //画第一个值的虚线和字
                drawDash(firstEndY)
                drawText(
                    totalStart, MARGIN_START + TEXT_MARGIN_END,
                    firstEndY
                )
            }
            //画初始和结束的时间
            drawText(
                textDateStart,
                MARGIN_START * 2 + TEXT_MARGIN_END,
                viewHeight - MARGIN_BOTTOM * 3 / 5
            )
            if (textDateEnd != textDateStart)
                drawText(
                    textDateEnd,
                    viewWidth - MARGIN_END * 2 - textPaint.measureText(lastTotalTemp.toString()) - TEXT_MARGIN_END,
                    viewHeight - MARGIN_BOTTOM * 3 / 5
                )
            if (textDateMax != textDateStart && textDateMax != textDateEnd)
                drawText(
                    textDateMax,
                    maxEndX - textPaint.measureText(textDateMax) / 2f,
                    viewHeight - MARGIN_BOTTOM * 3 / 5
                )
        }
    }
}