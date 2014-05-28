package ar.uba.dc.galli.qa.ml.utils.comparators;

import ar.uba.dc.galli.qa.ml.ar.featurescoring.*;

/**
 *	This feature computes the distance between the occurences of two search strings within
 * a passage.
 *  Typically the search string may span multiple word tokens in the passage.
 * 
 *  To compute the distance, we will compute the midpoint of the span of the two
 * search strings, and find the difference between the two midpoints.
 * 
 *  ... X .. X... X .... Y ... Y
 *           |              |
 *            <------------>
 * 
 *  where X are matches to search string 1 and Y are matches to the other search string.
 *
 *
 * @author NG, Jun Ping -- ngjp@nus.edu.sg
 * @version 24Dec2009
 * @see	FeatureSearchTermProximity
 *
 */
public class ProximityComparator extends BaseComparator {

	public ProximityComparator(String input) {
		super(input);
	
	}

	public ProximityComparator() {
	}

	@Override
	public boolean compare(String other) {
		Feature feature = new FeatureSearchTermProximity();
		
		result_confidence = ignore_case ? feature.GetScore(other.toLowerCase(), one.toLowerCase()): feature.GetScore(other, one);
		
		return result_confidence > trueness_threshold;
	}

	@Override
	public void subclass_constructor() {
		name = "proximity";
		method_confidence = 0.5;
		exact = false; 
		
	}

}
