/**
 * 
 */
package services;

import java.util.ArrayList;

import rmi.server.RMIService;

/**
 * Student
 * 
 * a concrete service class provides register student's name and score and get first student's info
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class StudentList extends RMIService {
	private ArrayList<Student> _list = null;
	
	public StudentList() {
		_list = new ArrayList<Student>();
	}
	
	public void generateNewStudent(String name, int score) {
		Student newStu = new Student(name, score);
		_list.add(newStu);
	}
	
	public Student getFirstStudent() {
		return _list.get(0);
	}
	
}
