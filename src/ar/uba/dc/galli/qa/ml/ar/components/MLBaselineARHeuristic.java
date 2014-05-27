package ar.uba.dc.galli.qa.ml.ar.components;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import edu.upc.freeling.ListSentence;

import sg.edu.nus.wing.qanus.framework.commons.DataItem;
import sg.edu.nus.wing.qanus.textprocessing.StanfordNER;
import sg.edu.nus.wing.qanus.textprocessing.StanfordPOSTagger;
import sg.edu.nus.wing.qanus.textprocessing.StopWordsFilter;
import ar.uba.dc.galli.qa.ml.ar.AnswerCandidate;
import ar.uba.dc.galli.qa.ml.ar.FreebaseQuerier;
import ar.uba.dc.galli.qa.ml.ar.LuceneInformationBaseQuerier;
import ar.uba.dc.galli.qa.ml.ar.featurescoring.FeatureSearchTermCoverage;
import ar.uba.dc.galli.qa.ml.ar.featurescoring.FeatureSearchTermProximity;
import ar.uba.dc.galli.qa.ml.ar.qasys.Question;
import ar.uba.dc.galli.qa.ml.textprocessing.FreelingAPI;
import ar.uba.dc.galli.qa.ml.textprocessing.StanfordAPI;
import ar.uba.dc.galli.qa.ml.utils.Configuration;
import ar.uba.dc.galli.qa.ml.utils.EnumTypes;
import ar.uba.dc.galli.qa.ml.utils.EnumTypes.QuestionSubType;
import ar.uba.dc.galli.qa.ml.utils.TextEntity;

public class MLBaselineARHeuristic {

	//private StanfordPOSTagger m_ModulePOS;
	//private StanfordNER m_ModuleNER; // TODO remove after the information is included into Lucene index
	//private StanfordNERWebService m_ModuleNER; // TODO remove after the information is included into Lucene index

	// Used to query Freebase to make sure some answers are "sane".
	// I mean, we want to be sure we return a country when asked for a country.
	private FreebaseQuerier m_FBQ;
	private LuceneInformationBaseQuerier m_InformationBase;
	
