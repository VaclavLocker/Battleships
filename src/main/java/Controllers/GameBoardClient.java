package Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import objects.Tile;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;

public class GameBoardClient {
    @FXML
    Pane yourPane;

    @FXML
    Pane enemyPane;

    @FXML
    Label turnLabel;

    @FXML
    Button sendMessage;

    @FXML
    TextField textField;

    @FXML
    TextArea textArea;

    //BOARD STUFF
    private final int size = 400;
    private final int spots = 10;
    private final int squareSize = size / spots;

    private ArrayList<Integer> yourBoard = new ArrayList<>(100);
    private final ArrayList<Integer> enemyBoard = new ArrayList<>(100);

    private final ArrayList<Tile> yourTiles = new ArrayList<>(100);
    private final ArrayList<Tile> enemyTiles = new ArrayList<>(100);

    //SOCKET STUFF
    private Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    //GAME INFO
    public static int YOUR_TURN = 1;
    public static int ENEMY_TURN = 2;
    public static int WINNER = 3;
    public static int TURN = ENEMY_TURN;
    public static String messages = "";


    public GameBoardClient(Pane you, Pane enemy, Label turn, Button message, TextField field, TextArea area) {
        yourPane = you;
        enemyPane = enemy;
        turnLabel = turn;
        sendMessage = message;
        textField = field;
        textArea = area;

        turnLabel.setText("Nepřítel");
        message.setOnAction(event -> sendMsg());

        yourBoard = SetupController.seaTiles;
    }

