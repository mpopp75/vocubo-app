package net.mpopp.vocubo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.mpopp.vocubo.HttpPostRequest.HttpPostRequestCallback

class MenuActivity : AppCompatActivity(), HttpPostRequestCallback {
    private var userSession: String? = null
    private var pref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val bnPractice = findViewById<Button>(R.id.bnPractice)
        val bnDictionaries = findViewById<Button>(R.id.bnDictionaries)
        val bnLogout = findViewById<Button>(R.id.bnLogout)
        bnPractice.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Log.d(this.javaClass.simpleName, "Practice button clicked")
                val intent = Intent(this@MenuActivity, PracticeActivity::class.java)
                startActivity(intent)
            }
        })
        bnDictionaries.setOnClickListener {
            val intent = Intent(this@MenuActivity, DictionariesActivity::class.java)
            startActivity(intent)
        }
        bnLogout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Log.d(this.javaClass.simpleName, "Logout button clicked")
                val url = "https://vocubo.mpopp.net/requests/app_logout.php"
                val request = HttpPostRequest(this@MenuActivity, url)
                request.setParameter("session_id", userSession!!)
                request.execute()
            }
        })
        pref = getSharedPreferences("vocubo", 0)
        val userName = pref!!.getString("user_name", "")
        userSession = pref!!.getString("user_session", "")
        tvLogin.setTextColor(resources.getColor(R.color.green))
        tvLogin.text = getText(R.string.login).toString() + ": " + userName
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

    override fun onRequestComplete(result: String?) {
        Log.i(this.javaClass.simpleName, "result: $result")
        try {
            if (result == "1") {
                val ed = pref!!.edit()
                ed.putInt("user_id", -1)
                ed.putString("user_name", "")
                ed.putString("user_session", "")
                ed.apply()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Log.e(this.javaClass.simpleName, "Logout failed")
                makeToast()
            }
        } catch (e: NumberFormatException) {
            Log.e(this.javaClass.simpleName, "NumberFormatException: $e")
            makeToast()
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Exception: $e")
            makeToast()
        }
    }

    private fun makeToast() {
        Toast.makeText(this, R.string.logout_failed, Toast.LENGTH_LONG).show()
    }
}