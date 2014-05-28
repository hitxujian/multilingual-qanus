/**
 * 
 */
package ar.uba.dc.galli.qa.ml.utils.comparators;

/**
 * an
 * @author julian
 *
 */
public abstract class BaseComparator {

	/**The left-side string to compare. You can populate it on instance creation*/
	public String one;
	/**The confidence of the method to comparator results method_confidence*result_confidence*/
	public double method_confidence = 0;
	/**The confidence inmanent to comparator results*/
	public double result_confidence = 0;
	/**If the comparator is an exact matching (equals method_confidece = 1)*/
	public boolean exact = false;
	/**The name of the comparator*/
	public String name;
	/**if comparations must ignore case or not*/
	public boolean ignore_case = true;
	/**For double comparators, binary classifies the result in true or false according to this*/
	public double trueness_threshold = 0.75;
	
	/** If the comparator is symetric in the order of the parameters*/
	public boolean symetric = false;
	
	/**
	 * 
	 */
	public BaseComparator(String input) {
		one = input;
		name = this.getClass().getCanonicalName();
		subclass_constructor();
	}
	
	public BaseComparator() 
	{
		name = this.getClass().getCanonicalName();
		subclass_constructor();
	}
	
	public void setIgnoreCase(boolean input)
	{
		ignore_case = input;
	}
	
	public abstract void subclass_constructor();
	
	public abstract boolean compare(String other);
	
	public boolean compare(String in_one, String other)
	{
		one = in_one;
		return compare(other);
	}
	
	public boolean compareInv(String in_one, String other)
	{
		one = other;
		return compare(in_one);
	}
	
	public boolean compareInv(String in_other)
	{
		BaseComparator other;
		try {
			other = this.getClass().newInstance();
			other.one = in_other;
			return other.compare(one);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return false;
		
		
		
		
	}

}
