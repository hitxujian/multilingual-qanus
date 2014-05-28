package ar.uba.dc.galli.qa.ml.utils.comparators;

public class EntityComparator extends BaseComparator {

	public EntityComparator(String input) {
		super(input);
	}

	public EntityComparator() {
	}

	@Override
	public void subclass_constructor() {
		name = "eq";
		method_confidence = 1.0;
		result_confidence = 1.0;
		symetric = false;
		exact = true;
	}

	@Override
	public boolean compare(String other) {
		EqualComparator eq = new EqualComparator();
		eq.setIgnoreCase(true);
		if(eq.compare(one, other)) return true;
		ContainsComparator contains = new ContainsComparator();
		contains.setIgnoreCase(true);
		if(contains.compare(one, other))
		{
			result_confidence = 0.5;
			exact= false;
			return true;
		}
		return false;
	}

}
