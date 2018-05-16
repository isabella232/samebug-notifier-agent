package com.samebug.notifier.agent;

import net.bytebuddy.asm.Advice;

import java.util.concurrent.BlockingQueue;

class ThrowableAdvice {

    @Advice.OnMethodExit
    public static void exit(@Advice.This() Throwable me) {
        BlockingQueue<Throwable> queue = ExceptionQueue.getInstance();
        queue.offer(me);
    }
}
