package sample;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    private Point2D head;

    private List<Point2D> body;

    public List<Point2D> getBody() {
        return body;
    }

    public Snake(Point2D head) {
        this.head = head;
        body = new ArrayList<>();
    }

    public Point2D getHead() {
        return head;
    }

    private void setHead(Point2D head) {
        this.head = head;
    }

    private void moveHead(double x, double y) {
        setHead(getHead().add(x, y));
    }

    private void moveSegments(){
        Point2D startPoint = getHead();
        for (int i = 0; i < body.size(); i++) {
            Point2D temp = body.get(i);
            body.set(i, startPoint);
            startPoint = temp;
        }
    }

    public void addSnakeSegment(){

        body.add(body.get(1));
    }

    public void move(double x, double y){
        moveSegments();
        moveHead(x, y);
    }
}