	public MLBaselineARHeuristic( FreebaseQuerier in_FBQ, LuceneInformationBaseQuerier in_InformationBase)
	{

		m_FBQ = in_FBQ;
		m_InformationBase = in_InformationBase;
		
	}
	public DataItem execute(Question question, String[] l_BestSentence, String l_ExpectedAnswerType, DataItem a_QuestionItem, boolean a_Analysis, DataItem l_AnalysisResults, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, String l_Query, ScoreDoc[] l_RetrievedDocs, String l_QuestionID) 
	{
		
		// Get POS annotations for ranked sentences
		// This is currently being built into the Lucene index. It will take some time. Once
		// this is done we can just retrieve the annotations from Lucene - cutting down on
		// run-time computation requirements.
		//System.out.println("Cant sentences: "+l_BestSentence.length);
		String[] l_POSTaggedBestSentence = StanfordAPI.getInstance().pos.ProcessText(l_BestSentence);
	
		// Variable used to hold the extracted answer (eventually) and the passage from which
		// it is extracted
		String l_Answer = "";
		String l_OriginalAnswerString = "";
		// -----
		DataItem response;
		response = switchCases( question, l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query, l_QuestionID);
		
		l_Answer = response.GetAttribute("answer");
		l_OriginalAnswerString = response.GetAttribute("original_string");
		l_AnalysisResults = response.GetFieldValues("analysis_results")[0];
		
		// Final post-processing
		// 1. Remove punctuation from the end of answers which invariably get extracted too
		if (l_Answer.length() > 0) {
			if (l_Answer.charAt(l_Answer.length() - 1) == '.'
					|| l_Answer.charAt(l_Answer.length() - 1) == ','
					|| l_Answer.charAt(l_Answer.length() - 1) == ':'
					|| l_Answer.charAt(l_Answer.length() - 1) == '"'
					|| l_Answer.charAt(l_Answer.length() - 1) == '?'
					|| l_Answer.charAt(l_Answer.length() - 1) == '\'') {
				l_Answer = l_Answer.substring(0, l_Answer.length() - 1);
			}
		}
		// more punctionation cleansing
		if (l_Answer.length() > 1) {
			if (l_Answer.substring(l_Answer.length() - 2, l_Answer.length()).compareToIgnoreCase("'s") == 0) {
				l_Answer = l_Answer.substring(0, l_Answer.length() - 2);
			}
		}
		if (l_Answer.startsWith("``")) {
			l_Answer = l_Answer.substring(2);
		}
	
	
		// No answer found =(
		if (l_Answer.length() == 0) {
			l_Answer = "NA";
		}
	
		//System.out.println("Respuesta:"+l_Answer);
	
		// Build the data item to return as result of this function
		DataItem l_Result = new DataItem("Result");
		l_Result.AddAttribute("QID", l_QuestionID);
		l_Result.AddField("Answer", l_Answer);
		l_Result.AddField("QuestionType", l_ExpectedAnswerType);
		l_Result.AddField("AnswerString", l_OriginalAnswerString);
	
	
		// Before returning, save the results we have queries from freebase for this question
		m_FBQ.SaveCache();
	
	
		// Return either analysis results of answer to question
		if (a_Analysis) {
			return l_AnalysisResults;
		} else {
			return l_Result;
		}
	}
	
	
	private DataItem switchCases(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{


		DataItem result = new DataItem("response");
		//System.out.println(l_ExpectedAnswerType);
		// Start of pattern based answer extraction - based on the identified expected
		// answer types of the questions we are handling
		if (  l_ExpectedAnswerType.compareToIgnoreCase("ABBR:exp") == 0) {
			
			result = abbrExpCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);
			
		} else if ( l_ExpectedAnswerType.compareToIgnoreCase("ABBR:abb") == 0) {

			// Abbreviations : Contractions -> What can we do?
			
		} else if ( (l_ExpectedAnswerType.length() >= 6
				&& l_ExpectedAnswerType.substring(0, 6).compareToIgnoreCase("HUM:gr") == 0)
				|| (l_ExpectedAnswerType.length() >= 11
				&& l_ExpectedAnswerType.substring(0, 11).compareToIgnoreCase("ENTY:cremat") == 0)) {

				result = humGrOrEntyCrematCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);
			

		} else if (l_ExpectedAnswerType.length() >= 7
				&& l_ExpectedAnswerType.substring(0, 7).compareTo("HUM:ind") == 0) {
		
			result = humIndCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);



		} else if (l_ExpectedAnswerType.length() >= 4
				&& l_ExpectedAnswerType.substring(0, 4).compareTo("HUM:") == 0) {
			
			result = humGeneralCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);


		} else if ( l_ExpectedAnswerType.length() >= 4
				&& l_ExpectedAnswerType.substring(0, 4).compareTo("LOC:") == 0) {

			result = locCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);
			
		} else if (  l_ExpectedAnswerType.length() >= 8
				&& l_ExpectedAnswerType.substring(0, 8).compareToIgnoreCase("NUM:date") == 0) {

			result = numDateCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);

		} else if ( l_ExpectedAnswerType.compareToIgnoreCase("NUM:period") == 0) {

			result = numPeriodCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);


		} else if (l_ExpectedAnswerType.compareToIgnoreCase("NUM:count") == 0) {

			result = numCountCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);


		} else if ( l_ExpectedAnswerType.length() >= 4
				&& l_ExpectedAnswerType.substring(0, 4).compareTo("NUM:") == 0) {

			result = numGeneralCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);


		} else if ( l_ExpectedAnswerType.substring(0, 5).compareTo("ENTY:") == 0) {

			result = entyGeneralCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);			

		} else {

			result = generalCase(question,  l_ExpectedAnswerType,  a_QuestionItem,  l_BestSentence,  a_Analysis,  l_AnalysisResults,  l_Answer,  l_OriginalAnswerString,  l_POSTaggedBestSentence,  l_QuestionTarget,  l_SubType,  l_QuestionText,  l_QuestionPOS,  l_RetrievedDocs,  l_Query,  l_QuestionID);		
		} // end if
		
	
		return result;
	
	}
	
	
	private DataItem abbrExpCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
		// Abbreviation - Expansions

		// Identify the abbreviation we want to expand.
		// Typically this should have been labeled as an "ORGANISATION" by the NER.
		

		String[] l_Candidates = question.getOrganizationNers(true);

		// l_Candidates now contain the identified possible abbreviations within the question


		// Look for Capital letters denoting the abbreviation by constructing
		// a new reg ex string
		LinkedList<String> l_Answers = new LinkedList<String>();
		LinkedList<String> l_OriginalAnswerStrings = new LinkedList<String>();
		
		for (String l_Candidate : l_Candidates)
		{
			
			String l_Regex = "";
			for (int i = 0; i < l_Candidate.length(); ++i) {
				if (Character.isLetter(l_Candidate.charAt(i))) {
					// We expect the answer to be something like
					// D... A... R.... etc for the abbreviation DAR...
					l_Regex += "[" + Character.toUpperCase(l_Candidate.charAt(i)) + "]+[A-Za-z0-9]+ ";
				} else if (l_Candidate.charAt(i) == '/') {
					break;
				}
			}
			
			l_Regex = l_Regex.trim();
			Pattern l_Pattern = Pattern.compile(l_Regex);
			
			for (int i = 0; i < l_BestSentence.length; ++i) {
				// Find possible matches within top N passages
				Matcher l_Matcher = l_Pattern.matcher(l_BestSentence[i]);
				if (l_Matcher.find()) {
					l_Answers.add(l_BestSentence[i].substring(l_Matcher.start(), l_Matcher.end()));
					l_OriginalAnswerStrings.add(l_BestSentence[i]);
				}
			}
		} // end for


		// If analysis is to be performed, we track the sentences that are retrieved
		if (a_Analysis) {
			for (String l_Candidate : l_Answers) {
				l_AnalysisResults.AddField("Stage3", l_Candidate);
			}
		}


		// Return the first answer candidate we have amongst all the candidates
		if (l_Answers.size() > 0) {
			l_Answer = l_Answers.getFirst();
			l_OriginalAnswerString = l_OriginalAnswerStrings.getFirst();
		}
		
		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem humGrOrEntyCrematCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{

		// HUM:gr - Human groups (companies, organizations)
		// ENTY:cremat - Entities

		// Now we try to extract candidate answers --- these would be the proper nouns (NNP)
		// We extract them from all the ranked sentences
		// We assume consecutive NNPs are part of the same NNP.
		// i.e. Tiger/NNP Woods/NNP form one NNP
		LinkedList<String> l_CandidateAnswers = new LinkedList<String>();
		LinkedList<String> l_AnswerSources = new LinkedList<String>();
	
		for(String sentence: l_BestSentence)
		{
			FreelingAPI free = FreelingAPI.getInstance();
			ListSentence ls = free.process(sentence);
			String[] free_entities = free.getEntitiesStr(ls);
			for(String l_CandidateAnswer: free_entities)
			{
				if (l_CandidateAnswer.length() > 0) {
		
					l_CandidateAnswer = l_CandidateAnswer.trim();
					Matcher l_EndingPuncMatcher = Pattern.compile("[\\.,\\?\\!']$").matcher(l_CandidateAnswer);
					if (l_EndingPuncMatcher.find()) {
						l_CandidateAnswer = l_EndingPuncMatcher.replaceAll("");
					}
					if (IsReasonableAnswerForHUMGR(l_CandidateAnswer)) {
						l_CandidateAnswers.add(l_CandidateAnswer);
						l_AnswerSources.add(sentence);
					}
					l_CandidateAnswer = "";
				}
			}
			
		}
		
		// Score each candidate answer relative to the subject and question target
		// We re-use these modules found in the FeatureScorer to help us compute coverage and proximity scores
		FeatureSearchTermCoverage l_FS_Coverage = new FeatureSearchTermCoverage();   
		FeatureSearchTermProximity l_FS_Proximity = new FeatureSearchTermProximity();

		// Look for highest scoring candidate answer
		double l_BestScore = Double.NEGATIVE_INFINITY;
		int l_CurrIndex = 0;
		int l_NumCandidates = l_CandidateAnswers.size();
		
		for (String l_Candidate : l_CandidateAnswers) {
		
			//System.out.println(l_Candidate);
			// Retrieve the sentence where this answer came from
			String l_SourceString = l_AnswerSources.get(l_CurrIndex);


			// Scoring based on different features
			// ---------------------------

			// Proximity score - between target and candidate
			// How near the target and candidate answer appear to each other within the source passage
			String[] l_TargetProximityStrings = {l_QuestionTarget, l_Candidate};
			double l_TargetProximityScore = l_FS_Proximity.GetScore(l_TargetProximityStrings, l_SourceString);

			// Coverage score of target within passage
			// How many words of the target appear within the passage
			String[] l_CoverageStrings = {l_QuestionTarget};
			double l_CoverageScore = l_FS_Coverage.GetScore(l_CoverageStrings, l_SourceString);

			// Penalise if the answer candidate consists of repeated words compared to the question target
			// So we multiply a -1 into the score
			String[] l_CandidateStrings = {l_Candidate};
			double l_RepeatedTermScore = -1 * l_FS_Coverage.GetScore(l_CandidateStrings, l_QuestionTarget);

			// Score derived from rank of source passage
			// This is not very ideal, because NNPs from the same source passage will get a different score.
			// But this is a simple implementation
			double l_SentenceScore = (double) (l_NumCandidates - l_CurrIndex) / l_NumCandidates;

			// Tally the score
			// Weights manually set. We could use supervised learning and learn these values.
			// But these values seem to work fine.
			double l_TotalScore = 
					(0.55 * l_CoverageScore)
					+ (0.2 * l_SentenceScore)
					+ (0.1 * l_TargetProximityScore)
					+ (0.15 * l_RepeatedTermScore);


			if (l_TotalScore > l_BestScore) {
				l_BestScore = l_TotalScore;
				l_Answer = l_Candidate;
				l_OriginalAnswerString = l_SourceString;
			}

			l_CurrIndex++;

		} // end for


		// If analysis is to be performed, we track the sentences that are retrieved
		if (a_Analysis) {
			for (String l_Candidate : l_CandidateAnswers) {
				l_AnalysisResults.AddField("Stage3", l_Candidate);
			}
		}
		
		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem humIndCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
		// HUM:ind - Humans - individuals
		

		// We make use of some of the functions within these classes to process the text
		FeatureSearchTermProximity l_FS_Proximity = new FeatureSearchTermProximity();
		FeatureSearchTermCoverage l_FS_Coverage = new FeatureSearchTermCoverage();

		// Get the subject, could be used subsequently to score sentences if needed
		// The subject is only applicable to identified TYPE1 and TYPE2 sentences.
		String l_Subject = "";
		
		//TODO: definir que hacer
		/*
		 
			l_Subject = BaselineQueryGenerator.GetSubjectOfHumIndQuestion(l_QuestionText, l_QuestionPOS, l_SubType);
		}
		*/

		// Extract proper nouns from all ranked sentences
		LinkedList<AnswerCandidate> l_CandidateAnswers = GetProperNounsOfPersons(l_BestSentence, l_POSTaggedBestSentence);

		// Choose the highest scoring candidate from all the candidates
		double l_BestScore = Double.NEGATIVE_INFINITY;
		
		int l_NumCandidates = l_CandidateAnswers.size();
		int l_CurrIndex = 0;
		LinkedHashMap<String, Double> l_CandidatesAndScores = new LinkedHashMap<String, Double>();
		for (AnswerCandidate l_CandidateAnswer : l_CandidateAnswers) {

			String l_CandidateAnswerString = l_CandidateAnswer.GetAnswer();
			String l_CandidateAnswerSource = l_CandidateAnswer.GetOrigSource();
			

			// Note that the answer string contains punctuations at the end possibly. We keep the punctuations
			// because we will need to do exact string matches between this answer string and the source passage.
			// removing the punctuations will cause the matches to fail
			// Some matches may need the punctuations to be removed though. Where needed,
			// RemoveTrailingPunctuations() is used.


			// Clear stop words from the query, we will use this to score the proximity.
			// That is how near the query appears to the candidate answer in the source passage
			String[] l_StopWordsFileNames = new String[1];
			l_StopWordsFileNames[0] = Configuration.BASELIBDIR+"lib" + File.separator + "common-word-"+Configuration.getLang()+".txt";
			StopWordsFilter l_StopWords = new StopWordsFilter(l_StopWordsFileNames);
			String[] l_StopWordsProcessArr = { l_Query };
			String l_CleanedQuery = l_StopWords.ProcessText(l_StopWordsProcessArr)[0];
			String[] l_ProximityStrings = {l_CleanedQuery, l_CandidateAnswerString};
			
			double l_ProximityScore = l_FS_Proximity.GetScore(l_ProximityStrings, l_CandidateAnswerSource);

			// Score based on how many words of the target or subject (if available) appear in the source passage
			String[] l_CoverageStrings_Target = {l_QuestionTarget};
			double l_CoverageScore_Target = l_FS_Coverage.GetScore(l_CoverageStrings_Target, l_CandidateAnswerSource);
			double l_CoverageScore_Subject = 0;
			if (l_Subject.length() > 0) {
				String[] l_CoverageStrings_Subject = {l_Subject};
				l_CoverageScore_Subject = l_FS_Coverage.GetScore(l_CoverageStrings_Subject, l_CandidateAnswerSource);
			}

			// Penalise if the answer candidate consists of repeated words compared to the question target
			// So we multiply a -1 into the score
			String[] l_CandidateStrings = {RemoveTrailingPunctuation(l_CandidateAnswerString)};
			double l_RepeatedTermScore = -1 * l_FS_Coverage.GetScore(l_CandidateStrings, l_QuestionTarget);

			// Score derived from rank of source passage - Not an ideal implementation as
			// candidates within the same source passage will get a different sentence score. But
			// this implementation is simple.
			double l_SentenceScore = (double) (l_NumCandidates - l_CurrIndex) / l_NumCandidates;
			
			// Penalise pronouns
			double l_IsPronoun = IsPronoun(RemoveTrailingPunctuation(l_CandidateAnswerString)) ? -1 : 0;

			
			// Tally the score
			// Sentence score depends on order of results returned by Lucene
			// Coverage considers how many keywords from the question target are within the sentence
			// Weights set manually. 
			double l_TotalScore = (0.5 * l_CoverageScore_Target)
					+ (0.25 * l_CoverageScore_Subject) // Only if subject is extracted 
					+ (0.35 * l_SentenceScore)
					+ (0.25 * l_ProximityScore)							
					+ (0.1 * l_RepeatedTermScore) // Penalties
					+ (0.5 * l_IsPronoun);

			// Store the candidate answer and its score
			l_CandidatesAndScores.put(RemoveTrailingPunctuation(l_CandidateAnswerString), l_TotalScore);

			

			if (l_TotalScore > l_BestScore) {

				l_BestScore = l_TotalScore;

				// Remove trailing punctionation marks
				l_CandidateAnswerString = RemoveTrailingPunctuation(l_CandidateAnswerString);

				l_Answer = l_CandidateAnswerString;
				l_OriginalAnswerString = l_CandidateAnswerSource;

			} // end if


			l_CurrIndex++;


		} // end for


		// If analysis is to be performed, we track the sentences that are retrieved
		if (a_Analysis) {
			for (AnswerCandidate l_Candidate : l_CandidateAnswers) {
				l_AnalysisResults.AddField("Stage3", l_Candidate.GetAnswer());
			}
		}


		// If we have an answer string, we see if we can expand on the name
		// For example given "Bush", we look up all candidate answers, and see
		// if there are longer versions like "President Bush", "George W. Bush" etc...
		// We choose the longer version to return as it is more specific.
		if (l_Answer.length() > 0) {

			double l_CurrBestAlternativeScore = Double.NEGATIVE_INFINITY;
			for (String l_Candidate : l_CandidatesAndScores.keySet()) {
				if (l_Candidate.length() > l_Answer.length() && l_Candidate.contains(l_Answer)) {
					double l_AltScore = l_CandidatesAndScores.get(l_Candidate);
					if (l_AltScore > l_CurrBestAlternativeScore) {
						l_CurrBestAlternativeScore = l_AltScore;
						l_Answer = l_Candidate;
						// Note we do not change the originating answer string as that
						// is evidence of the validity of our answer
					}
				}
			} // end for

		} // end if

		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem humGeneralCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{

		// All other types of HUMAN questions
		

		boolean answer_found = false;
		FreelingAPI free = FreelingAPI.getInstance();
		for(String sentence: l_BestSentence)
		{
			ListSentence ls = free.process(sentence);
			String[] free_entities = free.getEntitiesStr(ls);
			for(String answer: free_entities)
			{
				if (answer.length() > 0) {
					l_Answer = answer;
					l_OriginalAnswerString = sentence;
					answer_found = true;
					break;
				}
				
			}
			
			if(answer_found)break;
		}
		
		if (l_Answer.length() > 0) {
			// break in NN, so we can return the answer already
			//l_OriginalAnswerString = l_BestSentence[0];


			// If analysis is to be performed, we track the sentences that are retrieved
			if (a_Analysis) {
				l_AnalysisResults.AddField("Stage3", l_OriginalAnswerString);
			}
		}

		
		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem locCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
		// Location questions

		LinkedList<String> l_Candidates = new LinkedList<String>();
		LinkedList<String> l_OriginalAnswerStrings = new LinkedList<String>();

		FreelingAPI free = FreelingAPI.getInstance();
		int l_CurrIndex = 0;
		for(String sentence : l_BestSentence)
		{
			ListSentence ls = free.process(sentence);
			String[] locations = free.getEntitiesStr(ls, EnumTypes.LOCATION, true);
			for(String l_RawCandidate : locations)
			{
				l_Candidates.add(l_RawCandidate );
				l_OriginalAnswerStrings.add(l_BestSentence[l_CurrIndex]);
			}
			
			l_CurrIndex++;
				
		}

		// With the list of candidates, try to score them with some heuristics

		// Re-use these Featurescorer modules to help compute the scores we use
		FeatureSearchTermProximity l_FS_Proximity = new FeatureSearchTermProximity();
		FeatureSearchTermCoverage l_FS_Coverage = new FeatureSearchTermCoverage();

		// Find the highest scoring candidate
		double l_BestScore = Double.NEGATIVE_INFINITY;
		l_CurrIndex = 0;
		int l_NumCandidates = l_Candidates.size();
		for (String l_Candidate : l_Candidates) {

			// Passage from which candidate is derived
			String l_SourcePassage = l_OriginalAnswerStrings.get(l_CurrIndex);

			// Proximity score
			String[] l_ProximityStrings = {l_QuestionTarget, l_Candidate};
			double l_ProximityScore = l_FS_Proximity.GetScore(l_ProximityStrings, l_SourcePassage);

			// Coverage score of target within passage
			String[] l_CoverageStrings = {l_QuestionTarget};
			double l_CoverageScore = l_FS_Coverage.GetScore(l_CoverageStrings, l_SourcePassage);

			// Penalise if the answer candidate consists of repeated words compared to the question target
			// So we multiply a -1 into the score
			String[] l_CandidateStrings = {l_Candidate};
			double l_RepeatedTermScore = -1 * l_FS_Coverage.GetScore(l_CandidateStrings, l_QuestionTarget);

			// Score derived from rank of source passage
			double l_SentenceScore = (double) (l_NumCandidates - l_CurrIndex) / l_NumCandidates;

			// Score the sanity of the answer, ie. must correspond to question expected answer type
			int l_SanityScore = 1;
			/*if (l_ExpectedAnswerType.compareToIgnoreCase("LOC:country") == 0) {
				l_SanityScore = m_FBQ.CorrespondsToType(l_Candidate, FreebaseQuerier.ObjectTypes.COUNTRY);
			} else if (l_ExpectedAnswerType.compareToIgnoreCase("LOC:city") == 0) {
				l_SanityScore = m_FBQ.CorrespondsToType(l_Candidate, FreebaseQuerier.ObjectTypes.CITY);
			} else if (l_ExpectedAnswerType.compareToIgnoreCase("LOC:state") == 0) {
				l_SanityScore = m_FBQ.CorrespondsToType(l_Candidate, FreebaseQuerier.ObjectTypes.STATE);
			}*/

			// Tally the score
			double l_TotalScore = (0.6 * l_CoverageScore)
					+ (0.1 * l_SentenceScore)
					+ (0.2 * l_ProximityScore)
					+ (0.5 * l_SanityScore)
					+ (0.3 * l_RepeatedTermScore);


			if (l_TotalScore > l_BestScore) {
				l_BestScore = l_TotalScore;
				l_Answer = l_Candidate;
				l_OriginalAnswerString = l_SourcePassage;
			}

			l_CurrIndex++;

		} // end for


		// If analysis is to be performed, we track the sentences that are retrieved
		if (a_Analysis) {
			for (String l_Candidate : l_Candidates) {
				l_AnalysisResults.AddField("Stage3", l_Candidate);
			}
		}


		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem numDateCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
	
		
		String[] l_ExtractedResult = RetrieveBestCD(l_BestSentence, true, false);
		if (l_ExtractedResult[0].length() > 0) {
			l_Answer = l_ExtractedResult[0];
			l_OriginalAnswerString = l_ExtractedResult[1];
			// If analysis is to be performed, we track the sentences that are retrieved
			if (a_Analysis) {
				l_AnalysisResults.AddField("Stage3", l_OriginalAnswerString);
			}
		}

		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem numPeriodCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
		
		// We are unable to retrieve any "subject" from the question
		// Fall back to our default strategy
		String[] l_ExtractedResult = RetrieveBestCD(l_BestSentence, true, true);
		if (l_ExtractedResult[0].length() > 0) {
			l_Answer = l_ExtractedResult[0];
			l_OriginalAnswerString = l_ExtractedResult[1];
			// If analysis is to be performed, we track the sentences that are retrieved
			if (a_Analysis) {
				l_AnalysisResults.AddField("Stage3", l_OriginalAnswerString);
			}
		}

		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem numCountCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
		// Counts

		// We are unable to retrieve any "subject" from the question
		// Fall back to our default strategy
		String[] l_ExtractedResult = RetrieveBestCD(l_BestSentence, false, true);
		if (l_ExtractedResult[0].length() > 0) {
			l_Answer = l_ExtractedResult[0];
			l_OriginalAnswerString = l_ExtractedResult[1];

			// If analysis is to be performed, we track the sentences that are retrieved
			if (a_Analysis) {
				l_AnalysisResults.AddField("Stage3", l_OriginalAnswerString);
			}
		}



		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem numGeneralCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
		// Default strategy, look for the first /CD we come across
		String[] l_ExtractedResult = RetrieveBestCD(l_BestSentence, true, true);
		if (l_ExtractedResult[0].length() > 0) {
			l_Answer = l_ExtractedResult[0];
			l_OriginalAnswerString = l_ExtractedResult[1];

			// If analysis is to be performed, we track the sentences that are retrieved
			if (a_Analysis) {
				l_AnalysisResults.AddField("Stage3", l_OriginalAnswerString);
			}
		}
		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem entyGeneralCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
		// Entities
		// Default strategy

		// Look for any nouns and return the first one as the answer
		boolean answer_found = false;
		FreelingAPI free = FreelingAPI.getInstance();
		for(String sentence: l_BestSentence)
		{
			ListSentence ls = free.process(sentence);
			String[] free_nouns = free.getContinuousNounsStr(ls);
			for(String answer: free_nouns)
			{
				if (answer.length() > 0) {
					l_Answer = answer;
					l_OriginalAnswerString = sentence;
					answer_found = true;
					break;
				}
			}
			if(answer_found)break;
		}
		// If analysis is to be performed, we track the sentences that are retrieved
		if (a_Analysis) {
			l_AnalysisResults.AddField("Stage3", l_OriginalAnswerString);
		}

		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	private DataItem generalCase(Question question, String l_ExpectedAnswerType, DataItem a_QuestionItem, String[] l_BestSentence, boolean a_Analysis, DataItem l_AnalysisResults, String l_Answer, String l_OriginalAnswerString, String[] l_POSTaggedBestSentence, String l_QuestionTarget, QuestionSubType l_SubType, String l_QuestionText, String l_QuestionPOS, ScoreDoc[] l_RetrievedDocs, String l_Query, String l_QuestionID)
	{
		// Default case, return the first noun we come across

		//System.out.println("First answer:"+l_Answer);

		boolean answer_found = false;
		FreelingAPI free = FreelingAPI.getInstance();
		for(String sentence: l_BestSentence)
		{
			ListSentence ls = free.process(sentence);
			String[] free_nouns = free.getContinuousNounsStr(ls);
			for(String answer: free_nouns)
			{
				if (answer.length() > 0) {
					l_Answer = answer;
					l_OriginalAnswerString = sentence;
					answer_found = true;
					break;
				}
			}
			if(answer_found)break;
			
		}
		
		//System.out.println("Second answer:"+l_Answer);
	
		// If analysis is to be performed, we track the sentences that are retrieved
		if (a_Analysis) {
			l_AnalysisResults.AddField("Stage3", l_OriginalAnswerString);
		}
		DataItem result = new DataItem("response");
		result.AddAttribute("answer", l_Answer);
		result.AddAttribute("original_string", l_OriginalAnswerString);
		result.AddField("analysis_results", l_AnalysisResults);
		return result;
	}
	
	
	
	/**
	 * Check whether a given candidate answer is a reasonable choice for HUM:gr.
	 * Invalid answers could be days of the week (i.e. Monday, etc)....
	 *
	 * We need this because tons of non-proper nouns get tagged as NNPs by our POS tagger.
	 *
	 * @param a_CandidateAnswer [in] candidate answer to consider
	 * @return true if the answer is reasonable, false otherwise
	 */
	private boolean IsReasonableAnswerForHUMGR(String a_CandidateAnswer) {

		String[] l_DaysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
		String[] l_MonthsOfYear = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		String[] l_Countries = {"us", "uk", "france", "england", "cuba", "japan", "u.s", "america"};

		for (int i = 0; i < l_DaysOfWeek.length; ++i) {
			if (l_DaysOfWeek[i].compareToIgnoreCase(a_CandidateAnswer) == 0) {
				return false;
			}
		}
		for (int i = 0; i < l_MonthsOfYear.length; ++i) {
			if (l_MonthsOfYear[i].compareToIgnoreCase(a_CandidateAnswer) == 0) {
				return false;
			}
		}
		for (int i = 0; i < l_Countries.length; ++i) {
			if (l_Countries[i].compareToIgnoreCase(a_CandidateAnswer) == 0) {
				return false;
			}
		}

		return true;

	} // end IsReasonableAnswerForHUMGR()


	/**
	 * Retrieve a list of the proper nouns found within the provided sentences tagged with POS info.
	 *
	 * We will check with a NER to ensure that we have picked up candidates marked as PERSONS only.
	 *
	 * We will go through the sentences sequentially.
	 * 
	 * The candidate answers will have their POS tags kept intact.
	 *
	 * @param a_Sentences [in] provided sentences to extract nouns from
	 * @param a_POSSentences [in] sentences with POS annotations. Correspond to a_Sentences.
	 * @return linked list of proper nouns within sentences, arranged in the order they are found, or null on errors
	 */
	private LinkedList<AnswerCandidate> GetProperNounsOfPersons(String[] a_Sentences, String[] a_POSSentences) {

		if (a_Sentences == null) {
			return null;
		}
		
		int l_CurrIndex = 0;
		LinkedList<AnswerCandidate> l_Candidates = new LinkedList<AnswerCandidate>();
		
		FreelingAPI free = FreelingAPI.getInstance();
		
		for(String sentence : a_Sentences)
		{
			ListSentence ls = free.process(sentence);
			String[] persons = free.getEntitiesStr(ls, EnumTypes.PERSON, false);
			for(String l_RawCandidate : persons)
			{
				l_Candidates.add(new AnswerCandidate(l_RawCandidate, a_Sentences[l_CurrIndex]));
			}
			l_CurrIndex++;
				
		}
		
		return l_Candidates;

	} // end GetProperNounsOfPersons()

	/**
	 * Strips off the punctuation marks behind a string.
	 * These include
	 * , comma
	 * . fullstop
	 * ! exclaimation
	 * ? question
	 * ' apostrophe
	 *
	 * @param a_String [in] string to check for punctuations
	 * @return the string without the training punctuations.
	 */
	private String RemoveTrailingPunctuation(String a_String) {

		// Using regular expressiosn would be cleaner, but Java's support
		// for the '$' special regex symbol doesn't seem correct.
		// The regex pattern [ ,\\.\\?!']+$ did not work.

		while (a_String.endsWith(".")
				|| a_String.endsWith(",")
				|| a_String.endsWith("?")
				|| a_String.endsWith("!")
				|| a_String.endsWith("'")
				|| a_String.endsWith(" ")) {

			a_String = a_String.substring(0, a_String.length() - 1);

		} // end while

		return a_String;

	} // end a_String()

	
	/**
	 * Checks whether given string is a pronoun
	 * @param a_String [in] string to be checked
	 * @return true if the string is a pronoun, false otherwise.
	 */
	private boolean IsPronoun(String a_String) {

		String[] l_Pronouns = {"it", "we", "he", "she", "they", "our", "their"};
		for (String l_Pronoun : l_Pronouns) {
			if (a_String.compareToIgnoreCase(l_Pronoun) == 0) {
				return true;
			}
		}
		return false;

	} // end IsPronoun()
	

	/**
	 * Given a array of POS tagged sentences, retrieve the best choice of a "NUMBER" /CD
	 * from the sentences.
	 * This can be used as a default strategy for extracting answer strings from
	 * sentences.
	 * Currently what we do is to extract the first /CD that is encountered.
	 *
	 * @return an array, 1st element is extracted number, 2nd element is the sentence from which it is extracted.
	 *				or an empty array of 2 elements if no answer found.
	 */
	private String[] RetrieveBestCD( String[] a_Sentences, boolean dates, boolean others) {

		String[] l_Result = new String[2];
		l_Result[0] = ""; // Answer string
		l_Result[1] = ""; // Answer source

		// Sanity check
		if (a_Sentences == null) {
			return l_Result;
		}


		boolean answer_found = false;
		FreelingAPI free = FreelingAPI.getInstance();
		for(String sentence: a_Sentences)
		{
			ListSentence ls = free.process(sentence);
			String[] free_numbers = free.getNumbersStr(ls, dates, others);
			for(String answer: free_numbers)
			{
				if (answer.length() > 0 && answer.compareToIgnoreCase("a") != 0 && answer.compareToIgnoreCase("an") != 0) {
					l_Result[0] = answer;
					l_Result[1] = sentence;
					System.out.println("Second answer:"+l_Result[0]);
					answer_found = true;
					break;
				}
			}
			if(answer_found)break;
		}
		

		return l_Result;

	} // end RetrieveBestCD()
	

}
