package ar.uba.dc.galli.qa.ml.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;

import ar.uba.dc.galli.qa.ml.ar.qasys.Question;
import ar.uba.dc.galli.qa.ml.ar.qasys.QuestionAndAnswers;
import ar.uba.dc.galli.qa.ml.utils.comparators.EqualNoPunctComparator;

import ar.uba.dc.galli.qa.ml.utils.TextEntity;

public class Utils {

	public static PrintStream STDOUT= System.out;
	public static String encodeNewLine(String input)
	{
		return input.replaceAll("\n", "AORTIPRAPRAPRA");
	}
	
	public static void logToFile(String str)
	{
		PrintStream old_out = System.out;
		redirectStdOut();
		System.out.println(str);
		redirectStdOut(old_out);
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
		String log_file = "results.log";
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
	
	public static String toGson(Object obj) {
		Gson gson = new Gson();
		return gson.toJson(obj);
	}


	/**
	 * Removes any XML encoded escape chars in a given string.
	 *
	 * For example Bush&apos;s is actually Bush's
	 * We just strip away the apostrophes.
	 *
	 * &quot;Bush&quot; is actually "Bush"
	 * We also strip away the quotes.
	 *
	 * D&ampG is D&G, we replace accordingly.
	 *
	 * TODO < and > also
	 *
	 * @param a_String
	 * @return string with XML characters replaced
	 */
	public static String StripXMLChar(String a_String) {

		Pattern l_Pattern_APOSS = Pattern.compile("&apos;s");
		Matcher l_Matcher_APOSS = l_Pattern_APOSS.matcher(a_String);
		a_String = l_Matcher_APOSS.replaceAll("");

		Pattern l_Pattern_APOS = Pattern.compile("&apos;");
		Matcher l_Matcher_APOS = l_Pattern_APOS.matcher(a_String);
		a_String = l_Matcher_APOS.replaceAll("'");

		Pattern l_Pattern_QUOT = Pattern.compile("&quot;");
		Matcher l_Matcher_QUOT = l_Pattern_QUOT.matcher(a_String);
		a_String = l_Matcher_QUOT.replaceAll("");

		Pattern l_Pattern_AMP = Pattern.compile("&amp;");
		Matcher l_Matcher_AMP = l_Pattern_AMP.matcher(a_String);
		a_String = l_Matcher_AMP.replaceAll("&");

		return a_String;

	} // end StripXMLChar()

	public static void logToFile(Question question) {
		PrintStream old_out = System.out;
		redirectStdOut();
		question.print();
		redirectStdOut(old_out);
		
	}
	
	public static void saveResult(QuestionAndAnswers qas) {
		String file_name = "results/"+Configuration.LANG_YEAR+"_"+Configuration.LUCENERESULTS+"_"+Configuration.N_PASSAGES+"_"+Configuration.PASSAGE_RANK+"_"+Configuration.QUERYGENERATION+"_"+Configuration.TOPIC_INFERENCE+"_"+Configuration.ANSWERS_PER_QUESTION+".results";
		PrintStream old_out = System.out;
		redirectStdOut(file_name);
		System.out.println(qas.toGson()+", ");
		redirectStdOut(old_out);
		
	}

	


}
