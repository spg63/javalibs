package javalibs;
/**
 * Copyright (javalibs.c) 2019 Sean Grimes. All rights reserved.
 * @author Sean Grimes, spg63@drexel.edu
 * @since 1/31/19
 * License: MIT License
 */

import com.google.common.collect.EvictingQueue;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static volatile MTOut _instance;
    private TSL log_ = TSL.get();
    private MTOut(){}

    private AtomicBoolean writelnAtomic = new AtomicBoolean();
    private AtomicBoolean writeAtomic = new AtomicBoolean();
    private AtomicBoolean infoAtomic = new AtomicBoolean();
    private AtomicBoolean warnAtomic = new AtomicBoolean();
    private AtomicBoolean errAtomic = new AtomicBoolean();
    private EvictingQueue<String> buffer = EvictingQueue.create(30);

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

    // Stupid: If msg == previous, return
    public void writeln(String msg){
        synchronized (MTOut.class){
            if(inBuffer(msg)) return;
            Out.get().writeln(msg);
        }
/*
        synchronized (MTOut.class){
            if(!this.writelnAtomic.getAndSet(true)){
                Out.get().writeln(msg);
            }
            else
                return;
        }
*/
    }

    public void write(String msg){
        synchronized (MTOut.class){
            if(inBuffer(msg)) return;
            Out.get().write(msg);
        }
    }

    public void info(String msg){
        synchronized (MTOut.class) {
            if(inBuffer(msg)) return;
            log_.info(msg);
        }
    }

    public void debug(String msg){
        synchronized (MTOut.class){
            if(inBuffer(msg)) return;
            log_.debug(msg);
        }
    }

    public void trace(String msg){
        synchronized (MTOut.class){
            if(inBuffer(msg)) return;
            log_.trace(msg);
        }
    }

    public void warn(String msg){
        synchronized (MTOut.class) {
            if(inBuffer(msg)) return;
            log_.warn(msg);
        }
    }

    public void err(String msg){
        synchronized (MTOut.class) {
            if(inBuffer(msg)) return;
            log_.err(msg);
        }
    }

    private boolean inBuffer(String msg) {
        if(buffer.contains(msg)){
            buffer.add(msg);
            return true;
        }
        buffer.add(msg);
        return false;
    }

}
