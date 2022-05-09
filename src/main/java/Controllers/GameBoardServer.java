package Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import objects.Tile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

public class GameBoardServer {
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
    private ServerSocket ss;
    private Socket s;

    private final int PORT = 56789;

    private BufferedReader in;
    private PrintWriter out;

    //GAME INFO
    public static int YOUR_TURN = 1;
    public static int ENEMY_TURN = 2;
    public static int WINNER = 3;
    public static int TURN = YOUR_TURN;
    public static String messages = "";

    public GameBoardServer(Pane you, Pane enemy, Label turn, Button message, TextField field, TextArea area) {
        yourPane = you;
        enemyPane = enemy;
        turnLabel = turn;
        sendMessage = message;
        textField = field;
        textArea = area;

        message.setOnAction(event -> sendMsg());

        yourBoard = SetupController.seaTiles;
    }


    public void initialize() {
        Runnable Initialize = () -> {
            try {
                ss = new ServerSocket(PORT);
                s = ss.accept();

                System.out.println("SERVER STARTED");

                initCommunication(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        new Thread(Initialize).start();
    }


    private void initCommunication(Socket s) {
        try {
            this.out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
            this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (Exception e) {
            System.out.println("Chyba");
        }

        sendBoardToClient();

        Runnable requestListener = () -> {
            try {
                String request = null;
                while ((request = in.readLine()) != null) {
                    // Handle board update
                    if (request.startsWith("INIT_BOARD")) {
                        request = request.replace("INIT_BOARD", "");
                        request = request.replace("[","");
                        request = request.replace("]","");

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
                            yourTiles.get(Integer.parseInt(finalRequest)).hit();
                            checkWin();
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
    private void sendBoardToClient() {
        out.println("INIT_BOARD" + yourBoard.toString());
    }

    private void sendHitToClient(int id) {
        out.println("HIT" + id);
    }

    private void sendMissToClient(int id) {
        out.println("MISS" + id);
    }

    private void sendMessageToClient(String msg) {
        out.println("CHAT" + msg);
    }


    //FXML UPDATES
    private void initBoard() {
        fillYourBoard();
        fillEnemyBoard();
    }

    private void sendMsg() {
        if (!Objects.equals(textField.getText(), "")) {
            String msg = textField.getText();
            textField.setText("");

            messages += "Ty: " + msg + "\n";
            textArea.setText(messages);

            sendMessageToClient(msg);
        }
    }

    private void getMsg(String message) {
        if (!Objects.equals(message, "")) {
            messages += "Nepřítel: " + message + "\n";
            textArea.setText(messages);
        }
    }


    //HELPERS
    private void fillYourBoard() {
        int index = 1;

        for (int i = 0; i < size; i+= squareSize) {
            for (int j = 0; j < size; j+= squareSize) {
                Tile tile = new Tile(j, i, squareSize, squareSize, Tile.YOU, yourBoard.get(index));

                yourTiles.add(tile);
                yourPane.getChildren().add(tile);

                index++;
            }
        }
    }

    private void fillEnemyBoard() {
        int index = 1;

        for (int i = 0; i < size; i+= squareSize) {
            for (int j = 0; j < size; j+= squareSize) {
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
            tile.hit();

            if (tile.TYPE == Tile.WATER) {
                TURN = ENEMY_TURN;
                turnLabel.setText("Nepřítel");
                sendMissToClient(enemyTiles.indexOf(tile));
            } else if (tile.TYPE == Tile.SHIP) {
                checkIfSinked(tile);
                sendHitToClient(enemyTiles.indexOf(tile));
            }
        }

        checkWin();
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
            turnLabel.setText("Vyhrál jsi!!!!");
        }

        if (yourShips.stream().allMatch(sinked)) {
            TURN = WINNER;
            turnLabel.setText("Prohrál jsi :(");
        }

    }

    private void checkIfSinked(Tile tile) {
        ArrayList<Tile> shipParts = new ArrayList<>();
        for (Tile shipPart : enemyTiles) {
            if (shipPart.SHIP_ID == tile.SHIP_ID) shipParts.add(shipPart);
        }

        Predicate<Tile> sinked = ship -> ship.SHIP_ID == tile.SHIP_ID && ship.STATUS == Tile.SHOT;

        if (shipParts.stream().allMatch(sinked)) {
            shipParts.forEach((ship) -> ship.setFill(Tile.SHOT_BOAT_IMG));
        }
    }

}
