package ar.uba.dc.galli.qa.ml.utils.comparators;

import ar.uba.dc.galli.qa.ml.ar.featurescoring.*;

/**
 *  ocurrences of 'other' in 'one' / length('other'). Return a value between 0 and 1.
 *  One cover other, that's the order
 *  first covers (is bigger than**) second
 * @author julian
 * @see	FeatureSearchTermCoverage
 *
 */
public class CoverageComparator extends BaseComparator {

	public CoverageComparator(String input) {
		super(input);
	
	}

	public CoverageComparator() {
	}

	@Override
	public boolean compare(String other) {
		Feature feature = new FeatureSearchTermCoverage();
		
		result_confidence = ignore_case ? feature.GetScore(other.toLowerCase(), one.toLowerCase()): feature.GetScore(other, one);
		
		return result_confidence >= trueness_threshold;
	}

	@Override
	public void subclass_constructor() {
		name = "coverage";
		method_confidence = 0.7;
		exact = false; 
		
	}

}
