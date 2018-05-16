package com.samebug.notifier.client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

public class CrashMonitorClient {
    private final Connection connection;

    public CrashMonitorClient() {
        final String crashMonitorAddress = System.getProperty("com.samebug.notifier.agent.crashMonitorUrl");
        URL crashMonitorUrl;
        try {
            crashMonitorUrl = URI.create(crashMonitorAddress).toURL();
        } catch (MalformedURLException e) {
            crashMonitorUrl = null;
        }
        connection = crashMonitorUrl != null ? new Connection(crashMonitorUrl) : null;
    }

    public int report(final Throwable throwable) throws IOException {
        if (connection != null) {
            final HttpURLConnection http = connection.createConnection();
            writeReport(http, throwable);
            return connection.processResponse(http);
        } else {
            return -1;
        }
    }

    private void writeReport(final HttpURLConnection connnection, final Throwable throwable) throws IOException {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(connnection.getOutputStream());
            Serializer serializer = new Serializer(writer);
            serializer.serialize(UUID.randomUUID(), throwable);
            writer.flush();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (final IOException e) {
                    System.err.println("Crash monitor client failed to close output stream:\n");
                    // TODO need some protection against looping on reporting the report failure
//                    e.printStackTrace(System.err);
                }
            }
        }
    }

}
