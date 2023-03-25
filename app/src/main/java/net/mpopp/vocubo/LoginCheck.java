package net.mpopp.vocubo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginCheck extends Thread {

    private final MainActivity activity;

    private final String username;
    private final String password;

    private HttpURLConnection connection;

    public LoginCheck(MainActivity activity, String username, String password) {
        Log.d(this.getClass().getSimpleName(), "LoginCheck()");

        this.activity = activity;

        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        Log.d(this.getClass().getSimpleName(), "Login button clicked");

        try {
            String urlstring = "https://vocubo.mpopp.net/ajax_requests/app_login.php";
            URL url = new URL(urlstring + "?user=" + URLEncoder.encode(username, "UTF-8") +
                    "&pass=" + URLEncoder.encode(password, "UTF-8"));
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
