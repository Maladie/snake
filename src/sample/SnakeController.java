package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SnakeController {

    @FXML
    private Canvas canvas;
    @FXML
    private Button downButton;
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private Button upButton;
    @FXML
    private Button startButton;
    @FXML
    private TextField scoreTextField;
    @FXML
    private StackPane stackPane;
    @FXML
    private Label label;
    @FXML
    private GridPane gridPane;

    private Synthesizer midiSynth = createSynth();
    private MidiChannel[] midiChannels = midiSynth.getChannels();
    private final int gameScaleInPixels = 20;
    private int squaresSideLenght = gameScaleInPixels -1;
    private int xAxisPixelsToTravel = gameScaleInPixels;
    private int yAxisPixelsToTravel = 0;
    private Snake snake;
    private int score;
    private Apple currentApple;
    private int gameSpeed;
    private boolean isPaused;
    private boolean isRunning;
    private List<Button> buttonsList;
    private GraphicsContext graphicsContext;

    public void initialize() {
        graphicsContext = canvas.getGraphicsContext2D();
        buttonsList = Arrays.asList(downButton, upButton, leftButton, rightButton);
        startButton.setDefaultButton(true);
        setupGraphicsForStage();
        keysOnAction();
        buttonsOnAction();
    }

    private void setupGraphicsForStage(){
        gridPane.setStyle("-fx-background-color: black");
        label.setStyle("-fx-background-color: black");
        stackPane.setStyle("-fx-background-color: black");
        stackPane.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
    }

    private void keysOnAction() {
        canvas.setOnKeyPressed(event -> {
            if (!downButton.isDisabled()&& event.getCode() == KeyCode.DOWN) {
                downButton.fire();
                disableButton(upButton);
            } else if (!upButton.isDisabled() && event.getCode() == KeyCode.UP) {
                upButton.fire();
                disableButton(downButton);
            } else if (!leftButton.isDisabled() && event.getCode() == KeyCode.LEFT) {
                leftButton.fire();
                disableButton(rightButton);
            } else if (!rightButton.isDisabled() && event.getCode() == KeyCode.RIGHT) {
                rightButton.fire();
                disableButton(leftButton);
            }
        });
    }

    private void buttonsOnAction(){
        upButtonOnAction();
        downButtonOnAction();
        leftButtonOnAction();
        rightButtonOnAction();
        startButtonOnAction();
    }

    private void upButtonOnAction() {
        upButton.setOnAction(event -> {
            xAxisPixelsToTravel = 0;
            yAxisPixelsToTravel = -1* gameScaleInPixels;
        });
    }

    private void downButtonOnAction() {
        downButton.setOnAction(event -> {
            xAxisPixelsToTravel = 0;
            yAxisPixelsToTravel = 1* gameScaleInPixels;
        });
    }

    private void leftButtonOnAction() {
        leftButton.setOnAction(event -> {
            xAxisPixelsToTravel = -1* gameScaleInPixels;
            yAxisPixelsToTravel = 0;
        });
    }

    private void rightButtonOnAction() {
        rightButton.setOnAction(event -> {
            xAxisPixelsToTravel = 1* gameScaleInPixels;
            yAxisPixelsToTravel = 0;
        });
    }

    private void startButtonOnAction(){
        startButton.setDefaultButton(true);
        startButton.setOnAction(event -> {
            if(!isRunning){
                runSnakeGame();
                startButton.setText("Pause");
            }else{
                pauseUnpauseGame();
                }
        });
    }

    private void disableButton(Button buttonToDisable){
        buttonToDisable.setDisable(true);
        buttonsList.stream().filter(button1 -> !button1.equals(buttonToDisable)).forEach(button -> button.setDisable(false));
    }

    private void pauseUnpauseGame() {
        if (isPaused) {
            unpauseGame();
        } else {
            pauseGame();
        }
    }

    private void unpauseGame(){
        isPaused = false;
        startButton.setText("Pause");
    }

    private void pauseGame(){
        isPaused = true;
        startButton.setText("Unpause");
        showPauseMessageOnStage();
    }

    private void showPauseMessageOnStage(){
        graphicsContext.setFill(new Color(0.4627, 0.8353, 0.4471, 1));
        graphicsContext.setFont(new Font("Verdana", 24));
        graphicsContext.fillText("PAUZA", 110, 150);
    }

    private void runSnakeGame() {
        clearStage();
        initializeGameThread();
    }

    private void clearStage(){
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void initializeGameThread(){
        Thread snakeThread = new Thread(() -> {
            resetGame();
            createStartingSnakesBody();
            createNewApple();
            isRunning = true;
            while (isSnakeAlive()) {
                while(isPaused){
                    putThreadToSleep(gameSpeed);
                }
                adjustGameSpeed();
                moveSnake();
                if(!isSnakeAlive()){
                    break;
                }
                tryToEnlargeSnake();
                clearStage();
                drawCurrentSnakesBody();
                drawApple();
                putThreadToSleep(gameSpeed);
            }
            showGameOverMessage();
            isRunning = false;
            Platform.runLater(() -> startButton.setText("Start"));
        });
        snakeThread.setDaemon(true);
        snakeThread.start();
    }

    private void resetGame(){
        gameSpeed = 200;
        score = 0;
        scoreTextField.clear();
        xAxisPixelsToTravel = gameScaleInPixels;
        yAxisPixelsToTravel = 0;
        rightButton.fire();
        disableButton(leftButton);
    }

    private void createStartingSnakesBody() {
        snake = new Snake(new Point2D(160,160));
        snake.getBody().add(new Point2D(140,160));
        snake.getBody().add(new Point2D(120,160));
    }

    private void createNewApple(){
        Point2D applePoint = Apple.createRandomApplePoint(gameScaleInPixels);
        if(isAppleOnSnakesBody(applePoint)){
            createNewApple();
        }else{
            currentApple = new Apple(applePoint);
        }
    }

    private void moveSnake() {
        snake.move(xAxisPixelsToTravel, yAxisPixelsToTravel);
    }

    private void putThreadToSleep(int gameSpeed) {
        try {
            Thread.sleep(gameSpeed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void drawCurrentSnakesBody(){
        graphicsContext.setFill(new Color(0.6549, 0.2549, 0.6824, 1));
        graphicsContext.fillRect(snake.getHead().getX(), snake.getHead().getY(), squaresSideLenght, squaresSideLenght);
        for (Point2D point : snake.getBody()) {
            graphicsContext.fillRect(point.getX(), point.getY(), squaresSideLenght, squaresSideLenght);
        }
    }

    private void drawApple(){
        graphicsContext.setFill(new Color(0.4627, 0.8353, 0.4471, 1));
        graphicsContext.fillRect(currentApple.getApplePosition().getX(), currentApple.getApplePosition().getY(),
                squaresSideLenght, squaresSideLenght);
    }

    private void adjustGameSpeed(){
        gameSpeed = 200 - 2*score;
        if(gameSpeed < 30){
            gameSpeed = 30;
        }
    }

    private void showGameOverMessage(){
        graphicsContext.setFill(new Color(0.4627, 0.8353, 0.4471, 1));
        graphicsContext.setFont(new Font("Verdana", 24));
        graphicsContext.fillText("GAME OVER!", 80, 150);
    }

    private boolean isAppleOnSnakesBody(Point2D applePoint){
        return (applePoint.equals(snake.getHead()) || snake.getBody().contains(applePoint));
    }

    private void tryToEnlargeSnake(){
        if(isSnakeEatingApple()){
            snake.addSnakeSegment();
            createNewApple();
            scoreTextField.setText(++score*10 + "");
            midiChannels[0].noteOn(100, 1000);
        }
    }

    private boolean isSnakeEatingApple(){
        return snake.getHead().equals(currentApple.getApplePosition());
    }

    private boolean isSnakeAlive(){
        return (!(snake.getBody().contains(snake.getHead()) || (snake.getHead().getX() == canvas.getWidth()) || snake.getHead()
                .getX() < 0 || snake.getHead().getY() == canvas.getHeight() || snake.getHead().getY() < 0));
    }

    private Synthesizer createSynth(){
        try {
            midiSynth = MidiSystem.getSynthesizer();
            if (!midiSynth.isOpen()) {
                midiSynth.open();
            }
        } catch (MidiUnavailableException e) {
            throw new RuntimeException("Midi niedostępne");
        }
        return midiSynth;
    }

}
