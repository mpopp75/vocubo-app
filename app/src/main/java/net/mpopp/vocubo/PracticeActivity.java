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

public class PracticeActivity extends AppCompatActivity
        implements HttpPostRequest.HttpPostRequestCallback {

    int user_id;
    private String user_session;

    private TextView tvQuestion;
    private TextView tvHint;
    private EditText edAnswer;
    private Button bnSend;
    private TextView tvResult;
    private Button bnNext;
    private Button bnFinish;

    private int question_id;
    private String action;

    private final String url = "https://vocubo.mpopp.net/requests/app_practice.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        TextView tvLogin = findViewById(R.id.tvLogin);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvHint = findViewById(R.id.tvHint);
        edAnswer = findViewById(R.id.edAnswer);
        bnSend = findViewById(R.id.bnSend);
        tvResult = findViewById(R.id.tvResult);
        bnNext = findViewById(R.id.bnNext);
        bnFinish = findViewById(R.id.bnFinish);

        bnNext.setOnClickListener(v -> {
            edAnswer.setVisibility(View.VISIBLE);
            edAnswer.setText("");
            bnSend.setVisibility(View.VISIBLE);
            tvResult.setVisibility(View.INVISIBLE);
            bnNext.setVisibility(View.INVISIBLE);
            bnFinish.setVisibility(View.INVISIBLE);
            nextQuestion();
        });

        bnSend.setOnClickListener(v -> checkAnswer());

        bnFinish.setOnClickListener(v -> returnToMainActivity());

        SharedPreferences pref = getSharedPreferences("vocubo", 0);
        user_id = pref.getInt("user_id", -1);
        String user_name = pref.getString("user_name", "");
        user_session = pref.getString("user_session", "");

        tvLogin.setTextColor(Color.GREEN);
        tvLogin.setText(getText(R.string.login) + ": " + user_name);

        nextQuestion();
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

    private void nextQuestion() {
        action = "question";
        HttpPostRequest request = new HttpPostRequest(this, url);
        request.setParameter("session_id", user_session);
        request.setParameter("action", action);
        request.execute();
    }

    private void processQuestionResult(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonobject = new JSONObject(result);

                    Log.d(this.getClass().getSimpleName(), "httpCallback() - question");
                    question_id = jsonobject.getInt("id");
                    tvQuestion.setText(jsonobject.getString("word_base"));
                    tvHint.setText(jsonobject.getString("hints"));
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), "Exception: " + e);
                    Toast.makeText(PracticeActivity.this, R.string.connection_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkAnswer() {
        Log.d(this.getClass().getSimpleName(), "checkAnswer()");

        action = "answer";
        String answer = edAnswer.getText().toString();

        HttpPostRequest request = new HttpPostRequest(this, url);
        request.setParameter("session_id", user_session);
        request.setParameter("action", action);
        request.setParameter("question_id", String.valueOf(question_id));
        request.setParameter("answer", answer);
        request.execute();
    }

    @Override
    public void onRequestComplete(String result) {
        if (action.equals("question")) {
            processQuestionResult(result);
        } else {
            processAnswerResult(result);
        }
    }

    private void processAnswerResult(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonobject = new JSONObject(result);

                    Log.d(this.getClass().getSimpleName(), "httpCallback() - answer");
                    String correct = jsonobject.getString("correct");
                    String correct_answer = jsonobject.getString("correct_answer");

                    edAnswer.setVisibility(View.INVISIBLE);
                    bnSend.setVisibility(View.INVISIBLE);
                    tvResult.setVisibility(View.VISIBLE);
                    bnNext.setVisibility(View.VISIBLE);
                    bnFinish.setVisibility(View.VISIBLE);
                    if (correct.equals("correct")) {
                        tvResult.setTextColor(Color.GREEN);
                        tvResult.setText(R.string.answer_correct);
                    } else {
                        tvResult.setTextColor(Color.RED);

                        String output = getString(R.string.your_answer) + ": " + edAnswer.getText().toString() + "\n\n" +
                                getString(R.string.answer_false) + " " + correct_answer;

                        tvResult.setText(output);
                    }
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), "Exception: " + e);
                    Toast.makeText(PracticeActivity.this, R.string.connection_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
}