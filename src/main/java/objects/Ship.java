package objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;

public class Ship {
    private double startX;
    private double startY;
    private double length;
    private double width;
    private Rectangle rectangle;
    private double parity;
    private boolean[] xCoordinates;
    private boolean[] yCoordinates;
    private boolean horizontal = true;
    private double dockyardX;
    private double dockyardY;

    public Ship(double startX, double startY, double length, double width, Rectangle rectangle, int spots) {
        this.startX = startX;
        this.startY = startY;
        this.length = length;
        this.width = width;
        this.rectangle = rectangle;
        this.parity = (length/width) % 2;
        this.xCoordinates = new boolean[spots];
        this.yCoordinates = new boolean[spots];
        this.dockyardX = startX;
        this.dockyardY = startY;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public void setColor(Color color) {
        rectangle.setFill(color);
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public boolean even() {
        return parity == 0;
    }

    public int getTiles() {
        if (!horizontal) {
            return (int)(width/length);
        }
        return (int) (length/width);
    }

    public void clearXCoordinates() {
        Arrays.fill(xCoordinates, false);
    }

    public void addXCoordinate(int x) {
        xCoordinates[x] = true;
    }

    public int[] getXCoordinates() {
        ArrayList<Integer> coordinates = new ArrayList<>();
        for (int q = 0; q < xCoordinates.length; q++) {
            if (xCoordinates[q]) {
                coordinates.add(q);
            }
        }
        return coordinates.stream().mapToInt(i->i).toArray();
    }

    public void clearYCoordinates() {
        Arrays.fill(yCoordinates, false);
    }

    public void addYCoordinate(int y) {
        yCoordinates[y] = true;
    }

    public int[] getYCoordinates() {
        ArrayList<Integer> coordinates = new ArrayList<>();
        for (int q = 0; q < yCoordinates.length; q++) {
            if (yCoordinates[q]) {
                coordinates.add(q);
            }
        }
        return coordinates.stream().mapToInt(i->i).toArray();
    }

    public void clearCoordinates() {
        clearXCoordinates();
        clearYCoordinates();
    }

    public void rotate() {
        this.horizontal = !horizontal;
        double tempWidth = this.width;
        this.width = this.length;
        this.length = tempWidth;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public double getDockyardX() {
        return dockyardX;
    }

    public double getDockyardY() {
        return dockyardY;
    }

    public void draw() {
        rectangle.setWidth(length);
        rectangle.setHeight(width);
        rectangle.setTranslateX(startX);
        rectangle.setTranslateY(startY);
    }
}
