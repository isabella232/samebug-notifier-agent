package com.samebug.notifier.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
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
            final NotifierThread notifierThread = new NotifierThread();
            final AgentBuilder builder = createBuilder();
            return builder.installOn(instrumentation);
        } catch (Exception e) {
            System.err.println("Failed to install Samebug notifier agent!");
            e.printStackTrace(System.err);
            return null;
        }
    }

    // Extracts the dispatcher jar to a tmp file, so it can be added to the class path.
    // NOTE it is not enough to use the URL of the resource, because in production it is enclosed in the jar of the agent.
    static JarFile getDispatcherJar() throws IOException {
        final InputStream dispatcherJarContent = NotifierAgent.class.getResourceAsStream("/notifier-agent-dispatcher.jar");
        final File tmpDispatcherJar = File.createTempFile("samebug_notifier_agent_", ".jar");
        tmpDispatcherJar.deleteOnExit();
        final OutputStream out = new FileOutputStream(tmpDispatcherJar);
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = dispatcherJarContent.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        dispatcherJarContent.close();
        out.close();

        return new JarFile(tmpDispatcherJar);
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
        if (arguments != null && arguments.startsWith("crashMonitorUrl=")) {
            return arguments.substring("crashMonitorUrl=".length());
        } else {
            System.err.println("Failed to install Samebug notifier agent!");
            System.err.println("Cannot parse its arguments: " + arguments);
            return null;
        }
    }
}
