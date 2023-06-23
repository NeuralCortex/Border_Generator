/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fx.gen.cell;

import com.fx.gen.pojo.BorderPOJO;
import java.awt.Color;
import javafx.scene.control.TableCell;

/**
 *
 * @author pscha
 */
public class ColorCellBorder extends TableCell<BorderPOJO, Color> {

    @Override
    protected void updateItem(Color item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
            setStyle("");
        } else if (item != null && !empty) {
            int r = item.getRed();
            int g = item.getGreen();
            int b = item.getBlue();
            String hex = String.format("#%02x%02x%02x", r, g, b);
            setStyle("-fx-background-color:" + hex + ";");
            setGraphic(null);
            setText(null);
        }
    }
}
