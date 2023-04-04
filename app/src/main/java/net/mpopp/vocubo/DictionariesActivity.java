package net.mpopp.vocubo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DictionariesActivity extends AppCompatActivity implements HttpPostRequest.HttpPostRequestCallback {

    private String user_session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionaries);

        TextView tvLogin = findViewById(R.id.tvLogin);

        SharedPreferences pref = getSharedPreferences("vocubo", 0);
        String user_name = pref.getString("user_name", "");
        user_session = pref.getString("user_session", "");

        tvLogin.setTextColor(getResources().getColor(R.color.green));
        tvLogin.setText(getText(R.string.login) + ": " + user_name);

        HttpPostRequest request = new HttpPostRequest(this, "https://vocubo.mpopp.net/requests/app_dictionaries.php");
        request.setParameter("session_id", user_session);
        request.setParameter("action", "dictionary_list");
        request.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.appmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mnPreferences) {
            Intent intent = new Intent(this, PreferencesActivity.class);

            startActivity(intent);
            return true;
        }
        else if (id == R.id.mnAbout) {
            Intent intent = new Intent(this, AboutActivity.class);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestComplete(String result) {
        Log.d(this.getClass().getSimpleName(), "result: " + result);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray json = new JSONArray(result);
                    createListView(json);
                } catch (JSONException e) {
                    Log.e(this.getClass().getSimpleName(), "JSONException: " + e);
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), "Exception: " + e);
                }
            }
        });

    }

    private void createListView(JSONArray json) throws JSONException {
        ListView lvDictionaries = findViewById(R.id.lvDictionaries);

        ArrayList<JSONObject> dictionaryList = new ArrayList<JSONObject>();
        for (int i = 0; i < json.length(); i++) {
            dictionaryList.add(json.getJSONObject(i));
        }

        DictionaryAdapter adapter = new DictionaryAdapter(this, dictionaryList);
        lvDictionaries.setAdapter(adapter);
    }
}