package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * License: MIT License
 */

@SuppressWarnings("WeakerAccess")
public class Pair<L, R> {
    private L left;
    private R right;

    public Pair(L leftSide, R rightSide){
        this.left = leftSide;
        this.right = rightSide;
    }

    public L left() { return this.left; }
    public R right() { return this.right; }
}
