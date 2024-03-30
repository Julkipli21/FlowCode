package com.example.flowchart2code

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.widget.Button

class LineView(context: Context) : View(context) {
    private val paint = Paint()
    private var startButton: Button? = null
    private var endButton: Button? = null

    fun updateButtons(start: Button?, end: Button?) {
        startButton = start
        endButton = end
        invalidate() // Trigger a redraw of the view
    }

    fun getStartButton(): Button? {
        return startButton
    }

    fun getEndButton(): Button? {
        return endButton
    }

    fun isLineConnectedToButton(button: Button): Boolean {
        return (button == startButton && button.parent != null) || (button == endButton && button.parent != null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        startButton?.let { start ->
            endButton?.let { end ->
                // Ensure the buttons are still in the parent view
                if (start.parent != null && end.parent != null) {
                    // Calculate the center points for the circles around each button
                    val startCenterX = start.x + start.width / 2
                    val startCenterY = start.y + start.height / 2
                    val endCenterX = end.x + end.width / 2
                    val endCenterY = end.y + end.height / 2

                    // Calculate the angles for the points on the circles
                    val startAngle = Math.atan2((endCenterY - startCenterY).toDouble(), (endCenterX - startCenterX).toDouble())
                    val endAngle = Math.atan2((startCenterY - endCenterY).toDouble(), (startCenterX - endCenterX).toDouble())

                    // Calculate the start and end points on the circles
                    val startX = (startCenterX + start.width / 2 * Math.cos(startAngle)).toFloat()
                    val startY = (startCenterY + start.height / 2 * Math.sin(startAngle)).toFloat()
                    val endX = (endCenterX + end.width / 2 * Math.cos(endAngle)).toFloat()
                    val endY = (endCenterY + end.height / 2 * Math.sin(endAngle)).toFloat()

                    // Set the line color and width
                    paint.color = 0xFFFFFFFF.toInt() // White
                    paint.strokeWidth = 5f

                    // Draw the line between the points on the circles
                    canvas.drawLine(startX, startY, endX, endY, paint)

                    // Draw an arrowhead at the end of the line
                    drawArrowhead(canvas, startX, startY, endX, endY)
                }
            }
        }
    }


    private fun drawArrowhead(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float) {
        val arrowSize = 30f

        val angle = Math.atan2((endY - startY).toDouble(), (endX - startX).toDouble())
        val arrowX1 = endX - arrowSize * Math.cos(angle + Math.PI / 6).toFloat()
        val arrowY1 = endY - arrowSize * Math.sin(angle + Math.PI / 6).toFloat()
        val arrowX2 = endX - arrowSize * Math.cos(angle - Math.PI / 6).toFloat()
        val arrowY2 = endY - arrowSize * Math.sin(angle - Math.PI / 6).toFloat()

        paint.style = Paint.Style.FILL

        val path = Path()
        path.moveTo(endX, endY)
        path.lineTo(arrowX1, arrowY1)
        path.lineTo(arrowX2, arrowY2)
        path.close()

        canvas.drawPath(path, paint)

        paint.style = Paint.Style.STROKE
    }
}