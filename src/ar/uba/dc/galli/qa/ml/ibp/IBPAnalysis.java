package ar.uba.dc.galli.qa.ml.ibp;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.search.ScoreDoc;

import ar.uba.dc.galli.qa.ml.ar.LuceneInformationBaseQuerier;
import ar.uba.dc.galli.qa.ml.textprocessing.FreelingAPI;
import ar.uba.dc.galli.qa.ml.utils.Configuration;

public class IBPAnalysis {

	public static int NULLS = 0;
	public static int REDIRECTS = 0;
	public static int OTHER = 0;
	public static int ALL = 0;
	public static int VALID = 0;
	public IBPAnalysis() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// TODO Auto-generated method stu
		//^((?!hede).)*$
		//
		
		LuceneInformationBaseQuerier lq = new LuceneInformationBaseQuerier(new File("/home/julian/tesis/lucene-indexes/index-es-2006/"), 1);
		ScoreDoc[] sd = (ScoreDoc[]) lq.SearchQuery("algo");
		for (int i = 0; i < sd.length; i++) {
			ScoreDoc doc = sd[i];
			String body = lq.GetDoc(doc.doc).get("BODY");
			
			String[] sentences = FreelingAPI.getInstance("es-06").splitString(body);
			
			/*System.out.format("Sentences: %d %n", sentences.length);
			for (int j = 0; j < sentences.length; j++) {
				System.out.format("%d: %s %n", j+1, sentences[j]);
			}*/
			if(sentences.length ==1 )
			{
				Pattern pattern = Pattern.compile("\\.(.+)");
		        Matcher  matcher = pattern.matcher(sentences[0]);
		        int count = 0;
		        while (matcher.find())
		            count++;
		        if(count > 0 )
		        	System.out.format("%d %s : (%d) %s %n", i+1, lq.GetDoc(doc.doc).get("TITLE"), count, body);
				
				//System.out.println("No pude splittear un documento y lo tire");
				//System.out.println("DOC:: "+sentences[0]);
				sentences = new String[0];
			}
			else
			{
				System.out.format("%d %s : (sentences: %d) %s %n", i+1, lq.GetDoc(doc.doc).get("TITLE"),sentences.length, body);
			}
			
			
			
		}
		
	}

}
