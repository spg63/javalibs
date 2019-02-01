package javalibs;
/**
 * Copyright (javalibs.c) 2019 Sean Grimes. All rights reserved.
 * @author Sean Grimes, spg63@drexel.edu
 * @since 1/31/19
 * License: MIT License
 */

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple class that allows multiple threads to call the same print / log functions,
 * however the print or log functions will only be called a single time, cleaning up
 * the output and allowing everything to be more readable
 *
 * NOTE: This class will use TSL for logging calls
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
    private AtomicBoolean autoAtomic = new AtomicBoolean();

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

    public void writeln(Object msg){
        synchronized (MTOut.class){
            if(!this.writelnAtomic.getAndSet(true)){
                Out.get().writeln(msg);
            }
            else
                return;
        }

    }

    public void write(Object msg){

    }

    public void info(Object msg){

    }

    public void warn(Object msg){

    }

    public void err(Object msg){

    }

    public void autoLog(Object msg){

    }

}
