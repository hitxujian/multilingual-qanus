
package ar.uba.dc.galli.qa.ml.ar;

/**
 * Used to store an answer candidate, alongside the source passage from which it
 * is extracted.
 *
 * @author NG, Jun Ping -- junping@comp.nus.edu.sg
 * @version 04Jan2010
 */
public class AnswerCandidate implements Comparable{

	private String m_Answer;
	private String m_Source_Orig;
	private double m_Score = 0.0;  // Score of document
	

	/**
	 * Constructor
	 * @param a_Answer [in] answer candidate string
	 * @param a_Source [in] original source of candidate
	 * @param a_SourcewTags [in] source of candidate annotated with POS
	 */
	public AnswerCandidate(String a_Answer, String a_Source) {
		m_Answer = a_Answer;
		m_Source_Orig = a_Source;		
	}
	
	public AnswerCandidate(String a_Answer, String a_Source, double a_Score) {
		m_Answer = a_Answer;
		m_Source_Orig = a_Source;
		m_Score = a_Score;
	}

	public String GetAnswer() {
		return m_Answer;
	}

	public String GetOrigSource() {
		return m_Source_Orig;
	}
	
	public double GetScore() {
		return m_Score;
	}
	
	public int compareTo(Object o) {
		if (o == null) {
			return -1;
		}
		AnswerCandidate l_Object = (AnswerCandidate) o;
		// We want objects with higher score to go in front, to be used in Java's priority queue
		// This is the score is larger, we return -1
		if (m_Score > l_Object.GetScore()) {
			return -1;
		} else if (m_Score == l_Object.GetScore()) {
			return 0;
		} else {
			return 1;
		}
	} // end compareTo()

	
	@Override
	public boolean equals(Object o) {
		// Check for self-comparison
		if (this == o) {
			return true;
		}
		//use instanceof instead of getClass here for two reasons
		//1. if need be, it can match any supertype, and not just one class;
		//2. it renders an explict check for "that == null" redundant, since
		//it does the check for null already - "null instanceof [type]" always
		//returns false. (See Effective Java by Joshua Bloch.)
		if (!(o instanceof AnswerCandidate)) {
			return false;
		}
		//Alternative to the above line :
		//if ( aThat == null || aThat.getClass() != this.getClass() ) return false;
		//cast to native object is now safe
		AnswerCandidate l_Object = (AnswerCandidate) o;
		//now a proper field-by-field evaluation can be made
		return m_Answer.toLowerCase().compareToIgnoreCase( l_Object.GetAnswer().toLowerCase()) == 0 && 
				m_Source_Orig.toLowerCase().compareToIgnoreCase( l_Object.GetOrigSource().toLowerCase()) == 0;
	} // end equals()



} // end class AnswerCandidate
