package net.mpopp.vocubo

import android.content.Intent
import android.content.SharedPreferences
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

class MainActivity : AppCompatActivity(), HttpPostRequestCallback {
    private var etUserName: EditText? = null
    private var etPassword: EditText? = null
    private var tvError: TextView? = null
    private var pref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etUserName = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        val bnLogin = findViewById<Button>(R.id.bnLogin)
        tvError = findViewById(R.id.tvError)
        pref = getSharedPreferences("vocubo", 0)
        val userName = pref!!.getString("user_name", "")
        val userSession = pref!!.getString("user_session", "")

        // Check if user is already logged in
        if (userName != "" && userSession != "") {
            val url = "https://vocubo.mpopp.net/requests/app_checklogin.php"
            val request = HttpPostRequest(this, url)
            request.setParameter("session_id", userSession!!)
            request.execute()
        }
        bnLogin.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val url = "https://vocubo.mpopp.net/requests/app_login.php"
                val user = etUserName!!.text.toString()
                val pass = etPassword!!.text.toString()
                val request = HttpPostRequest(this@MainActivity, url)
                request.setParameter("user", user)
                request.setParameter("pass", pass)
                try {
                    request.execute()
                } catch (e: Exception) {
                    Log.e(this.javaClass.toString(), "Exception: $e")
                }
            }
        })
    }

    // Login check
    override fun onRequestComplete(result: String?) {
        try {
            val jsonObject = JSONObject(result!!)
            if (jsonObject["user_id"] as Int == -1) {
                // login not successful
                tvError!!.visibility = View.VISIBLE
                tvError!!.setTextColor(Color.RED)
                tvError!!.setText(R.string.login_unsuccessful)
            } else {
                // login successful -> start new activity
                Log.d(this.javaClass.simpleName, "Login: $result")
                val ed = pref!!.edit()
                ed.putInt("user_id", jsonObject["user_id"] as Int)
                ed.putString("user_name", jsonObject["user_name"].toString())
                ed.putString("user_session", jsonObject["user_session"].toString())
                ed.apply()
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Exception: $e")
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show()
        }
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
}