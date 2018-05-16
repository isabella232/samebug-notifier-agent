package com.samebug.notifier.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Writer;
import java.util.ArrayList;
import java.util.UUID;

public class Serializer {
    private final Writer writer;
    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        gson = gsonBuilder.create();
    }

    public Serializer(final Writer writer) {
        this.writer = writer;
    }

    public void serialize(final UUID id, final Throwable throwable) {
        final CrashReport transformed = transform(id, throwable);
        gson.toJson(transformed, writer);
    }

    private static CrashReport transform(final UUID id, final Throwable throwable) {
        final ArrayList<Throwable> causeChain = collectCauses(throwable);
        final StackTraceSegment[] segments = new StackTraceSegment[causeChain.size()];
        for (int i = 0; i < causeChain.size(); ++i) {
            segments[i] = transform(causeChain.get(i));
        }

        return new CrashReport(id, new StackTrace(segments));
    }

    private static ArrayList<Throwable> collectCauses(final Throwable throwable) {
        final ArrayList<Throwable> restOfCauseChain = throwable.getCause() == null ?
                new ArrayList<Throwable>() : collectCauses(throwable.getCause());
        restOfCauseChain.add(0, throwable);
        return restOfCauseChain;
    }

    private static StackTraceSegment transform(final Throwable throwable) {
        final StackTraceElement[] frames = throwable.getStackTrace();
        final String[] stringFrames = new String[frames.length];
        for (int i = 0; i < frames.length; ++i) {
            stringFrames[i] = frames[i].toString();
        }
        return new StackTraceSegment(
                throwable.getClass().getName(),
                throwable.getMessage(),
                stringFrames
        );
    }
}

class CrashReport {
    public final UUID id;
    public final StackTrace trace;

    public CrashReport(UUID id, StackTrace trace) {
        this.id = id;
        this.trace = trace;
    }
}

class StackTrace {
    public final StackTraceSegment[] segments;

    public StackTrace(StackTraceSegment[] segments) {
        this.segments = segments;
    }
}

class StackTraceSegment {
    public final String exceptionType;
    public final String message;
    public final String[] frames;

    public StackTraceSegment(String exceptionType, String message, String[] frames) {
        this.exceptionType = exceptionType;
        this.message = message;
        this.frames = frames;
    }
}
