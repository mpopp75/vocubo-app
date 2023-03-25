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
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private EditText etUserName;
    private EditText etPassword;
    private TextView tvError;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUserName = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button bnLogin = findViewById(R.id.bnLogin);
        tvError = findViewById(R.id.tvError);

        pref = getSharedPreferences("vocubo", 0);

        bnLogin.setOnClickListener(this);

        // lets make life a bit easier for now; autofill user/password
        Button bnFill = findViewById(R.id.bnFill);
        bnFill.setOnClickListener(v -> {
            etUserName.setText(FillValues.username);
            etPassword.setText(FillValues.password);
        });
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

    public void onClick(View v) {
        LoginCheck lc = new LoginCheck(this,
                etUserName.getText().toString(),
                etPassword.getText().toString());

        lc.start();
    }

    public void httpCallback(String response) {

        try {
            JSONObject jsonobject = new JSONObject(response);
            if ((int)jsonobject.get("user_id") == -1) {
                // login not successful

                tvError.setVisibility(View.VISIBLE);
                tvError.setTextColor(Color.RED);
                tvError.setText(R.string.login_unsuccessful);
            } else {
                // login successful -> start new activity

                SharedPreferences.Editor ed = pref.edit();

                ed.putInt("user_id", (int)jsonobject.get("user_id"));
                ed.putString("user_name", jsonobject.get("user_name").toString());
                ed.putString("user_session", jsonobject.get("user_session").toString());
                ed.apply();

                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
            }
        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(), "JSONException: " + e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}