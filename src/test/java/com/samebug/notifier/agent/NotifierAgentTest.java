package com.samebug.notifier.agent;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class NotifierAgentTest {
    private ClassLoader classLoader;

    @Before
    public void setUp() throws Exception {
        classLoader = ClassLoader.getSystemClassLoader();
    }

    @Test
    public void testAgent() throws Exception {
        final Instrumentation instrumentation = ByteBuddyAgent.install();
        instrumentation.appendToBootstrapClassLoaderSearch(NotifierAgent.getDispatcherJar());
        final AgentBuilder.Identified.Extendable builder = NotifierAgent.createBuilder();
        final ClassFileTransformer classFileTransformer = builder.installOnByteBuddyAgent();
        try {
            Class<?> type = classLoader.loadClass(Throwable.class.getName());
            try {
                "".charAt(33);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Assert.assertEquals(ExceptionQueue.getInstance().size(), 1);
        } finally {
            ByteBuddyAgent.getInstrumentation().removeTransformer(classFileTransformer);
        }
    }
}
