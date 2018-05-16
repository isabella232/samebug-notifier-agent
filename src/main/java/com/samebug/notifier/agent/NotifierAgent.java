package com.samebug.notifier.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.jar.JarFile;

public class NotifierAgent {
    public static void premain(final String arguments, final Instrumentation instrumentation) {
        transform(arguments, instrumentation);
    }

    public static void agentmain(final String arguments, final Instrumentation instrumentation) {
        transform(arguments, instrumentation);
    }


    static ClassFileTransformer transform(final String arguments, final Instrumentation instrumentation) {
        final String crashMonitorUrl = parseConfig(arguments);
        // TODO find a better way of passing config
        if (crashMonitorUrl != null) {
            System.setProperty("com.samebug.notifier.agent.crashMonitorUrl", crashMonitorUrl);
        }

        try {
            final JarFile dispatcher = getDispatcherJar();
            instrumentation.appendToBootstrapClassLoaderSearch(dispatcher);
            final AgentBuilder builder = createBuilder();
            return builder.installOn(instrumentation);
        } catch (Exception e) {
            System.err.println("Failed to install Samebug notifier agent!");
            e.printStackTrace(System.err);
            return null;
        }
    }

    static JarFile getDispatcherJar() throws IOException {
        final URL dispatcherUrl = NotifierAgent.class.getResource("/notifier-agent-dispatcher.jar");
        final JarFile dispatcherJar = new JarFile(dispatcherUrl.getFile());
        return dispatcherJar;
    }

    static AgentBuilder.Identified.Extendable createBuilder() {
        return new AgentBuilder.Default()
                .disableClassFormatChanges()
                .ignore(ElementMatchers.none())
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .type(ElementMatchers.is(Throwable.class))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice()
                                .include(ThrowableAdvice.class.getClassLoader())
                                .advice(ElementMatchers.<MethodDescription>named("printStackTrace")
                                                .and(ElementMatchers.<MethodDescription>isPrivate()),
                                        ThrowableAdvice.class.getName())
                );

    }

    static String parseConfig(final String arguments) {
        if (arguments.startsWith("crashMonitorUrl=")) {
            return arguments.substring("crashMonitorUrl=".length());
        } else {
            System.err.println("Failed to install Samebug notifier agent!");
            System.err.println("Cannot parse its arguments: " + arguments);
            return null;
        }
    }
}
