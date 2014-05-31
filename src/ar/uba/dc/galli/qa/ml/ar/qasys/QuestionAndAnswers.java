package ar.uba.dc.galli.qa.ml.ar.qasys;

import com.google.gson.Gson;

import ar.uba.dc.galli.qa.ml.utils.Configuration;
import ar.uba.dc.galli.qa.ml.utils.Utils;
import sg.edu.nus.wing.qanus.framework.commons.DataItem;

public class QuestionAndAnswers {

	public Question question;
	public String[] answers;
	public String lang_year = Configuration.LANG_YEAR;
	public int lucene_results = Configuration.LUCENERESULTS;
	public int n_passages = Configuration.N_PASSAGES;
	public int passage_rank = Configuration.PASSAGE_RANK;
	public int querygeneration = Configuration.QUERYGENERATION;
	public int topic_inference = Configuration.TOPIC_INFERENCE;
	public int answers_per_question = Configuration.ANSWERS_PER_QUESTION;

	public QuestionAndAnswers(Question in_question, DataItem[] in_answers) {
		question = in_question;
		answers = new String[in_answers.length];
		
		for (int i = 0; i < in_answers.length; i++) 
		{
			if(in_answers[i] != null)
			{
				answers[i] = in_answers[i].GetAttribute("answer");
				
			}
			else
			{
				answers[i] = "NIL";
			}
		}
	
	}
	
	public String toGson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
