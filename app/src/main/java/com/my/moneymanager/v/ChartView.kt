package com.my.moneymanager.v

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.my.moneymanager.m.bean.Record
import kotlin.math.max

/**
 * 保证净剩数据是>0的。其中借本来为负数，这里取反，还同理。以期看到出借金额增减情况
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
    private var colorDash = Color.parseColor("#ffcccccc")
    private var totalStart = 0f
    private var firstEndY = 0f
    private var firstTotalTemp = 0f
    private var lastEndY = 0f
    private var lastTotalTemp = 0f
    private var maxEndY = 0f
    private var maxEndX = 0f

    init {
        initialize()
    }

    companion object {
        private const val MARGIN_START: Float = 20f
        private const val MARGIN_END: Float = 20f
        private const val MARGIN_BOTTOM: Float = 80f
        private const val MARGIN_TOP: Float = 80f
        private const val DASH_WIDTH: Float = 20f
        private const val TEXT_MARGIN_END: Float = 5f
    }

    private fun initialize() {
        paint.apply {
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
            isDither = true
            strokeWidth = 4f
            strokeCap = Paint.Cap.ROUND
        }
        textPaint.apply {
            color = colorText
            style = Paint.Style.STROKE
            strokeWidth = 2f
            textSize = 24f
            isAntiAlias = true
            isDither = true
            strokeCap = Paint.Cap.ROUND
        }
    }

    fun setData(newList: List<Record>, totalStart: Float = 0f) {
        if (newList.isEmpty()) return
        this.list = newList
        //得到最大值
        this.totalStart = totalStart
        var total = totalStart
        max = 0f
        for (i in list.indices) {
            val money = list[i].money.toFloat()
            total -= money
            max = max(max, total)
        }
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
                Color.parseColor("#ff1dc9e3"),
                Color.parseColor("#ff1de6ba"),
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (viewWidth <= 0 || viewHeight <= 0) return
        val valueWidth = viewWidth.toFloat() - MARGIN_START - MARGIN_END
        val valueHeight = viewHeight.toFloat() - MARGIN_BOTTOM - MARGIN_TOP
        canvas?.let {
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
                        path.lineTo(
                            lastX,
                            lastY
                        )
                        firstEndY = lastY
                        firstTotalTemp = totalTemp
                    }
                    val money = list[i].money.toFloat()
                    totalTemp -= money
                    val endX = MARGIN_START + (i + 1) * chartWidth
                    val endY = viewHeight - MARGIN_BOTTOM - totalTemp / max * valueHeight
                    val midX = (endX + lastX) / 2f
                    path.cubicTo(midX, lastY, midX, endY, endX, endY)
                    it.drawText(
                        (i + 1).toString(),
                        endX - textPaint.measureText((i + 1).toString()) / 2f,
                        30f,
                        textPaint
                    )
                    lastX = endX
                    lastY = endY
                    if (i == list.size - 1) {
                        lastEndY = endY
                        lastTotalTemp = totalTemp
                    }
                    if (totalTemp == max) {
                        maxEndY = endY
                        maxEndX = endX
                    }
                }
            }
            path.lineTo(viewWidth.toFloat() - MARGIN_END, viewHeight - MARGIN_BOTTOM)
            path.lineTo(MARGIN_START, viewHeight - MARGIN_BOTTOM)
            path.close()
            it.drawPath(path, paint)
            //画0
            textPaint.getTextBounds("0", 0, 1, rect)
            it.drawText(
                "0",
                MARGIN_START + TEXT_MARGIN_END,
                viewHeight - MARGIN_BOTTOM + rect.height() / 2f,
                textPaint
            )
            //画最后一个值的虚线和字
            textPaint.color = colorDash
            var startX = MARGIN_START
            while (startX < (viewWidth - MARGIN_END)) {
                it.drawLine(startX, lastEndY, startX + DASH_WIDTH / 2f, lastEndY, textPaint)
                startX += DASH_WIDTH
                it.drawLine(startX, lastEndY, startX + DASH_WIDTH / 2f, lastEndY, textPaint)
                startX += DASH_WIDTH / 2f
            }
            textPaint.color = colorText
            textPaint.getTextBounds(
                lastTotalTemp.toString(),
                0,
                lastTotalTemp.toString().length,
                rect
            )
            it.drawText(
                lastTotalTemp.toString(),
                viewWidth - MARGIN_END - textPaint.measureText(lastTotalTemp.toString()) - TEXT_MARGIN_END,
                lastEndY + rect.height().toFloat() / 2f,
                textPaint
            )
            if (maxEndY != lastEndY) {//如果最后的值就是最大值，就不需要再画了
                //画最大值的虚线和字
                textPaint.color = colorDash
                startX = MARGIN_START
                while (startX < (viewWidth - MARGIN_END)) {
                    it.drawLine(startX, maxEndY, startX + DASH_WIDTH / 2f, maxEndY, textPaint)
                    startX += DASH_WIDTH
                    it.drawLine(startX, maxEndY, startX + DASH_WIDTH / 2f, maxEndY, textPaint)
                    startX += DASH_WIDTH / 2f
                }
                textPaint.color = colorText
                textPaint.getTextBounds(max.toString(), 0, max.toString().length, rect)
                it.drawText(
                    max.toString(),
                    maxEndX - textPaint.measureText(max.toString()) / 2f,
                    maxEndY + rect.height().toFloat() / 2f,
                    textPaint
                )
            }
            if (firstEndY != lastEndY) {//如果第一个值就是最后的值，就不需要再画了
                //画第一个值的虚线和字
                textPaint.color = colorDash
                startX = MARGIN_START
                while (startX < (viewWidth - MARGIN_END)) {
                    it.drawLine(startX, firstEndY, startX + DASH_WIDTH / 2f, firstEndY, textPaint)
                    startX += DASH_WIDTH
                    it.drawLine(startX, firstEndY, startX + DASH_WIDTH / 2f, firstEndY, textPaint)
                    startX += DASH_WIDTH / 2f
                }
                textPaint.color = colorText
                textPaint.getTextBounds(totalStart.toString(), 0, totalStart.toString().length, rect)
                it.drawText(
                    totalStart.toString(),
                    MARGIN_START + TEXT_MARGIN_END,
                    firstEndY + rect.height().toFloat() / 2f,
                    textPaint
                )
            }
        }
    }
}