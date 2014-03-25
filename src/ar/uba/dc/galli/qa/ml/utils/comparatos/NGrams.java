package ar.uba.dc.galli.qa.ml.utils.comparatos;

import java.util.*;

public class NGrams {

    public static List<String> ngrams_base(int n, String str) {
        List<String> ngrams = new ArrayList<String>();
        String[] words = str.split(" ");
        for (int i = 0; i < words.length - n + 1; i++)
        {
        	String with_punkt = concat(words, i, i+n);
        	
        	String no_punkt = concat(words, i, i+n, true);
        	
        	ngrams.add(no_punkt);
        	
        	if(no_punkt.compareToIgnoreCase(with_punkt) != 0) ngrams.add(with_punkt);
        }
            
        return ngrams;
    }

    
    public static String concat(String[] words, int start, int end, boolean no_punk) 
    {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
        	sb.append((i > start ? " " : "") + EqualNoPunctComparator.removeThis(words[i], "[¡,\\?]"));
        return sb.toString();
    
    }
    
    public static String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
        	sb.append((i > start ? " " : "") + words[i]);			        
            
        return sb.toString();
    }
    
    /**
     * starts from 2
     */
    public static List<String> ngrams(int n, String str)
    {
    	List<String> ngrams = new ArrayList<String>();
    	 for (int i = 2; i <= n; i++) {
             for (String ngram : ngrams_base(i, str))
            	 ngrams.add(ngram);
    	 }
    	 return ngrams;
    }
    
    /**
     * starts from 'from'
     */
    public static List<String> ngrams(int from, int n, String str)
    {
    	List<String> ngrams = new ArrayList<String>();
    	 for (int i = from; i <= n; i++) {
             for (String ngram : ngrams_base(i, str))
            	 ngrams.add(ngram);
    	 }
    	 return ngrams;
    }

    public static void main(String[] args) {
    	
    	for(String ngram: ngrams(4, "This is my car. Fucking Nasty Little Beatch. Understand?"))
    			{
    	    System.out.println(ngram);
    			}
    		
    	//for (int n = 1; n <= 3; n++) {
          //  for (String ngram : ngrams(n, "This is my car."))
            //    System.out.println(ngram);
            //System.out.println();
        //}
    }
}


