package uclisp;

import java.util.Vector;

public class Bcar extends Function {
	@Override
	public Object evaluate(Interpreter interp, Vector sexp)
			throws RunTimeException {
		
		try {
			Object o = interp.evaluateSExp(sexp.elementAt(0));
			if (o instanceof Nil) {
				return new Nil();			
			}
			List list = (List)o;
			if (list.size() < 1) {
				return new Nil();
			}
			else {
				return list.get(0);			
			}			
		} catch (ClassCastException cce) {
		        throw new RunTimeException("Non-list type used for car.", sexp);
		}
		
	}
	
	@Override
	public void verifyArguments(Vector sexp) throws RunTimeException {
		if (sexp.size() != 1)
            throw new RunTimeException("Incorrect number of arguments to car.", sexp);
	}
}
