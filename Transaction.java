package Hw2Part1;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

//import Hw2Part1.Database;

public class Transaction  implements Runnable {
	public int type;
	ReentrantLock lock;
	
	public static volatile HashMap<Integer, ArrayList<Integer> > FlightDB = new HashMap< Integer, ArrayList<Integer> > ();
	public static volatile HashMap<Integer, ArrayList<Integer> > ClientDB = new HashMap< Integer, ArrayList<Integer> > ();
	
	
	public Transaction(int type, ReentrantLock l) {
		this.type = type;
		lock = l;
	}
	
	public void run() {
		boolean ans = lock.tryLock();
		synchronized(this) { 
	      // Returns True if lock is free
		    if(ans)
		    {
		    	try {
					Random r = new Random();
					System.out.println(Thread.currentThread().getName()+" (Start) " + FlightDB);  
		        
				//for(int i=0; i<10; i++)
				//{
					
					int F = r.nextInt(1000);
					int C = r.nextInt(1000);
					Reserve(F,C);
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
		    		 notifyAll();
		    	}
		    	
		    }
		    else
		    {
		        System.out.println(Thread.currentThread().getName()+" waiting for lock");
		        try
		        {
		          wait(1000);
		        }
		        catch(InterruptedException e)
		        {
		          e.printStackTrace();
		        }
		    }
		}
				
	}
	
	public synchronized void Reserve(int F, int C_id) {
		synchronized(this) {
		if(FlightDB.containsKey(F)) //This flight ID is valid
			{
				ArrayList<Integer> customers = FlightDB.get(F);
				customers.add(C_id);
				FlightDB.put(F, customers);
			}
		else
		{
			ArrayList<Integer> customers = new ArrayList<Integer> ();
			customers.add(C_id);
			FlightDB.put(F, customers);
		}
		}
	}
	
	public static synchronized void Cancel(int F, int C_id) {
		if(FlightDB.containsKey(F)) //This flight ID is valid
		{
			ArrayList<Integer> customers = FlightDB.get(F);
			if(customers.contains(C_id))
			{
				int index = customers.indexOf(C_id);
				customers.remove(index);
				FlightDB.put(F, customers);
			}
			
			
		}
		else
		{
			System.out.println("No such Flight exists");
		}
	}

	public static void main(String[] args) {
		Random r = new Random();
		ReentrantLock rl = new ReentrantLock();
		ExecutorService executor = Executors.newFixedThreadPool(5);//creating a pool of 5 threads
		
		for (int i = 0; i < 10; i++) 
		{ 
			int Ttype = r.nextInt(2);
            Runnable worker = new Transaction(Ttype,rl);
            executor.execute(worker);//calling execute method of ExecutorService  
        }  
        executor.shutdown(); 
        
        while (!executor.isTerminated()) 
        {   }  
        
        

	}

}

