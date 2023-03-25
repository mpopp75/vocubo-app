package net.mpopp.vocubo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LogoutCheck extends Thread {

    private final MenuActivity activity;

    private HttpURLConnection connection;

    private final String session_id;

    public LogoutCheck(MenuActivity activity, String session_id) {
        Log.d(this.getClass().getSimpleName(), "LogoutCheck()");

        this.activity = activity;
        this.session_id = session_id;
    }

    @Override
    public void run() {
        try {
            String urlstring = "https://vocubo.mpopp.net/ajax_requests/app_logout.php";
            URL url = new URL(urlstring + "?session_id=" + URLEncoder.encode(session_id, "UTF-8"));
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
