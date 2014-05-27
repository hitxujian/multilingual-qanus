package ar.uba.dc.galli.qa.ml.utils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import sg.edu.nus.wing.qanus.framework.util.DirectoryAndFileManipulation;

import ar.uba.dc.galli.qa.ml.ibp.Controller;

public class Configuration {

	
	
	public static String BASELIBDIR = "/home/julian/git/multilingual-qanus/dist/";
	public static final String STANFORD_API_BASE_URL = BASELIBDIR;
	public static String WIKIDIR = "/home/julian/tesis/wiki/";
	public static String INDEXDIR = "/home/julian/tesis/lucene-indexes/";
	public static String QDIR = "/home/julian/tesis/questions/";
	public static String PREGUNTAS_EN = QDIR+"q-en.xml";
	public static String PREGUNTAS_ES = QDIR+"q-es.xml";
	public static String PREGUNTAS_PT = QDIR+"q-pt.xml";
	
	/*Cuantas preguntas itera del total del xml 200 = todas*/
	public static int FROM_QUESTION = 0;
	public static int UP_TO_N_QUESTIONS = 4;
	
	public static String LANG = "en";
	public static String INDEX = "pt-2007"; // "pt-2007"
	public static int LUCENERESULTS = 50; //default at qanus was 50
	public static int N_PASSAGES = 20; //default at quanus was 40
	
	
	public static boolean USE_STANFORD = false;
	
	private static String LANG_YEAR = "";
	
	public static boolean SUPPORT_WIKI = true;
	public static boolean SUPPORT_NIL = true;
	public static boolean SUPPORT_NEWS = true;
	public static boolean QTYPE_FACTOID = true;
	public static boolean QTYPE_LIST = true;
	public static boolean QTYPE_DEFINITION = true;
	public static boolean GROUP_ENTITY_NER_AND_NOUNS = true;
	
	public static String RESPUESTAS_ES = QDIR+"a-es.xml";
	public static String RESPUESTAS_PT = QDIR+"a-pt.xml";
	
	public static boolean EVAL_PASSAGES = false; //false es evaldocs
	
	
	public static int QUERYGENERATION = 2; //1, 2, 3 o 4
	
	public static boolean LOG_CONSOLE_SOLO = true;
	
	public static String DOCS_LOGFILE = "/home/julian/tesis/clef/queries-eval/"+LANG+"_"+LUCENERESULTS+"_"+INDEX+"-"+QUERYGENERATION+".log";

	
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
	
	public static boolean OutputCheckAndEmpty(String l_OutputFolder) {
		File l_TargetFile = new File(l_OutputFolder);
		
		if (!DirectoryAndFileManipulation.CreateDirectoryIfNonExistent(l_TargetFile)) {
			Logger.getLogger(Controller.class.getName()).log(Level.WARNING, "Unable to access the target folder.");
			
			return false;
		}	
		else
		{
			File[] files = l_TargetFile.listFiles();
			if(files.length > 0)
				Logger.getLogger(Controller.class.getName()).log(Level.FINE, "Target folder is not empty. Deleting files...");
			
			boolean l_DeleteOk = true;
			for (int i = 0; i < files.length; i++) l_DeleteOk &= files[i].delete();
				
			
			if(!l_DeleteOk)Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, "Unable to delete some files.");
		}

		return true;
	}
	
	public static boolean FileExists(String l_File) {
		
		File l_TheFile = new File(l_File);
		return l_TheFile.exists();
	}
	
	public static String GetWikipediaFromOption(String option)
	{
			
		String wikiPostfix = "-pages-articles.xml";
		String[] wikipedias = {"simplewiki-20060704", "simplewiki-20130724", "eswiki-20060705", "enwiki-20061104", "ptwiki-20070201" };
		
		String res = "";
		try {
			res = Configuration.WIKIDIR+wikipedias[OptionToIndex(option)]+wikiPostfix;
		}
		catch (NullPointerException e) {
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
		catch (NullPointerException e) {
			Logger.getLogger("QANUS").logp(Level.SEVERE, Configuration.class.getName(), "GetLuceneIndexFromOption", "Your --wiki parameter is incorrectly configured, options are: simple-06, simple-13, es-06, en-06, pt-07");
			System.exit(1);
		}
		
		return res;
		
	}
	
	public static String langFromLangYear(String langYear)
	{
		
		return langYear.substring(0, langYear.length()-3);
	}
	

	public static String GetQuestionFileFromOption(String option) {
		
		if(option.compareToIgnoreCase("pt-07") == 0)
		{
			return PREGUNTAS_PT;
		}
		else
			return PREGUNTAS_ES;
			
			
	}

	public static String GetTargetFile() {
		// TODO Auto-generated method stub
		return "/tmp/ml-qa/";
	}

	public static void setLangYear(String getOptionArgument) {
		// TODO Auto-generated method stub
		LANG_YEAR = getOptionArgument;
	}
	
	public static String getLang()
	{
		return langFromLangYear(LANG_YEAR);
	}
}

