package cz.romario.opensudoku.gui;

import java.util.LinkedList;

import android.util.Log;


//public class TaskQueue {
//	   private LinkedList<Runnable> tasks;
//	   private Thread thread;
//	   private boolean running;
//	   private Runnable internalRunnable;
//	  
//	   private class InternalRunnable implements Runnable {
//	     public void run() {
//	    	 
//	       internalRun();
//	     }
//	   }
//	  
//	   public TaskQueue() {
//	     tasks = new LinkedList<Runnable>();
//	     internalRunnable = new InternalRunnable();
//	   }
//	  
//	   public void start() {
//	     if (!running) {
//	       thread = new Thread(internalRunnable);
//	       thread.setDaemon(true);
//	       running = true;
//	       thread.start();
//	     }
//	   }
//	  
//	   public void stop() {
//	     running = false;
//	   }
//	 
//	  public void addTask(Runnable task) {
//	     synchronized(tasks) {
//	         tasks.addFirst(task);
//	         tasks.notify(); // notify any waiting threads
//	     }
//	   }
//	  
//	   private Runnable getNextTask() {
//	     synchronized(tasks) {
//	       if (tasks.isEmpty()) {
//	         try {
//	           tasks.wait();
//	         } catch (InterruptedException e) {
//	           Log.e("androidx", "Task interrupted", e);
//	           stop();
//	         }
//	       }
//	       return tasks.removeLast();
//	     }
//	   }
//	  
//	  
//	   private void internalRun() {
//	     while(running) {
//	       Runnable task = getNextTask();
//	       try {
//	         task.run();
//	       } catch (Throwable t) {
//	         Log.e("androidx", "Task threw an exception", t);
//	       }
//	     }
//	   }
//	}