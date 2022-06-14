import java.awt.*;
import java.util.ArrayList;

public class Lander {
    private Coordinate position;
    private double hSpeed;
    private double vSpeed;
    private int fuel;
    private int width;
    private int height;
    public Moon moon;

    public Lander(Coordinate position) {
        this.position = position;
        hSpeed = 0;
        vSpeed = 0;
        width = 20;
        height = 20;
        fuel = 100; // arbitrary units
        moon = null; // initially not associated with any moon
    }

    public int altitude() {
        // returns the height above the terrain
        if (moon !=null) {
            return moon.altitude(position) - height;  // check altitude to landing legs
        } else {
            return -1;
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawOval(position.x - width/2, position.y-height, width, height);
        g2.drawLine(
                position.x - width/4, position.y,
                position.x - width/2, position.y + height / 2
                );
        g2.drawLine(
                position.x + width/4, position.y,
                position.x + width/2, position.y + height / 2
        );
    }

    public void update() {
        if (altitude() > 0) {
            position.y++;
        }
    }

}
