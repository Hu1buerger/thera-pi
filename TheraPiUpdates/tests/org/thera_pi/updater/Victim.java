package org.thera_pi.updater;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

final class Victim implements Stoppable, Runnable {
    private final AtomicBoolean goOn = new AtomicBoolean(true);
    private CountDownLatch stopper;

    @Override
    public void stop(CountDownLatch latch) {
        this.stopper = latch;
        goOn.set(false);

    }

    @Override
    public void run() {

        while (goOn.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //just testing
            }
        }
        stopper.countDown();

    }
}
