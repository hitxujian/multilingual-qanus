package ar.uba.dc.galli.qa.ml.ar.featurescoring;


import java.io.File;


import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import ar.uba.dc.galli.qa.ml.ar.AnswerCandidate;
import ar.uba.dc.galli.qa.ml.ar.FreebaseQuerier;
import ar.uba.dc.galli.qa.ml.ar.LuceneInformationBaseQuerier;
import ar.uba.dc.galli.qa.ml.ar.components.*;
import ar.uba.dc.galli.qa.ml.ar.qasys.Question;
import ar.uba.dc.galli.qa.ml.textprocessing.FreelingAPI;
import ar.uba.dc.galli.qa.ml.textprocessing.StanfordAPI;
import ar.uba.dc.galli.qa.ml.utils.Configuration;
import ar.uba.dc.galli.qa.ml.utils.Utils;
import ar.uba.dc.galli.qa.ml.utils.EnumTypes.QuestionSubType;

import sg.edu.nus.wing.qanus.framework.commons.IStrategyModule;
import sg.edu.nus.wing.qanus.framework.commons.DataItem;
import sg.edu.nus.wing.qanus.framework.commons.IAnalyzable;
import sg.edu.nus.wing.qanus.textprocessing.StanfordNER;
import sg.edu.nus.wing.qanus.textprocessing.StanfordPOSTagger;
import sg.edu.nus.wing.qanus.textprocessing.StanfordNERWebService;
import sg.edu.nus.wing.qanus.textprocessing.StopWordsFilter;

import sg.yeefan.searchenginewrapper.*;
import sg.yeefan.searchenginewrapper.clients.*;

/**
 * This is an attempt to improve on the BasicIRBasedStrategy that was the first one to
 * be shipped with QANUS.
 *
 * In this strategy, we will attempt to identify several features, and score
 * candidate passages based on these features. The scores of individual features
 * are summed linearly to identify the best passage.
 * This best passage will then be scoured for the answer string.
 *
 * We also put in improvements to the heuristics used for answer string extraction:
 * 1. Abbreviations (ABBR:exp)
 *	Use of regular expressions to identify expansions
 * 2. Locations (LOC:*)
 *	Use of NER information to identify locations from best ranked sentence
 * 3. Individual people (HUM:ind)
 *	Locate proper nouns from best ranked sentence
 *	Score each candidate based on features such as its proximity with terms in the query
 *  Two special sub types of HUM:ind questions have been identified :
 *    Type 1 - Who is the CEO [of ABC]?
 *    Type 2 - Who supervised the transplant?
 *  They are identified based on how these questions are expected to be answered. For these
 *  types of questions, the query used is a bit different from the rest of the questions.
 *  The "subject" of the question (such as CEO of ABC company) is identified and used as the query
 *  instead.
 * 4. Dates (NUM:date)
 *	For questions that look for years, a regular expression is used to pick out possible years
 *  Otherwise the date of the newspaper article from which the candidate passage is retrieved from is used
 * 5. Counts (NUM:count)
 *	The "subject" of the question (for example "miners" in "How many miners...") is retrieved.
 *  Numerals are retrieved from various candidate passages and scored based on their proximity to the
 *  occurence of the "subject" within the candidate passages. 
 * 6. Groups (HUM:gr)
 *  Proper nouns are identified from the candidate passages.
 *  These candidates are scored based on a few features, including the proximity of the noun
 *  and occurences of the question target within the candidate passages.
 *
 *  The code in this file is long, un-wieldy and not the best example of how code should be written.
 * But keeping the if-elses in the same code body makes it easier to read(?)....
 *
 *  This hopefully can also serve as a (somewhat negative) example of how new answer retrieval
 * strategies can be added to QANUS.
 *  A new class implementing IStrategyModule is all that is needed.
 *
 *
 * @author NG, Jun Ping -- junping@comp.nus.edu.sg
 * @version v04Jan2010
 */
public class FeatureScoringStrategy implements IStrategyModule, IAnalyzable {

	// Retrieve 100 top documents from search engine for each search
	private final int RESULTS_TO_RETRIEVE = Configuration.LUCENERESULTS; // was 100

	// The Lucene engine
	private LuceneInformationBaseQuerier m_InformationBase;

	// Temporary annotation modules
	// While we are still building this into our Lucene index
	//private StanfordPOSTagger m_ModulePOS;
	//private StanfordNER m_ModuleNER; // TODO remove after the information is included into Lucene index
	//private StanfordNERWebService m_ModuleNER; // TODO remove after the information is included into Lucene index

	//private FreelingAPI m_free;
	// Used to query Freebase to make sure some answers are "sane".
	// I mean, we want to be sure we return a country when asked for a country.
	private FreebaseQuerier m_FBQ;


	// Specially identified types of question sub types, detracting from the taxonomy of
	// (Li and Roth 2002)
	


	public FeatureScoringStrategy(File a_IBFolder) {

		// Initialise required components
		m_InformationBase = new LuceneInformationBaseQuerier(a_IBFolder, RESULTS_TO_RETRIEVE);

		// Processing modules
		//m_ModulePOS =stan.pos; 
		//m_ModuleNER =stan.ner; 
		//m_ModuleNER = new StanfordNERWebService();
		
		// Validation modules
		m_FBQ = new FreebaseQuerier(Configuration.BASELIBDIR+"choppingboard" + File.separator + "temp" + File.separator + "freebase-cache");

	}

	public String GetModuleID() {
		return "FeatureScoringStrategy";
	}

	
	public DataItem GetAnalysisInfoForQuestion(Question a_QuestionItem) {
		return GetAnswerForQuestion(a_QuestionItem, true);
	} // end GetAnalysisInfoForQuestion()


