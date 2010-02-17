package org.javagrok.analysis;

import java.io.File;
import java.util.Set;

import javax.lang.model.element.Element;

public class Uno extends AbstractAnalyzer {
	
	

	@Override
	public void init(AnalysisContext ctx) {
		System.out.println("Current dir: " + (new File(".")).toString());
	}

	@Override
	public void process(AnalysisContext ctx, Set<? extends Element> elements) {
		// TODO Auto-generated method stub

	}
	
	private void parseLine(String line) {
		line = removeCommentAndTrim(line);
		if (line.length() < 2) {
			throw new IllegalArgumentException("line should be longer");
		}
		// First char tells if its a method annotation or a parameter annotation
		char firstChar = line.charAt(0);
		
		line = line.substring(2).trim();
		String[] parts = line.split(" ");
		if (parts.length != 4) {
			throw new IllegalArgumentException("Method information should have 4 parts");
		}
		int i = 0;
		String property = parts[i++];
		String className = parts[i++];
		String methodName = parts[i++];
		
		
		Boolean holds = false;
		switch (firstChar) {
		case 'm':  // Method annotation
			holds = Boolean.parseBoolean(parts[i++]);
			
			System.out.println("---");
			System.out.println("Property: " + property);
			System.out.println("ClassName: " + className);
			System.out.println("MethodName: " + methodName);
			System.out.println("Property holds: " + holds);
			break;
		case 'p':  // Parameter annotation		
			try {
				int parameterNumber = Integer.parseInt(parts[i]);
				i++;
				String parameterName = parts[i++];
				holds = Boolean.parseBoolean(parts[i++]);
				System.out.println("---");
				System.out.println("Property: " + property);
				System.out.println("ClassName: " + className);
				System.out.println("MethodName: " + methodName);
				System.out.println("Parameter: " + parameterName + " (" + parameterNumber + ")");
				System.out.println("Property holds: " + holds);
			} 
			catch (NumberFormatException e) {
				throw new IllegalArgumentException("Parameter number was not an integer: " + parts[i]);
			}
			break;
		default:
			throw new IllegalArgumentException("line has to begin with either m or p");
		}

	}

	private String removeCommentAndTrim(String line) {
		String returnString = line;
		int startPosOfComment = returnString.indexOf("//");
		if (startPosOfComment >= 0) {
			returnString = returnString.substring(0, startPosOfComment);
		}
		return returnString.trim();
	}
}
