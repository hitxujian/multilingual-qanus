package ar.uba.dc.galli.qa.ml.ibp;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.WikiXMLParser;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.SAXException;

import ar.uba.dc.galli.qa.ml.utils.Configuration;

import sg.edu.nus.wing.qanus.framework.commons.BasicController;
import sg.edu.nus.wing.qanus.framework.util.DirectoryAndFileManipulation;


/**
 * Stock implementation of the Controller of the  information base processing stage.
 * 
 * Basically we take in text from a corpus, and index it with an engine like Lucene.
 *
 * This Controller is the entry point for the information base preparation stage.
 *
 * This can be a reference implementation for your own customised evaluation
 * components.
 *
 * @author NG, Jun Ping -- junping@comp.nus.edu.sg
 * @version Jan 16, 2010
 */
public class Controller extends BasicController {


	public Controller() {

		// The command line options expected to use with this Controller
		AddOptionWithRequiredArgument("wiki", "Options are: simple-06, simple-13, es-06, en-06, pt-07", String.class);
			MakeOptionCompulsory("wiki");
			
	}
	
	
	public boolean Entry(String[] args) {

		// Check that the arguments are supplied correctly.
		boolean l_OkSoFar = super.Entry(args);
		
		String l_WikipediaFile = Configuration.GetWikipediaFromOption((String) GetOptionArgument("wiki"));
		String l_LuceneFolder = Configuration.GetLuceneIndexFromOption((String) GetOptionArgument("wiki"));

		// Ensure that the source file/folder exists
		if (l_OkSoFar && !Configuration.FileExists(l_WikipediaFile)) {
			// source file does not exist, we cannot continue.
			Logger.getLogger("QANUS").logp(Level.SEVERE, Controller.class.getName(), "Entry", "Cannot access source folder.");
			l_OkSoFar = false;
		}

		// Create the target file/folder if it doesn't exist
		if (l_OkSoFar && !Configuration.OutputCheckAndEmpty(l_LuceneFolder)) {
			// No point continueing if the results produced later cannot be saved?
			Logger.getLogger("QANUS").logp(Level.SEVERE, Controller.class.getName(), "Entry", "Cannot access target folder.");
			l_OkSoFar = false;
		}

		 
		// Don't proceed if the requirements for a successfull execution are not met
		if (!l_OkSoFar) return false;
		
		long start = System.currentTimeMillis();


		System.out.println("Extracting "+l_WikipediaFile+" in directory "+l_LuceneFolder+"...");

		try {

			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, new StandardAnalyzer(Version.LUCENE_40));
			IndexWriter luceneIW = new IndexWriter(FSDirectory.open(new File(l_LuceneFolder)), config);

			IArticleFilter handler = new ArticleFilter(luceneIW);
			WikiXMLParser wxp = new WikiXMLParser(l_WikipediaFile, handler);
			wxp.parse();
			luceneIW.close();
			
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}

		long finish = System.currentTimeMillis();
		System.out.println("Success creating index [" + ((finish - start) /1000) + " secs]");
		System.out.format("%s & %s & %s & %s & %s \\\\ \\hline %n", "total", "nulos", "redirects","otros", "validos");
		System.out.format("%d & %d & %d & %d & %d \\\\ \\hline %n", IBPAnalysis.ALL, IBPAnalysis.NULLS, IBPAnalysis.REDIRECTS, IBPAnalysis.OTHER, IBPAnalysis.VALID);

		
		return true;


	}
	
	







	/**
	 * Entry point function.
	 * You don't have to change this usually, so you can just copy this to make your own
	 * Controller.
	 *
	 * @param args 
	 */
	public static void main(String args[]){

		// --------------------------------------------------------
		// Start up an instance of the controller, and invoke it to get the machinery going
		// This illustrates the standard way this can be done. When creating your own
		// Controller, or customising it for your needs, you can take this as a reference
		// implementation.
		Controller l_Ctr = new Controller();

		// If a log is desired, uncomment this. When this is commented, log messages
		// are not saved to any file
		//l_Ctr.SetUpLog();

		

		// This call will jump-start the machinery. Must be called, else nothing will happen!
		if (!l_Ctr.Entry(args)) {
			// An error happened
			// Fix it if you want
		} else {
			// Successfull invoked engine.
			// Any post-processing?
		}


		// --------------------


	} // end main()


	
	
} // end class
