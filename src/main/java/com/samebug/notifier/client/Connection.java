package com.samebug.notifier.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

class Connection {
    private final URL serverUrl;

    public Connection(final URL serverUrl) {
        this.serverUrl = serverUrl;
    }

    public HttpURLConnection createConnection() throws IOException {
        URLConnection urlConnection = serverUrl.openConnection();
        if (urlConnection == null || !(urlConnection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("Crash monitor connection cannot handle protocol: " + serverUrl);
        }
        HttpURLConnection connection = (HttpURLConnection) urlConnection;
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setDoOutput(true);
        connection.connect();
        return connection;
    }

    public int processResponse(final HttpURLConnection conn) throws IOException {
        final int rc = conn.getResponseCode();
        return rc;
    }
}
