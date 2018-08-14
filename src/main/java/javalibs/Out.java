package javalibs;/*
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/1/15
 */
@SuppressWarnings({"unused", "SpellCheckingInspection", "WeakerAccess"})
public class Out implements java.io.Serializable{
    private static final long serialVersionUID = 80085L;
    private static volatile Out _instance;

    private Out(){ }

    public static Out get(){
        if(_instance == null){
            synchronized(Out.class){
                if(_instance == null){
                    _instance = new Out();
                }
            }
        }
        return _instance;
    }

    public void writeln(Object msg){
        System.out.println(msg);
    }
    public void write(Object msg){
        System.out.print(msg);
    }
    public void writef(String format, Object... args){
        System.out.print(String.format(format, args));
    }
    public void writeln_err(Object msg){
        System.err.println(msg);
    }
    public void write_err(Object msg){
        System.err.print(msg);
    }
    public String timer_millis(Stopwatch sw){
        return sw.elapsed(TimeUnit.MILLISECONDS) + " milliseconds";
    }
    public String timer_secs(Stopwatch sw){
        return sw.elapsed(TimeUnit.SECONDS) + " seconds";
    }
    public String timer_mins(Stopwatch sw){
        return sw.elapsed(TimeUnit.MINUTES) + " minutes";
    }
}
