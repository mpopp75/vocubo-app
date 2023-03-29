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

public class PracticeActivity extends AppCompatActivity {

    private SharedPreferences pref;

    int user_id;
    private String user_session;

    private TextView tvLogin;
    private TextView tvQuestion;
    private TextView tvHint;
    private EditText edAnswer;
    private Button bnSend;
    private TextView tvResult;
    private Button bnNext;
    private Button bnFinish;

    private int question_id;
    private String action;

    private String url = "https://vocubo.mpopp.net/ajax_requests/app_practice.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        tvLogin = findViewById(R.id.tvLogin);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvHint = findViewById(R.id.tvHint);
        edAnswer = findViewById(R.id.edAnswer);
        bnSend = findViewById(R.id.bnSend);
        tvResult = findViewById(R.id.tvResult);
        bnNext = findViewById(R.id.bnNext);
        bnFinish = findViewById(R.id.bnFinish);

        bnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edAnswer.setVisibility(View.VISIBLE);
                edAnswer.setText("");
                bnSend.setVisibility(View.VISIBLE);
                tvResult.setVisibility(View.INVISIBLE);
                bnNext.setVisibility(View.INVISIBLE);
                bnFinish.setVisibility(View.INVISIBLE);
                nextQuestion();
            }
        });

        bnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });

        bnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMainActivity();
            }
        });

        pref = getSharedPreferences("vocubo", 0);
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
        HttpPostRequest request = new HttpPostRequest(url);
        request.setParameter("session_id", user_session);
        request.setParameter("action", "question");
        request.execute();

        processQuestionResult(request.getResult());
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
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void checkAnswer() {
        Log.d(this.getClass().getSimpleName(), "checkAnswer()");
        String answer = edAnswer.getText().toString();

        HttpPostRequest request = new HttpPostRequest(url);
        request.setParameter("session_id", user_session);
        request.setParameter("action", "answer");
        request.setParameter("question_id", String.valueOf(question_id));
        request.setParameter("answer", answer);
        request.execute();

        processAnswerResult(request.getResult());
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
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
}