//
// Bprint - a builtin function to print out an sexp

package uclisp;

import org.javagrok.analysis.Retained;
import org.javagrok.analysis.NotRetained;
import org.javagrok.analysis.ExceptionProperty;
import org.javagrok.analysis.LentThis;
import org.javagrok.analysis.UniqueReturn;
import java.io.PrintStream;

import java.util.Vector;

public class Bprint extends Function
{
    //
    // Bprint public member functions

    @LentThis
    @UniqueReturn
    public Object evaluate (@NotRetained Interpreter interp, @NotRetained Vector sexp)
        throws RunTimeException
    {
        return evaluate(interp, sexp, "");
    }
    
    // Special evaluate taking a seperator
    
    private Object evaluate(Interpreter interp, Vector sexp, String sep) throws RunTimeException {
    	for (int i = 0; i < sexp.size(); i++) {
            printValue(interp, sexp.elementAt(i));
            if (i < sexp.size()-1) {
            	_out.print(sep);
            }
        }
        _out.flush();
        return new Nil();
	}
    

    //
    // Bprint protected member functions

    @LentThis
    @ExceptionProperty(throwsWhen="<br>RunTimeException when !(v instanceof Vector)", exceptionsThrown="explicitly throws RunTimeException")
    public void printValue (@NotRetained Interpreter interp, @Retained Object v)
        throws RunTimeException
    {
        if ((v instanceof String) ||
            (v instanceof Integer)) {
            _out.print(v);

        } else if (v instanceof Nil) {
            _out.print("nil");

        } else if (v instanceof List) {
        	_out.print("(");
            evaluate(interp, (Vector)v, " ");
            _out.print(")");

        } else if (v instanceof Name) {
            printValue(interp, interp.evaluateSExp(v));

        } else if (v instanceof Vector) {
            printValue(interp, interp.evaluateSExp(v));

        } else {
            throw new RunTimeException("print: invalid type: " +
                                       v.getClass().getName(), v);
        }
    }

	//
    // Bprint public static member functions



	public static void setOutputStream (@Retained PrintStream out)
    {
        _out = out;
    }

    //
    // Bprint protected static data members

    static PrintStream _out = System.out;
}
