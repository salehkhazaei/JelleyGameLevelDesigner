/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leveldesigner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Saleh
 */
public class DesignerFrame extends JFrame {

    public enum STATE {
        MOVE_OBJECT,
        ROTATE_OBJECT,
        SET_VELOCITY,
        SET_DISTANCE,
        CREATE_OBJECT,
        NONE
    }

    public static BufferedImage resize(BufferedImage buf, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, buf.getType());
        Graphics2D g = img.createGraphics();
        g.drawImage(buf, 0, 0, w, h, null);
        return img;
    }

    HashMap<String, BufferedImage> objmap = new HashMap<>();

    {
        try {
            background = ImageIO.read(new File("background.png"));
            objmap.put("brick", resize(ImageIO.read(new File("brick.png")), 150, 80));
            objmap.put("chocolate", resize(ImageIO.read(new File("chocolate.png")), 50, 50));
            objmap.put("food", resize(ImageIO.read(new File("food.png")), 150, 100));
            objmap.put("star", resize(ImageIO.read(new File("star.png")), 50, 50));
        } catch (IOException ex) {
            Logger.getLogger(DesignerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    BufferedImage background;
    int W = 525;
    int H = 700;
    int mouse_X, mouse_Y;

    ArrayList<GameObject> objects = new ArrayList<>();
    int selected = -1;
    Iterator it = objmap.keySet().iterator();
    BufferedImage selected_obj = objmap.get(it.next());
    STATE state = STATE.NONE;
    int startX, startY;

    public DesignerFrame() {
        this.setSize(700, 700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        this.setVisible(true);
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    System.out.println("Click" + System.currentTimeMillis());
                    GameObject obj = (selected >= 0 ? objects.get(selected) : null);
                    switch (state) {
                        case CREATE_OBJECT:
                            objects.add(new GameObject(selected_obj, new Point(e.getX()- selected_obj.getWidth() / 2, e.getY() - selected_obj.getHeight()/ 2)));
                            selected = objects.size() - 1;
                            state = STATE.NONE;
                            break;
                        case MOVE_OBJECT:
                            if (selected < 0) {
                                break;
                            }
                            obj.pos.setLocation(obj.pos.x + e.getX() - startX, obj.pos.y + e.getY() - startY);
                            state = STATE.NONE;
                            break;
                        case ROTATE_OBJECT:
                            if (selected < 0) {
                                break;
                            }
                            Point p = objects.get(selected).pos;
                            Point v1 = new Point(startX - p.x, startY - p.y);
                            Point v2 = new Point(e.getX() - p.x, e.getY() - p.y);
                            double dot = v1.x * -v2.y + v1.y * v2.x;
                            objects.get(selected).rotation = objects.get(selected).rotation +  Math.acos((v1.x * v2.x + v1.y * v2.y)
                                    / (Math.sqrt(v1.x * v1.x + v1.y * v1.y)
                                    * Math.sqrt(v2.x * v2.x + v2.y * v2.y))) * (dot < 0 ? -1 : 1);
                            state = STATE.NONE;
                            break;
                        case SET_DISTANCE:
                            if (selected < 0) {
                                break;
                            }
                            objects.get(selected).d_x = obj.pos.x + e.getX() - startX;
                            objects.get(selected).d_y = obj.pos.y + e.getY() - startY;
                            state = STATE.NONE;
                            break;
                        case SET_VELOCITY:
                            if (selected < 0) {
                                break;
                            }
                            objects.get(selected).v_x = obj.pos.x + (e.getX() - startX) / 3;
                            objects.get(selected).v_y = obj.pos.y + (e.getY() - startY) / 3;
                            state = STATE.NONE;
                            break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouse_X = e.getX();
                mouse_Y = e.getY();
            }
        });
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_MINUS:
                    case KeyEvent.VK_ESCAPE:
                        selected = -1;
                        state = STATE.NONE;
                        break;
                    case KeyEvent.VK_DELETE:
                        synchronized (objects) {
                            if (selected >= 0) {
                                objects.remove(selected);
                                selected = -1;
                                state = STATE.NONE;
                            }
                        }
                        break;
                    case 'A':
                        if (!it.hasNext()) {
                            it = objmap.keySet().iterator();
                        }
                        selected_obj = objmap.get(it.next());
                        break;
                    case 'S':
                        String no = JOptionPane.showInputDialog("Please enter level number:");
                        try {
                            FileOutputStream fos = new FileOutputStream(new File("level-" + no + ".json"));
                            fos.write(("[").getBytes());
                            for (int i = 0; i < objects.size(); i++) {
                                GameObject obj = objects.get(i);
                                String name = null;
                                for (String n : objmap.keySet()) {
                                    if (objmap.get(n) == obj.buf) {
                                        name = n;
                                    }
                                }
                                if (name == null) {
                                    System.out.println("FUCK");
                                }
                                fos.write(("{\"type\":\"" + name
                                        + "\",\"posx\":" + obj.pos.getX()
                                        + ",\"posy\":" + obj.pos.getY()
                                        + ",\"rotation\":" + obj.rotation
                                        + ",\"v_x\":" + obj.v_x
                                        + ",\"v_y\":" + obj.v_y
                                        + ",\"d_x\":" + obj.d_x
                                        + ",\"d_y\":" + obj.d_y
                                        + "}" + (i != objects.size() - 1 ? "," : "")).getBytes());
                            }
                            fos.write(("]").getBytes());
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(DesignerFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(DesignerFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case 'C':
                        state = STATE.CREATE_OBJECT;
                        break;
                    case 'M':
                        if (selected < 0) {
                            break;
                        }
                        startX = mouse_X;
                        startY = mouse_Y;
                        state = STATE.MOVE_OBJECT;
                        break;
                    case 'R':
                        if (selected < 0) {
                            break;
                        }
                        startX = mouse_X;
                        startY = mouse_Y;
                        state = STATE.ROTATE_OBJECT;
                        break;
                    case 'V':
                        if (selected < 0) {
                            break;
                        }
                        startX = mouse_X;
                        startY = mouse_Y;
                        state = STATE.SET_VELOCITY;
                        break;
                    case 'D':
                        if (selected < 0) {
                            break;
                        }
                        startX = mouse_X;
                        startY = mouse_Y;
                        state = STATE.SET_DISTANCE;
                        break;
                    case 'Q':
                        System.exit(0);
                        break;
                }

                if (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') {
                    if (e.getKeyCode() - '0' < objects.size()) {
                        selected = e.getKeyCode() - '0';
                    }
                }
            }
        });
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    repaint();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DesignerFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }.start();
    }

    @Override
    public void paint(Graphics gg) {
        BufferedImage buf = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buf.createGraphics();

        g.drawImage(background, 0, 0, W, H, null);
        synchronized (objects) {
            for (int i = 0; i < objects.size(); i++) {
                GameObject obj = objects.get(i);
                if (i == selected) {
                    int temp_X = obj.pos.x, temp_Y = obj.pos.y;
                    double temp_rotation = obj.rotation;
                    boolean show_movementline = false;
                    switch (state) {
                        case MOVE_OBJECT:
                            temp_X = obj.pos.x + mouse_X - startX;
                            temp_Y = obj.pos.y + mouse_Y - startY;
                            break;
                        case ROTATE_OBJECT:
                            Point p = objects.get(selected).pos;
                            Point v1 = new Point(startX - p.x, startY - p.y);
                            Point v2 = new Point(mouse_X - p.x, mouse_Y - p.y);
                            double dot = v1.x * -v2.y + v1.y * v2.x;
                            temp_rotation = temp_rotation + Math.acos((v1.x * v2.x + v1.y * v2.y)
                                    / (Math.sqrt(v1.x * v1.x + v1.y * v1.y)
                                    * Math.sqrt(v2.x * v2.x + v2.y * v2.y))) * (dot < 0 ? -1 : 1);
                            break;
                        case SET_DISTANCE:
                            temp_X = obj.pos.x + mouse_X - startX;
                            temp_Y = obj.pos.y + mouse_Y - startY;
                            show_movementline = true;
                            break;
                        case SET_VELOCITY:
                            temp_X = obj.pos.x + (mouse_X - startX) / 3;
                            temp_Y = obj.pos.y + (mouse_Y - startY) / 3;
                            show_movementline = true;
                            break;
                    }

                    g.translate(temp_X + obj.buf.getWidth() / 2, temp_Y + obj.buf.getHeight() / 2);
                    g.rotate(temp_rotation);

                    g.drawImage(obj.buf, -obj.buf.getWidth() / 2, -obj.buf.getHeight() / 2, null);
                    g.setColor(Color.black);
                    g.drawRect((int) -obj.buf.getWidth() / 2, (int) -obj.buf.getHeight() / 2, obj.buf.getWidth(), obj.buf.getHeight());
                    g.setColor(Color.red);

                    g.rotate(-temp_rotation);
                    g.translate(-(temp_X + obj.buf.getWidth() / 2), -(temp_Y + obj.buf.getHeight() / 2));

                    g.fillOval((int) temp_X - 10, (int) temp_Y - 10, 20, 20);
                    g.setColor(Color.white);
                    g.drawString("" + i, (int) temp_X - 3, (int) temp_Y + 5);

                    if (show_movementline) {
                        g.setStroke(new BasicStroke(4));
                        g.setColor(Color.BLUE);
                        g.drawLine(obj.pos.x + obj.buf.getWidth() / 2, obj.pos.y + obj.buf.getHeight() / 2, temp_X + obj.buf.getWidth() / 2, temp_Y + obj.buf.getHeight() / 2);
                        g.setStroke(new BasicStroke(1));
                    }
                } else {
                    int temp_X = obj.pos.x, temp_Y = obj.pos.y;
                    double temp_rotation = obj.rotation;
                    g.translate(temp_X + obj.buf.getWidth() / 2, temp_Y + obj.buf.getHeight() / 2);
                    g.rotate(temp_rotation);

                    g.drawImage(obj.buf, -obj.buf.getWidth() / 2, -obj.buf.getHeight() / 2, null);
                    g.setColor(Color.red);

                    g.rotate(-temp_rotation);
                    g.translate(-(temp_X + obj.buf.getWidth() / 2), -(temp_Y + obj.buf.getHeight() / 2));

                    g.fillOval((int) obj.pos.getX() - 10, (int) obj.pos.getY() - 10, 20, 20);
                    g.setColor(Color.white);
                    g.drawString("" + i, (int) obj.pos.getX() - 3, (int) obj.pos.getY() + 5);
                }
            }
            if (state == STATE.CREATE_OBJECT) {
                g.drawImage(selected_obj, mouse_X - selected_obj.getWidth() / 2, mouse_Y - selected_obj.getHeight()/ 2, null);
            }

            g.setColor(Color.white);
            g.drawString("Mouse_X:" + mouse_X, 560, 20);
            g.drawString("Mouse_Y:" + mouse_Y, 560, 50);
            g.drawString("Selected Object:" + selected, 560, 80);
            g.drawString("Current Object:", 560, 110);
            g.drawImage(selected_obj, 612 - selected_obj.getWidth() / 2, 130, null);

            g.drawString("A: next object", 540, 300);
            g.drawString("S: save level", 540, 320);
            g.drawString("Q: quit", 540, 340);
            g.drawString("C: create new object", 540, 360);
            g.drawString("M: move object", 540, 380);
            g.drawString("R: rotate object", 540, 400);
            g.drawString("V: set velocity", 540, 420);
            g.drawString("D: set distance", 540, 440);
            g.drawString("-/ESC: de-select", 540, 460);
            g.drawString("0-9: select object", 540, 480);
            g.drawString("Del: remote object", 540, 500);
            g.drawString("Click: set", 540, 520);
        }
        gg.drawImage(buf, 0, 0, null);
    }
}
