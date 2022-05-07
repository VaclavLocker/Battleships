package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import objects.Ship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetupController {

    //Lukášovo info start
    public static final int SERVER = 1;
    public static final int CLIENT = 2;
    public static ArrayList<Integer> seaTiles = new ArrayList<>(Collections.nCopies(100,0));
    public static int type = CLIENT;
    //Lukášovo info konec

    @FXML
    StackPane stack;

    @FXML
    Pane pane;

    @FXML
    BorderPane layout;

    @FXML
    Button rotateBtn;

    GridPane errorPane;
    Pane waitingPane;
    Button closePaneButton;

    private final int size = 400;
    private final int spots = 10;
    private final int squareSize = size / spots;
    private final int numberOfPieces = 5;
    private int lastSelectedShip;

    private ArrayList<Ship> ships;
    private Rectangle[][] grid;

    public void rotateShip() {
        Ship ship = ships.get(lastSelectedShip);
        if (validateShipRotation(ship)) {
            ship.rotate();
            released(ship);
        }
    }

    @FXML
    public void initialize() {
        // Setup Error page if player didn't place all ships on board and tried to start game
        errorPane = new GridPane();
        waitingPane = new Pane();
        closePaneButton = new Button();
        errorPane.setStyle("-fx-background-color: gray");
        Label text = new Label("Musíš dát všechny lodě na desku");
        closePaneButton.setText("Zpět");
        closePaneButton.setOnMouseClicked(mouseEvent -> errorPane.setVisible(false));
        VBox center = new VBox(text, closePaneButton);
        center.setAlignment(Pos.CENTER);
        errorPane.getChildren().add(center);
        errorPane.setAlignment(Pos.CENTER);
        stack.getChildren().add(1, errorPane);
        errorPane.setVisible(false);

        // Create Gameboard Sea Grid
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

        // Create Ships
        ships = new ArrayList<>();
        for (int i = 0; i < numberOfPieces; i++) {
            Rectangle r = new Rectangle();
            r.setFill(Color.GRAY);
            r.setStroke(Color.BLACK);
            int x = -250;
            int y = size/(numberOfPieces+1)*(i+1);
            Ship ship = new Ship(x, y, squareSize*(i+1), squareSize, r, spots, (i+1));
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
        ships.forEach(localShip -> {
            localShip.setColor(Color.GRAY);
        });
        ship.setColor(Color.DARKGOLDENROD);
        lastSelectedShip = ships.indexOf(ship);
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
            setBottomTiles(ship);
            ship.draw();
        } catch (Exception e) {
            if (!ship.isHorizontal()) {
                ship.rotate();
            }
            ship.setStartX(ship.getDockyardX());
            ship.setStartY(ship.getDockyardY());
            for (int i = 0; i < ship.getXCoordinates().length; i++) {
                for (int j = 0; j < ship.getYCoordinates().length; j++) {
                    grid[ship.getXCoordinates()[i]][ship.getYCoordinates()[j]].setFill(Color.BLUE);
                }
            }
            ship.clearCoordinates();
            ship.draw();
        }
    }

    public void setBottomTiles(Ship ship) {
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

    public boolean validateShipRotation(Ship ship) {
        int xGrid = (int) (ship.getStartX() + squareSize/2) / squareSize;
        int yGrid = (int) (ship.getStartY() + squareSize/2) / squareSize;
        if (ship.isHorizontal()) {
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
        } else {
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
        }
        return true;
    }


    public void startServer(ActionEvent event) throws IOException {
        type = SERVER;
/*
        int[] tempTiles = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 5, 5, 5, 5, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        seaTiles = (ArrayList<Integer>) IntStream.of(tempTiles).boxed().collect(Collectors.toList());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/cz/spse/battleships/gameboard.fxml")));
        stage.setScene(new Scene(root));
        stage.show();
*/
        playerReady(event);
    }

    public void startClient(ActionEvent event) throws IOException {
/*
        type = CLIENT;
        int[] tempTiles = {0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 2, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 1, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0};
        seaTiles = (ArrayList<Integer>) IntStream.of(tempTiles).boxed().collect(Collectors.toList());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/cz/spse/battleships/gameboard.fxml")));
        stage.setScene(new Scene(root));
        stage.show();
*/
        playerReady(event);
    }
    public void playerReady(ActionEvent event) throws IOException {
        if (ships.stream().noneMatch(ship -> ship.getDockyardX() == ship.getStartX())) {
            ships.forEach(ship -> {
                if (ship.isHorizontal()) {
                    for (int xCord:ship.getXCoordinates()) {
                        seaTiles.set((ship.getYCoordinates()[0]+1)*spots-spots+(xCord+1), ship.getId());
                    }
                } else {
                    for (int yCord:ship.getYCoordinates()) {
                        seaTiles.set((yCord+1)*spots-spots+(ship.getXCoordinates()[0]+1), ship.getId());
                    }
                }
            });

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/cz/spse/battleships/gameboard.fxml")));
            stage.setScene(new Scene(root));
            stage.show();

        } else {
            errorPane.setVisible(true);
        };
    }
}
