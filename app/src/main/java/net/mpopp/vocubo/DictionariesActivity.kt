package net.mpopp.vocubo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.mpopp.vocubo.HttpPostRequest.HttpPostRequestCallback
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DictionariesActivity : AppCompatActivity(), HttpPostRequestCallback {
    private var userSession: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionaries)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val pref = getSharedPreferences("vocubo", 0)
        val userName = pref.getString("user_name", "")
        userSession = pref.getString("user_session", "")
        tvLogin.setTextColor(resources.getColor(R.color.green))
        tvLogin.text = getText(R.string.login).toString() + ": " + userName
        val request =
            HttpPostRequest(this, "https://vocubo.mpopp.net/requests/app_dictionaries.php")
        request.setParameter("session_id", userSession.toString())
        request.setParameter("action", "dictionary_list")
        request.execute()
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
        Log.d(this.javaClass.simpleName, "result: $result")
        runOnUiThread(object : Runnable {
            override fun run() {
                try {
                    val json = JSONArray(result)
                    createListView(json)
                } catch (e: JSONException) {
                    Log.e(this.javaClass.simpleName, "JSONException: $e")
                } catch (e: Exception) {
                    Log.e(this.javaClass.simpleName, "Exception: $e")
                }
            }
        })
    }

    @Throws(JSONException::class)
    private fun createListView(json: JSONArray) {
        val lvDictionaries = findViewById<ListView>(R.id.lvDictionaries)
        val dictionaryList = ArrayList<JSONObject>()
        for (i in 0 until json.length()) {
            dictionaryList.add(json.getJSONObject(i))
        }
        val adapter = DictionaryAdapter(this, dictionaryList)
        lvDictionaries.adapter = adapter
    }
}