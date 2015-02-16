package utility;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Generator {
	protected int getUniform(int min, int max) {
		Random r = new Random();
		return r.nextInt(max) % (max - min + 1) + min;
	}
	
	protected double getUniform(double min, double max) {
		Random r = new Random();
		return r.nextDouble() * (max - min) + min;
	}
	
	protected double getGaussian(double mean, double variance) {
		Random r = new Random();
		return mean + r.nextGaussian() * variance;
	}
	
	protected abstract void printUsage();
	protected abstract void parseArgs(String[] args);
	protected abstract void generateCentroid();
	protected abstract void generateData();	
	
	protected static class Reminder {
		private Timer timer;
	
		public Reminder(int seconds) {
			timer = new Timer();
			timer.schedule(new RemindTask(), seconds);
		}
		
		public void cancel() {
			timer.cancel();
		}
		
		class RemindTask extends TimerTask {
			public void run() {
				System.out.format("Data generation failed, please retry!%n");
				timer.cancel(); 
				System.exit(-1);
			}
		}
	}
}
