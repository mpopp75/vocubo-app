package net.mpopp.vocubo;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpPostRequest extends AsyncTask<Void, Void, String> {
    private final String url;
    private final Map<String, String> parameters;
    private final HttpPostRequestCallback callback;

    private final int timeout;

    public HttpPostRequest(HttpPostRequestCallback callback, String url) {
        this.callback = callback;
        this.url = url;
        this.parameters = new HashMap<>();

        // default timeout is 5000 milliseconds
        this.timeout = 5000;
    }

    public HttpPostRequest(HttpPostRequestCallback callback, String url, int timeout) {
        this.callback = callback;
        this.url = url;
        this.parameters = new HashMap<>();
        this.timeout = timeout;
    }

    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    @Override
    protected String doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        String result = null;
        try {
            URL url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            OutputStream outputStream = connection.getOutputStream();
            StringBuilder builder = new StringBuilder();

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }

            String parameters = builder.toString();
            parameters = parameters.substring(0, parameters.length() - 1);

            outputStream.write(parameters.getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                result = responseBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            callback.onRequestComplete(result);
        }
    }

    public interface HttpPostRequestCallback {
        void onRequestComplete(String result);
    }
}