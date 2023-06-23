/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fx.gen.pojo;

import com.fx.gen.painter.BorderRoutePainter;
import java.awt.Color;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author pscha
 */
public class BorderPOJO {

    private final StringProperty fileName = new SimpleStringProperty();
    private final BooleanProperty active = new SimpleBooleanProperty();
    private BorderRoutePainter borderRoutePainter;
    private int countCoord;
    private String length;
    private Color color;

    public BorderPOJO(String fileName, boolean active, BorderRoutePainter borderRoutePainter, int countCoord, Color color, String length) {
        setFileName(fileName);
        setActive(active);
        this.borderRoutePainter = borderRoutePainter;
        this.countCoord = countCoord;
        this.color = color;
        this.length = length;
    }

    public String getFileName() {
        return fileName.get();
    }

    public void setFileName(String value) {
        fileName.set(value);
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean value) {
        active.set(value);
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public BorderRoutePainter getBorderRoutePainter() {
        return borderRoutePainter;
    }

    public void setBorderRoutePainter(BorderRoutePainter borderRoutePainter) {
        this.borderRoutePainter = borderRoutePainter;
    }

    public int getCountCoord() {
        return countCoord;
    }

    public void setCountCoord(int countCoord) {
        this.countCoord = countCoord;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
