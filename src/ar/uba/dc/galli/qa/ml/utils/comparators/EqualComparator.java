/**
 * 
 */
package ar.uba.dc.galli.qa.ml.utils.comparators;

/**
 * compares strings by strict equality
 * @author julian
 *
 */
public class EqualComparator extends BaseComparator {

	/**
	 * @param one of the string to compare
	 */
	public EqualComparator(String input) {
		super(input);
		
	}
	
	public EqualComparator() {
		super();

	}
	
	public boolean compare(String other)
	{
		return ignore_case? one.compareToIgnoreCase(other) == 0: one.compareTo(other) == 0;
	}

	@Override
	public void subclass_constructor() {
		name = "eq";
		method_confidence = 1.0;
		result_confidence = 1.0;
		symetric = true;
		exact = true; 
	}

}
