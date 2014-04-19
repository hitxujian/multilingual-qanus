package ar.uba.dc.galli.qa.ml.textprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.*;
import java.util.StringTokenizer;

import ar.uba.dc.galli.qa.ml.utils.Configuration;
import ar.uba.dc.galli.qa.ml.utils.EnumTypes;
import ar.uba.dc.galli.qa.ml.utils.TextEntity;
import ar.uba.dc.galli.qa.ml.utils.Timer;

import edu.upc.freeling.ListWord;
import edu.upc.freeling.Util;
import edu.upc.freeling.Word;

import sg.edu.nus.wing.qanus.textprocessing.PorterStemmer;
import sg.edu.nus.wing.qanus.textprocessing.QuestionClassifierWithStanfordClassifier;
import sg.edu.nus.wing.qanus.textprocessing.StanfordGrammarParser;
import sg.edu.nus.wing.qanus.textprocessing.StanfordNER;
import sg.edu.nus.wing.qanus.textprocessing.StanfordPOSTagger;


/**
 * Interfaz para los distintos analizadores de Stanford
 * @author julian
 *
 */
public class StanfordAPI {

	public StanfordPOSTagger pos;
	public StanfordNER ner;
	public QuestionClassifierWithStanfordClassifier qc;
//	public PorterStemmer ps;
//	public StanfordGrammarParser gr;

	
	private static final boolean VERBOSE_LOAD = true;
	
	private static StanfordAPI instance;
	
	public static StanfordAPI getInstance() 
	{		
		if(instance == null)
		{
			System.loadLibrary( "freeling_javaAPI" );
			Util.initLocale( "default" );
			instance = new StanfordAPI();
		}
		return instance;

	}
	
	public StanfordAPI() {
	
		   	
			System.out.print("StanfordNER...");
			ner = new StanfordNER(Configuration.BASELIBDIR+"lib" + File.separator + "ner-eng-ie.crf-4-conll-distsim.ser.gz");
			Timer timer = new Timer();
			
			pos = new StanfordPOSTagger(Configuration.BASELIBDIR+"lib" + File.separator + "bidirectional-wsj-0-18.tagger");
			//if(VERBOSE_LOAD)System.out.println("["+timer.tic()+"secs ]");
			//if(VERBOSE_LOAD)System.out.print("StanfordQC...");
			if(VERBOSE_LOAD)System.out.print("Stanford  QC...");
			loadQC();
			if(VERBOSE_LOAD)System.out.println(" [ "+timer.tic()+"secs ] Ok!");
	       //ps = new PorterStemmer();
	       
	       //gr = new StanfordGrammarParser(BASE_URL+"lib"+ File.separator+ "stanford-parser-2008-10-26.jar");
	        //Thesaurus thesaurus = new Thesaurus();
	       
	}
	
	public void loadQC()
	{
	   try{
		   qc = new QuestionClassifierWithStanfordClassifier(Configuration.BASELIBDIR+"lib"+ File.separator +"trec_classifier.stanford-classifier", "choppingboard" + File.separator + "temp");
		  
	    }catch (Exception ex) {System.out.println("Error al iniciar QC:" + ex);}
	}
	

	public String qc(String input) throws FileNotFoundException
	{
		PrintStream stderr = System.err;
		File file = new File("qc-loading.log");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setErr(ps);
		
		String[] param = {input};
		String[] aux = qc.ProcessText(param);
		
		
		/*qc_res = aux[0];
		qc_class = EnumTypes.val(this.getQcClass(false));
		qc_subclass = EnumTypes.val(this.getQcClass(true));
		//qc_confidence = aux[1];
		qc_confidence = "1.0";
		*/
		
		
		System.setErr(stderr);
		
		return aux[0];
		
		
	}
	
    public TextEntity setQcType(TextEntity question_word, String posAnnotatedSentence)
    {
    	 
    	EnumTypes asked_entity;
    	boolean which = question_word.term.compareToIgnoreCase("which") == 0 || question_word.matched_str.compareToIgnoreCase("WDT") == 0;
    	boolean where = question_word.term.compareToIgnoreCase("where") == 0 || question_word.matched_str.compareToIgnoreCase("WRB") == 0;
    	boolean who = question_word.term.compareToIgnoreCase("who") == 0 || question_word.matched_str.compareToIgnoreCase("WP") == 0;
    	boolean whom = question_word.term.compareToIgnoreCase("whom") == 0 || (question_word.matched_str.compareToIgnoreCase("WP") == 0 && wordPOSExists("IN", posAnnotatedSentence));
    	
        if (where) asked_entity = EnumTypes.val("WHERE");
        else if (who && !whom) asked_entity = EnumTypes.val("WHO");
        else if (whom) asked_entity = EnumTypes.val("WHOM");
        else if (which) asked_entity = EnumTypes.val("WHICH");
        else
        {
        	asked_entity = EnumTypes.val("NOVALUE");
        }
      
        return new TextEntity(question_word.term, question_word.tag, question_word.subtype, question_word.matched_str, question_word.comparator_used, asked_entity);

    	
    	
    	
    }
	   
