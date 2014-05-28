package ar.uba.dc.galli.qa.ml.utils;

import ar.uba.dc.galli.qa.ml.utils.comparators.BaseComparator;

import com.google.gson.Gson;


/**
 * A tuple with information
 * @author julian
 *
 */
public class TextEntity extends Object
{
    
    
    public static boolean HTML_PRINT = false;

    
    
    public String term;
    public String tag;
    public String subtype;
    public String lang;
    public String recognized_by;
    public String comparator_used;
    public EnumTypes qc_type = null;
    public boolean verified;
    public boolean multiword;
    public boolean exact_match;
    public String matched_str;
    public int total_words;
    public double confidence;
    public boolean used_in_response = false;
    
    
  
    public TextEntity(String in_term,String  in_tag, String in_recognized_by)
    {
        term = in_term;
        tag = in_tag;
        recognized_by = in_recognized_by;
            
    }
    
	public TextEntity(String in_term,String  in_tag)
    {
        term = in_term;
        tag = in_tag;
            
    }
	
	
    
    public TextEntity(String in_term,String in_type, String in_db_field, String in_value, String comp)
    {
        term = in_term;
        tag = in_type;
        matched_str = in_value;
        subtype = in_db_field;
        comparator_used = comp;
        
            
    }
    
    public TextEntity(String in_term,String  in_type, String in_matched_str, String in_subtype, String in_comparator, EnumTypes in_qc_type)
    {
    	term = in_term;
    	tag = in_type;
    	matched_str = in_matched_str;
    	subtype = in_subtype;
        comparator_used = in_comparator;
        
    	qc_type = in_qc_type;
            
    }
    
    public boolean isUsedInResponse() {
  		return used_in_response;
  	}

  	public void setUsedInResponse(boolean used_in_response) {
  		this.used_in_response = used_in_response;
  	}
    
    @Override
    public boolean equals(Object in_other)
    {
    	//return true;
    	TextEntity other = (TextEntity) in_other;
    	boolean res =  (term.compareToIgnoreCase(other.term) == 0);
    	return res;		
    	/*if(type.compareTo("PERSON") == 0 || type.compareTo("LOCATION") == 0 || type.compareTo("ORGANIZATION") == 0 || type.compareTo("NP00000es") == 0 || type.compareTo("NP00000en") == 0 || type.compareTo("NP00000") == 0)
    	{
    		//	&& type.compareToIgnoreCase(other.type).compareTo(0);
    		res &= (other.type.compareTo("PERSON") == 0 || other.type.compareTo("LOCATION") == 0 || other.type.compareTo("ORGANIZATION") == 0 || other.type.compareTo("NP00000es") == 0 || other.type.compareTo("NP00000en") == 0 || other.type.compareTo("NP00000") == 0);
    	}
    	else
    	{
        	//null copula con todos
        	if(subtype != null && other.subtype != null) res =  res && (subtype.compareToIgnoreCase(other.subtype) == 0);
        	if(other.comparator_used != null && comparator_used != null) res = res && (comparator_used.compareToIgnoreCase(other.comparator_used) == 0);
        	if(other.matched_str != null && matched_str != null) res = res &&  (matched_str.compareToIgnoreCase(other.matched_str) == 0);
    		
    	}

    	return res;*/
    }
    
    public boolean compareWithComparator(TextEntity other, BaseComparator comparator)
    {
    	return comparator.compare(this.term, other.term);
    }
    
    public void print()
    {
    	if(HTML_PRINT)System.out.print("<span style='color:red'>");
    	String res = "{term: "+term+", type: "+tag;
    	if(subtype != null) res+=", subtype: "+subtype.toLowerCase();
    	if(matched_str != null) res+=", matched: "+matched_str;
    	if(comparator_used != null) res+=", comp: "+comparator_used;
    	if(qc_type != null) res+=", qtype: "+qc_type.toString();
    	System.out.println(res+"}");
    	if(HTML_PRINT)System.out.print("</span>");
    }
    
    public String toString()
    {
    	Gson gson = new Gson();
    	return gson.toJson(this);
    }
    
    @Override
    public int hashCode()
    {
    	return 1;
    }
    
}