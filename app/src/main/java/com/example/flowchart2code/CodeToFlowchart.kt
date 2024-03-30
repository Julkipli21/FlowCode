package com.example.flowchart2code

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import okhttp3.*

class CodeToFlowchart : ComponentActivity() {
    private lateinit var editTextCode: EditText
    private lateinit var buttonConvert: Button
    private lateinit var aiResponse: TextView
    private var isDefaultTextSet = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_to_flowchart)

        editTextCode = findViewById(R.id.editTextCode)
        buttonConvert = findViewById(R.id.buttonConvert)

        // Set default text when EditText is clicked
        editTextCode.setOnClickListener {
            if (!isDefaultTextSet) {
                editTextCode.setText("#include <iostream>\nusing namespace std;\n")
                isDefaultTextSet = true
            }
        }

        // Start typing below the default text
        editTextCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isDefaultTextSet && start == 0 && count == 0) {
                    // Text is deleted at the beginning, reset default text
                    editTextCode.setText("#include <iostream>\nusing namespace std;\n")
                    editTextCode.setSelection(editTextCode.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun onConvertButtonClicked(view: View) {
        var inputCode = editTextCode.text.toString()

        if(isCppCode(inputCode)){
            var prompt = """
            $inputCode
            based on this provided code transform it into something like this "#include <iostream> using namespace std; int main() {" should become "start: Start",  " return 0; }" should be 
            "end: End". For declarations like int x = 0; ignore it. For inputs like anything needs to input something like "cin>>'Enter a number'", they should be 
            "input: Enter a number". For all kinds of outputs like "cout<<'Hello World!'" should be like "output: Display Hello World!". Where every steps are separated by " -> ". Provide it in textual 
            single line without any explanations and line breaks. Understand the given code even if it lacks like indentation always analyze and understand it as a c++ code and even the 
            provided code doesn't have like namespace std in it. Provide only what is given in provided code. Also your provided should be understandable and should be easy to understood and 
            simplify as possible also. sample output "start: Start -> output: Hello World! -> end: End" (this is just a sample reference, you should reference your provided into like it).
            in "outputs: " and "inputs: " always add and make it something like "input: Enter a number" or "output: Display i" means make the provided as understandable as possible like when if its for output always add maybe like "output: Display" and same for input "input: Enter a " and always add an "end: End" at the very last
            """.trimMargin()

            if(inputCode.contains("loop") || inputCode.contains("while")){
                prompt = """
                        |$prompt 
                        |If the provided code consists of "for loop, while or do while loop" provide it like "loop: for loop" then convert the rest inside the loop once only, do not provide it 
                        |continuously in repeat, loop through it once only. After the end of loop, always provide  a decision like this "decision: if i is less than 10 -> no: greater than 10 -> 
                        |loop: while loop -> yes: less than  or equal to 10 -> end: End ". "decision" is for exiting the loop or repeating the loop. Always provide "no: " after the decision then 
                        |"loop: ". yes: " is for when the loop logic is satisfied and exits the loop and "no: " for repeating the loop. Use this reference as example "loop: while loop -> output: output i 
                        |-> process: increment i -> decision: if i less than 10 -> no: greater than 10 -> loop: while loop -> yes: less than  or equal to 10 -> end: End".  Means if the condition is not 
                        |met, it will go back to the loop again and if met, it will exits the loop. Example code "while(i <= 10){ cout<<i; i++; } return 0;" should be provided and converted into 
                        |"loop: while loop -> output: display i -> process: increment i -> decision: if i is less than 10 -> no: i is greater than 10 -> loop: while loop" means if it the decision falls under "no: ", it will iterate back 
                        |to loop and always use "no" step node as iterator to loop, while the "yes" condition for exiting loop means after the second "loop: ", it should be "yes: " and continue it until "end: End". Then proceed to yes after the no " -> yes: i is less than 10 -> end: End". 
                        |Sample outputs "start: Start -> loop: while loop -> output: display i -> process: increment i -> decision: if i less than 10 -> no: greater than 10 -> loop: while loop -> yes: less than  
                        |or equal to 10 -> end: End" (sample reference for provided code with loops).
                    """.trimMargin()
            }
            else{
                if(inputCode.contains("if") || inputCode.contains("else")){
                    prompt = """
                        |$prompt 
                        |If there is a decision in provide code like "if and else", it should be outputted "decision: check if number < 0" then add separator which is " -> " then proceed to 
                        |"yes: less than 0" then another separator and proceed to whatever code is for the next until it reaches the "end: End" but not for else yet. After reaching the "end: End"
                        |, if there is else in provided code, continue into it after the "end: End", something like "end: End -> no: greater than or equal to 0" then add separator and continue the 
                        |code inside of it until it reaches "end: End" again. Sample output "start: Start -> input: Enter x -> decision: check if x > 10 -> yes: x is greater than 10 -> output: x 
                        |is greater than 10 -> end: End -> no: x is lower or equal to 10 -> output: x is lower or equal to 10 -> end: End" (this is for decisions like if and else)
                    """.trimMargin()
                }
            }

//            val prompt = """
//            $inputCode
//            based on this provided code transform it into something like this "#include <iostream> using namespace std; int main() {" should become "start: Start",  " return 0; }" should be "end: End" Each are named step nodes.
//            For declarations like int x = 0; ignore it. For inputs like anything needs to input something like "cin>>'Enter a number'", they should be "input: Enter a number". For all kinds of outputs like "cout<<'Hello World!'"
//            should be "output: Hello World!". If there is a decision in provide code like "if and else", it should be outputted "decision: check if number < 0" then add separator which is " -> " then proceed to "yes: less than 0"
//            then another separator and proceed to whatever code is for the next until it reaches the "end: End" but not for else yet. After reaching the "end: End", if there is else in provided code, coninue into it after the
//            "end: End", something like "end: End -> no: greater than or equal to 0" then add separator and continue the code inside of it until it reaches "end: End" again. If there's a loop in given code like "for loop, while or
//            do while loop" provide it like "loop: for loop" then convert the rest inside the loop code until it reaches the end of loop once only, do not provide it continuesly like provide all repeatedly until finish, only loop
//            through it once. You should provide a decision step node for loops, decision is for exiting the loop or repeating the loop decision just like in the raptor diagram application. Yes for when the loop logic is satisfied
//            and exits the loop and No for repeating the loop. You should provide it like this "loop: while loop -> output: i -> process: i++ -> decision: if i <= 10 -> no: greater than 10 -> loop: while loop -> yes: less than  or
//            equal to 10 -> end: End" (sample reference for provided code with loops" after the decision, always provide the "no" decision then back to loop then provide the "yes" like this "decision: if i <= 10 -> no: greater than
//            10 -> loop: while loop -> yes: less than  or equal to 10 -> end: End" then proceed providing the rest of the provided code in step node until it reaches "end: End" means if the condition is not met, it will go back to
//            the loop step node again. Example "while(i <= 10){ cout<<i; i++; } return 0;" should be "loop: while loop -> output: i -> process: i++ -> decision: if i <= 10 -> no: i <= 10 -> loop: while loop" means if it the decision
//            falls under "no: ", it will iterate back to loop and always use "no" step node as iterator to loop, while the "yes" condition or step node as exiting loop. Then proceed to yes after the no " -> yes: i >= 10 -> end: End".
//            Again always provide it once for provided code with loops in it, only convert and provide once. see the below reference and follow how it's done.
//            Where every step nodes are separated by " -> ". Provide it in textual single line without any explanations and line breaks. Understand the given code even if it lacks like indentation always analyze and understand it as
//            a c++ code and provide only what is given in provided code, don't provide loops when not included in code. Also your provided should be understandable and should be easy to understood and simplify as possible also.
//            sample outputs "start: Start -> input: Enter x -> decision: check if x > 10 -> yes: x is greater than 10 -> output: x is greater than 10 -> end: End -> no: x is lower or equal to 10 -> output: x is lower or equal to 10
//            -> end: End" (this is for decisions like if and else), "start: Start -> loop: while loop -> output: i -> process: i++ -> decision: if i <= 10 -> no: greater than 10 -> loop: while loop -> yes: less than  or equal to 10
//            -> end: End" (sample reference for provided code with loops).
//            """.trimMargin()
//
//            based on this code please transform or convert this into this format ( start: Start -> loop: while number >= 0 -> input: Enter a number -> process: sum += number -> decision: check if number < 0 -> yes: less than 0 -> output: The sum is sum -> end: End -> no: greater than or equal to 0 -> loop: while number >= 0 ) if there is loop in provided code you should include it like eg. "loop: while number >= 0" if used code contains "while" and "for loop" if for loop only if there is loop in provided code. It should be two loop for yes decision and for no decision if for example not even number then it should be "no: Odd number -> loop: For Loop" then done no need to continue or outputting input again, means it will go back to loop until it satisfy and exit through "yes" decision Always use the "no" for repeating the loop even "end: End" don't just completely stop output ( no: greater than 0 -> loop: For loop ) for "no" decision then immediately stop providing after these two, prevent outputting after it. whole output should be like ( start: Start -> loop: while number >= 0 -> input: Enter a number -> process: sum += number -> decision: check if number < 0 -> yes: less than 0 -> output: The sum is sum -> end: End -> no: greater than or equal to 0 -> loop: while number >= 0 ) if have loop in provided code. Every step should be separated by "->" depending on the provided c++ code. Do not explain, add notes
//            or footnote and do not respond in a list, provide it in a single line without any line breaks or space lines. Provide the converted right away. both start and end should be
//            lock as "start: Start and end: End" which "#include <iostream> using namespace std; int main() {" should be the "start: Start" , "return 0; }" should be the "end: End",
//            "cout" should be "output: Hello World" and depending on the given code and declaring variables are nothing ignore it. provide only what is present on the provided c++ code. Inputs, outputs, decisions, loops and processes
//            should be the inside code and next to it should be ":" and then the value inside of it should be the value inside of the variable. In decision should be like decision: if
//            input1 < input2. If say for example there is no cin or inputs, decisions or loops in provided code, then don't include it in the output. If there is decision if else statement, after "decision: Decision" next should always be "yes: Yes" then finish the yes statement then go to "no: No" yes and no is the outcome of decision. Sample if even do this so it should be like "yes: Even" and "no: Odd". Another sample output is (start: Start -> input: Any number -> process: remainder -> decision: Check if even or odd -> yes: Even -> output: Number is Even -> end: End -> no: Odd -> output: Number is Odd -> end: End). Understand the given code even if it lacks like indentation always analyze it like a c++ code and provide only what is given in provided code, don't provide loops when not included in code. don't provide the sample code if not understood the provided code you should only use it as reference. Also your provided should be understandable and should be easy to understood and simplify as possible also.
//        """.trimMargin()

            val intent = Intent(this, ConvertedFlow::class.java)
            intent.putExtra("question", prompt)
            startActivity(intent)
        }
        else{
            Toast.makeText(this, "The provided code is not in C++.", Toast.LENGTH_SHORT).show()
        }
    }

    fun isCppCode(inputCode: CharSequence): Boolean {
        // List of common C++ keywords or patterns
        val cppKeywords = listOf("int", "double", "float", "char", "void", "if", "else", "for", "while", "class", "struct", "namespace", "#include", "#define")

        // Check if any C++ keyword or pattern exists in the input code
        return cppKeywords.any { keyword -> inputCode.contains(keyword) }
    }
}