    public void initialize() {
        Runnable Initialize = () -> {
            try {
                socket = new Socket(SetupController.ip, SetupController.port);

                System.out.println("CLIENT STARTED");
                initCommunication(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        new Thread(Initialize).start();
    }

    private void initCommunication(Socket s) {
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Chyba");
        }

        sendBoardToServer();

        Runnable requestListener = () -> {
            try {
                String request = null;
                while ((request = in.readLine()) != null) {
                    // Handle board update
                    if (request.startsWith("INIT_BOARD")) {
                        request = request.replace("INIT_BOARD", "");
                        request = request.replace("[", "");
                        request = request.replace("]", "");

                        String[] tiles = request.split(", ");
                        for (String tile : tiles) {
                            enemyBoard.add(Integer.parseInt(tile));
                        }

                        Platform.runLater(this::initBoard);

                    } else if (request.startsWith("HIT")) {
                        request = request.replace("HIT", "");
                        request = request.trim();

                        String finalRequest = request;
                        Platform.runLater(() -> {
                            hitTile(yourTiles.get(Integer.parseInt(finalRequest)));
                        });
                    } else if (request.startsWith("MISS")) {
                        request = request.replace("MISS", "");
                        request = request.trim();

                        String finalRequest = request;
                        Platform.runLater(() -> {
                            yourTiles.get(Integer.parseInt(finalRequest)).hit();
                            TURN = YOUR_TURN;
                            turnLabel.setText("Ty");
                        });
                    } else if (request.startsWith("CHAT")) {
                        request = request.replace("CHAT", "");
                        request = request.trim();

                        String finalRequest1 = request;
                        Platform.runLater(() -> {
                            getMsg(finalRequest1);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        new Thread(requestListener).start();
    }

    //SOCKET COMMUNICATION
    private void sendBoardToServer() {
        out.println("INIT_BOARD" + yourBoard.toString());
    }

    private void sendHitToServer(int id) {
        out.println("HIT" + id);
    }

    private void sendMissToServer(int id) {
        out.println("MISS" + id);
    }

    private void sendMessageToServer(String msg) {
        out.println("CHAT" + msg);
    }

    //FXML UPDATES
    private void initBoard() {
        fillYourBoard();
        fillEnemyBoard();
    }

    private void hitTile(Tile tile) {
        tile.hit();
        checkIfSinked(tile);
        checkWin();
    }

    private void sendMsg() {
        if (!Objects.equals(textField.getText(), "")) {
            String msg = textField.getText();
            textField.setText("");

            messages += "Ty: " + msg + "\n";
            textArea.setText(messages);

            sendMessageToServer(msg);
        }
    }

    private void getMsg(String message) {
        if (!Objects.equals(message, "")) {
            messages += "Nepřítel: " + message + "\n";
            textArea.setText(messages);
        }
    }

    private void fillYourBoard() {
        int index = 1;

        for (int i = 0; i < size; i += squareSize) {
            for (int j = 0; j < size; j += squareSize) {
                Tile tile = new Tile(j, i, squareSize, squareSize, Tile.YOU, yourBoard.get(index));
                yourTiles.add(tile);
                yourPane.getChildren().add(tile);

                index++;
            }
        }
    }

    private void fillEnemyBoard() {
        int index = 1;

        for (int i = 0; i < size; i += squareSize) {
            for (int j = 0; j < size; j += squareSize) {
                Tile tile = new Tile(j, i, squareSize, squareSize, Tile.ENEMY, enemyBoard.get(index));
                tile.setOnMouseClicked(event -> onClick(tile));

                enemyTiles.add(tile);
                enemyPane.getChildren().add(tile);

                index++;
            }
        }
    }

    private void onClick(Tile tile) {
        if (TURN == YOUR_TURN && tile.STATUS == Tile.CLEAR && TURN != WINNER) {
            hitTile(tile);

            if (tile.TYPE == Tile.WATER) {
                TURN = ENEMY_TURN;
                turnLabel.setText("Nepřítel");
                sendMissToServer(enemyTiles.indexOf(tile));
            } else if (tile.TYPE == Tile.SHIP) {
                sendHitToServer(enemyTiles.indexOf(tile));
            }
        }
    }

    private void revealShips() {
        enemyTiles.forEach(Tile::reveal);
    }

    private void checkWin() {
        ArrayList<Tile> enemyShips = new ArrayList<>();
        for (Tile shipPart : enemyTiles) {
            if (shipPart.SHIP_ID != 0) enemyShips.add(shipPart);
        }

        ArrayList<Tile> yourShips = new ArrayList<>();
        for (Tile shipPart : yourTiles) {
            if (shipPart.SHIP_ID != 0) yourShips.add(shipPart);
        }

        Predicate<Tile> sinked = ship -> ship.STATUS == Tile.SHOT;

        if (enemyShips.stream().allMatch(sinked)) {
            TURN = WINNER;
            revealShips();
            turnLabel.setText("Vyhrál jsi!!!!");
        }

        if (yourShips.stream().allMatch(sinked)) {
            TURN = WINNER;
            revealShips();
            turnLabel.setText("Prohrál jsi :(");
        }

    }

    private void checkIfSinked(Tile tile) {
        ArrayList<Tile> shipParts = new ArrayList<>();
        ArrayList<Tile> currentTiles = new ArrayList<>(tile.OWNER == Tile.ENEMY ? enemyTiles : yourTiles);

        for (Tile shipPart : currentTiles) {
            if (shipPart.SHIP_ID == tile.SHIP_ID) shipParts.add(shipPart);
        }

        Predicate<Tile> sinked = ship -> ship.SHIP_ID == tile.SHIP_ID && ship.STATUS == Tile.SHOT;

        if (shipParts.stream().allMatch(sinked)) {
            shipParts.forEach((ship) -> ship.setFill(Tile.SHOT_BOAT_IMG));

            for (Tile ship : shipParts) {
                int shipIndex = currentTiles.indexOf(ship);

                // TOP
                boolean TOP = Math.floorDiv(shipIndex, spots) == 0;
                // BOT
                boolean BOT = Math.floorDiv(shipIndex, spots) == spots - 1;
                // LEFT
                boolean LEFT = shipIndex % spots == 0;
                // RIGHT
                boolean RIGHT = (shipIndex + 1) % spots == 0;

                if (TOP && LEFT) {
                    currentTiles.get(shipIndex + 1).hit();
                    currentTiles.get(shipIndex + spots).hit();
                    currentTiles.get(shipIndex + spots + 1).hit();
                } else if ( TOP && RIGHT) {
                    currentTiles.get(shipIndex - 1).hit();
                    currentTiles.get(shipIndex + spots).hit();
                    currentTiles.get(shipIndex + spots - 1).hit();
                } else if ( BOT && LEFT ) {
                    currentTiles.get(shipIndex + 1).hit();
                    currentTiles.get(shipIndex - spots).hit();
                    currentTiles.get(shipIndex - spots + 1).hit();
                } else if ( BOT && RIGHT) {
                    currentTiles.get(shipIndex - 1).hit();
                    currentTiles.get(shipIndex - spots).hit();
                    currentTiles.get(shipIndex - spots - 1).hit();
                } else if ( TOP ) {
                    currentTiles.get(shipIndex - 1).hit();
                    currentTiles.get(shipIndex + 1).hit();
                    currentTiles.get(shipIndex + spots - 1).hit();
                    currentTiles.get(shipIndex + spots).hit();
                    currentTiles.get(shipIndex + spots + 1).hit();
                } else if ( BOT ) {
                    currentTiles.get(shipIndex - 1).hit();
                    currentTiles.get(shipIndex + 1).hit();
                    currentTiles.get(shipIndex - spots - 1).hit();
                    currentTiles.get(shipIndex - spots).hit();
                    currentTiles.get(shipIndex - spots + 1).hit();
                } else if ( LEFT ) {
                    currentTiles.get(shipIndex - spots).hit();
                    currentTiles.get(shipIndex - spots + 1).hit();
                    currentTiles.get(shipIndex + 1).hit();
                    currentTiles.get(shipIndex + spots).hit();
                    currentTiles.get(shipIndex + spots + 1).hit();
                } else if ( RIGHT ) {
                    currentTiles.get(shipIndex - spots).hit();
                    currentTiles.get(shipIndex - spots - 1).hit();
                    currentTiles.get(shipIndex - 1).hit();
                    currentTiles.get(shipIndex + spots).hit();
                    currentTiles.get(shipIndex + spots - 1).hit();
                } else {
                    currentTiles.get(shipIndex - spots - 1).hit();
                    currentTiles.get(shipIndex - spots).hit();
                    currentTiles.get(shipIndex - spots + 1).hit();
                    currentTiles.get(shipIndex - 1).hit();
                    currentTiles.get(shipIndex + 1).hit();
                    currentTiles.get(shipIndex + spots - 1).hit();
                    currentTiles.get(shipIndex + spots).hit();
                    currentTiles.get(shipIndex + spots + 1).hit();
                }
            }

        }
    }

}
