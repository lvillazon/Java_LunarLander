import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;

public class Moon extends JPanel implements Runnable{

    private final int SCREEN_HEIGHT = 480;
    private final int SCREEN_WIDTH = 640;
    private final int BOTTOM_MARGIN = -50;
    public final int TARGET_DRAW_TIME = 17; // notional time in ms for each frame update

    private JFrame frame;
    private ArrayList<Coordinate> points;
    private Thread gameThread;
    private volatile boolean running;   // volatile to preserve state across threads
    private long updateDurationMillis;

    private Lander lander;

    public Moon() {
        frame = new JFrame("Lunar Lander");
        frame.setSize(640, 480);
        setBounds(0,0, 640, 480);
        setBackground(Color.black);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        points = generateLandscape();
        lander = null;
        frame.add(this);
        frame.setVisible(true);
    }

    public Coordinate startPosition() {
        return(new Coordinate(SCREEN_WIDTH/2, 20));
    }

    public void addLander(Lander l) {
        lander = l;
        lander.moon = this;
    }

    public int altitude(Coordinate p) {
        // returns the height of position p above the terrain
        // find the closest terrain point to the right of p
        int i=0;
        while (i<points.size() && points.get(i).x < p.x) {
            i++;
        }
        // TODO: interpolate beteeen this point and the previous to find the exact height
        // for now just return the height of this point - y coord of lander
        return points.get(i).y - p.y;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(128,128,128));
        // draws lines between the points in the array
        Graphics2D g2 = (Graphics2D) g;
        for (int i=0; i<points.size()-1; i++) {
            g2.draw(new Line2D.Double(points.get(i).x, points.get(i).y,
                                      points.get(i+1).x, points.get(i+1).y));
        }
        // draw lander, if any
        if (lander != null) {
            lander.update();
            lander.draw(g2);
        }
    }

    private ArrayList<Coordinate> generateLandscape() {
        final int MAX_HEIGHT = (int)(SCREEN_HEIGHT *0.2);  // max height of the tallest mountain
        final int MIN_HEIGHT = 50;  // lowest valley
        ArrayList<Coordinate> pointList;

        // start and end points of the landscape are the same, to allow wrap around scrolling
        int y = (int)(SCREEN_HEIGHT - Math.random() * (MAX_HEIGHT - MIN_HEIGHT) - MIN_HEIGHT);
        System.out.println("generating surface...");
        pointList = landscapePoints(new ArrayList<Coordinate>(),
                                    new Coordinate(0, y),
                                    new Coordinate(SCREEN_WIDTH, y),
                                    (int)(SCREEN_HEIGHT*.6));
        // sort the points according to x coordinate
        pointList = sortByX(pointList);
        // move the landscape down, so the lowest point is at the bootom of the screen
        pointList = normaliseLandscape(pointList);
        // add a landing site
        //pointList = addLandingSite(pointList, 50);
        //pointList = addLandingSite(pointList, 20);
        return pointList;
    }

    private ArrayList<Coordinate> landscapePoints(ArrayList<Coordinate> pointList,
                                                  Coordinate startPoint,
                                                  Coordinate endPoint,
                                                  int deviationLimit) {
        // returns an array of points representing lunar surface
        // uses the midpoint displacement algorithm
        // described at https://bitesofcode.wordpress.com/2016/12/23/landscape-generation-using-midpoint-displacement/

        final double DECAY = 1.2;  // rate at which the midpoint deviation amount is reduced
        if (endPoint.x - startPoint.x < 20) { // stop recursing when x coords are close together
            pointList.add(startPoint);
            pointList.add(endPoint);
        } else {
            // calculate a new midpoint
            double position = Math.random()*0.25 + 0.25;  // half way along the line, horizontally
            int x = (int)((endPoint.x - startPoint.x) * position) + startPoint.x;
            int y = (int)((endPoint.y - startPoint.y) * position) + startPoint.y;
            // deviate the midpoint up or down
            y = (int)(y + ((Math.random() * 2) -1) * deviationLimit);
            if (y > SCREEN_HEIGHT) {
                y = SCREEN_HEIGHT;
            }
            // add a new point at this deviated mid point
            pointList.add(new Coordinate(x,y));
            deviationLimit = (int)(deviationLimit * 1/Math.pow(2, DECAY));
            landscapePoints(pointList, startPoint, new Coordinate(x,y), deviationLimit);
            landscapePoints(pointList, new Coordinate(x,y), endPoint, deviationLimit);
        }
        return pointList;
    }

    private ArrayList<Coordinate> normaliseLandscape(ArrayList<Coordinate> pointList) {
        // moves all points down, so the lowest point is at the bottom of the screen
        // (leaving some space for the score multiplier label)
        // this is complicated slightly by the fact that y coords INCREASE as you go down the screen
        // so SCREEN_HEIGHT is the lowest point
        int lowest = 0;
        for (int i = 0; i<pointList.size(); i++) {
            if (pointList.get(i).y > lowest) {
                lowest = pointList.get(i).y;
            }
        }

        for (int i = 0; i<pointList.size(); i++) {
            Coordinate coord = pointList.get(i);
            coord.y += (SCREEN_HEIGHT - (lowest - BOTTOM_MARGIN));
            pointList.set(i, coord);
        }
        return pointList;
    }

    ArrayList<Coordinate> sortByX(ArrayList<Coordinate> pointList) {
        // return list sorted in ascending order of x coord
        Collections.sort(pointList);
//        pointList.sort((p1, p2) -> p1[0]-p2[0]);
        return pointList;
    }

    @Override
    public void addNotify() {
        System.out.println("SEQUENCE: Game addNotify");
        // called when the Game object is added to the JFrame
        // good place to put initialisation code for the game
        super.addNotify();
        initInput();
        initGame();
    }

    private void initInput() {
        // assigns a dispatcher for mouse and keyboard events
        //inputHandler = new InputHandler();
        //addKeyListener(inputHandler);
        //addMouseListener(inputHandler);
    }

    private void initGame() {
        // give the game its own thread
        System.out.println("SEQUENCE: Game thread started");
        running = true;
        gameThread = new Thread(this, "Game Thread");
        gameThread.start();
    }

    @Override
    public void run() {
        // Game loop
        long sleepDurationMillis = 0;

        while (running) {
            long beforeFrameDraw = System.nanoTime();
            repaint();

            updateDurationMillis = (System.nanoTime() - beforeFrameDraw) / 1000000L;
            sleepDurationMillis = Math.max(2, TARGET_DRAW_TIME - updateDurationMillis);
            //System.out.println(getFPS());
            try {
                Thread.sleep(sleepDurationMillis);
            } catch (InterruptedException e) {
                System.out.println("Failed to sleep game loop!");
                e.printStackTrace();
            }
        }
        // quit when not running
        System.exit(0);
    }
}
