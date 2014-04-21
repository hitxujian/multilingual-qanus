package ar.uba.dc.galli.qa.ml.ibp;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.search.ScoreDoc;

import ar.uba.dc.galli.qa.ml.ar.LuceneInformationBaseQuerier;
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
		
		Pattern p = Pattern.compile("^[a-zA-Z]+([0-9]+).*");
		Matcher m = p.matcher("Testing123Testing");

		String x = "testing: algo";
		x.matches("(.*):.*");
		if (m.find()) {
		    System.out.println(m.group(1));
		}
		
		System.exit(1);
		// TODO Auto-generated method stu
		LuceneInformationBaseQuerier lq = new LuceneInformationBaseQuerier(new File("/home/julian/tesis/lucene-indexes/index-simple/"), 100);
		ScoreDoc[] sd = (ScoreDoc[]) lq.SearchQuery("Article Discussion");
		for (int i = 0; i < sd.length; i++) {
			ScoreDoc doc = sd[i];
			System.out.format("Doc %d: %s %n", i, lq.GetDoc(doc.doc).get("TITLE"));
		}
		
	}

}
