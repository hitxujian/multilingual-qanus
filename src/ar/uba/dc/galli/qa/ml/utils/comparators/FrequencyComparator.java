package ar.uba.dc.galli.qa.ml.utils.comparators;

import ar.uba.dc.galli.qa.ml.ar.featurescoring.*;

/**
 * count the number of times words in other appear in one.
 * result is the ratio of count over the length of other
 * The score will be between 0 and 1 inclusive.
 * @author julian
 * @see	FeatureSearchTermCoverage
 *
 */
public class FrequencyComparator extends BaseComparator {

	public FrequencyComparator(String input) {
		super(input);
	
	}

	public FrequencyComparator() {
	}

	@Override
	public boolean compare(String other) {
		Feature feature = new FeatureSearchTermSpan();
		
		result_confidence = ignore_case ? feature.GetScore(other.toLowerCase(), one.toLowerCase()): feature.GetScore(other, one);
		
		return result_confidence > trueness_threshold;
	}

	@Override
	public void subclass_constructor() {
		name = "frequency";
		method_confidence = 0.5;
		exact = false; 
		
	}

}
