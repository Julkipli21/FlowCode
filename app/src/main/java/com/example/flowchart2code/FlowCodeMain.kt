package com.example.flowchart2code

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import java.util.Locale
import java.util.Stack

data class ButtonConnection(val startButton: Button, val endButton: Button)

class Tryl : ComponentActivity() {
    private var undoButton: Button? = null
    private var textButton: Button? = null
    private var deleteButton: Button? = null
    private var convertButton: Button? = null

    private var movingButton: Button? = null

    private var startButton: Button? = null
    private var decisionButton: Button? = null
    private var yesButton: Button? = null
    private var noButton: Button? = null
    private var inputButton: Button? = null
    private var outputButton: Button? = null
    private var processButton: Button? = null
    private var loopButton: Button? = null
    private var endButton: Button? = null

    private var lastCreatedButton: Button? = null
    private var isFirstButton = true

    private val lineViews = mutableListOf<LineView>()
    private val buttonConnections = mutableListOf<ButtonConnection>()

    private val buttonList = mutableListOf<Button>()
    private val undoStack = Stack<undoAction>()
    private val buttonCountMap: MutableMap<String, Int> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flowcode_main)

        undoButton = findViewById(R.id.undo)
        undoButton?.isClickable = false
        undoButton?.isEnabled = false

        textButton = findViewById(R.id.text)
        textButton?.isClickable = false
        textButton?.isEnabled = false

        deleteButton = findViewById(R.id.delete)
        deleteButton?.isClickable = false
        deleteButton?.isEnabled = false

        convertButton = findViewById(R.id.convert)
        convertButton?.isClickable = true
        convertButton?.isEnabled = true

        val buttonStart = findViewById<Button>(R.id.start)
        val buttonDecision = findViewById<Button>(R.id.decision)
        val buttonYes = findViewById<Button>(R.id.yes)
        val buttonNo = findViewById<Button>(R.id.no)
        val buttonInput = findViewById<Button>(R.id.input)
        val buttonOutput = findViewById<Button>(R.id.output)
        val buttonProcess = findViewById<Button>(R.id.process)
        val buttonLoop = findViewById<Button>(R.id.loop)
        val buttonEnd = findViewById<Button>(R.id.end)

        // Set only the start button as clickable initially
        setButtonClickable(buttonStart, true)
        setButtonClickable(buttonDecision, false)
        setButtonClickable(buttonInput, false)
        setButtonClickable(buttonOutput, false)
        setButtonClickable(buttonProcess, false)
        setButtonClickable(buttonLoop, false)
        setButtonClickable(buttonEnd, false)

        buttonStart.setOnClickListener {
            val clone = createButtonClone(buttonStart)
            addCloneToMainContent(clone)
            makeButtonDraggable(clone)
            startButton = clone
            decisionButton?.let { drawLineBetweenButtons(startButton!!, it) }
            updateConvertButtonState()

            buttonList.add(clone)
            clickButton(startButton!!)

            // After cloning, make the start button unclickable
            setButtonClickable(buttonStart, false)
            // Make other buttons clickable
            setButtonClickable(buttonDecision, false)
            setButtonClickable(buttonInput, true)
            setButtonClickable(buttonOutput, true)
            setButtonClickable(buttonProcess, false)
            setButtonClickable(buttonLoop, true)
            setButtonClickable(buttonEnd, false)

            updateUndoButtonState()
        }

        buttonInput.setOnClickListener {
            val clone = createButtonClone(buttonInput)
            addCloneToMainContent(clone)
            makeButtonDraggable(clone)
            inputButton = clone

            buttonList.lastOrNull()?.let { drawLineBetweenButtons(it, inputButton!!) }
            buttonList.add(clone)
            clickButton(inputButton!!)

            setButtonClickable(buttonInput, true)
            setButtonClickable(buttonStart, false)
            setButtonClickable(buttonLoop, true)
            setButtonClickable(buttonDecision, true)
            setButtonClickable(buttonOutput, false)
            setButtonClickable(buttonProcess, true)
            setButtonClickable(buttonEnd, false)
        }

        buttonOutput.setOnClickListener {
            val clone = createButtonClone(buttonOutput)
            addCloneToMainContent(clone)
            makeButtonDraggable(clone)
            outputButton = clone

            buttonList.lastOrNull()?.let { drawLineBetweenButtons(it, outputButton!!) }
            buttonList.add(clone)
            clickButton(outputButton!!)

            setButtonClickable(buttonStart, false)
            setButtonClickable(buttonDecision, false)
            setButtonClickable(buttonInput, false)
            setButtonClickable(buttonOutput, true)
            setButtonClickable(buttonLoop, false)
            setButtonClickable(buttonProcess, false)
            setButtonClickable(buttonEnd, true)

            loopButton?.let{ setButtonClickable(buttonProcess, true) }
        }

        buttonProcess.setOnClickListener {
            val clone = createButtonClone(buttonProcess)
            addCloneToMainContent(clone)
            makeButtonDraggable(clone)
            processButton = clone

            buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, processButton!!) }
            buttonList.add(clone)
            clickButton(processButton!!)

            setButtonClickable(buttonProcess, true)
            setButtonClickable(buttonStart, false)
            setButtonClickable(buttonDecision, true)
            setButtonClickable(buttonInput, true)
            setButtonClickable(buttonOutput, true)
            setButtonClickable(buttonEnd, true)
        }

        buttonDecision.setOnClickListener {
            val clone = createButtonClone(buttonDecision)
            addCloneToMainContent(clone)
            makeButtonDraggable(clone)
            decisionButton = clone

            buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, decisionButton!!) }
            buttonList.add(clone)
            clickButton(decisionButton!!)

            // After cloning the decision button, clone the "Yes" and "No" buttons
            val cloneYes = createButtonClone(buttonYes)
            val cloneNo = createButtonClone(buttonNo)
            yesButton = cloneYes
            noButton = cloneNo

            // Set positions of the intermediate buttons on both sides of the decision button
            val horizontalSpacing = resources.getDimensionPixelSize(R.dimen.horizontal_spacing)
            val horizontalSpacingNo = resources.getDimensionPixelSize(R.dimen.horizontal_spacing_no)
            val verticalSpacing = resources.getDimensionPixelSize(R.dimen.vertical_spacing)

            // Position the "Yes" button to the left of the decision button
            cloneYes.x = decisionButton!!.x - cloneYes.width - horizontalSpacing
            cloneYes.y = decisionButton!!.y + verticalSpacing

            // Position the "No" button to the right of the decision button
            cloneNo.x = decisionButton!!.x + cloneNo.width + horizontalSpacingNo
            cloneNo.y = decisionButton!!.y + verticalSpacing

            // Add the clones to the main content
            addCloneToMainContent(cloneYes)
            addCloneToMainContent(cloneNo)
            drawLineBetweenButtons(decisionButton!!, cloneYes)
            drawLineBetweenButtons(decisionButton!!, cloneNo)

            // Make the "Yes" and "No" buttons draggable
            makeButtonDraggable(yesButton!!)
            makeButtonDraggable(noButton!!)

            clickButton(yesButton!!)
            clickButton(noButton!!)

            loopButton?.let {
                drawLineBetweenButtons(noButton!!, it)
                buttonList.add(noButton!!)
                buttonList.add(it)
                buttonList.add(decisionButton!!)
            }

            buttonList.add(yesButton!!)

            setButtonClickable(buttonInput, true)
            setButtonClickable(buttonOutput, true)
            setButtonClickable(buttonProcess, true)
            setButtonClickable(buttonLoop, false)
            setButtonClickable(buttonEnd, true)
            updateConvertButtonState()
        }

        buttonLoop.setOnClickListener {
            val clone = createButtonClone(buttonLoop)
            addCloneToMainContent(clone)
            makeButtonDraggable(clone)
            loopButton = clone

            buttonList.lastOrNull()?.let{drawLineBetweenButtons(it, loopButton!!)}
            buttonList.add(clone)

            clickButton(loopButton!!)

            setButtonClickable(buttonStart, false)
            setButtonClickable(buttonDecision, true)
            setButtonClickable(buttonInput, false)
            setButtonClickable(buttonOutput, true)
            setButtonClickable(buttonLoop, false)
            setButtonClickable(buttonProcess, true)
            setButtonClickable(buttonEnd, false)
        }

        buttonEnd.setOnClickListener {
            if((buttonList.contains(noButton) && buttonList.contains(loopButton)) || !buttonList.contains(decisionButton)){
                val clone = createButtonClone(buttonEnd)
                addCloneToMainContent(clone)
                makeButtonDraggable(clone)
                endButton = clone

                buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, endButton!!) }
                buttonList.add(clone)
                clickButton(endButton!!)

                setButtonClickable(buttonProcess, false)
                setButtonClickable(buttonLoop, false)
                setButtonClickable(buttonStart, false)
                setButtonClickable(buttonDecision, false)
                setButtonClickable(buttonInput, false)
                setButtonClickable(buttonOutput, false)
                setButtonClickable(buttonEnd, false)
                updateConvertButtonState()
            }
            else {
                if (!buttonList.contains(noButton!!)) {
                    val clone = createButtonClone(buttonEnd)
                    addCloneToMainContent(clone)
                    makeButtonDraggable(clone)
                    endButton = clone
                    clickButton(endButton!!)

                    buttonList.lastOrNull()?.let { drawLineBetweenButtons(it, endButton!!) }
                    buttonList.add(decisionButton!!)
                    buttonList.add(noButton!!)
                } else {

                    buttonList.lastOrNull()?.let { drawLineBetweenButtons(it, endButton!!) }
                    buttonList.add(endButton!!)
                }
            }

            updateConvertButtonState()
        }

        undoButton?.setOnClickListener {
            if (undoStack.isNotEmpty()) {
                val lastAction = undoStack.pop()

                when (lastAction) {
                    is undoAction.Rename -> {
                        lastAction.button.text = lastAction.previousText
                    }
                    is undoAction.Delete -> {
                        // Restore the deleted button
                        addCloneToMainContent(lastAction.button)
                        buttonList.lastOrNull()?.let { drawLineBetweenButtons(it, lastAction.button) }
                        buttonList.add(lastAction.button)
                    }
                    // Add cases for other types of undoable actions here if needed
                    else -> {}
                }
                // After performing the undo, update the undo button state
                updateUndoButtonState()
            }
        }

        // Coroutine scope for delaying modifications
        val scope = CoroutineScope(Dispatchers.Main)

        convertButton?.setOnClickListener {
            val intent = Intent(this, FlowCodeConverter::class.java)
            val buttonInfoList = mutableListOf<ButtonInfo>()

            // Disable buttons
            listOf(buttonStart, buttonInput, buttonOutput, buttonProcess, buttonDecision, buttonLoop, buttonEnd)
                .forEach { setButtonClickable(it, false) }

            // Modify buttons with delay
            scope.launch {
                modifyButtonsWithDelay(buttonList, buttonInfoList)

                // Log button connections
                buttonConnections.forEach { connection ->
                    Log.d("ButtonConnection", "${connection.startButton.text} -> ${connection.endButton.text}")
                }

                // Put buttonInfoList into intent
                intent.putParcelableArrayListExtra("buttonInfoList", ArrayList(buttonInfoList))
                startActivity(intent)

                // Clear button counters map
                buttonCounters.clear()
            }
        }
    }

    // Function to modify buttons with delay
    suspend fun modifyButtonsWithDelay(buttonList: List<Button>, buttonInfoList: MutableList<ButtonInfo>) {
        buttonList.forEach { button ->
            // Extract text information
            val buttonText = button.text.toString()
            val buttonType = if (buttonText == "Start" || buttonText == "End") buttonText.lowercase() else "${getIdForButtonType2(button.id)}${incrementAndGetCounter(buttonText.lowercase(Locale.getDefault()))}"
            val formattedText = "$buttonType: $buttonText"
            buttonInfoList.add(ButtonInfo(buttonText, formattedText))

            // Increase the size of the button
            val layoutParams = button.layoutParams as ViewGroup.MarginLayoutParams
            val widthIncrease = 30
            val heightIncrease = 30
            layoutParams.width += widthIncrease
            layoutParams.height += heightIncrease

            // Adjust margins to keep the button centered
            val horizontalMarginIncrease = widthIncrease / 2
            val verticalMarginIncrease = heightIncrease / 2
            layoutParams.setMargins(
                layoutParams.leftMargin - horizontalMarginIncrease,
                layoutParams.topMargin - verticalMarginIncrease,
                layoutParams.rightMargin - horizontalMarginIncrease,
                layoutParams.bottomMargin - verticalMarginIncrease
            )
            button.layoutParams = layoutParams

//            // Set stroke width for the button background
//            val strokeWidth = 10
//            val buttonBackground = button.background as? GradientDrawable
//            buttonBackground?.setStroke(strokeWidth, Color.parseColor("#04d60b"))

            // Delay for 2 seconds
            delay(1000)

            // Revert changes after delay
            layoutParams.width -= widthIncrease
            layoutParams.height -= heightIncrease
            layoutParams.setMargins(
                layoutParams.leftMargin + horizontalMarginIncrease,
                layoutParams.topMargin + verticalMarginIncrease,
                layoutParams.rightMargin + horizontalMarginIncrease,
                layoutParams.bottomMargin + verticalMarginIncrease
            )
            button.layoutParams = layoutParams
//            buttonBackground?.setStroke(0, Color.TRANSPARENT) // Remove stroke
        }
    }

    private fun setButtonClickable(button: Button?, clickable: Boolean) {
        button?.isClickable = clickable
        button?.isEnabled = clickable
    }

    private var buttonCounters = mutableMapOf<String, Int>()

    private fun incrementAndGetCounter(buttonType: String): Int {
        val currentCount = buttonCounters.getOrDefault(buttonType, 0)
        buttonCounters[buttonType] = currentCount + 1
        return currentCount + 1
    }

    private val idToButtonTypeMap = mapOf(
        1 to "start",
        2 to "loop",
        3 to "input",
        4 to "iterate",
        5 to "output",
        6 to "process",
        7 to "decision",
        8 to "yes",
        9 to "no",
        10 to "end"
    )

    private val idToButtonTypeReverseMap = idToButtonTypeMap.entries.associateBy({ it.value }) { it.key }

    private fun getIdForButtonType1(buttonType: String): Int {
        // Use the global idToButtonTypeMap to get the numeric ID for the given buttonType
        return idToButtonTypeMap.entries.find { it.value == buttonType }?.key ?: View.generateViewId()
    }

    private fun getIdForButtonType2(buttonID: Int): Comparable<*> {
        // Use the global idToButtonTypeMap to get the numeric ID for the given buttonType
        return idToButtonTypeMap.entries.find { it.key == buttonID }?.value ?: View.generateViewId()
    }

    private fun createButtonClone(originalButton: Button): Button {
        val clone = Button(this)

        // Set the desired text size (change this value as needed)
        val textSizeInSp = 8
        clone.textSize = textSizeInSp.toFloat()

        // Remove elevation-related animations
        clone.stateListAnimator = null

        clone.background = originalButton.background
        clone.text = originalButton.text
        clone.setTextColor(originalButton.currentTextColor)

        val buttonType = originalButton.text.toString().lowercase(Locale.getDefault())
        clone.id = getIdForButtonType1(buttonType)

        clone.layoutParams = LinearLayout.LayoutParams(
            originalButton.layoutParams.width,
            originalButton.layoutParams.height
        )

        val mainContent = findViewById<FrameLayout>(R.id.mainContent)
        val xPosition = (mainContent.width - originalButton.width) / 2f
        val yPosition = if (isFirstButton) {
            resources.getDimensionPixelSize(R.dimen.top_spacing).toFloat()
        } else {
            lastCreatedButton?.let {
                it.y + it.height + resources.getDimensionPixelSize(R.dimen.vertical_spacing)
            } ?: 0f
        }

        clone.x = xPosition
        clone.y = yPosition

        lastCreatedButton = clone
        isFirstButton = false

        return clone
    }

    private fun addCloneToMainContent(clone: View) {
        val mainContent = findViewById<FrameLayout>(R.id.mainContent)
        mainContent.addView(clone)
        updateLines()
    }

    private fun makeButtonDraggable(button: Button) {
        var startX: Float = 0f
        var startY: Float = 0f

        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX - v.x
                    startY = event.rawY - v.y
                }
                MotionEvent.ACTION_MOVE -> {
                    v.x = event.rawX - startX
                    v.y = event.rawY - startY
                    updateLines() // Update line while dragging buttons
                }
                MotionEvent.ACTION_UP -> {
                }
            }
            false
        }
    }

    private fun clickButton(button: Button) {
        button.setOnClickListener {
            if (button != startButton && button != endButton) {
                textButton?.setBackgroundColor(0xFF0b9655.toInt())
                textButton?.setTextColor(0xFFFFFFFF.toInt())
                textButton?.isClickable = true
                textButton?.isEnabled = true

                // Styling for the delete button when the button is not the "end" button
                if (button != endButton) {
                    deleteButton?.setBackgroundColor(0xFF8F1923.toInt())
                    deleteButton?.setTextColor(0xFFFFFFFF.toInt())
                    deleteButton?.isClickable = true
                    deleteButton?.isEnabled = true
                }

                // Listener for renaming a button
                textButton?.setOnClickListener {
                    if (button.parent is ViewGroup) {
                        val editText = EditText(this).apply {
                            setText(button.text)
                        }
                        val dialog = AlertDialog.Builder(this).apply {
                            setView(editText)
                            setPositiveButton("OK") { dialog, _ ->
                                val newText = editText.text.toString()
                                // Add undo action before changing the text
                                val previousText = button.text.toString()
                                undoStack.push(undoAction.Rename(button, previousText, newText))
                                updateUndoButtonState()
                                button.text = newText
                                dialog.dismiss()
                            }
                            setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                        }.create()
                        dialog.show()
                    }

                    // Set the original background color
                    textButton?.setBackgroundColor(0xFF555555.toInt())
                    deleteButton?.setBackgroundColor(0xFF555555.toInt())
                    textButton?.setTextColor(0xFFA3A1A1.toInt())
                    deleteButton?.setTextColor(0xFFA3A1A1.toInt())

                }

                // Listener for deleting a button
                deleteButton?.setOnClickListener {
                    if (button.parent is ViewGroup) {
                        val parentView = button.parent as ViewGroup
                        val buttonIndex = parentView.indexOfChild(button)
                        val buttonText = button.text.toString()
                        val connectedButtons = getConnectedButtons(button) // Function to retrieve connected buttons

                        // Record the delete action in the undo stack
                        undoStack.push(undoAction.Delete(button))

                        // Remove the button from the parent view
                        parentView.removeView(button)

                        // Remove the button from the buttonList and all elements after it
                        buttonList.removeIf { it == button || buttonList.indexOf(it) > buttonList.indexOf(button) }

                        // Remove any lines connected to the button
                        lineViews.filter { it.isLineConnectedToButton(button) }
                            .forEach { parentView.removeView(it) }

                        // Remove all buttons after the deleted button in the parent view
                        for (i in parentView.childCount - 1 downTo buttonIndex + 1) {
                            parentView.removeViewAt(i)
                        }

                        // After deleting the button, make necessary buttons clickable and update UI
                        setButtonClickable(inputButton, true)
                        setButtonClickable(outputButton, true)
                        setButtonClickable(processButton, true)
                        setButtonClickable(endButton, true)
                        updateLines()
                        lastCreatedButton = null
                        updateConvertButtonState()

                        // Update the undo button state
                        updateUndoButtonState()
                    }
                }
            }
        }
    }

    private fun drawLineBetweenButtons(start: Button, end: Button) {
        val mainContent = findViewById<FrameLayout>(R.id.mainContent)
        val lineView = LineView(this)
        mainContent.addView(lineView)

        // Update the buttons in the LineView
        lineView.updateButtons(start, end)
        lineViews.add(lineView)

        // Store the connection in the buttonConnections list
        buttonConnections.add(ButtonConnection(start, end))
    }

    private fun getConnectedButtons(button: Button): List<Button> {
        return buttonConnections.filter { it.startButton == button }.map { it.endButton }
    }

    private fun updateLines() {
        val mainContent = findViewById<FrameLayout>(R.id.mainContent)
        val validLineViews = mutableListOf<LineView>()
        lineViews.forEach { lineView ->
            val isLineConnected = lineView.getStartButton() != null && lineView.getEndButton() != null
            if (isLineConnected && mainContent.indexOfChild(lineView) != -1) {
                validLineViews.add(lineView)
            } else {
                mainContent.removeView(lineView)
            }
        }
        lineViews.clear()
        lineViews.addAll(validLineViews)
        lineViews.forEach { it.invalidate() }
    }

    private fun updateUndoButtonState(){
        val hasActionsToUndo = undoStack.isNotEmpty()
        undoButton?.apply{
            isEnabled = hasActionsToUndo
            isClickable = hasActionsToUndo
            setBackgroundColor(if (hasActionsToUndo) Color.parseColor("#26a3a3") else Color.parseColor("#555555"))
            setTextColor(if (hasActionsToUndo) Color.parseColor("#FFFFFF") else Color.parseColor("#A3A1A1"))


        }
    }

    private fun updateConvertButtonState() {
        val mainContent = findViewById<FrameLayout>(R.id.mainContent)

        val startButtonPresent = startButton != null && mainContent.indexOfChild(startButton!!) != -1
        val outputButtonPresent = outputButton != null && mainContent.indexOfChild(outputButton!!) != -1
        val endButtonPresent = endButton != null && mainContent.indexOfChild(endButton!!) != -1

        val onlyStartOutputEndPresent = startButtonPresent && outputButtonPresent && endButtonPresent
        val allButtonsPresent = listOf(startButton, inputButton, processButton, endButton)
            .all { it != null && mainContent.indexOfChild(it) != -1 }

        val buttonsPresent = onlyStartOutputEndPresent || allButtonsPresent


        convertButton?.apply {
            isClickable = buttonsPresent
            isEnabled = buttonsPresent

            // Get the original background drawable
            val originalBackground = background

            // Set the background color to #171E28 when buttons are present
            if (buttonsPresent) {
                background = originalBackground
                setTextColor(0xFFFFFFFF.toInt())
            } else {
                // Restore the original background when buttons are not present
                background = originalBackground
                setTextColor(0xFFACACAC.toInt())
            }
        }
    }

    sealed class undoAction {
        data class Rename(val button: Button, val previousText: String, val newText: String) : undoAction()
        data class Delete(val button: Button) : undoAction()
    }
}