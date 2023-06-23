package com.fx.gen.pojo;

/**
 *
 * @author pscha
 */
public class Combo {

    private int idx;
    private int size;

    public Combo(int idx, int size) {
        this.idx = idx;
        this.size = size;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return idx + " - " + size;
    }

}
