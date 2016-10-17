package com.abarruda.musicbot.processor.responder.responses;


public class RefTest {
	
	public static class Complete {
		private boolean complete = false;
		
		public void setComplete() {
			complete = true;
		}
		
		public boolean getvalue() {
			return complete;
		}
	}
	
	
	private static void startThread(Complete complete) {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				int i = 0;
				while(!complete.getvalue()) {
					System.out.println(i);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					i++;
				}
			}
			
		};
		new Thread(thread).start();
	}

	public static void main(String[] args) throws InterruptedException {
		
		Complete complete = new Complete();
		startThread(complete);
		Thread.sleep(5000);
		System.out.println("Stopping");
		complete.setComplete();
		
		

	}

}
