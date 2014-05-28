package ar.uba.dc.galli.qa.ml.utils.comparators;

import ar.uba.dc.galli.qa.ml.ar.featurescoring.*;

/**
 * Ver la clase implementadora
 * @author NG, Jun Ping -- ngjp@nus.edu.sg
 * @version 24Dec2009
 * @see	FeatureSearchTermSpan
 *
 */
public class SpanComparator extends BaseComparator {

	public SpanComparator(String input) {
		super(input);
	
	}

	public SpanComparator() {
	}

	@Override
	public boolean compare(String other) {
		Feature feature = new FeatureSearchTermSpan();
		
		result_confidence = ignore_case ? feature.GetScore(other.toLowerCase(), one.toLowerCase()): feature.GetScore(other, one);
		
		return result_confidence > trueness_threshold;
	}

	@Override
	public void subclass_constructor() {
		name = "span";
		method_confidence = 0.5;
		exact = false; 
		
	}

}
