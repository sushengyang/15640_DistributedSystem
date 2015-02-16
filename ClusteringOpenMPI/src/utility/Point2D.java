package utility;

import java.io.Serializable;

public class Point2D implements Serializable {
    private double x;
    private double y;
    
    public Point2D() {
    	this.x = 0.0;
    	this.y = 0.0;
    }
    
    public Point2D(double x, double y) {
    	this.x = x;
    	this.y = y;
    }
    
    public double getX() {
    	return this.x;
    }
    
    public double getY() {
    	return this.y;
    }
    
    public void setX(double x) {
    	this.x = x;
    }
    
    public void setY(double y) {
    	this.y = y;
    }
}
