package Hw2Part1;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;


public class Flight {
	public int F_id;
	public int MaxCapacity;
	public ReentrantLock Tlock = new ReentrantLock();
	public HashSet<Integer> Customers = new HashSet<Integer> ();
	public ReentrantLock CustLock = new ReentrantLock();
	
	
	public Flight(int id, int cap) {
		F_id = id;
		MaxCapacity = cap;
	}
}


