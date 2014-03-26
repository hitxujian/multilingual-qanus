package ar.uba.dc.galli.qa.ml.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import ar.uba.dc.galli.qa.ml.ibp.Controller;

public class Configuration {

	public static String WIKIDIR = "/home/julian/tesis/wiki/";
	public static String INDEXDIR = "/home/julian/tesis/lucene-indexes/";
	public static String PREGUNTAS_EN = "/home/julian/tesis/clef/clef2007/preguntas_harry_en.xml";
	public static String PREGUNTAS_ES = "/home/julian/tesis/clef/clef2007/ES_EN_MONO.xml";//preguntas_harry_es.xml";
	public static String RESPUESTAS_ES = "/home/julian/tesis/clef/clef2007/ES_GOLDSTANDARD_2007.xml";//respuestas_harry.xml";
	public static String PREGUNTAS_PT = "/home/julian/tesis/clef/clef2007/PT_EN_MONO.xml";
	public static String RESPUESTAS_PT = "/home/julian/tesis/clef/clef2007/PT_GOLDSTANDARD_2007.xml";
	
	public static boolean SUPPORT_WIKI = true;
	public static boolean SUPPORT_NIL = true;
	public static boolean SUPPORT_NEWS = true;
	public static boolean QTYPE_FACTOID = true;
	public static boolean QTYPE_LIST = true;
	public static boolean QTYPE_DEFINITION = true;
	public static boolean GROUP_ENTITY_NER_AND_NOUNS = true;
	
	/*Cuantas preguntas itera del total del xml 200 = todas*/
	public static int UP_TO_N_QUESTIONS = 200;
	
	public static boolean EVAL_PASSAGES = false; //false es evaldocs
	
	public static String LANG = "pt";
	public static String INDEX = "pt-2007"; // "pt-2007"
	public static int LUCENERESULTS = 100;
	public static int QUERYGENERATION = 2; //1, 2, 3 o 4
	
	public static boolean LOG_CONSOLE_SOLO = true;
	
	public static String DOCS_LOGFILE = "/home/julian/tesis/clef/queries-eval/"+LANG+"_"+LUCENERESULTS+"_"+INDEX+"-"+QUERYGENERATION+".log";

	public static int N_PASSAGES = 10;
	public static int PASSAGE_RANK = 2;
	public static String PASSAGES_LOGFILE = "/home/julian/tesis/clef/passage-eval/"+N_PASSAGES+"_"+LUCENERESULTS+"_3.log";
	
	public static Level LOGLEVEL = Level.ALL;
	
	
	public static int OptionToIndex(String option)
	{
		int extractThis = 5;
		if(option.compareTo("simple-06") == 0) extractThis = 0;
		if(option.compareTo("simple-13") == 0) extractThis = 1;
		if(option.compareTo("es-06") == 0) extractThis = 2;
		if(option.compareTo("en-06") == 0) extractThis = 3;
		if(option.compareTo("pt-07") == 0) extractThis = 4;
		return extractThis;
	}
	
	public static String GetWikipediaFromOption(String option)
	{
			
		String wikiPostfix = "-pages-articles.xml";
		String[] wikipedias = {"simplewiki-20060704", "simplewiki-20130724", "eswiki-20060705", "enwiki-20061104", "ptwiki-20070201" };
		
		String res = "";
		try {
			res = Configuration.WIKIDIR+wikipedias[OptionToIndex(option)]+wikiPostfix;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			Logger.getLogger("QANUS").logp(Level.SEVERE, Configuration.class.getName(), "GetWikipediaFromOption", "Your --wiki parameter is incorrectly configured, options are: simple-06, simple-13, es-06, en-06, pt-07");
			System.exit(1);
		}
		
		return res; 

		

	}
	
	public static String GetLuceneIndexFromOption(String option)
	{
		String[] outputDirs  = {"index-simple", "index-simple-2013",   "index-es-2006",    "index-en-2006" , "index-pt-2007" };
		
		String res = "";
		try {
			res = Configuration.INDEXDIR+outputDirs[OptionToIndex(option)];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			Logger.getLogger("QANUS").logp(Level.SEVERE, Configuration.class.getName(), "GetLuceneIndexFromOption", "Your --wiki parameter is incorrectly configured, options are: simple-06, simple-13, es-06, en-06, pt-07");
			System.exit(1);
		}
		
		return res;
		
	}
}

