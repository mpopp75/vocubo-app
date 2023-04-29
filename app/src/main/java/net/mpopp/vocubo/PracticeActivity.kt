package net.mpopp.vocubo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.mpopp.vocubo.HttpPostRequest.HttpPostRequestCallback
import org.json.JSONObject

class PracticeActivity : AppCompatActivity(), HttpPostRequestCallback {
    private var userId = 0
    private var userSession: String? = null
    private var tvQuestion: TextView? = null
    private var tvHint: TextView? = null
    private var edAnswer: EditText? = null
    private var bnSend: Button? = null
    private var tvResult: TextView? = null
    private var bnNext: Button? = null
    private var bnFinish: Button? = null
    private var questionId = 0
    private var action: String? = null
    private val url = "https://vocubo.mpopp.net/requests/app_practice.php"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        tvQuestion = findViewById(R.id.tvQuestion)
        tvHint = findViewById(R.id.tvHint)
        edAnswer = findViewById(R.id.edAnswer)
        bnSend = findViewById(R.id.bnSend)
        tvResult = findViewById(R.id.tvResult)
        bnNext = findViewById(R.id.bnNext)
        bnFinish = findViewById(R.id.bnFinish)
        bnNext!!.setOnClickListener {
            edAnswer!!.visibility = View.VISIBLE
            edAnswer!!.setText("")
            bnSend!!.visibility = View.VISIBLE
            tvResult!!.visibility = View.INVISIBLE
            bnNext!!.visibility = View.INVISIBLE
            bnFinish!!.visibility = View.INVISIBLE
            nextQuestion()
        }
        bnSend!!.setOnClickListener { checkAnswer() }
        bnFinish!!.setOnClickListener { returnToMainActivity() }
        val pref = getSharedPreferences("vocubo", 0)
        userId = pref.getInt("user_id", -1)
        val userName = pref.getString("user_name", "")
        userSession = pref.getString("user_session", "")
        tvLogin.setTextColor(resources.getColor(R.color.green))
        tvLogin.text = getText(R.string.login).toString() + ": " + userName
        nextQuestion()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.appmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.mnPreferences) {
            val intent = Intent(this, PreferencesActivity::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.mnAbout) {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun nextQuestion() {
        action = "question"
        val request = HttpPostRequest(this, url)
        request.setParameter("session_id", userSession!!)
        request.setParameter("action", action!!)
        request.execute()
    }

    private fun processQuestionResult(result: String?) {
        runOnUiThread(object : Runnable {
            override fun run() {
                try {
                    val jsonObject = JSONObject(result!!)
                    Log.d(this.javaClass.simpleName, "httpCallback() - question")
                    questionId = jsonObject.getInt("id")
                    tvQuestion!!.text = jsonObject.getString("word_base")
                    tvHint!!.text = jsonObject.getString("hints")
                } catch (e: Exception) {
                    Log.e(this.javaClass.simpleName, "Exception: $e")
                    Toast.makeText(
                        this@PracticeActivity,
                        R.string.connection_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun checkAnswer() {
        Log.d(this.javaClass.simpleName, "checkAnswer()")
        action = "answer"
        val answer = edAnswer!!.text.toString()
        val request = HttpPostRequest(this, url)
        request.setParameter("session_id", userSession!!)
        request.setParameter("action", action!!)
        request.setParameter("question_id", questionId.toString())
        request.setParameter("answer", answer)
        request.execute()
    }

    override fun onRequestComplete(result: String?) {
        if (action == "question") {
            processQuestionResult(result)
        } else {
            processAnswerResult(result)
        }
    }

    private fun processAnswerResult(result: String?) {
        runOnUiThread(object : Runnable {
            override fun run() {
                try {
                    val jsonobject = JSONObject(result!!)
                    Log.d(this.javaClass.simpleName, "httpCallback() - answer")
                    val correct = jsonobject.getString("correct")
                    val correctAnswer = jsonobject.getString("correct_answer")
                    edAnswer!!.visibility = View.INVISIBLE
                    bnSend!!.visibility = View.INVISIBLE
                    tvResult!!.visibility = View.VISIBLE
                    bnNext!!.visibility = View.VISIBLE
                    bnFinish!!.visibility = View.VISIBLE
                    if (correct == "correct") {
                        tvResult!!.setTextColor(Color.GREEN)
                        tvResult!!.setText(R.string.answer_correct)
                    } else {
                        tvResult!!.setTextColor(Color.RED)
                        val output = """${getString(R.string.your_answer)}: ${edAnswer!!.text}

${getString(R.string.answer_false)} $correctAnswer"""
                        tvResult!!.text = output
                    }
                } catch (e: Exception) {
                    Log.e(this.javaClass.simpleName, "Exception: $e")
                    Toast.makeText(
                        this@PracticeActivity,
                        R.string.connection_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun returnToMainActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}