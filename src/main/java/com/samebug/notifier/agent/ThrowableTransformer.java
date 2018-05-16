package com.samebug.notifier.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;

class ThrowableTransformer {
    public AgentBuilder.Identified.Extendable createBuilder(final Instrumentation instrumentiation) throws IOException, URISyntaxException {
        final URL dispatcherJarUrl = getClass().getResource("/notifier-agent-dispatcher-0.1.0-SNAPSHOT.jar");
        final JarFile jar = new JarFile(new File(dispatcherJarUrl.toURI()));
        instrumentiation.appendToBootstrapClassLoaderSearch(jar);
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

}
