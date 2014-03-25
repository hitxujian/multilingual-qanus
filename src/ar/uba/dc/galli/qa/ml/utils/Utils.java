package ar.uba.dc.galli.qa.ml.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang3.ArrayUtils;

import ar.uba.dc.galli.qa.ml.utils.comparatos.EqualNoPunctComparator;

import ar.uba.dc.galli.qa.ml.utils.TextEntity;

public class Utils {

	public static PrintStream STDOUT= System.out;
	public static String encodeNewLine(String input)
	{
		return input.replaceAll("\n", "AORTIPRAPRAPRA");
	}
	
	public static String decodeNewLine(String input)
	{
		return input.replaceAll("AORTIPRAPRAPRA", "\n");
	}
	
	public static boolean arrayCointainsInsensitive(String[] array, String str)
	{
		for(String x : array)
			if(x.compareToIgnoreCase(str) == 0 ) return true;
	
		return false;
	}
	
	public static void redirectStdOut()
	{
		String log_file = "mitic.log";
		try {
			System.setOut(new PrintStream(new FileOutputStream(log_file, true)));
			System.setErr(new PrintStream(new FileOutputStream(log_file, true)));
		} catch (FileNotFoundException e) {e.printStackTrace();}
	}
	
	public static void redirectStdErr(String log_file)
	{
		try {
			System.setErr(new PrintStream(new FileOutputStream(log_file, true)));
		} catch (FileNotFoundException e) {e.printStackTrace();}
	}
	
	
	
	public static void redirectStdOut(String log_file)
	{
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(log_file, true));
			System.setOut(ps);
			System.setErr(ps);
		} catch (FileNotFoundException e) {e.printStackTrace();}
	}
	
	
	public static void redirectStdErr(PrintStream console)
	{
		System.setErr(console);
	}
	
	public static void redirectStdOut(PrintStream console)
	{
		
		System.setOut(console);
		System.setErr(console);
	}
	
	public static void print(String str)
	{
		System.out.print(str);
	}
	
	public static void println(String str)
	{
		System.out.println(str);
	}
	
	public static String clean(String in)
	{
		return EqualNoPunctComparator.replace(EqualNoPunctComparator.remove(in));
		
	}
	
	public static String concatString(String[] in)
	{
		String res = "";
		for (int i = 0; i < in.length; i++) {
			res+=in[i]+" ";
		}

		if(res.length() > 0)
			res = res.substring(0, res.length()-1);
		
		return res;
	}
	
	public static String[] flattenTextEntities(TextEntity[] in)
	{
		String[] res = {};
		for (int i = 0; i < in.length; i++) 
			res = ArrayUtils.add(res, in[i].term);
		return res;
	}

	public static String textEntitiesToJson(TextEntity[] in)
	{
		String str = concatString(flattenTextEntities(in));
		if(str.length() > 0)
			return "{"+str+"}";
		return "0";
	}
	public static String toJson(TextEntity[] in)
	{
		return textEntitiesToJson(in);

	}



}
