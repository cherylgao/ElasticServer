package com.cheryl.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Latch {
    private CountDownLatch cdl = new CountDownLatch(1);

    public void done() {
        cdl.countDown();
    }

    public boolean await() {
        return await(30, TimeUnit.SECONDS);
    }

    public boolean await(long timeout, TimeUnit unit) {
        try {
            return cdl.await(timeout, unit);
        }
        catch (InterruptedException e) {
        }
        return false;
    }
}