    public boolean wordPOSExists(String type, String posAnnotatedSentence)
    {
    
    	StringTokenizer tokens = new StringTokenizer(posAnnotatedSentence);
        
		 while (tokens.hasMoreTokens()) 
		 {
			String word = tokens.nextToken();
			int delimPos = word.indexOf('/');
			if (delimPos != -1) 
			{
				try 
				{
					String posTag = word.substring(delimPos+1);
					if (posTag.substring(0,2).compareToIgnoreCase(type) == 0) return true;
						} catch (Exception ex) {
							continue;
						}
				}
	 	  }
		 
		  return false;
			
    }
    //Parte un string qc_class:qc_subclass
    public String getQcClass(String qc_all)
    {
    	return qc_all.substring(0, qc_all.indexOf(":"));
    }
    
    public String getQcSubclass(String qc_all)
    {
    	return qc_all.substring( qc_all.indexOf(":")+1, qc_all.length());
    }
	
	
	public String[] getEntities(String type, String nerAnnotatedSentence)
    {

        ArrayList<String> res = new ArrayList<String>();
        
        StringTokenizer tokens = new StringTokenizer(nerAnnotatedSentence);
        
        String last_ner = "";
        String entity = "";
        String mega_entity = "";
        String word;
        String ner_tag;
        String word_alone;
        int delim_ner;
        
		while (tokens.hasMoreTokens()) 
        {
			word = tokens.nextToken();
            
			delim_ner = word.indexOf('/');

			if (delim_ner != -1) 
            {
				
			  try
              {
              
              	ner_tag = word.substring(delim_ner+1);
                word_alone = word.substring(0, delim_ner);
		              
                //La palabra es del tipo que queremos (por ejemplo: PERSON)
                if(ner_tag.length() >= type.length() && ner_tag.substring(0,type.length()).compareToIgnoreCase(type) == 0)
                {

                  if(last_ner.compareToIgnoreCase(type) == 0)//Venimos de otra palabra del tipo (PERSON...)
                  {
                     entity +=" "+word_alone;
                  
                  }
                  else if(last_ner.compareToIgnoreCase("") == 0) //No venimos de nui
                  {
                      //System.out.println("Vengo de null");
                      entity = word_alone;
                      last_ner = type;
                  }
                  else
                  {
                      //System.out.println("Vengo de "+ lastNer);
                      res.add( entity);
                      last_ner=type;
                      entity = word_alone;
                      
                  }
									
                }
                else 
                {
                    //System.out.println("no matchee");
                    if(last_ner.compareToIgnoreCase("") != 0)
                    {
                          //System.out.println("Vengo de "+ lastNer);
                          res.add( entity);
                    }                    
                    last_ner = "";
                    entity = "";
                }
                
                
					} catch (Exception ex) {
                    System.out.println("excepcion: "+ex);
					continue;
					}
				}
            
         }
        
        if(last_ner.compareToIgnoreCase("") != 0)
        {
            res.add(entity);
        }                     
         
        return res.toArray(new String[0]);

    }
	
	public TextEntity[] getEntities(String nerAnnotatedSentence)
	{
		String[] persons = getEntities("PERSON", nerAnnotatedSentence);
		String[] locations = getEntities("LOCATION", nerAnnotatedSentence);
	    String[] organizations = getEntities("ORGANIZATION", nerAnnotatedSentence);
		
	    LinkedList<TextEntity> res = new LinkedList<TextEntity>();
	    
	    for(String person: persons)res.add(new TextEntity(person, "PERSON", "StanfordAPI"));
	    for(String location: locations)res.add(new TextEntity(location, "LOCATION","StanfordAPI"));
	    for(String organization: organizations)res.add(new TextEntity(organization, "ORGANIZATION","StanfordAPI"));
	    
	    return res.toArray(new TextEntity[0]);
		
	}
	
