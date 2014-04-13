package ar.uba.dc.galli.qa.ml.ar;



import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.store.FSDirectory;

import ar.uba.dc.galli.qa.ml.ar.er.FactoidPipelineErrorAnalyzer;
import ar.uba.dc.galli.qa.ml.ar.featurescoring.FeatureScoringStrategy;
import ar.uba.dc.galli.qa.ml.utils.Configuration;
import sg.edu.nus.wing.qanus.framework.commons.BasicController;
import sg.edu.nus.wing.qanus.framework.commons.IRegisterableModule;
import sg.edu.nus.wing.qanus.framework.commons.IXMLParser;
import sg.edu.nus.wing.qanus.framework.ar.*;
import sg.edu.nus.wing.qanus.framework.ar.er.ErrorAnalyzer;



/**
 * Stock implementation of controller module for answer retrieval stage.
 * Makes use of annotated TREC 2007 questions and a feature-scoring based
 * retrieval technique.
 *
 * This Controller is the entry point for the Answer Retrieval stage.
 * 
 * This can be a reference implementation for your own customised answer retrieval
 * components.
 *
 * @author NG, Jun Ping - junping@comp.nus.edu.sg
 * @version 15Jan2010
 */
public class Controller extends BasicController {


	// For the ar.FrameworkController
	// Notes:
	/*
	 * When creating your XML handlers and modules to customise this portion, you
	 * may need this info read in from the command line:
	 * GetSourceFile1() gives the information base source. Could be null.
	 * GetSourceFile2() gives the question source.
	 * GetTargetFile() gives the folder to save the answers in
	 * GetErrorAnalysisSource() gives the folder to load error analysis reference information from
	 * GetErrorAnalysisTarget() gives the folder to store error analysis information to
	 */


	private AnswerRetriever m_StageEngine;


	public Controller() {

		// The command line options expected to use with this Controller
		AddOptionWithRequiredArgument("run", "Options are: 'pt-07' and 'es-06'", String.class);
			MakeOptionCompulsory("run");
			
	}

	protected boolean Entry(String[] args) {

		// Check that the arguments are supplied correctly.
		boolean l_OkSoFar = super.Entry(args);
		String l_LuceneFolder = Configuration.GetLuceneIndexFromOption((String) GetOptionArgument("run"));
		String l_QuestionFile = Configuration.GetQuestionFileFromOption((String) GetOptionArgument("run"));
		// Ensure that the source file/folder exists
		String l_TargetFolder = Configuration.GetTargetFile();
		
		if (l_OkSoFar && !Configuration.FileExists(l_QuestionFile)) {
			System.out.println(l_QuestionFile);
			Logger.getLogger("QANUS").logp(Level.SEVERE, Controller.class.getName(), "Entry", "Cannot access question files");
			l_OkSoFar = false;
		}
		
		if (l_OkSoFar && !Configuration.FileExists(l_LuceneFolder)) {
			Logger.getLogger("QANUS").logp(Level.SEVERE, Controller.class.getName(), "Entry", "Cannot access index-files");
			l_OkSoFar = false;
		}
		
		// Create the target file/folder if it doesn't exist
		if (l_OkSoFar && !Configuration.OutputCheckAndEmpty(l_TargetFolder)) {
			// No point continueing if the results produced later cannot be saved?
			Logger.getLogger("QANUS").logp(Level.SEVERE, Controller.class.getName(), "Entry", "Cannot access target folder.");
			l_OkSoFar = false;
		}

		
		// Don't proceed if the requirements for a successfull execution are not met
		if (!l_OkSoFar) return false;
		System.out.println("Chequeo archivos y paso la pelota a AnswerRetriever");
		
		FeatureScoringStrategy l_Module = new FeatureScoringStrategy(new File(l_LuceneFolder));

		m_StageEngine = new AnswerRetriever(new File(l_QuestionFile),new File(l_TargetFolder));


		return m_StageEngine.Go();
		

	} // end Entry()


	/**
	 * Entry point function.
	 * You don't have to change this usually, so you can just copy this to make your own
	 * Controller.
	 *
	 * @param args
	 */
	public static void main(String args[]) {

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

	

	
} // end class Controller
