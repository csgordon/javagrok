package uclisp;

import java.util.Vector;

public class Bmin extends Function {
	@Override
	public Object evaluate(Interpreter interp, Vector sexp)
			throws RunTimeException {
		try {
			// There is at least one argument, hence 
			// it is okay to initialize the minimum like this
            int min = Integer.MAX_VALUE;

            for (int i = 0; i < sexp.size(); i++) {
                Integer ival = (Integer)interp.evaluateSExp(sexp.elementAt(i));
                if (ival <= min) {
                	min = ival;
                }
            } 

            return new Integer(min);

        } catch (ClassCastException cce) {
            throw new RunTimeException("Non-integer type used for " +
                                       "minimum expression.", sexp);
        }
	}
	
	@Override
	public void verifyArguments(Vector sexp) throws RunTimeException {
		if (sexp.size() < 1)
            throw new RunTimeException("Incorrect number of arguments to min.", sexp);
	}
}
