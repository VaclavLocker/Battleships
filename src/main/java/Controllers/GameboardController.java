package Controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import objects.Ship;

import java.util.ArrayList;

public class GameboardController {

    @FXML
    Pane yourPane;

    @FXML
    Pane enemyPane;

    private final int size = 400;
    private final int spots = 10;
    private final int squareSize = size / spots;

    private Rectangle[][] yourGrid;
    private Rectangle[][] enemyGrid;

    @FXML
    public void initialize() {
        yourGrid = new Rectangle[spots][spots];
        for (int i = 0; i < size; i+= squareSize) {
            for (int j = 0; j < size; j+= squareSize) {
                Rectangle r = new Rectangle(i, j, squareSize, squareSize);
                yourGrid[i/squareSize][j/squareSize] = r;
                r.setFill(Color.BLUE);
                r.setStroke(Color.BLACK);
                yourPane.getChildren().add(r);
            }
        }

        enemyGrid = new Rectangle[spots][spots];
        for (int i = 0; i < size; i+= squareSize) {
            for (int j = 0; j < size; j+= squareSize) {
                Rectangle r = new Rectangle(i, j, squareSize, squareSize);
                enemyGrid[i/squareSize][j/squareSize] = r;
                r.setFill(Color.BLUE);
                r.setStroke(Color.BLACK);
                enemyPane.getChildren().add(r);
            }
        }
    }
}

