package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import objects.Ship;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class SetupController {

    @FXML
    Pane pane;

    @FXML
    BorderPane layout;

    @FXML
    Button rotateBtn;

    private final int size = 400;
    private final int spots = 10;
    private final int squareSize = size / spots;
    private final int numberOfPieces = 5;

    private ArrayList<Ship> ships;
    private Rectangle[][] grid;


    public void rotateShip() {
        Ship ship = ships.get(0);
        ship.rotate();
        released(ship);
    }

    @FXML
    public void initialize() {
        grid = new Rectangle[spots][spots];
        for (int i = 0; i < size; i+= squareSize) {
            for (int j = 0; j < size; j+= squareSize) {
                Rectangle r = new Rectangle(i, j, squareSize, squareSize);
                grid[i/squareSize][j/squareSize] = r;
                r.setFill(Color.BLUE);
                r.setStroke(Color.BLACK);
                pane.getChildren().add(r);
            }
        }
        ships = new ArrayList<>();
        for (int i = 0; i < numberOfPieces; i++) {
            Rectangle r = new Rectangle();
            r.setFill(Color.GRAY);
            r.setStroke(Color.BLACK);
            int x = -250;
            int y = size/(numberOfPieces+1)*(i+1);
            Ship ship = new Ship(x, y, squareSize*(i+1), squareSize, r, spots);
            ships.add(ship);
            r.setOnMousePressed(event -> pressed(ship));
            r.setOnMouseDragged(event -> dragged(event, ship));
            r.setOnMouseReleased(event -> released(ship));
            pane.getChildren().add(r);
            ship.draw();
        }
    }

    public void randomPositions() {
        // TODO Create randomize ships method
        // int x = squareSize / 2 + squareSize * (int)(Math.random() * spots);
        // int y = squareSize / 2 + squareSize * (int)(Math.random() * spots);
    }

    public void pressed(Ship ship) {
        ship.setColor(Color.DARKGOLDENROD);
    }

    public void dragged(MouseEvent event, Ship ship) {

        if(ship.even()) {
            if (ship.isHorizontal()) {
                ship.setStartX((ship.getStartX() - (double)squareSize/2) + event.getX());
                ship.setStartY((ship.getStartY() - (double)squareSize/2) + event.getY());
            } else {
                ship.setStartY((ship.getStartY() - (double)squareSize / 2) + event.getY());
                ship.setStartX((ship.getStartX() - (double)squareSize/2) + event.getX());
            }
        } else {
            if (ship.isHorizontal()) {
                ship.setStartX((ship.getStartX() - ship.getLength()/2) + event.getX());
                ship.setStartY((ship.getStartY() - (double)squareSize/2) + event.getY());
            } else {
                ship.setStartY((ship.getStartY() - ship.getWidth()/2) + event.getY());
                ship.setStartX((ship.getStartX() - (double)squareSize/2) + event.getX());
            }
        }
        ship.draw();

    }

    public void released(Ship ship) {
        ship.setColor(Color.GRAY);
        try {
            if (!placeValidator(ship)) {
              ship.setStartY(ship.getYCoordinates()[0]*squareSize);
              ship.setStartX(ship.getXCoordinates()[0]*squareSize);
            }
            for (int i = 0; i < ship.getXCoordinates().length; i++) {
                for (int j = 0; j < ship.getYCoordinates().length; j++) {
                    grid[ship.getXCoordinates()[i]][ship.getYCoordinates()[j]].setFill(Color.BLUE);
                }
            }
            ship.clearCoordinates();
            int gridX;
            int gridY;
            if (ship.isHorizontal()) {
                gridY = (int) (ship.getStartY() + squareSize/2) / squareSize;
                ship.addYCoordinate(gridY);
                if(ship.even()) {
                    gridX = (int) (ship.getStartX() + squareSize/2) / squareSize;
                    for (int i = 0; i < ship.getTiles(); i++) {
                        ship.addXCoordinate(gridX+i);
                        grid[gridX+i][gridY].setFill(Color.CRIMSON);
                    }
                    ship.setStartX(squareSize * gridX);
                } else {
                    gridX = (int) (ship.getStartX() + ship.getLength()/2) / squareSize;
                    ship.addXCoordinate(gridX);
                    grid[gridX][gridY].setFill(Color.CRIMSON);
                    for (int i = 0; i < (ship.getTiles()-1)/2; i++) {
                        ship.addXCoordinate(gridX-(i+1));
                        ship.addXCoordinate(gridX+(i+1));
                        grid[gridX-(i+1)][gridY].setFill(Color.CRIMSON);
                        grid[gridX+(i+1)][gridY].setFill(Color.CRIMSON);
                    }
                    System.out.println(gridX);
                    ship.setStartX(squareSize * gridX - (squareSize*((double)(ship.getTiles()-1)/2)));
                }
                ship.setStartY(squareSize * gridY);
            } else {
                gridX = (int) (ship.getStartX() + squareSize/2) / squareSize;
                ship.addXCoordinate(gridX);
                if(ship.even()) {
                    gridY = (int) (ship.getStartY() + squareSize/2) / squareSize;
                    for (int i = 0; i < ship.getTiles(); i++) {
                        ship.addYCoordinate(gridY+i);
                        grid[gridX][gridY+i].setFill(Color.CRIMSON);
                    }
                    ship.setStartY(squareSize * gridY);
                } else {
                    gridY = (int) (ship.getStartY() + ship.getWidth()/2) / squareSize;
                    ship.addYCoordinate(gridY);
                    grid[gridX][gridY].setFill(Color.CRIMSON);
                    for (int i = 0; i < (ship.getTiles()-1)/2; i++) {
                        ship.addYCoordinate(gridY-(i+1));
                        ship.addYCoordinate(gridY+(i+1));
                        grid[gridX][gridY-(i+1)].setFill(Color.CRIMSON);
                        grid[gridX][gridY+(i+1)].setFill(Color.CRIMSON);
                    }
                    ship.setStartY(squareSize * gridY - (squareSize*((double)(ship.getTiles()-1)/2)));
                }
                ship.setStartX(squareSize * gridX);
            }
            ship.draw();
        } catch (Exception e) {
            ship.setStartX(ship.getDockyardX());
            ship.setStartY(ship.getDockyardY());
            System.err.println("CHYBA");
            for (int i = 0; i < ship.getXCoordinates().length; i++) {
                for (int j = 0; j < ship.getYCoordinates().length; j++) {
                    grid[ship.getXCoordinates()[i]][ship.getYCoordinates()[j]].setFill(Color.BLUE);
                }
            }
            ship.clearCoordinates();
            ship.draw();
        }
    }

    public boolean placeValidator(Ship ship) {
        double xCord;
        double yCord;
        if (ship.isHorizontal()) {
         xCord = ship.even() ? (ship.getStartX() + (double)squareSize/2) : ship.getStartX() + ship.getLength()/2;
         yCord = ship.getStartY() + (double)squareSize/2;
        } else {
        xCord = ship.getStartX() + (double)squareSize/2;
        yCord = ship.even() ? (ship.getStartY() + (double)squareSize/2) : ship.getStartY() + ship.getWidth()/2;
        }
        if (0 > xCord || xCord > 400 || yCord < 0 || yCord > 400) {
            return false;
        }

        int xGrid = (int) (ship.getStartX() + squareSize/2) / squareSize;
        int yGrid = (int) (ship.getStartY() + squareSize/2) / squareSize;
        if (ship.isHorizontal()) {
            for (int i = xGrid-1; i < xGrid+ship.getTiles()+1; i++) {
                for (int j = yGrid-1; j < yGrid+2; j++) {
                    if (i >= 0 && i < 10 && j >= 0 && j < 10) {
                        int finalJ = j;
                        int finalI = i;
                        if (!(IntStream.of(ship.getXCoordinates()).anyMatch(x -> x == finalI) && IntStream.of(ship.getYCoordinates()).anyMatch(y -> y == finalJ))) {
                            if (grid[i][j].getFill() == Color.CRIMSON) {
                                return false;
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = xGrid-1; i < xGrid+2; i++) {
                for (int j = yGrid-1; j < yGrid+ship.getTiles()+1; j++) {
                    if (i >= 0 && i < 10 && j >= 0 && j < 10) {
                        int finalJ = j;
                        int finalI = i;
                        if (!(IntStream.of(ship.getXCoordinates()).anyMatch(x -> x == finalI) && IntStream.of(ship.getYCoordinates()).anyMatch(y -> y == finalJ))) {
                            if (grid[i][j].getFill() == Color.CRIMSON) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
