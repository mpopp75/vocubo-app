package net.mpopp.vocubo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PracticeRequests extends Thread {

    private HttpURLConnection connection;
    private PracticeActivity activity;
    private String action;
    private String user_session;

    private int question_id;
    private String answer;

    public PracticeRequests(PracticeActivity activity, String action, String user_session) {
        this.activity = activity;
        this.action = action;
        this.user_session = user_session;
    }

    public PracticeRequests(PracticeActivity activity, String action, int question_id,
                            String answer, String user_session) {
        this.activity = activity;
        this.action = action;
        this.user_session = user_session;
        this.question_id = question_id;
        this.answer = answer;
    }

    @Override
    public void run() {
        Log.d(this.getClass().getSimpleName(), "PracticeRequests.run()");

        try {
            String urlstring = "https://vocubo.mpopp.net/ajax_requests/app_practice.php";
            URL url;
            if (action.equals("question")) {
                url = new URL(urlstring + "?session_id=" + URLEncoder.encode(user_session, "UTF-8") +
                        "&action=" + URLEncoder.encode(action, "UTF-8"));
            } else if (action.equals("answer")) {
                url = new URL(urlstring + "?session_id=" + URLEncoder.encode(user_session, "UTF-8") +
                        "&action=" + URLEncoder.encode(action, "UTF-8") +
                        "&question_id=" + URLEncoder.encode(String.valueOf(question_id), "UTF-8") +
                        "&answer=" + URLEncoder.encode(answer, "UTF-8"));
            } else {
                Log.e(this.getClass().getSimpleName(), "Unknown action error");
                throw new Exception("Unknown action error");
            }

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");

            InputStream instream = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(instream);

            BufferedReader in = new BufferedReader(isr);

            StringBuilder r = new StringBuilder();

            String receivedLine;

            while ((receivedLine = in.readLine()) != null) {
                r.append(receivedLine).append("\n");
            }
            in.close();

            String response = r.toString();
            Log.d(this.getClass().getSimpleName(), "Response: " + response);

            activity.httpCallback(response);

        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "IOException: " + e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Exception: " + e);
        } finally {
            connection.disconnect();
        }
    }
}