	public TextEntity[] getVerbs(String posAnnotatedSentence)
    {
	    ArrayList<TextEntity> res = new ArrayList<TextEntity>();
        StringTokenizer tokens = new StringTokenizer(posAnnotatedSentence);
        int delim_pos;
        String word;
        String pos_tag;
		while (tokens.hasMoreTokens()) 
        {
			word = tokens.nextToken();
			delim_pos = word.indexOf('/');
            
			if (delim_pos != -1) 
            {
			try 
              {
				pos_tag = word.substring(delim_pos+1);
                
				if(pos_tag.substring(0,2).compareToIgnoreCase("VB") == 0) 
                { 
					// Match all VB tags like VB VBZ VBG VBD
					res.add( new TextEntity(word.substring(0, delim_pos), "VERB", null, null, pos_tag));
				}
			 } catch (Exception ex) {continue;}
		    }
         }

        return res.toArray(new TextEntity[0]);
    }
	
	
	public TextEntity[] getNouns(String posAnnotatedSentence)
    {
	    ArrayList<TextEntity> res = new ArrayList<TextEntity>();
        StringTokenizer tokens = new StringTokenizer(posAnnotatedSentence);
        int delim_pos;
        String word;
        String pos_tag;
		while (tokens.hasMoreTokens()) 
        {
			word = tokens.nextToken();
			delim_pos = word.indexOf('/');
            
			if (delim_pos != -1) 
            {
			try 
              {
				pos_tag = word.substring(delim_pos+1);
                
				if(pos_tag.substring(0,2).compareToIgnoreCase("NN") == 0) 
                { 
                	// Match all NN tags like NNS NNP NN NNPS
					res.add( new TextEntity(word.substring(0, delim_pos), pos_tag));
				}
			 } catch (Exception ex) {continue;}
		    }
         }

        return res.toArray(new TextEntity[0]);
    }
	
	
    public TextEntity[] getQWords(String posAnnotatedSentence)
    {
    
		ArrayList<TextEntity> res = new ArrayList<TextEntity>();
        
        StringTokenizer tokens = new StringTokenizer(posAnnotatedSentence);
        
		while (tokens.hasMoreTokens()) 
        {
			String word = tokens.nextToken();
            //System.out.println("Word: " + word);
			int delimPos = word.indexOf('/');
            
			if (delimPos != -1) 
			{
				try 
				{
					String posTag = word.substring(delimPos+1);
					if (posTag.substring(0,1).compareToIgnoreCase("W") == 0) 
					{
						res.add(setQcType(new TextEntity((word.substring(0, delimPos)), "QWord", "StanfordPOS", posTag.substring(0,2), "en"), posAnnotatedSentence));
					}
				} catch (Exception ex) 
				{
					continue;
				}
			}
         }

        return res.toArray(new TextEntity[0]);
    }
	

	public boolean isEntity(String str, String nerAnnotatedSentence)
	{
		boolean res = false;
		for(TextEntity e : getEntities(nerAnnotatedSentence))
		{
			if(e.term.compareToIgnoreCase(str) == 0)
			{
				res = true;
			}
		}
		return res;
	}

	public ListWord tokenize(String input) {
		ListWord res = new ListWord();
		StringTokenizer tokens = new StringTokenizer(input);
		while (tokens.hasMoreTokens()) 
	    {
			res.pushBack(new Word(tokens.nextToken()));
	    }
		return res;
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		StanfordAPI api = StanfordAPI.getInstance();
		String[] some_questions = {"What's his name?", "Where do you come from?", "What's your phone number?", "How old are you?", "When were you born?",
									"What does he look like?", "In what school does Harry Potter study?"};
		
		for (int i = 0; i < some_questions.length; i++) {
			String res = api.qc(some_questions[i]);	
			System.out.format("%s & %s \\\\ \\hline \n", some_questions[i], res);
		}
		
		//System.out.println(api.qc_res);
		//System.out.println(api.qc_confidence);

	}

	public TextEntity[] getAdjectives(String posAnnotatedSentence) {
	  ArrayList<TextEntity> res = new ArrayList<TextEntity>();
        StringTokenizer tokens = new StringTokenizer(posAnnotatedSentence);
        int delim_pos;
        String word;
        String pos_tag;
		while (tokens.hasMoreTokens()) 
        {
			word = tokens.nextToken();
			delim_pos = word.indexOf('/');
            
			if (delim_pos != -1) 
            {
			try 
              {
				pos_tag = word.substring(delim_pos+1);
                
				if(pos_tag.substring(0,2).compareToIgnoreCase("JJ") == 0) 
                { 
                	// Match all NN tags like NNS NNP NN NNPS
					res.add( new TextEntity(word.substring(0, delim_pos), pos_tag));
				}
			 } catch (Exception ex) {continue;}
		    }
         }

        return res.toArray(new TextEntity[0]);
	}
	
}
