package ar.uba.dc.galli.qa.ml.ibp;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.xml.sax.SAXException;


/**
 * Print title an content of all the wiki pages in the dump.
 * 
 */
class ArticleFilter implements IArticleFilter {

	final static Pattern regex = Pattern.compile("[A-Z][\\p{L}\\w\\p{Blank},\\\"\\';\\[\\]\\(\\)-]+[\\.!]", 
			Pattern.CANON_EQ);

	// Convert to plain text
	WikiModel wikiModel = new WikiModel("${image}", "${title}");

	private IndexWriter luceneIW;

	public ArticleFilter(IndexWriter in_luceneIW) {
		luceneIW = in_luceneIW;
	}

	public String nullToString(String str)
	{
		if(str != null) return str;
		else return "";
	}


	public void add(String id, String title, String time, String body) {


		Document doc = new Document();		
		//System.out.println(id+" "+title);
		doc.add(new Field("ID", nullToString(id), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("TITLE", nullToString(title), Field.Store.YES, Field.Index.ANALYZED));
		//doc.add(new StringField("time", nullToString(time), Field.Store.YES));
		doc.add(new Field("BODY", nullToString(body), Field.Store.YES,  Field.Index.ANALYZED));
		doc.add(new Field("ALL", nullToString(id+" "+title+" "+body), Field.Store.YES, Field.Index.ANALYZED));

		//System.out.println(doc.get("ID")+ " \"" + doc.get("TITLE")+ "\" "+doc.get("BODY"));
		try {
			luceneIW.addDocument(doc);
		} catch (IOException e) {
			System.out.println("Error writing in the index");
			e.printStackTrace();
		}
	}

	public void process(WikiArticle page, Siteinfo siteinfo) throws SAXException {

		IBPAnalysis.ALL++;

		//if(page.getTitle().compareToIgnoreCase("Gustave Flaubert") != 0) return; 
		if(page == null || page.getText() == null )
		{
			IBPAnalysis.NULLS++;
			return;
		}
		
		if(page.getText().startsWith("#REDIRECT") || page.getText().startsWith("#redirect"))
		{
			IBPAnalysis.REDIRECTS++;
			return;
		}
		String otherRegex = "(WP|wp|Biografias|Wikipedia|Wikiproyecto|Imagen|Plantilla|MediaWiki|Template|Category|Help|Image|Ayuda|Portal|Ajuda|Categoria|Categoría|Imagem|Predefinição";
		otherRegex+="|Livros do Brasil por título|Lista de escritores por nombre|Lista de livros por título|List of people by name|Lista de autores por nome";
		otherRegex+="):.*";
		if(page.getTitle().matches(otherRegex))
		{
			IBPAnalysis.OTHER++;
			//System.out.println("Other: "+IBPAnalysis.OTHER + " "+page.getTitle());
			return;
		}
		
	
		
		
		
		PrintStream out = null;

		try {
			out = new PrintStream(System.out, true, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String texto = page.getText();
		// Zap headings ==some text== or ===some text===

		// <ref>{{Cite web|url=http://tmh.floonet.net/articles/falseprinciple.html |title="The False Principle of our Education" by Max Stirner |publisher=Tmh.floonet.net |date= |accessdate=2010-09-20}}</ref>
		// <ref>Christopher Gray, ''Leaving the Twentieth Century'', p. 88.</ref>
		// <ref>Sochen, June. 1972. ''The New Woman: Feminism in Greenwich Village 1910Ð1920.'' New York: Quadrangle.</ref>

		// String refexp = "[A-Za-z0-9+\\s\\{\\}:_=''|\\.\\w#\"\\(\\)\\[\\]/,?&%Ð-]+";

		String todo, primero, separador, segundo;
		LinkedList<Integer> start = new LinkedList<Integer>(), end = new LinkedList<Integer>();
		LinkedList<String> replacements = new LinkedList<String>();

		Pattern pattern = Pattern.compile("\\[\\[([^\\|:\\[\\]]{0,30})(\\||:|)([^\\|:\\[\\]]{0,30})\\]\\]"); 
		Pattern img_pattern = Pattern.compile("\\n\\[\\[Image:[^(\\]\\])]*\\]\\]");
		Pattern del_pattern = Pattern.compile("\\[\\[([^\\|:\\[\\]]{0,30})(:)([^\\|:\\[\\]]{0,30})\\]\\]");
		Pattern strip_pattern = Pattern.compile("\\[\\[([^\\|:\\[\\]]{0,30})([^\\|:\\[\\]]{0,30})\\]\\]");
		Pattern second_pattern = Pattern.compile("\\[\\[([^\\|:\\[\\]]{0,30})(\\|)([^\\|:\\[\\]]{0,30})\\]\\]");
		
		Pattern fivequote_strong_pattern = Pattern.compile("'''''(.{0,60})'''''");
		Pattern triplequote_strong_pattern = Pattern.compile("'''(.{0,60})'''");
		Pattern doublequote_strong_pattern = Pattern.compile("''(.{0,60})''");
		
		Pattern fivequote_pattern = Pattern.compile("'''''([^']*)'''''");
		Pattern triplequote_pattern = Pattern.compile("'''([^']*)'''");
		Pattern doublequote_pattern = Pattern.compile("''([^']*)''");
		
		Matcher matcher;

		matcher = fivequote_strong_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {

			if(matcher.group(1).matches(".*'''''.*")) continue;
			start.add(matcher.start(0));
			end.add(matcher.end(0));
			replacements.add(matcher.group(1));
			//System.out.format("start: %d, end:%d, match: %s %n",matcher.start(0), matcher.end(0), matcher.group(1));
		}
		
		for (int i = start.size()-1; i >= 0; i--)
			texto = texto.substring(0, start.get(i))+""+replacements.get(i)+""+texto.substring(end.get(i), texto.length());
		
		matcher = triplequote_strong_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {

			if(matcher.group(1).matches(".*'''.*")) continue;
			start.add(matcher.start(0));
			end.add(matcher.end(0));
			replacements.add(matcher.group(1));
			//System.out.format("start: %d, end:%d, match: %s %n",matcher.start(0), matcher.end(0), matcher.group(1));
		}
		
		for (int i = start.size()-1; i >= 0; i--)
			texto = texto.substring(0, start.get(i))+""+replacements.get(i)+""+texto.substring(end.get(i), texto.length());
		
		matcher = doublequote_strong_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {

			if(matcher.group(1).matches(".*''.*")) continue;
			start.add(matcher.start(0));
			end.add(matcher.end(0));
			replacements.add(matcher.group(1));
			//System.out.format("start: %d, end:%d, match: %s %n",matcher.start(0), matcher.end(0), matcher.group(1));
		}
		
		for (int i = start.size()-1; i >= 0; i--)
			texto = texto.substring(0, start.get(i))+""+replacements.get(i)+""+texto.substring(end.get(i), texto.length());
		
		
		matcher = fivequote_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {

			start.add(matcher.start(0));
			end.add(matcher.end(0));
			replacements.add(matcher.group(1));
			//System.out.format("start: %d, end:%d, match: %s %n",matcher.start(0), matcher.end(0), matcher.group(1));
		}
		
		for (int i = start.size()-1; i >= 0; i--)
		{
			texto = texto.substring(0, start.get(i))+""+replacements.get(i)+""+texto.substring(end.get(i), texto.length());
		}
		
		
		matcher = triplequote_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {

			start.add(matcher.start(0));
			end.add(matcher.end(0));
			replacements.add(matcher.group(1));
			//System.out.format("start: %d, end:%d, match: %s %n",matcher.start(0), matcher.end(0), matcher.group(1));
		}
		
		for (int i = start.size()-1; i >= 0; i--)
		{
			texto = texto.substring(0, start.get(i))+""+replacements.get(i)+""+texto.substring(end.get(i), texto.length());
		}
		//System.out.println(texto);
		
		matcher = doublequote_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {

			start.add(matcher.start(0));
			end.add(matcher.end(0));
			replacements.add(matcher.group(1));
		}

		for (int i = start.size()-1; i >= 0; i--) texto = texto.substring(0, start.get(i))+""+replacements.get(i)+""+texto.substring(end.get(i), texto.length());

		matcher = del_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {

			start.add(matcher.start(0));
			end.add(matcher.end(0));
		}

		for (int i = start.size()-1; i >= 0; i--) texto = texto.substring(0, start.get(i))+" "+texto.substring(end.get(i), texto.length());

		
		matcher = strip_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {
			start.add(matcher.start(0));
			end.add(matcher.end(0));
			replacements.add(matcher.group(1));
		}
		for (int i = start.size()-1; i >= 0; i--) texto = texto.substring(0, start.get(i))+replacements.get(i)+texto.substring(end.get(i), texto.length());


		matcher = img_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>();replacements =new LinkedList<String>();
		while (matcher.find()) {
			//System.out.println(matcher.group(0));
			start.add(matcher.start(0));
			end.add(matcher.end(0));
		}

		for (int i = start.size()-1; i >= 0; i--) texto = texto.substring(0, start.get(i))+". "+texto.substring(end.get(i), texto.length());


		matcher = second_pattern.matcher(texto);
		start = new LinkedList<Integer>(); end = new LinkedList<Integer>(); replacements =new LinkedList<String>();
		while (matcher.find()) {
			start.add(matcher.start(0));
			end.add(matcher.end(0));
			replacements.add(matcher.group(3));
		}
		for (int i = start.size()-1; i >= 0; i--) texto = texto.substring(0, start.get(i))+replacements.get(i)+texto.substring(end.get(i), texto.length());


		// return;
		/*System.out.format("I found the text" +
                " \"%s\" starting at " +
                "index %d and ending at index %d.%n",
                matcher.group(),
                matcher.start(),
                matcher.end());*/
		//System.out.format("I found the text" +
		//" \"%s\", \"%s\",\"%s\" ,\"%s\" \n",matcher.group(0), matcher.group(1),  matcher.group(2), matcher.group(3));

		//System.out.println(texto);
		//System.out.println("texto:"+texto);
		String wikiText = texto.
				replaceAll("[=]+[A-Za-z+\\s-]+[=]+", " ").
				replaceAll("\\{\\{[A-Za-z0-9+\\s-]+\\}\\}"," ").
				replaceAll("(?m)<ref>.+</ref>"," ").
				replaceAll("(?m)<ref name=\"[A-Za-z0-9\\s-]+\">.+</ref>"," ").
				replaceAll("<ref>"," <ref>");

		// Remove text inside {{ }}
		String plainStr = wikiModel.render(new PlainTextConverter(), wikiText).
				replaceAll("\\{\\{[A-Za-z+\\s-]+\\}\\}"," ");
		//System.out.println("regex:'"+plainStr+"'");
		
		Matcher regexMatcher = regex.matcher(plainStr);
		boolean first = true;
		//out.println(page.getTitle());
		String text = "";
		
		while (regexMatcher.find())
		{
			// Get sentences with 6 or more words
			String sentence = regexMatcher.group();
			//System.out.println(sentence);
			if (matchSpaces(sentence, 5)) 
			{
				text+=" "+sentence;
			}
		}
		
		
		
		
			/*if(page.getTitle().compareToIgnoreCase("Harry Potter 3") == 0)
				System.out.println(page.getTitle()+" "+page.getText());
			if(page.getTitle().compareToIgnoreCase("Harry Potter y el prisionero de Azkabán") == 0)
				System.out.println(page.getTitle()+" "+page.getText());
			*/
		/*	if(page.getTitle().compareToIgnoreCase("Harry Potter y el prisionero de Azkaban") == 0)
			{
				System.out.println(page.getTitle()+" "+page.getText());
				System.out.println(text);
			}
		*/		
			
			
		
		
		
		//{
		//System.out.println("FLAUBERT: text: "+text);
		//System.out.println(text);
		IBPAnalysis.VALID++;
		//String plainStrs = wikiModel.render(new PlainTextConverter(), page.getText());
		//System.out.println(page.getText());
		//System.out.println(text);
	    //if(IBPAnalysis.VALID > 0) System.exit(1);
		//if(true) return;
	    
		
		add(page.getId(), page.getTitle(), page.getTimeStamp(), text);
		//}
		// System.out.println(matcher.group(0)+"  "+matcher.group(1));

	}

	private boolean matchSpaces(String sentence, int matches) {

		int c =0;
		for (int i=0; i< sentence.length(); i++) {
			if (sentence.charAt(i) == ' ') c++;
			if (c == matches) return true;
		}
		return false;
	}

}