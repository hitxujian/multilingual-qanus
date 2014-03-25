package ar.uba.dc.galli.qa.ml.utils;

public class Timer {

	long start_time;
	long end_time;

	public Timer() {
		start_time = System.currentTimeMillis();
	}
	
	/** tic and restart */
	public long tic()
	{
		end_time = System.currentTimeMillis();
		long res = (end_time - start_time) / 1000;
		start_time = System.currentTimeMillis();
		return res;
	}
	
	public void restart()
	{
		start_time = System.currentTimeMillis();
	}
	



}
