package com.samebug.notifier.agent;

import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

public class NotifierAgent {
    public static void premain(final String arguments, final Instrumentation instrumentation) {
        transform(arguments, instrumentation);
    }

    public static void agentmain(final String arguments, final Instrumentation instrumentation) {
        transform(arguments, instrumentation);
    }

    private static void transform(final String arguments, final Instrumentation instrumentation) {
        final String crashMonitorUrl = parseConfig(arguments);
        // TODO find a better way of passing config
        if (crashMonitorUrl != null) {
            System.setProperty("com.samebug.notifier.agent.crashMonitorUrl", crashMonitorUrl);
        }


        final ThrowableTransformer transformer = new ThrowableTransformer();
        try {
            final AgentBuilder builder = transformer.createBuilder(instrumentation);
            builder.installOn(instrumentation);
        } catch (Exception e) {
            System.err.println("Failed to install Samebug notifier agent!");
            e.printStackTrace(System.err);
        }
    }

    private static String parseConfig(final String arguments) {
        if (arguments.startsWith("crashMonitorUrl=")) {
            return arguments.substring("crashMonitorUrl=".length());
        } else {
            System.err.println("Failed to install Samebug notifier agent!");
            System.err.println("Cannot parse its arguments: " + arguments);
            return null;
        }
    }
}
