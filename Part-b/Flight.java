package Hw2Part2;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;


public class Flight {
	public int F_id;
	public int TotalBookings = 0;
	public ReentrantLock Tlock = new ReentrantLock();
	public HashSet<Integer> Customers = new HashSet<Integer> ();
	public ReentrantLock CustLock = new ReentrantLock();
	
	
	public Flight(int id) {
		F_id = id;
	}
}
