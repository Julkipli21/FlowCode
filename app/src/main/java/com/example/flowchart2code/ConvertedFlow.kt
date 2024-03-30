package com.example.flowchart2code

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.isInvisible
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ConvertedFlow : ComponentActivity() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()

    private var lastCreatedButton: Button? = null
    private var isFirstButton = true

    private var startButton: Button? = null
    private var decisionButton: Button? = null
    private var yesButton: Button? = null
    private var noButton: Button? = null
    private var inputButton: Button? = null
    private var outputButton: Button? = null
    private var processButton: Button? = null
    private var loopButton: Button? = null
    private var endButton: Button? = null

    private val lineViews = mutableListOf<LineView>()
    private val buttonList = mutableListOf<Button>()
//    private var cloneButton: Button? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converted_flow)

        var vision = findViewById<TextView>(R.id.vision)
        val buttonStart = findViewById<Button>(R.id.start)
        val buttonLoop = findViewById<Button>(R.id.loop)
        val buttonDecision = findViewById<Button>(R.id.decision)
        val buttonYes = findViewById<Button>(R.id.yes)
        val buttonNo = findViewById<Button>(R.id.no)
        val buttonInput = findViewById<Button>(R.id.input)
        val buttonOutput = findViewById<Button>(R.id.output)
        val buttonProcess = findViewById<Button>(R.id.process)
        val buttonEnd = findViewById<Button>(R.id.end)

        val question = intent.getStringExtra("question")
        var trylngs = ""

