package com.fx.gen.pojo;

import com.fx.gen.painter.BorderPainter;
import java.awt.Color;

/**
 *
 * @author pscha
 */
public class BorderData {

    private boolean use;
    private int dist;
    private Color color;
    private BorderPainter borderPainter;

    public BorderData(boolean use, int dist, Color color, BorderPainter borderPainter) {
        this.use = use;
        this.dist = dist;
        this.color = color;
        this.borderPainter = borderPainter;
    }

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    public int getDist() {
        return dist;
    }

    public void setDist(int dist) {
        this.dist = dist;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BorderPainter getBorderPainter() {
        return borderPainter;
    }

    public void setBorderPainter(BorderPainter borderPainter) {
        this.borderPainter = borderPainter;
    }
}
