package ar.uba.dc.galli.qa.ml.ar;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;

import sg.edu.nus.wing.qanus.framework.commons.IInformationBaseQuerier;


/**
 * Front-end to a information base implemented with a Lucene index.
 *
 *
 * @author NG, Jun Ping -- junping@comp.nus.edu.sg
 * @version v18Jan2010
 */
public class LuceneInformationBaseQuerier implements IInformationBaseQuerier {

	// The searcher through the Lucene index
	private IndexSearcher m_Searcher;

	// The default no. of results to retrieve each time
	private int m_DefaultNumResults;

	/**
	 * Constructor.
	 * @param a_KBFolder [in] folder where Lucene knowledge base is stored
	 */
	public LuceneInformationBaseQuerier(File a_KBFolder, int a_DefaultNumResults) {

		m_DefaultNumResults = a_DefaultNumResults;

		try {
			// Build an IndexSearcher using the in-memory index
			Directory directory = FSDirectory.open(a_KBFolder);
			IndexReader reader = IndexReader.open(directory);
			m_Searcher = new IndexSearcher(reader);
			
		} catch (Exception e) {
			Logger.getLogger("QANUS").logp(Level.WARNING, LuceneInformationBaseQuerier.class.getName(), "Constructor", "Unable to initialise Lucene index searcher.", e);
		}

	} // end constructor

	/**
	 * Takes a search string and retrieve relevant source documents from KB
	 * @param a_QueryString [in] the search string
	 * @param a_NumResults [in] the number of documents to return
	 * @throws ParseException
	 * @throws IOException
	 * @return array of top scored documents, or null on any errors
	 */
	//private ScoreDoc[] Search(String a_QueryString, int a_NumResults) throws ParseException, IOException {
	public Object SearchQuery(String a_Query) {

		// Build a Query object
		QueryParser l_QP = new QueryParser(Version.LUCENE_40, "ALL", new StandardAnalyzer(Version.LUCENE_40));
		Query l_ParsedQuery = null;
		try {
			
			// Search for the l_ParsedQuery
			l_ParsedQuery = l_QP.parse(a_Query);
			
			TopDocs l_TopDocResult = m_Searcher.search(l_ParsedQuery, m_DefaultNumResults);
			ScoreDoc[] l_ScoreDocs = l_TopDocResult.scoreDocs;
			return l_ScoreDocs;

		} catch  (IOException ex) {
			Logger.getLogger("QANUS").logp(Level.WARNING, LuceneInformationBaseQuerier.class.getName(), "SearchQuery", "Exception conducting search.", ex);
		} catch (ParseException ex) {
			Logger.getLogger("QANUS").logp(Level.WARNING, LuceneInformationBaseQuerier.class.getName(), "SearchQuery", "Exception parsing query.", ex);
		}

		return null;

	} // end Search()

	/**
	 * Retrieve a document based on a given ID.
	 * @param a_ID [in] the ID of the document we want to retrieve.
	 * @return the document with the specified ID, or null if the document cannot be retrieved
	 */
	public Document GetDoc(int a_ID) {

		try {
			return m_Searcher.doc(a_ID);
		} catch (CorruptIndexException ex) {
			return null;
		} catch (IOException ex) {
			return null;
		} // end try-catch

	} // end GetDoc()
	

} // end class LuceneInformationBaseQuerier

