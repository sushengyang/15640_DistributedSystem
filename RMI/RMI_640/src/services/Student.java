/**
 * 
 */
package services;

import rmi.server.RMIService;

/**
 * Student
 * 
 * a concrete service class provides set/get student's name and score 
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class Student extends RMIService {
	private String _name;
	private int _score;
	
	public Student() {
		_name = "NULL";
		_score = 0;
	}
	public Student(String name, int score) {
		_name = name;
		_score = score;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public void setScore(int score) {
		_score = score;
	}
	
	public String getName()	 {
		return _name;
	}
	
	public int getScore() {
		return _score;
	}
}
