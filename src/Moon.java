import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class Moon extends JPanel {

    private final int SCREEN_HEIGHT = 480;
    private final int SCREEN_WIDTH = 640;
    private final int BOTTOM_MARGIN = -50;

    private JFrame frame;
    private ArrayList<int[]> points;

    public Moon() {
        frame = new JFrame("Lunar Lander");
        frame.setSize(640, 480);
        setBounds(0,0, 640, 480);
        setBackground(Color.black);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        points = generateLandscape();
        frame.add(this);
        frame.setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(128,128,128));
        // draws lines between the points in the array
        Graphics2D g2 = (Graphics2D) g;
        for (int i=0; i<points.size()-1; i++) {
            g2.draw(new Line2D.Double(points.get(i)[0], points.get(i)[1],
                                      points.get(i+1)[0], points.get(i+1)[1]));
        }
    }

    private ArrayList<int[]> generateLandscape() {
        final int MAX_HEIGHT = (int)(SCREEN_HEIGHT *0.2);  // max height of the tallest mountain
        final int MIN_HEIGHT = 50;  // lowest valley
        ArrayList<int[]> pointList;

        // start and end points of the landscape are the same, to allow wrap around scrolling
        int y = (int)(SCREEN_HEIGHT - Math.random() * (MAX_HEIGHT - MIN_HEIGHT) - MIN_HEIGHT);
        System.out.println("generating surface...");
        pointList = landscapePoints(new ArrayList<int[]>(),
                                    new int[]{0, y},
                                    new int[]{SCREEN_WIDTH, y},
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

    private ArrayList<int[]> landscapePoints(ArrayList<int[]> pointList,
                                             int[] startPoint,
                                             int[] endPoint,
                                             int deviationLimit) {
        // returns an array of points representing lunar surface
        // uses the midpoint displacement algorithm
        // described at https://bitesofcode.wordpress.com/2016/12/23/landscape-generation-using-midpoint-displacement/

        final double DECAY = 1.2;  // rate at which the midpoint deviation amount is reduced
        if (endPoint[0] - startPoint[0] < 20) { // stop recursing when x coords are close together
            pointList.add(startPoint);
            pointList.add(endPoint);
        } else {
            // calculate a new midpoint
            double position = Math.random()*0.25 + 0.25;  // half way along the line, horizontally
            int x = (int)((endPoint[0] - startPoint[0])*position) + startPoint[0];
            int y = (int)((endPoint[1] - startPoint[1])*position) + startPoint[1];
            // deviate the midpoint up or down
            y = (int)(y + ((Math.random() * 2) -1) * deviationLimit);
            if (y > SCREEN_HEIGHT) {
                y = SCREEN_HEIGHT;
            }
            // add a new point at this deviated mid point
            pointList.add(new int[] {x,y});
            deviationLimit = (int)(deviationLimit * 1/Math.pow(2, DECAY));
            landscapePoints(pointList, startPoint, new int[]{x,y}, deviationLimit);
            landscapePoints(pointList, new int[]{x,y}, endPoint, deviationLimit);
        }
        return pointList;
    }

    private ArrayList<int[]> normaliseLandscape(ArrayList<int[]> pointList) {
        // moves all points down, so the lowest point is at the bottom of the screen
        // (leaving some space for the score multiplier label)
        // this is complicated slightly by the fact that y coords INCREASE as you go down the screen
        // so SCREEN_HEIGHT is the lowest point
        int lowest = 0;
        for (int i = 0; i<pointList.size(); i++) {
            if (pointList.get(i)[1] > lowest) {
                lowest = pointList.get(i)[1];
            }
        }

        for (int i = 0; i<pointList.size(); i++) {
            int[] coord = pointList.get(i);
            coord[1] += (SCREEN_HEIGHT - (lowest - BOTTOM_MARGIN));
            pointList.set(i, coord);
        }
        return pointList;
    }

    ArrayList<int[]> sortByX(ArrayList<int[]> pointList) {
        // return list sorted in ascending order of x coord
        pointList.sort((p1, p2) -> p1[0]-p2[0]);
        return pointList;
    }
}