	/**
	 * Retrieves an answer for the provided question.
	 * @param question [in] structure containing question and annotations
	 * @return Answer to the question
	 */
	public DataItem GetAnswerForQuestion(Question question) {
		return GetAnswerForQuestion(question, false);
	} // end GetAnswerForQuestion()


	/**
	 * Retrieves an answer for the provided question.
	 * @param question [in] structure containing question and annotations
	 * @return Answer to the question
	 */
	public DataItem GetAnswerForQuestion(Question question, boolean a_Analysis) {

		// Retrieve question and annotations
		
		String l_QuestionType = question.getQType();
		if (l_QuestionType.compareToIgnoreCase("FACTOID") != 0) {
			System.out.println("Non factoid");
			return null; // TODO list questions?
			// Plans are to support list questions soon
		}

		String l_QuestionID = question.getId();
		String l_QuestionTarget = question.getTarget();

		String 	l_ExpectedAnswerType =question.qc_all;
		// Retreive actual question string
		
		String l_QuestionText =  question.getQuestionEn();
			
		// Retrieve POS annotation
		String l_QuestionPOS = question.l_annotatedPOS[0];
		

		// If analysis is required, prepare the return items
		DataItem l_AnalysisResults = new DataItem("Analysis");
		l_AnalysisResults.AddAttribute("QID", l_QuestionID);



		// Query to use depends on question type
		// First identify question type and subtype
		String l_Query = null;
		
		QuestionSubType l_SubType = MLBaselineQueryGenerator.GetQuestionSubType(l_QuestionText, l_QuestionPOS, l_ExpectedAnswerType);
		l_Query = MLBaselineQueryGenerator.generateQuery(l_QuestionTarget, l_QuestionText, l_QuestionPOS, l_ExpectedAnswerType, question);
		
		
		question.print();
		System.out.println("Fin de la generacion de queries:"+l_Query);
		
		ScoreDoc[] l_RetrievedDocs = null;
		

		// Retrieve documents based on the search string from the search engine
		l_RetrievedDocs = (ScoreDoc[]) m_InformationBase.SearchQuery(l_Query);
		
	

		if (l_RetrievedDocs == null)
		{
			Logger.getLogger("QANUS").logp(Level.WARNING, FeatureScoringStrategy.class.getName(), "GetAnswerForQuestion", "General exception");
			System.exit(1);
		}

		/*System.out.format("Se obtuvieron %d documento ejecutando la query '%s' %n", l_RetrievedDocs.length, l_Query); 
		for (int i = 0; i < l_RetrievedDocs.length; i++) {
			ScoreDoc doc = l_RetrievedDocs[i];
			System.out.format("Doc %d: %s %n", i, m_InformationBase.GetDoc(doc.doc).get("TITLE"));
		}*/
		
		String[] l_BestSentence = BaselinePassageExtractor.extractPassages(l_Query, l_RetrievedDocs, m_InformationBase, a_Analysis, l_AnalysisResults);
		// If analysis is to be performed, we track the sentences that are retrieved
		if (a_Analysis) {
			for (String l_Sentence : l_BestSentence) {
				l_AnalysisResults.AddField("Stage2", l_Sentence);
			}
		}
		System.out.format("Se obtuvieron %d oraciones con BaselinePassageExtractor %n", l_BestSentence.length);
		//if(true)return null;
		
		//System.out.println("Comenzando heuristicas");
		MLBaselineARHeuristic ar = new MLBaselineARHeuristic( m_FBQ, m_InformationBase);
		DataItem res = ar.execute(question, l_BestSentence, l_ExpectedAnswerType, question.toDataItem(), a_Analysis, l_AnalysisResults, l_QuestionTarget, l_SubType, l_QuestionText, l_QuestionPOS, l_Query, l_RetrievedDocs, l_QuestionID);
		System.out.println("Respuesta: "+res.GetAttribute("answer"));
		return res;
		// Pattern-based answer extraction
		// Use expected question answer type, try to see if we can find a similar type in best sentence
		

	} // end GetAnswerForQuestion()

	
	/**
	 * Retrieve the humber of hits when we send the query to a search engine.
	 *
	 * Un used because making use of the count of hits from the web doesn't seem useful.
	 * Keeping this around for reference, in case later this proves useful.
	 *
	 * @param a_Query [in] the query to use
	 * @return the number of hits as reported by the search engine, or 0 on errors
	 */
	private long GetNumberOfHits(String a_Query) {

		SearchEngineClient l_SearchEngine = new YahooClient();
		l_SearchEngine.setLabel("GetNumberOfHits-" + Math.random());
		l_SearchEngine.setQuery(a_Query);
		l_SearchEngine.setStartIndex(1);
		l_SearchEngine.setNumResults(1);
		SearchEngineResults l_SearchResults = null;
		try {
			l_SearchResults = l_SearchEngine.getResults();
		} catch (SearchEngineException e) {
			Logger.getLogger("QANUS").logp(Level.WARNING, FeatureScoringStrategy.class.getName(), "GetNumberOfHits", "Error doing search [" + a_Query + "]");
			return 0;
		}

		return l_SearchResults.getTotalResults();

	} // end GetNumberOfHits();

	@Override
	public DataItem GetAnalysisInfoForQuestion(DataItem a_QuestionItem) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataItem GetAnswerForQuestion(DataItem a_QuestionItem) {
		// TODO Auto-generated method stub
		return null;
	}



	




	
	
} // end class FeatureScoringStrategy

