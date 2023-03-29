package net.mpopp.vocubo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity
        implements View.OnClickListener, HttpPostRequest.HttpPostRequestCallback {

    private String user_session;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        TextView tvLogin = findViewById(R.id.tvLogin);
        Button bnPractice = findViewById(R.id.bnPractice);
        Button bnLogout = findViewById(R.id.bnLogout);

        bnPractice.setOnClickListener(this);
        bnLogout.setOnClickListener(this);

        pref = getSharedPreferences("vocubo", 0);
        String user_name = pref.getString("user_name", "");
        user_session = pref.getString("user_session", "");

        tvLogin.setTextColor(Color.GREEN);
        tvLogin.setText(getText(R.string.login) + ": " + user_name);
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
    public void onClick(View v) {
        String buttonClicked = ((Button) v).getText().toString();

        switch (buttonClicked) {
            case "Practice" :
                Log.d(this.getClass().getSimpleName(), "Practice button clicked");

                Intent intent = new Intent(this, PracticeActivity.class);
                startActivity(intent);

                break;
            case "Logout" :
                Log.d(this.getClass().getSimpleName(), "Logout button clicked");

                String url = "https://vocubo.mpopp.net/requests/app_logout.php";

                HttpPostRequest request = new HttpPostRequest(this, url);
                request.setParameter("session_id", user_session);
                request.execute();
                break;
            default :
                Log.w(this.getClass().getSimpleName(), "Unknown button clicked");
        }
    }

    @Override
    public void onRequestComplete(String result) {
        Log.i(this.getClass().getSimpleName(), "result: " + result);

        try {
            if (result.equals("1")) {
                SharedPreferences.Editor ed = pref.edit();
                ed.putInt("user_id", -1);
                ed.putString("user_name", "");
                ed.putString("user_session", "");
                ed.apply();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                Log.e(this.getClass().getSimpleName(), "Logout failed");
                makeToast();
            }
        } catch(NumberFormatException e) {
            Log.e(this.getClass().getSimpleName(), "NumberFormatException: " + e);
            makeToast();
        } catch(Exception e) {
            Log.e(this.getClass().getSimpleName(), "Exception: " + e);
            makeToast();
        }
    }

    private void makeToast() {
        Toast.makeText(this, R.string.logout_failed, Toast.LENGTH_LONG).show();
    }
}