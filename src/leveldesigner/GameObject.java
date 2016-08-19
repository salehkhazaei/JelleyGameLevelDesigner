/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leveldesigner;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 *
 * @author Saleh
 */
public class GameObject {
    BufferedImage buf;
    Point pos;
    double rotation;
    double v_x,v_y;
    int d_x,d_y;

    public GameObject(BufferedImage buf, Point pos) {
        this.buf = buf;
        this.pos = pos;
        this.rotation = 0.0;
        this.v_x = 0.0;
        this.v_y = 0.0;
        this.d_x = 0;
        this.d_y = 0;
    }
}
