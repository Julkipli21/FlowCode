package com.example.flowchart2code

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class FlowCodeConverter : ComponentActivity() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flowcode_converter)

        val txtResponse = findViewById<TextView>(R.id.txtResponse)

        // Retrieve the passed information from the intent
        val buttonInfoList = intent.getParcelableArrayListExtra<ButtonInfo>("buttonInfoList")


        val data = buttonInfoList?.joinToString(" -> ") { it.formattedText }

        val question =
            """
                $data
                please convert this into a c++ code with includes iostream and namespace std; . Where "start: Start" should be "#include <iostream> using namespace std; int main() {" 
                and "end: End" should be " return 0; }". Do not add any labels, footnotes or any quotations, provide it in textual single line without any explanations and line breaks 
                but include a short but informative comments for each steps just to make it understandable sample "// Inputs int input1; int input2; // Process int result = input1 + 
                input2; // Output result;" no need to provide comments for "numbers", "start" and "end" sample(// Start or // End). Do not use or output unimportant spaces. Do not 
                provide anything before the "start" and after "end". In converting to code in cout or cin please indicate and provide what needs to be entered and also make the code 
                cleaner and understandable, provide it in having include namespace std type. also do not include or provide ( ```cpp ```) something, provide only the code.
                Example "start: Start -> output: Hello World! -> end: End" should become and converted into "#include <iostream> using namespace std; int main() { cout<<"Hello World" return 0; }".
                "start: Start -> loop: while loop -> output: display i -> process: increment i -> decision: if i less than 10 -> no: greater than 10 -> loop: while loop -> yes: less than or equal to 10 -> end: End"
                should be converted into "#include <iostream> using namespace std; int main() { int i = 6; while(i <= 10){ cout<<i<<" "; i+++; } return 0; }". this are just sample reference please use it as guide in converting,
                provide it with indentations in c++ code.
            """.trimMargin()

        getResponse(question) { response ->
            runOnUiThread {
                txtResponse.text = response
            }
        }

//        runOnUiThread {
//                txtResponse.text = data
////                txtResponse.text = ""
////                txtResponse.text = question
//        }
    }

    fun copyText(view: View) {
        val txtResponse = findViewById<TextView>(R.id.txtResponse)
        val textToCopy = txtResponse.text.toString()

        val clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Copied Code", textToCopy)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun getResponse(questions: String, callback: (String) -> Unit) {
        val apiKey = "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"
//        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 wews
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
            .post(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonRequestBody))
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
}