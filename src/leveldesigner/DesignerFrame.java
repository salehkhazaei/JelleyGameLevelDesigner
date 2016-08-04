/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leveldesigner;

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
import javafx.util.Pair;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Saleh
 */
public class DesignerFrame extends JFrame {

    public static BufferedImage resize(BufferedImage buf, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, buf.getType());
        Graphics2D g = img.createGraphics();
        g.drawImage(buf, 0, 0, w, h, null);
        return img;
    }

    HashMap<String, BufferedImage> objmap = new HashMap<>();
    BufferedImage background;

    {
        try {
            background = ImageIO.read(new File("background.png"));
            objmap.put("brick", resize(ImageIO.read(new File("brick.png")), 150, 80));
            objmap.put("chocolate", resize(ImageIO.read(new File("chocolate.png")), 50, 50));
            objmap.put("food", resize(ImageIO.read(new File("food.png")), 150, 100));
            objmap.put("jelley", resize(ImageIO.read(new File("jelley.png")), 100, 80));
        } catch (IOException ex) {
            Logger.getLogger(DesignerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    int W = 525;
    int H = 700;
    int mouse_X, mouse_Y;

    ArrayList<Pair<BufferedImage, Point>> objects = new ArrayList<>();
    int selected = -1;
    Iterator it = objmap.keySet().iterator();
    BufferedImage selected_obj = objmap.get(it.next());

    public DesignerFrame() {
        this.setSize(700, 700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        this.setVisible(true);
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (selected == -1) {
                        if (selected_obj != null) {
                            objects.add(new Pair<>(selected_obj, new Point(e.getX(), e.getY())));
                        }
                    } else if (selected < objects.size()) {
                        objects.get(selected).getValue().setLocation(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
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
                System.out.println(e.getKeyCode());
                if (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') {
                    if (e.getKeyCode() - '0' < objects.size()) {
                        selected = e.getKeyCode() - '0';
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_MINUS) {
                    selected = -1;
                }
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    synchronized (objects) {
                        objects.remove(selected);
                        selected = -1;
                    }
                }
                if (e.getKeyCode() == 'A') {
                    if (!it.hasNext()) {
                        it = objmap.keySet().iterator();
                    }
                    selected_obj = objmap.get(it.next());
                }
                if (e.getKeyCode() == 'S') {
                    String no = JOptionPane.showInputDialog("Please enter level number:");
                    try {
                        FileOutputStream fos = new FileOutputStream(new File("level-" + no + ".json"));
                        fos.write(("[").getBytes());
                        for (int i = 0; i < objects.size(); i++) {
                            Pair<BufferedImage, Point> obj = objects.get(i);
                            String name = null;
                            for (String n : objmap.keySet()) {
                                if (objmap.get(n) == obj.getKey()) {
                                    name = n;
                                }
                            }
                            if (name == null) {
                                System.out.println("FUCK");
                            }
                            fos.write(("{\"type\":\"" + name + "\",\"posx\":" + obj.getValue().getX() + ",\"posy\":" + obj.getValue().getY() + "}" + (i != objects.size() - 1 ? "," : "")).getBytes());
                        }
                        fos.write(("]").getBytes());
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(DesignerFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(DesignerFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (e.getKeyCode() == 'Q') {
                    System.exit(0);
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
                Pair<BufferedImage, Point> obj = objects.get(i);
                g.drawImage(obj.getKey(), (int) obj.getValue().getX(), (int) obj.getValue().getY(), null);
                if (i == selected) {
                    g.setColor(Color.black);
                    g.drawRect((int) obj.getValue().getX(), (int) obj.getValue().getY(), obj.getKey().getWidth(), obj.getKey().getHeight());
                }
                g.setColor(Color.red);
                g.fillOval((int) obj.getValue().getX() - 10, (int) obj.getValue().getY() - 10, 20, 20);
                g.setColor(Color.white);
                g.drawString("" + i, (int) obj.getValue().getX() - 3, (int) obj.getValue().getY() + 5);
            }
            g.setColor(Color.white);
            g.drawString("Mouse_X:" + mouse_X, 560, 20);
            g.drawString("Mouse_Y:" + mouse_Y, 560, 50);
            g.drawString("Selected Object:" + selected, 560, 80);
            g.drawString("Current Object:", 560, 110);
            g.drawImage(selected_obj, 612 - selected_obj.getWidth() / 2, 130, null);

            g.drawString("A: next object"               , 540, 300);
            g.drawString("S: save level"                , 540, 320);
            g.drawString("Q: quit"                      , 540, 340);
            g.drawString("-: de-select"                 , 540, 360);
            g.drawString("0-9: select object"           , 540, 380);
            g.drawString("Del: remote object"           , 540, 400);
            g.drawString("Click: if a object is selected", 540, 420);
            g.drawString("it will move the object"      , 540, 440);
            g.drawString("else it will create new one"  , 540, 460);
        }
        gg.drawImage(buf, 0, 0, null);
    }
}
