package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * License: MIT License
 */

@SuppressWarnings("WeakerAccess")
public class Pair<L, R> {
    private final L left;
    private final R right;

    public Pair(L leftSide, R rightSide){
        this.left = leftSide;
        this.right = rightSide;
    }

    public L left() { return this.left; }
    public R right() { return this.right; }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Pair)) return false;
        Pair<?, ?> other = (Pair<?, ?>) o;
        return java.util.Objects.equals(left, other.left) &&
               java.util.Objects.equals(right, other.right);
    }

    @Override
    public int hashCode(){
        return java.util.Objects.hash(left, right);
    }

    @Override
    public String toString(){
        return "(" + left + ", " + right + ")";
    }
}
