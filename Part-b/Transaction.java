package Hw2Part2;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import Hw2Part2.Flight;


public class Transaction implements Runnable{
	static int Simulation_Time = 1000;
	
	public static volatile ArrayList<Flight> FlightDB = new ArrayList<Flight> ();
	
	public Transaction() {
	}
	
	public synchronized void run() {
	      
		    	try {
					Random r = new Random();
					
					//System.out.println(Thread.currentThread().getName()+" (Start) " + FlightDB);  
					
					int F = r.nextInt(200)+1;
					int C = r.nextInt(10)+1;
					int ch = r.nextInt(10);
					
					if(ch<=3) {
						System.out.println("Book seat in Flight "+F+" for customer "+C);
						Reserve(F,C);
						System.out.println();
					}
					else if(ch==4) {
						System.out.println("Cancel seat in Flight "+F+" for customer "+C);
						Cancel(F,C);
						System.out.println();
					}
					else if(ch==5) {
						System.out.println("Flights booked for Customer "+ C +" is/are "+My_Flights(C));
						System.out.println();
					}
					else if(ch==6 || ch==7) {
						System.out.println("The total number of reservations made "+Total_Reservations());
						System.out.println();
					}
					else
					{
						int F2 = r.nextInt(20)+1;
						System.out.println("Transfer customer "+C+" in Flight "+F+" to flight "+F2);
						Transfer(F,F2,C);
						System.out.println();
					}
		    	}
		    	catch (Exception e) 
		    	{
					e.printStackTrace();
				}
		    	
		/*try {
			Thread.sleep(Simulation_Time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */ 
	return;
		    
				
	}
	
	public synchronized void Reserve(int F, int C_id) throws InterruptedException {
		
		for(Flight f : FlightDB) {
			if(f.F_id == F)
			{
				
				while(!f.CustLock.tryLock()) {
					Thread.sleep(1000);
				}
				while(!f.Tlock.tryLock()) {
					Thread.sleep(1000);
				}
				try {
					
					if(f.Customers.size() <= 180) {
						f.Customers.add(C_id); 
					}
					f.TotalBookings = f.TotalBookings + 1;
				}
				finally	
				{
					f.CustLock.unlock();
					f.Tlock.unlock();
				}
				
			}
					
		
		}
		Thread.sleep(Simulation_Time);
		System.out.println("Reserved succesfully");
		return;
	}
	
	public static synchronized void Cancel(int F, int C_id) throws InterruptedException {
		boolean found=false;
		for(Flight f : FlightDB) {
			if(f.F_id == F)
			{
				
				if(f.Customers.contains(C_id)) {
					found = true;
					while(!f.CustLock.tryLock()) {
						Thread.sleep(1000);
					}
					//Also change the number of bookings
					while(!f.Tlock.tryLock()) {
						Thread.sleep(1000);
					}
					try {
						f.Customers.remove(C_id);
						f.TotalBookings = f.TotalBookings - 1;
					}
					finally	
						{f.CustLock.unlock();
						f.Tlock.unlock();
						Thread.sleep(Simulation_Time);
						System.out.println("Cancelled Succesfully");}
					
				}
			}
		}
		if(!found)
		{
			Thread.sleep(Simulation_Time);
			System.out.println("No such Flight or No such Customer exists");
		}
		return;
		
	}
	
	public synchronized static ArrayList<Integer> My_Flights(int C_id) throws InterruptedException {
		ArrayList<Integer> l = new ArrayList<Integer> ();
		for(Flight f : FlightDB) {
			while(!f.CustLock.tryLock())
			{
				Thread.sleep(1000);
			}
			try 
			{
				if(f.Customers.contains(C_id)) {
					l.add(f.F_id);
					break;
				}
			}
			finally
			{
				f.CustLock.unlock();
			}
		}
		Thread.sleep(Simulation_Time);
		return l;
	}
	
	public synchronized static int Total_Reservations() throws InterruptedException {
		int tot=0;
		for(Flight f : FlightDB) {
			while(!f.Tlock.tryLock())
			{
				Thread.sleep(1000);
			}
			try 
			{
				//f.Tlock.lock();
				tot = tot + f.TotalBookings;
			}
			finally
			{
				f.Tlock.unlock();
			}
				
		}
		Thread.sleep(Simulation_Time);
		return tot;
	}
	
	public synchronized static void Transfer(int F1, int F2, int c_id) throws InterruptedException {
		for(Flight f : FlightDB) {
			if(f.F_id == F1)
			{
				while(!f.CustLock.tryLock()) {
					Thread.sleep(1000);
				}
				try 
				{
					HashSet<Integer> customers1 = f.Customers;
					if(customers1.contains(c_id))
					{
					
						for(Flight f2 : FlightDB)
						{
							if(f2.F_id == F2)
							{
								while(!f2.CustLock.tryLock())
								{
									Thread.sleep(1000);
								}
								try {
									
									if(f2.Customers.size() <= 180)
									{
										customers1.remove(c_id);
										while(!f.Tlock.tryLock())
										{
											Thread.sleep(1000);
										}
										
										f.TotalBookings = f.TotalBookings - 1;
										f.Tlock.unlock();
										
										f2.Customers.add(c_id);
										while(!f2.Tlock.tryLock())
										{
											Thread.sleep(1000);
										}
										
										f2.TotalBookings = f2.TotalBookings + 1;
										f2.Tlock.unlock();
										f.Tlock.lock();
										System.out.println("Transfer Complete");
										return;
									}
								}
								finally {
									f2.CustLock.unlock();
								}
								break;
							}
						}
					}
					
				}
				finally {
					f.CustLock.unlock();
					Thread.sleep(Simulation_Time);
				}
				break;
			}
		}
		Thread.sleep(Simulation_Time);
		System.out.println("Transfer could not complete");
		
	}

	public static void main(String[] args) throws InterruptedException{
		long st = System.currentTimeMillis();
		for (int i = 1; i <= 200; i++) 
		{ 
			Flight e=new Flight(i);
			FlightDB.add(e);
		}
		
		ArrayList<Thread> threads = new ArrayList<Thread> ();
		int no_of_transactions = 5;
		
		for (int i = 0; i < no_of_transactions; i++) 
		{ 
            Runnable worker = new Transaction();
            Thread t = new Thread(worker);
            threads.add(t); 
        } 
		
		for (int i = 0; i < no_of_transactions; i++) 
		{
			threads.get(i).start();
			//System.out.println(Thread.currentThread().getName()+" has Started");
		}
		
		for (int i = 0; i < no_of_transactions; i++) 
		{
			threads.get(i).join();
			//System.out.println(Thread.currentThread().getName()+" has joined");
		}
		
		
		long et = System.currentTimeMillis();
		System.out.println((et - st)/1000.0); 
        

	}

}

