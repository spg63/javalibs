package javalibs;
/**
 * Copyright (javalibs.c) 2019 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 1/31/19
 * License: MIT License
 */

import com.google.common.collect.EvictingQueue;

/**
 * Simple class that allows multiple threads to call the same print / log functions,
 * however the print or log functions will only be called a single time, cleaning up
 * the output and allowing everything to be more readable
 *
 * NOTE: This class will use TSL for logging calls
 * NOTE (MORE IMPORTANT): This is a bullshit way to handle this but I don't have the
 * time to figure out the 'right' way to do this. I'm sure somebody that knows Java
 * better an I do would have a pretty good way to handle this off the bat. As it stands
 * this is literally just synchronizing the methods and checking if the string to be
 * printed / logged is already in the buffer. It if is then I assume I don't want to
 * see it again so quickly so I ignore the msg. Each function call adds the new string,
 * or an empty string (to move the circular buffer forward).
 */
public class MTOut {
    private static final int BUFFER_SIZE = 64;
    private static volatile MTOut _instance;
    private final TSL log = TSL.get();
    private final EvictingQueue<String> buffer = EvictingQueue.create(BUFFER_SIZE);

    private MTOut(){}

    public static MTOut get(){
        if(_instance == null){
            synchronized (MTOut.class){
                if(_instance == null){
                    _instance = new MTOut();
                }
            }
        }
        return _instance;
    }

    public void writeln(String msg){
        synchronized (this){
            if(inBuffer(msg)) return;
            Out.get().writeln(msg);
        }
    }

    public void write(String msg){
        synchronized (this){
            if(inBuffer(msg)) return;
            Out.get().write(msg);
        }
    }

    public void info(String msg){
        synchronized (this) {
            if(inBuffer(msg)) return;
            log.info(msg);
        }
    }

    public void results(String msg){
        synchronized (this) {
            if(inBuffer(msg)) return;
            log.results(msg);
        }
    }

    public void swarm(String msg) {
        synchronized (this) {
            if(inBuffer(msg)) return;
            log.swarm(msg);
        }
    }

    public void debug(String msg){
        synchronized (this){
            if(inBuffer(msg)) return;
            log.debug(msg);
        }
    }

    public void trace(String msg){
        synchronized (this){
            if(inBuffer(msg)) return;
            log.trace(msg);
        }
    }

    public void warn(String msg){
        synchronized (this) {
            if(inBuffer(msg)) return;
            log.warn(msg);
        }
    }

    public void err(String msg){
        synchronized (this) {
            if(inBuffer(msg)) return;
            log.err(msg);
        }
    }

    private boolean inBuffer(String msg) {
        boolean found = buffer.contains(msg);
        buffer.add(msg);
        return found;
    }
}
