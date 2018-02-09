package Hw2Part2;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;


public class Flight {
	int F_id;
	int TotalBookings = 0;
	ReentrantLock Tlock = new ReentrantLock();
	HashSet<Integer> Customers = new HashSet<Integer> ();
	ReentrantLock CustLock = new ReentrantLock();
	
	
	public Flight(int id) {
		F_id = id;
	}
}
