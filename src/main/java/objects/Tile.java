package objects;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {

    //SHIP ID
    public int SHIP_ID;
    //OWNER
    public static final int ENEMY = 1;
    public static final int YOU = 2;
    public int OWNER;

    //TYPE
    public static final int WATER = 1;
    public static final int SHIP = 2;
    public int TYPE;

    //STATUS
    public static final int CLEAR = 1;
    public static final int SHOT = 2;
    public int STATUS;

    public static final ImagePattern MISS_IMG = new ImagePattern(new Image("file:src/main/resources/images/miss.png"));
    public static final ImagePattern SHOT_BOAT_IMG = new ImagePattern(new Image("file:src/main/resources/images/hit_you.png"));
    public static final ImagePattern SHOT_WATER_IMG = new ImagePattern(new Image("file:src/main/resources/images/hit_enemy.png"));

    public static final Color WATER_COLOR = Color.web("#316BA2");
    public static final Color SHIP_COLOR = Color.web("#6E6E6E");

    public Tile(int x, int y, int sizeX, int sizeY, int owner, int tileID) {
        super(x, y, sizeX, sizeY);

        OWNER =  owner;
        TYPE = tileID != 0 ? SHIP : WATER;
        STATUS = CLEAR;
        SHIP_ID = tileID;

        this.setStroke(Color.BLACK);

        if (OWNER == YOU) {
            this.setFill(TYPE == SHIP ? SHIP_COLOR : WATER_COLOR);
        } else if (OWNER == ENEMY) {
            this.setFill(WATER_COLOR);
        }
    }

    public void hit() {
        STATUS = SHOT;

        if (TYPE == WATER) {
            this.setFill(MISS_IMG);
        } else if (TYPE == SHIP) {
            if (OWNER == YOU) {
                this.setFill(SHOT_BOAT_IMG);
            } else if (OWNER == ENEMY) {
                this.setFill(SHOT_WATER_IMG);
            }
        }
    }

    public void reveal() {
        if (STATUS == CLEAR) {
            this.setFill(TYPE == SHIP ? SHIP_COLOR : WATER_COLOR);
        }
    }

}
