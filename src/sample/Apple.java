package sample;

import javafx.geometry.Point2D;

import java.util.Random;

public class Apple {

    public Point2D getApplePosition() {
        return applePosition;
    }

    private static Random randomNumbersGenerator = new Random();

    private Point2D applePosition;

    public Apple(Point2D applePosition) {
        this.applePosition = applePosition;
    }

    public static Point2D createRandomApplePoint(int gameScaleInPixels){
        return new Point2D((randomNumbersGenerator.nextInt(15)* gameScaleInPixels),
                (randomNumbersGenerator.nextInt(15)* gameScaleInPixels));
    }

}
