package com.samebug.notifier.client;

import org.junit.Test;

import java.io.StringWriter;
import java.util.UUID;

public class SerializerTest {
    @Test
    public void testWithExceptionMessage() throws Exception {
        final StringWriter buffer = new StringWriter();
        Serializer serializer = new Serializer(buffer);
        serializer.serialize(
                UUID.fromString("39905681-b3bb-4b7e-8727-bf1119b8d9fc"),
                new Exception("test message")
        );

        // TODO diff content and 001.json
        System.out.println(buffer);
    }
}
