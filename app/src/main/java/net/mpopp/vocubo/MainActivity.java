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
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements HttpPostRequest.HttpPostRequestCallback {
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

        // lets make life a bit easier for now; autofill user/password
        Button bnFill = findViewById(R.id.bnFill);
        bnFill.setOnClickListener(v -> {
            etUserName.setText(FillValues.username);
            etPassword.setText(FillValues.password);
        });

        bnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://vocubo.mpopp.net/requests/app_login.php";
                String user = etUserName.getText().toString();
                String pass = etPassword.getText().toString();

                HttpPostRequest request = new HttpPostRequest(MainActivity.this, url);
                request.setParameter("user", user);
                request.setParameter("pass", pass);
                try {
                    request.execute();
                } catch (Exception e) {
                    Log.e(this.getClass().toString(), "Exception: " + e);
                }
            }
        });
    }

    @Override
    public void onRequestComplete(String result) {
        try {
            JSONObject jsonobject = new JSONObject(result);
            if ((int)jsonobject.get("user_id") == -1) {
                // login not successful

                tvError.setVisibility(View.VISIBLE);
                tvError.setTextColor(Color.RED);
                tvError.setText(R.string.login_unsuccessful);
            } else {
                // login successful -> start new activity

                Log.d(this.getClass().getSimpleName(), "Login: " + result);

                SharedPreferences.Editor ed = pref.edit();

                ed.putInt("user_id", (int)jsonobject.get("user_id"));
                ed.putString("user_name", jsonobject.get("user_name").toString());
                ed.putString("user_session", jsonobject.get("user_session").toString());
                ed.apply();

                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Exception: " + e);
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
        }
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
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}