package ar.uba.dc.galli.qa.ml.ar.featurescoring;


import java.util.ArrayList;
import java.util.PriorityQueue;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.upc.freeling.ListSentence;
import edu.upc.freeling.ListWord;


import ar.uba.dc.galli.qa.ml.ar.DocumentScore;
import ar.uba.dc.galli.qa.ml.ar.qasys.Question;
import ar.uba.dc.galli.qa.ml.textprocessing.FreelingAPI;
import ar.uba.dc.galli.qa.ml.utils.Configuration;
import ar.uba.dc.galli.qa.ml.utils.EnumTypes;
import ar.uba.dc.galli.qa.ml.utils.TextEntity;
import ar.uba.dc.galli.qa.ml.utils.comparators.BaseComparator;
import ar.uba.dc.galli.qa.ml.utils.comparators.EqualNoPunctComparator;


/**
 * Responsible for tabulating the scores of candidate passages.
 * 
 * @author NG, Jun Ping -- junping@comp.nus.edu.sg
 * @version v04Jan2010
 */
public class FeatureScorer {

	// Used to hold all the added documents
	private ArrayList<String> m_DocumentStore;


	/**
	 * Constructor
	 */
	public FeatureScorer() {
		m_DocumentStore = new ArrayList<String>();
	} // end constructor

	public int documentStoreSize()
	{
		return m_DocumentStore.size();
	}

	/**
	 * Adds a document to the class for subsequent consideration.
	 * @param a_DocumentString [in] the actual string that make up the document.
	 */
	public void AddDocument(String a_DocumentString) {
		m_DocumentStore.add(a_DocumentString);
	} // end AddDocument()


	/**
	 * Retrieve the top N documents based on the scores assigned to the documents.
	 *
	 * @param a_Query [in] search query used to score the documents against
	 * @param a_NumTopDocs [in] the number of documents to retrieve
	 * @param question 
	 * @return array of strings of the top N documents
	 */
	public String[] RetrieveTopDocuments(String a_Query, int a_NumTopDocs, Question question) {


		// We use a priority queue to quickly retrive top scoring documents
		PriorityQueue<DocumentScore> l_TopDocuments = new PriorityQueue<DocumentScore>();	
		Logger.getLogger("QANUS").log(Level.FINER, "Retrieving [" + a_NumTopDocs + "] documents for query [" + a_Query + "]");


		// Init the features to use - new features can be added here
		FeatureSearchTermFrequency l_Feature_Frequency = new FeatureSearchTermFrequency();
		FeatureSearchTermSpan l_Feature_Proximity = new FeatureSearchTermSpan();
		FeatureSearchTermCoverage l_Feature_Coverage = new FeatureSearchTermCoverage();
		

		// Calculate score for each document
		int l_DocIndex = 0;
		for (String l_Document : m_DocumentStore) {

			double l_DocScore = 0;
		

			String[] l_QueryArray = { a_Query };
			
			// Invoke the various features
			double l_FreqScore = l_Feature_Frequency.GetScore(l_QueryArray, l_Document);
			l_DocScore += (0.05)*l_FreqScore;

			double l_ProxScore = l_Feature_Proximity.GetScore(l_QueryArray, l_Document);
			l_DocScore += (0.05)*l_ProxScore;

			double l_CoverageScore = l_Feature_Coverage.GetScore(l_QueryArray, l_Document);
			l_DocScore += (0.9)*l_CoverageScore;

			Logger.getLogger("QANUS").log(Level.FINER, "F:" + l_FreqScore + "-P:" + l_ProxScore + "-C:" + l_CoverageScore + "-[" + l_Document + "]");
			
			
			double final_score = 0.0, qnouns = 0.0, qners = 0.0, qverb = 0.0, token = 0.0;
			if(Configuration.PASSAGE_RANK == 1) //baseline
			{
				final_score = l_DocScore;
			}
			else
			{
				
				
				if(Configuration.PASSAGE_RANK == 2)
				{
					qnouns = getQNounsScore(l_Document, question);
					qners = getQNERScore(l_Document, question);
					qverb = getQVerbsScore(l_Document, question);
					final_score = 0.4 * l_DocScore + 0.25 * qnouns + 0.2 * qners + 0.15* qverb;
				}
				else if(Configuration.PASSAGE_RANK == 3)
				{
					token = tokenFeature(l_Document);
					//final_score = 0.4 * l_DocScore + 0.15* token + 0.25 * qnouns + 0.1 * qners + 0.1* qverb;
					//System.out.println("ERROR en FeatureScorer.java PASSAGE_RANK not in {1,2,3}");
					final_score = 0.8 * l_DocScore + 0.2* token;// + 0.25 * qnouns + 0.1 * qners + 0.1* qverb;
				}
				else
				{
					
					System.out.println("ERROR en FeatureScorer.java PASSAGE_RANK not in {1,2,3}");
					System.exit(1);
					
				}
			}
			
			//System.out.format("final: %.2f N: %.2f NE: %.2f V: %.2f T: %.2f F: %.2f P: %.2f C: %.2f [%s] %n",final_score, qnouns, qners, qverb,token, l_FreqScore ,l_ProxScore, l_CoverageScore, l_Document);

			
			l_TopDocuments.add(new DocumentScore(l_DocIndex, final_score, l_Document));
			l_DocIndex++;

		} // end String l_Document..
			
		

		// Create a result array to return the top X documents in		
		String[] l_ResultArray = new String[(a_NumTopDocs<l_TopDocuments.size())?a_NumTopDocs:l_TopDocuments.size()];
		for (int i = 0; i < l_ResultArray.length; ++i) {
			l_ResultArray[i] = l_TopDocuments.remove().GetDocText();
		} // end for i

		return l_ResultArray;

		
	} // end RetrieveTopDocuments()
	