//        vision.text = question

        if (question != null) {
            getResponse(question) { response ->
                runOnUiThread {
//                    vision.text = response
                    vision.isInvisible = true

                    val data = response?.split("->")?.map { it.trim() }?.toTypedArray()

                    // Iterate through each part
                    data?.forEach { part ->
                        var (resId, text) = part.split(":").map { it.trim() }
//                        trylngs += "$resId"
//                        vision.text = "${buttonStart.text}"

                        if(resId == "yes"){
                            text = "yes"
                        }
                        else if(resId == "no"){
                            text = "no"
                        }

                        if (resId == buttonStart.text){
                            val clone = createButtonClone(buttonStart)
                            addCloneToMainContent(clone)
                            startButton = clone

                            buttonList.add(startButton!!)
                        }
                        else if (resId.contains(buttonDecision.text)){
                            val clone = createButtonClone(buttonDecision)
                            clone.text = text
                            addCloneToMainContent(clone)
                            decisionButton = clone

                            buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, decisionButton!!) }
                            buttonList.add(decisionButton!!)

                            yesButton = createButtonClone(buttonYes)
                            yesButton!!.text = "yes"
                            noButton = createButtonClone(buttonNo)
                            noButton!!.text = "no"

                            // Set positions of the intermediate buttons on both sides of the decision button
                            val horizontalSpacing = resources.getDimensionPixelSize(R.dimen.horizontal_spacing)
                            val horizontalSpacingNo = resources.getDimensionPixelSize(R.dimen.horizontal_spacing_no)
                            val verticalSpacing = resources.getDimensionPixelSize(R.dimen.vertical_spacing)

                            // Position the "Yes" button to the left of the decision button
                            yesButton!!.x = decisionButton!!.x - yesButton!!.width - horizontalSpacing
                            yesButton!!.y = decisionButton!!.y + verticalSpacing

                            // Position the "No" button to the right of the decision button
                            noButton!!.x = decisionButton!!.x + noButton!!.width + horizontalSpacingNo
                            noButton!!.y = decisionButton!!.y + verticalSpacing

                            // Add the clones to the main content
                            addCloneToMainContent(yesButton!!)
                            addCloneToMainContent(noButton!!)
                            drawLineBetweenButtons(decisionButton!!, yesButton!!)
                            drawLineBetweenButtons(decisionButton!!, noButton!!)

                            loopButton?.let{ drawLineBetweenButtons(noButton!!, it) }
                        }
                        else if (resId.contains(buttonYes.text)){
                            yesButton!!.text = text
                            buttonList.add(yesButton!!)
                        }
                        else if (resId.contains(buttonNo.text)){
                            noButton!!.text = text
                        }
                        else if(resId.contains(buttonLoop.text)){
                            if(!buttonList.contains(loopButton)){
                                val clone = createButtonClone(buttonLoop)
                                clone.text = text
                                addCloneToMainContent(clone)
                                loopButton = clone

                                buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, loopButton!!) }
                                buttonList.add(loopButton!!)
                            }
                        }
                        else if (resId.contains(buttonInput.text)){
                            val clone = createButtonClone(buttonInput)
                            clone.text = text
                            addCloneToMainContent(clone)
                            inputButton = clone

                            buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, inputButton!!) }
                            buttonList.add(inputButton!!)
                        }
                        else if (resId.contains(buttonOutput.text)){
                            val clone = createButtonClone(buttonOutput)
                            clone.text = text
                            addCloneToMainContent(clone)
                            outputButton = clone

                            buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, outputButton!!) }
                            buttonList.add(outputButton!!)
                        }
                        else if (resId.contains(buttonProcess.text)){
                            val clone = createButtonClone(buttonProcess)
                            clone.text = text
                            addCloneToMainContent(clone)
                            processButton = clone

                            buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, processButton!!) }
                            buttonList.add(processButton!!)
                        }
                        else if (buttonEnd.text.contains(resId)){
                            if(!buttonList.contains(decisionButton)){
                                val clone = createButtonClone(buttonEnd)
                                addCloneToMainContent(clone)
                                endButton = clone

                                buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, endButton!!) }
                                buttonList.add(endButton!!)
                            } else {
                                if(!buttonList.contains(noButton!!)){
                                    val clone = createButtonClone(buttonEnd)
                                    addCloneToMainContent(clone)
                                    endButton = clone

                                    buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, endButton!!) }
                                    buttonList.add(endButton!!)
                                    buttonList.add(decisionButton!!)
                                    buttonList.add(noButton!!)

                                    if(buttonList.contains(loopButton)){
                                        buttonList.add(loopButton!!)
                                    }
                                }
                                else{
                                    buttonList.lastOrNull()?.let{ drawLineBetweenButtons(it, endButton!!) }
                                    buttonList.add(endButton!!)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createButtonClone(originalButton: Button): Button {
        val clone = Button(this)

        // Set the desired text size (change this value as needed)
        val textSizeInSp = 8
        clone.textSize = textSizeInSp.toFloat()

//        clone.stateListAnimator = null

        clone.background = originalButton.background
        clone.text = originalButton.text
        clone.setTextColor(originalButton.currentTextColor)

        clone.layoutParams = LinearLayout.LayoutParams(
            originalButton.layoutParams.width,
            originalButton.layoutParams.height
        )

        val mainContent = findViewById<FrameLayout>(R.id.mainContent)
        val xPosition = (mainContent.width - originalButton.width) / 2f
        val verticalSpacing = resources.getDimensionPixelSize(R.dimen.vertical_spacing)

        val yPosition = if (isFirstButton) {
            resources.getDimensionPixelSize(R.dimen.top_spacing).toFloat()
        } else {
            lastCreatedButton?.let {
                it.y + it.height + verticalSpacing + 150f
            } ?: 0f
        }

        clone.x = xPosition
        clone.y = yPosition

        lastCreatedButton = clone
        isFirstButton = false

        clone.setOnTouchListener(@SuppressLint("ClickableViewAccessibility")
        object : View.OnTouchListener {
            private var startX = 0f
            private var startY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
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
                }
                return true
            }
        })

        return clone
    }

    private fun addCloneToMainContent(clone: View) {
        val mainContent = findViewById<FrameLayout>(R.id.mainContent)
        mainContent.addView(clone)
    }

    fun getResponse(questions: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.openai.com/v1/chat/completions"

        val questionList = mutableListOf<Map<String, String>>()

        // Add a user message to the question list
        val userMessage = mapOf("role" to "user", "content" to questions)
        questionList.add(userMessage)

        val requestBody = mapOf(
            "model" to "gpt-3.5-turbo-16k",
            "messages" to questionList,
            "max_tokens" to 15000
        )

        val jsonRequestBody = JSONObject(requestBody).toString()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(
                RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    jsonRequestBody
                )
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    Log.v("data", body)
                } else {
                    Log.v("data", "empty")
                }
                // Log the API response for troubleshooting
                Log.v("API_RESPONSE", body ?: "empty")

                try {
                    val jsonObject = JSONObject(body)
                    val choicesArray = jsonObject.getJSONArray("choices")

                    if (choicesArray.length() > 0) {
                        val assistantMessage = choicesArray.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")

                        callback(assistantMessage)
                    }
                } catch (e: Exception) {
                    Log.e("error", "Error processing response", e)
                }
            }
        })
    }

    private fun drawLineBetweenButtons(start: Button, end: Button) {
        val mainContent = findViewById<FrameLayout>(R.id.mainContent)
        val lineView = LineView(this)
        mainContent.addView(lineView)

        // Update the buttons in the LineView
        lineView.updateButtons(start, end)
        lineViews.add(lineView)
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
}