package Hw2Part1;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import Hw2Part2.Flight;


public class Transaction  implements Runnable {
	public ReentrantLock lock;
	public static int Simulation_Time = 2000;
	
	public static volatile ArrayList<Flight> FlightDB = new ArrayList<Flight> ();
	
	public Transaction(ReentrantLock l) {
		lock = l;
	}
	
	public synchronized void run() {
		boolean ans = lock.tryLock();
		
		while(!ans)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	      // Returns True if lock is free
		    if(ans)
		    {

		    	try {
					Random r = new Random();
					//System.out.println(Thread.currentThread().getName()+" (Start) " + FlightDB);  
		        
					int F = r.nextInt(20)+1;
					int C = r.nextInt(10);
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
		    	finally 
		    	{
		    		 //System.out.println(FlightDB);
		    		// System.out.println(Thread.currentThread().getName()+" (End)");//prints thread name
		    		 lock.unlock();
		    		 //notifyAll();
		    	}
		    }
		    else
		    {
		    	while(!lock.tryLock())
				{
					try {
						
						Thread.sleep(1000);
						//System.out.println(Thread.currentThread().getName() + "woke up from sleep.");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

		       /* System.out.println(Thread.currentThread().getName()+" waiting for lock");
		        try
		        {
		          Thread.sleep(1000);
		        }
		        catch(InterruptedException e)
		        {
		          e.printStackTrace();
		        }*/
		    }
		return;
		    
				
	}
	
	public synchronized void Reserve(int F, int C_id) throws InterruptedException {
		boolean found=false;
		for(Flight f : FlightDB) {
			if(f.F_id == F)
			{
				found = true;
				if(f.Customers.size() <= 180) {
					f.Customers.add(C_id);
					f.TotalBookings++;
					break;
				}
			}
		}
		
		/*if(!found)
		{
			Flight f= new Flight(F);
			f.Customers.add(C_id);
			f.TotalBookings++;
			FlightDB.add(f);
		}*/
		
		System.out.println("Reserved succesfully");
		Thread.sleep(Simulation_Time);
		return;
	}
	
	public static synchronized void Cancel(int F, int C_id) throws InterruptedException {
		boolean found=false;
		for(Flight f : FlightDB) {
			if(f.F_id == F)
			{
				found = true;
				if(f.Customers.contains(C_id)) {
					f.Customers.remove(C_id);
					f.TotalBookings--;
					System.out.println("Cancelled Succesfully");
					break;
				}
			}
		}
		if(!found)
		{
			System.out.println("No such Flight or No such Customer exists");
		}
		Thread.sleep(Simulation_Time);
		return;
		
	}
	
	public static ArrayList<Integer> My_Flights(int C_id) throws InterruptedException {
		ArrayList<Integer> l = new ArrayList<Integer> ();
		for(Flight f : FlightDB) {
			if(f.Customers.contains(C_id)) {
				l.add(f.F_id);
				break;
			}
		}
		Thread.sleep(Simulation_Time);
		return l;
	}
	
	public static int Total_Reservations() throws InterruptedException {
		int tot=0;
		for(Flight f : FlightDB) {
			tot = tot + f.TotalBookings;
		}
		Thread.sleep(Simulation_Time);
		return tot;
	}
	
	public static void Transfer(int F1, int F2, int c_id) throws InterruptedException {
		for(Flight f : FlightDB) {
			if(f.F_id == F1)
			{
				HashSet<Integer> customers1 = f.Customers;
				if(customers1.contains(c_id))
				{
					boolean foundF2 = false;
					for(Flight f2 : FlightDB)
					{
						if(f2.F_id == F2)
						{
							if(f2.Customers.size() <= 180)
							{
								customers1.remove(c_id);
								f.TotalBookings = f.TotalBookings - 1;
								f2.Customers.add(c_id);
								f2.TotalBookings = f2.TotalBookings + 1;
								Thread.sleep(Simulation_Time);
								System.out.println("Transfer Complete");
								return;
							}
							break;
						}
					}
					
				}
				break;
			}
		}
		Thread.sleep(Simulation_Time);
		System.out.println("Transfer could not complete");
		
	}

	public static void main(String[] args) throws InterruptedException{
		long st = System.currentTimeMillis();
		for (int i = 1; i <= 20; i++) 
		{ 
			Flight e=new Flight(i);
			FlightDB.add(e);
		}
		
		ReentrantLock rl = new ReentrantLock();
		//ExecutorService executor = Executors.newFixedThreadPool(5);//creating a pool of 5 threads
		
		for (int i = 0; i < 25; i++) 
		{ 
            Runnable worker = new Transaction(rl);
            Thread t = new Thread(worker);
            t.start();
            t.join();
            //executor.execute(worker);//calling execute method of ExecutorService  
        } 
		
		
		/*if(!executor.isTerminated())	
		{	
			executor.shutdown();	
			executor.awaitTermination(5L,TimeUnit.SECONDS);
		}*/	
		
		long et = System.currentTimeMillis();
		System.out.println((et - st)/1000.0); 
        

	}
		
		/*ReentrantLock rl = new ReentrantLock();
		ExecutorService executor = Executors.newFixedThreadPool(5);//creating a pool of 5 threads
		
		long t = System.currentTimeMillis();
		while( System.currentTimeMillis() - t < 1000) 
		{ 
            Runnable worker = new Transaction(rl);
            executor.execute(worker);//calling execute method of ExecutorService 
            
            //System.out.println((System.currentTimeMillis()-t));
        } 
		
		System.out.println("I am out of loop");
		
		executor.shutdownNow();
	    if (!executor.awaitTermination(5L, TimeUnit.MICROSECONDS)) {
	        System.out.println("Still waiting...");
	        //System.exit(0);
	    }
	    System.out.println("Exiting normally...");	*/
        

	

}