	public double getQNounsScore(String passage, Question question)
	{
		FreelingAPI free = FreelingAPI.getInstance();
		
		if(question.getNouns().length == 0) return 0.0;
	
		ListSentence ls = free.process(passage);
		TextEntity[] passage_nouns = free.getNouns(ls);
		BaseComparator comp = new EqualNoPunctComparator();
		
		int noun_presence = 0;
		
		for(TextEntity entity : question.getNouns())
		{
			
			for (int i = 0; i < passage_nouns.length; i++) {
				if(comp.compare(entity.term, passage_nouns[i].term) && comp.compare(entity.subtype, passage_nouns[i].subtype))
				{
					noun_presence++;
				}
			}
			
		}
		
		return  noun_presence * 1.0 / question.getNouns().length *1.0;
	}
	
	public double getQVerbsScore(String passage, Question question)
	{
		FreelingAPI free = FreelingAPI.getInstance();
		
		if(question.getVerbs().length == 0) return 0.0;
	
		ListSentence ls = free.process(passage);
		TextEntity[] passage_verbs = free.getVerbs(ls);
		BaseComparator comp = new EqualNoPunctComparator();
		
		int verb_presence = 0;
		
		for(TextEntity entity : question.getVerbs())
		{
			
			for (int i = 0; i < passage_verbs.length; i++) {
				if(comp.compare(entity.term, passage_verbs[i].term) && comp.compare(entity.subtype, passage_verbs[i].subtype))
				{
					verb_presence++;
				}
			}
			
		}
		
		return  verb_presence * 1.0 / question.getVerbs().length *1.0;
	}
	
	
	public double getQNERScore(String passage, Question question)
	{
		FreelingAPI free = FreelingAPI.getInstance();
		
		if(question.getEntities().length == 0) return 0.0;
	
		ListSentence ls = free.process(passage);
		TextEntity[] passage_entities = free.getEntities(ls);
		BaseComparator comp = new EqualNoPunctComparator();
		
		int entity_presence = 0;
		
		for(TextEntity entity : question.getEntities())
		{
			
			for (int i = 0; i < passage_entities.length; i++) {
				if(comp.compare(entity.term, passage_entities[i].term) && comp.compare(entity.subtype, passage_entities[i].subtype))
				{
					entity_presence++;
				}
			}
			
		}
		
		return  entity_presence * 1.0 / question.getEntities().length *1.0;
	}
	
	
	
	
	public double tokenFeature(String passage)
	{
		int total_tokens = totalTokens(passage);

		if(total_tokens > 4 && total_tokens < 100)
		{
			return 1.0;	
		}

		if(total_tokens > 4 && total_tokens < 200)
		{
			return 0.5;
		}

		return 0.0;


	}
	
	public int totalTokens(String passage)
	{
		FreelingAPI free = FreelingAPI.getInstance();
		ListWord words = free.tokenize(passage);
		return (int)words.size();
	}

	
	

} // end class FeatureScorer
