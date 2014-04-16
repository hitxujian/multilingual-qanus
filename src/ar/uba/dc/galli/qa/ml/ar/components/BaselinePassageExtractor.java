package ar.uba.dc.galli.qa.ml.ar.components;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import sg.edu.nus.wing.qanus.framework.commons.DataItem;

import ar.uba.dc.galli.qa.ml.ar.LuceneInformationBaseQuerier;
import ar.uba.dc.galli.qa.ml.ar.featurescoring.FeatureScorer;
import ar.uba.dc.galli.qa.ml.ar.featurescoring.FeatureScoringStrategy;

public class BaselinePassageExtractor {

	public BaselinePassageExtractor() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static String[] extractPassages(String l_Query, ScoreDoc[] l_RetrievedDocs, LuceneInformationBaseQuerier m_InformationBase, boolean a_Analysis, DataItem l_AnalysisResults) {
		// Perform search and process results for answers
		
			// Iterate over the Documents in the Hits object
			FeatureScorer l_FScorer = new FeatureScorer();
			for (int i = 0; i < l_RetrievedDocs.length; i++) {

				ScoreDoc l_ScoreDoc = l_RetrievedDocs[i];
				Document l_Doc = m_InformationBase.GetDoc(l_ScoreDoc.doc);
				String[] l_ArrText = l_Doc.getValues("BODY");
				String l_Headline = l_Doc.get("TITLE");


				// Treat each sentence within every document as a 'passage'
				// We will rank each passage based on a set of features, and use the top scoring
				// passage for answer string extraction
				for (String l_Sentence : l_ArrText) {
					// Add the passage to our scorer for scoring
					l_FScorer.AddDocument(l_Sentence);

					// If analysis is to be performed, we track the sentences that are retrieved
					if (a_Analysis) {
						l_AnalysisResults.AddField("Stage1", l_Sentence);
					}
				} // end for

			} // end for i

			
			// Retrieve the N-best passages from all the retrieved documents
			return l_FScorer.RetrieveTopDocuments(l_Query, 40);
			

	}

}
