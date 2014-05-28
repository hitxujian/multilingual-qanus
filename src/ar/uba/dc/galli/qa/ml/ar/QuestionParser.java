package ar.uba.dc.galli.qa.ml.ar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//import models.Question;

import org.apache.commons.lang3.ArrayUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import ar.uba.dc.galli.qa.ml.ar.qasys.Question;
import ar.uba.dc.galli.qa.ml.utils.Configuration;
import ar.uba.dc.galli.qa.ml.utils.comparators.BaseComparator;
import ar.uba.dc.galli.qa.ml.utils.comparators.ContainsComparator;

//import uba.utils.Utils;
//import ar.uba.dc.galli.qa.ml.utils.comparators.BaseComparator;
//import ar.uba.dc.galli.qa.ml.utils.comparators.ContainsComparator;

public class QuestionParser {

	
	public static Question[] getQuestions(File xmlFile)
	{
		SAXBuilder builder = new SAXBuilder();
		Question[] res = {};
		
		try {

			Document document = (Document) builder.build(xmlFile);

			Element rootNode = document.getRootElement();
			
			List topics = rootNode.getChildren("t");
			
			String id, group, text, text_en, answer, q_type, q_ans, support;//, snippet;
			
			for (int i = 0; i < topics.size(); i++) {

				Element t = (Element) topics.get(i);
				List questions = t.getChildren("q");
				for (int j = 0; j < questions.size(); j++) {
					
					Element q = (Element) questions.get(j);
					id = q.getAttributeValue("q_id").toString();
					q_type = q.getAttributeValue("q_type").toString();
					q_ans = q.getAttributeValue("q_exp_ans_type").toString();
					support = q.getChild("answer").getAttributeValue("a_support");
					//snippet = q.getChild("answer").getAttributeValue("a_support");
					group = t.getAttributeValue("t_string").toString();
					text = q.getChild("question").getText();
					answer = q.getChild("answer").getChildText("a_string");
					text_en = q.getChild("q_translation").getText();
					res = ArrayUtils.add(res, new Question(id, group, text, answer, text_en, new String[0], q_type, q_ans,support));	
					
				}
				
			}

		} catch (IOException io) {
			System.out.println(io.getMessage());
		} catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		}

		return res;
	}


	public static Question[] filterSupport(Question[] qs)
	{
		return filterSupport(qs, Configuration.SUPPORT_WIKI, Configuration.SUPPORT_NIL,Configuration.SUPPORT_NEWS);
	}
	
	public static Question[] filterQType(Question[] qs)
	{
		return filterQType(qs, Configuration.QTYPE_FACTOID, Configuration.QTYPE_DEFINITION,Configuration.QTYPE_LIST);
	}
	
	public static Question[] filterSupport(Question[] qs, boolean wiki, boolean nil, boolean payed)
	{
		int cnil = 0, cpayed = 0, cwiki = 0;
		BaseComparator comp = new ContainsComparator();
		Question[] res = {};
		for (int i = 0; i < qs.length; i++) {
			
			if(qs[i].getSupport().compareTo("NIL") == 0 || qs[i].getSupport().length() == 0)
			{
				cnil++;
				if(nil) res = ArrayUtils.add(res, qs[i]);
				//Utils.println("NIL:"+qs[i].getSupport());
				
			}
			else if(comp.compare(qs[i].getSupport(), "html"))
			{
				cwiki++;
				if(wiki) res = ArrayUtils.add(res, qs[i]);
			}
			else
			{
				cpayed++;
				if(payed) res = ArrayUtils.add(res, qs[i]);
			}
			
		}
		//System.out.format("{wiki: %d, payed: %d, nil: %d}", cwiki, cpayed, cnil);
		return res;
	}
	
	public static Question[] filterQType(Question[] qs, boolean factoid, boolean definition, boolean list)
	{
		int cfactoid = 0, cdefinition = 0, clist = 0;
		BaseComparator comp = new ContainsComparator();
		Question[] res = {};
		for (int i = 0; i < qs.length; i++) {
			if(qs[i].getQType().compareTo("FACTOID") == 0 )
			{
				cfactoid++;
				if(factoid) res = ArrayUtils.add(res, qs[i]);
				//Utils.println("NIL:"+qs[i].getSupport());
				
			}
			else if(qs[i].getQType().compareTo("LIST") == 0 )
			{
				clist++;
				if(list) res = ArrayUtils.add(res, qs[i]);
			}
			else
			{
				cdefinition++;
				if(definition) res = ArrayUtils.add(res, qs[i]);
			}
			
		}
		//System.out.format("{list: %d, def: %d, fact: %d}", clist, cdefinition, cfactoid);
		return res;
	}
	
	public static Question[] filterAnsType(Question[] qs)
	{
		
		int cfactoid = 0, cdefinition = 0, clist = 0;
		BaseComparator comp = new ContainsComparator();
		Question[] res = {};
		ArrayList<String>x = new ArrayList<String>();
		for (int i = 0; i < qs.length; i++) {
			x.add(qs[i].getQAns());
			//Utils.println(qs[i].getQAns());
		}
		String[] types = {"COUNT","OBJECT",	"MEASURE","PERSON",	"TIME",	"LOCATION",	"ORGANIZATION",	"OTHER"};
		for (int i = 0; i < types.length; i++) {
			//Utils.println(" & "+Integer.toString(Collections.frequency(x, types[i])));	
		}
		
		//Utils.println("Total & "+Integer.toString(x.size()));
		return qs;
	}
	
	
	public static Question[] getGroup(Question[] qs, String group)
	{
		LinkedList<Question> res = new LinkedList<Question>();
		for (int i = 0; i < qs.length; i++) {
			if(qs[i].getGroup().compareTo(group) == 0)
			{
				res.add(qs[i]);
			}
		}
		return res.toArray(new Question[0]); 
	}

	public static Question getById(Question[] qs, String id)
	{
		LinkedList<Question> res = new LinkedList<Question>();
		for (int i = 0; i < qs.length; i++)
			if(qs[i].getId().compareTo(id) == 0)
				return qs[i];
		return null; 
	}

}
