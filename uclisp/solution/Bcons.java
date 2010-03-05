package uclisp;

import java.util.Vector;

public class Bcons extends Function {
	@Override
	public void verifyArguments(Vector sexp) throws RunTimeException {
		if (sexp.size() != 2)
            throw new RunTimeException("Incorrect number of arguments to cons.", sexp);
		super.verifyArguments(sexp);
	}
	
	@Override
	public Object evaluate(Interpreter interp, Vector sexp)
			throws RunTimeException {
		List newList = new List();
		Object head = interp.evaluateSExp(sexp.elementAt(0));
		Object tail = interp.evaluateSExp(sexp.elementAt(1));
		
		newList.add(head);
		if (tail instanceof List) {
			newList.addAll((List)tail);			
		}
		
		return newList;
	}
}
