package ar.uba.dc.galli.qa.ml.utils.comparators;

public class ContainsComparator extends BaseComparator {

	public ContainsComparator(String input) {
		super(input);
	
	}
	
	public ContainsComparator() {
	}

	@Override
	public boolean compare(String other) {
		
		return ignore_case? one.toLowerCase().contains(other.toLowerCase()) : one.contains(other);
		
	}

	@Override
	public void subclass_constructor() {
		name = "contains";
		method_confidence = 1.0;
		result_confidence = 1.0;
		exact = false; 
		
	}

}
