package org.thera_pi.updater;

import java.util.concurrent.CountDownLatch;

public interface Stoppable {
    
    /**contract is:
     * 
     * The Stoppable stops executing asap.<p>
     * when done it counts latch down by one.
     * 
     * @param latch
     */
    public abstract void stop(CountDownLatch latch);




}
