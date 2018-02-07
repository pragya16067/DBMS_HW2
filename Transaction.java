package Hw2Part1;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

//import Hw2Part1.Database;

public class Transaction  implements Runnable {
	ReentrantLock lock;
	
	public static volatile HashMap<Integer, ArrayList<Integer> > FlightDB = new HashMap< Integer, ArrayList<Integer> > ();
	public static volatile HashMap<Integer, ArrayList<Integer> > ClientDB = new HashMap< Integer, ArrayList<Integer> > ();
	
	
	public Transaction(ReentrantLock l) {
		lock = l;
	}
	
	public synchronized void run() {
		boolean ans = lock.tryLock();
	      // Returns True if lock is free
		    if(ans)
		    {
		    	try {
					Random r = new Random();
					System.out.println(Thread.currentThread().getName()+" (Start) " + FlightDB);  
		        
					int F = r.nextInt(100);
					int C = r.nextInt(1000);
					int ch = r.nextInt(10);
					if(ch<=3) {
						Reserve(F,C);
					}
					else if(ch==4) {
						Cancel(F,C);
					}
					else if(ch==5) {
						System.out.println("Flights booked for Customer "+C+" is/are "+My_Flights(C));
					}
					else if(ch==6 || ch==7) {
						System.out.println("The total number of reservations made "+Total_Reservations());
					}
					else
					{
						int F2 = r.nextInt(1000);
						Transfer(F,F2,C);
					}
		    	}
		    	catch (Exception e) 
		    	{
					e.printStackTrace();
				}
		    	finally 
		    	{
		    		 System.out.println(FlightDB);
		    		 System.out.println(Thread.currentThread().getName()+" (End)");//prints thread name
		    		 lock.unlock();
		    		 //notifyAll();
		    	}
		    	
		    }
		    else
		    {
		        System.out.println(Thread.currentThread().getName()+" waiting for lock");
		        try
		        {
		          Thread.sleep(1000);
		        }
		        catch(InterruptedException e)
		        {
		          e.printStackTrace();
		        }
		    }
		
				
	}
	
	public synchronized void Reserve(int F, int C_id) {
		synchronized(this) {
			if(FlightDB.containsKey(F)) //This flight ID is valid
			{
				ArrayList<Integer> customers = FlightDB.get(F);
				if(customers.size() <= 180) //Max 180 seats in an Airbus A320
				{
					customers.add(C_id);
				}
				FlightDB.put(F, customers);
				
			}
			else
			{
				ArrayList<Integer> customers = new ArrayList<Integer> ();
				customers.add(C_id);
				FlightDB.put(F, customers);
			}
			
			if(ClientDB.containsKey(C_id)) //This flight ID is valid
			{
				ArrayList<Integer> flight = ClientDB.get(F);
				flight.add(F);
				ClientDB.put(C_id, flight);
			}
			else
			{
				ArrayList<Integer> flight = new ArrayList<Integer> ();
				flight.add(F);
				ClientDB.put(C_id, flight);
			}
			
			System.out.print("Reserved succesfully");
		}
	}
	
	public static synchronized void Cancel(int F, int C_id) {
		if(FlightDB.containsKey(F) && ClientDB.containsKey(C_id)) //This flight ID is valid and customer id is valid
		{
			ArrayList<Integer> customers = FlightDB.get(F);
			if(customers.contains(C_id))
			{
				int index = customers.indexOf(C_id);
				customers.remove(index);
				FlightDB.put(F, customers);
			}
			
			ArrayList<Integer> flights = ClientDB.get(C_id);
			if(flights.contains(F))
			{
				int index = flights.indexOf(F);
				flights.remove(index);
				ClientDB.put(C_id, flights);
			}
		}
		else
		{
			System.out.println("No such Flight or No such Customer exists");
		}
	}
	
	public static ArrayList<Integer> My_Flights(int C_id) {
		ArrayList<Integer> flights = new ArrayList<Integer> ();
		if(ClientDB.containsKey(C_id))
		{
			flights = ClientDB.get(C_id);
			return flights;
		}
		return flights;
	}
	
	public static int Total_Reservations() {
		int ctr=0;
		for(int k : FlightDB.keySet() ) {
			ctr += FlightDB.get(k).size();
		}
		return ctr;
	}
	
	public static void Transfer(int F1, int F2, int c_id) {
		if(FlightDB.containsKey(F1))
		{
			ArrayList<Integer> customers1 = FlightDB.get(F1);
			int i=-1;
			if(customers1.contains(c_id))
			{
				i = customers1.indexOf(c_id);
				if(FlightDB.containsKey(F2)) 
				{
					ArrayList<Integer> customers2 = FlightDB.get(F2);
					if(customers2.size() <= 180) //Max 180 
					{
						customers1.remove(i);
						customers2.add(c_id);
						FlightDB.put(F1, customers1);
						FlightDB.put(F2, customers2);
					}
				}
				else
				{
					ArrayList<Integer> f= new ArrayList<Integer> ();
					customers1.remove(i);
					f.add(c_id);
					FlightDB.put(F1, customers1);
					FlightDB.put(F2, f);
				}
			}
		}
	}

	public static void main(String[] args) throws InterruptedException{
		Random r = new Random();
		ReentrantLock rl = new ReentrantLock();
		ExecutorService executor = Executors.newFixedThreadPool(5);//creating a pool of 5 threads
		
		for (int i = 0; i < 10; i++) 
		{ 
            Runnable worker = new Transaction(rl);
            executor.execute(worker);//calling execute method of ExecutorService  
        } 
		if(!executor.isTerminated())	
		{	
			executor.shutdown();	
			executor.awaitTermination(5L,TimeUnit.SECONDS);
		}	
        

	}

}

