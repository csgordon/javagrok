package org.javagrok.analysis;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCTypeAnnotation;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public class Uno extends AbstractAnalyzer {
	
	private static boolean ADD_NOT_ESCAPING = false;
	private static boolean ADD_ESCAPING = false;
	
	private static boolean ADD_NOT_RETAINED = true;
	private static boolean ADD_RETAINED = true;

	private static boolean ADD_UNIQUE_RETURN = true;
	private static boolean ADD_NONUNIQUE_RETURN = true;
	
	private static boolean ADD_NOT_RETAINED_THIS = false;
	private static boolean ADD_RETAINED_THIS = false;
	
	private boolean unoFileExists = false;
	private int nonAnnotatedMethods = 0;
	private boolean emitErrorAboutMissingProperty = true;
	
	private HashSet<String> visitedKeys = new HashSet<String>();

	@Override
	public void init(AnalysisContext ctx) {
		File file = new File("dist/uno-result/out");
		if (!file.exists()) {
			ctx.info("UNO: analysis file does not exist (" + file.getAbsolutePath() + ")");
			return;
		}
		try {
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String line;
			ctx.info("UNO: reading and parsing file...");
			int i = 0;
			while ((line = br.readLine()) != null) {
				parseLine(line, ctx);
				i++;
			}
			unoFileExists = true;
			ctx.info("UNO: Processed " + i + " lines.");
			in.close();
		}
		catch (IOException e) {
			ctx.info("UNO: Could not read file: " + e.getMessage());
		}
		catch (Exception e) {
			ctx.info("UNO: Caught exception: " + e.getMessage());
		}
		ctx.info("UNO: analysis initialized");
	}

	// from interface Analyzer
    public void process(final AnalysisContext ctx, Set<? extends Element> elements) {
    	if (!unoFileExists) return;
        for (Element elem : elements) {
            // we'll get an Element for each top-level class in a compilation unit (source file),
            // but scanning the AST from the top-level compilation unit will visit all classes
            // defined therein, which would result in adding annotations multiple times for source
            // files that define multiple top-level classes; so we specifically find the
            // JCClassDecl in the JCCompilationUnit that corresponds to the class we're processing
            // and only traverse its AST subtree
            Symbol.ClassSymbol csym = (Symbol.ClassSymbol)elem;
            JCCompilationUnit unit = ctx.getCompilationUnit(elem);
            for (JCTree def : unit.defs) {
                if (def.getTag() == JCTree.CLASSDEF && ((JCClassDecl)def).name == csym.name) {
                    def.accept(new UnoScanner(ctx, unit.getPackageName().toString()));
                }
            }
        }
    }
	
    class UnoScanner extends TreeScanner {
    	
    	private static final int NUMBER_OF_MISSING_INFORMATION_TO_DISPLAY = 5;
		private AnalysisContext ctx;
		private String packageName;
		private String className = "";
    	
		public UnoScanner(AnalysisContext ctx, String packageName) {
			this.ctx = ctx;
			this.packageName = packageName;
		}

		@Override
		public void visitClassDef(JCClassDecl tree) {
			if (!unoFileExists) return;
			Name name = tree.getSimpleName();
			if (name == null || "".equals(name.toString())) {
        		return; // Ignore classes without a name.
        	}
			
			String oldClassName = this.className;
			// Build correct class name for inner classes
			if (this.className.equals("")) {
				this.className = tree.getSimpleName().toString();				
			}
			else { 
				this.className += "$" + tree.getSimpleName().toString();
			}
			
			// Iterate over all fields of the class
			List<JCTree> members = tree.getMembers();
			for (JCTree m : members) {
				if (m instanceof JCVariableDecl) {
					JCVariableDecl var = (JCVariableDecl) m; // var is a field of the class

					JCModifiers modifiers = var.getModifiers();
					
					String key = this.packageName + "." + this.className + "." + var.getName();
									
					//if (visitedKeys.contains(key) || !modifiers.getFlags().contains(Modifier.PRIVATE)) {
					if (visitedKeys.contains(key) || !modifiers.toString().contains("private")) {
						continue;
					}
					visitedKeys.add(key);
					if (trueFieldProperties.containsKey(key) && trueFieldProperties.get(key).contains(UnoProperty.NESCFIELD)) {
						if (ADD_NOT_ESCAPING) {
							ctx.info("UNO: Adding annotation to field: " + key + " : @NotEscaping");							
							ctx.addAnnotation(var, NotEscaping.class);
						}
					}
					else if (falseFieldProperties.containsKey(key) && falseFieldProperties.get(key).contains(UnoProperty.NESCFIELD)) {
						if (ADD_ESCAPING) {
							ctx.info("UNO: Adding annotation to field: " + key + " : @Escaping");
							ctx.addAnnotation(var, Escaping.class);							
						}
					}
					else {
						//ctx.info("UNO: Can't find NESCFIELD field information for key: " + key);
					}
				}
			}
			
			super.visitClassDef(tree);
			
			// Rebuild correct class name when we leave an inner class
			this.className = oldClassName;
		}
		
        public void visitMethodDef (JCMethodDecl tree) {
        	if (!unoFileExists) return;
			JCTree returnType = tree.getReturnType();
			String returnTypeString = resolveTypeToString(returnType);
			
			String key = this.packageName + "." + this.className + " " + returnTypeString + " " + tree.getName() + "(" + resolveParameterList(tree) + ")" ;

        	if (visitedKeys.contains(key)) {
				return;
			}
			visitedKeys.add(key);
			
			// Add uniqueness information of returned object
			if (!(returnType instanceof JCTree.JCPrimitiveTypeTree) && !"java.lang.String".equals(resolveTypeToString(returnType))) {
				if (trueMethodProperties.containsKey(key) && trueMethodProperties.get(key).contains(UnoProperty.UNIQRET)) {
					if (ADD_UNIQUE_RETURN) {
						ctx.info("UNO: Adding annotation for method: " + key + " : @UniqueReturn");
						ctx.addAnnotation(tree, UniqueReturn.class);						
					}
				}
				else if (falseMethodProperties.containsKey(key) && falseMethodProperties.get(key).contains(UnoProperty.UNIQRET)) {
					if (ADD_NONUNIQUE_RETURN) {
						ctx.info("UNO: Adding annotation for method: " + key + " : @NonUniqueReturn");
						ctx.addAnnotation(tree, NonUniqueReturn.class);						
					}
				}
				else {
					nonAnnotatedMethods++;
					if (emitErrorAboutMissingProperty) {
						ctx.info("UNO: No property for method with key: \"" + key + "\". Was the file we read in from UNO really generated with the same source code we are analyzing now?");        			
						if (nonAnnotatedMethods > NUMBER_OF_MISSING_INFORMATION_TO_DISPLAY) {
							ctx.info("UNO: Future missing property messages ommited...");
							emitErrorAboutMissingProperty = false;
						}        			
					}
				}	
			}
			
			// Add retention information of receiver object
			
			if (trueMethodProperties.containsKey(key) && trueMethodProperties.get(key).contains(UnoProperty.LENTBASE)) {
				if (ADD_NOT_RETAINED_THIS) {
					ctx.info("UNO: Adding annotation for method: " + key + " : @NotRetainedThis");
					ctx.addAnnotation(tree, NotRetainedThis.class);
				}
			}
			else if (falseMethodProperties.containsKey(key) && falseMethodProperties.get(key).contains(UnoProperty.LENTBASE)) {
				if (ADD_RETAINED_THIS) {
					ctx.info("UNO: Adding annotation for method: " + key + " : @RetainedThis");
					ctx.addAnnotation(tree, RetainedThis.class);
				}
			}
			else {
				//ctx.info("UNO: Can't find LentThis field information for key: " + key);
			}
        	
			// Add parameter retention information
            int i = 0; // i is the parameter index
            for (JCVariableDecl param : tree.params) {
            	key = this.packageName + "." + this.className + " " + returnTypeString + " " + tree.getName() + "(" + resolveParameterList(tree) + ")" + "[" + i + "]";
            	if (trueParameterProperties.containsKey(key) && trueParameterProperties.get(key).contains(UnoProperty.LENTPAR)) {
            		if (ADD_NOT_RETAINED) {
            			ctx.info("UNO: Adding annotation for parameter: " + key + " : @NotRetained");
            			ctx.addAnnotation(param, NotRetained.class);            			
            		}
            	}
            	else if (falseParameterProperties.containsKey(key) && falseParameterProperties.get(key).contains(UnoProperty.LENTPAR)) {
            		if (ADD_RETAINED) {
            			ctx.info("UNO: Adding annotation for parameter: " + key + " : @Retained");
            			ctx.addAnnotation(param, Retained.class);            			
            		}
            	}
            	else {
            		//ctx.info("UNO: No property for parameter with key: \"" + key + "\".");        			
            	}
                i++;
            }
        }

		private String resolveParameterList(JCMethodDecl tree) {
			String paramList = "";
			boolean first = true;
			
			for (JCVariableDecl var : tree.getParameters()) {
				String str = resolveTypeToString(var.getType());
				
				
				if (first) {
					first = false;
					paramList = str;
				} 
				else {
					paramList = paramList + "," + str;					
				}
			}
			
			
			return paramList;
		}
		
		// Helper method to resolve a JCTree which is supposed
		// to represent a type into a string which can be
		// used to generate a key for the hash map.
		private String resolveTypeToString(JCTree type) {
			String typeString = "";
			if (type == null) {
				return "void";
			}
			
			if (type instanceof JCTree.JCTypeApply) {
				type = ((JCTree.JCTypeApply) type).getType();
			}
			
			if (type instanceof JCTree.JCIdent) {
				typeString = ((JCTree.JCIdent) type).sym.getQualifiedName().toString();
			}
			else if (type instanceof JCTree.JCPrimitiveTypeTree) {
				typeString = ((JCTree.JCPrimitiveTypeTree) type).getPrimitiveTypeKind().toString().toLowerCase();
			}
			else if (type instanceof JCTree.JCArrayTypeTree) {
				typeString = resolveTypeToString(((JCTree.JCArrayTypeTree) type).elemtype) + "[]";
			}
			else {
				typeString = type.getClass().getName();					
			}
			
			return typeString;
		}
      
    }
    
    private enum UnoProperty {
    	UNIQRET, LENTPAR, NESCPAR, LENTBASE, OWN, OWNPAR, STORE, NESCFIELD, OWNFIELD, UNIQPAR
    }
    
    private HashMap<String, HashSet<UnoProperty>> trueFieldProperties = new HashMap<String, HashSet<UnoProperty>>();
    private HashMap<String, HashSet<UnoProperty>> trueMethodProperties = new HashMap<String, HashSet<UnoProperty>>();
    private HashMap<String, HashSet<UnoProperty>> trueParameterProperties = new HashMap<String, HashSet<UnoProperty>>();
    private HashMap<String, HashSet<UnoProperty>> falseFieldProperties = new HashMap<String, HashSet<UnoProperty>>();
    private HashMap<String, HashSet<UnoProperty>> falseMethodProperties = new HashMap<String, HashSet<UnoProperty>>();
    private HashMap<String, HashSet<UnoProperty>> falseParameterProperties = new HashMap<String, HashSet<UnoProperty>>();
    
    UnoProperty getProptertyFromString(String str) {
    	if ("UniqRet".equals(str)) {
    		return UnoProperty.UNIQRET;
    	}
    	else if ("LentPar".equals(str)) {
    		return UnoProperty.LENTPAR;
    	}
    	else if ("NEscPar".equals(str)) {
    		return UnoProperty.NESCPAR;
    	}
    	else if ("UniqPar".equals(str)) {
    		return UnoProperty.UNIQPAR;
    	}
    	else if ("LentBase".equals(str)) {
    		return UnoProperty.LENTBASE;
    	}
    	else if ("Own".equals(str)) {
    		return UnoProperty.OWN;
    	}
    	else if ("OwnPar".equals(str)) {
    		return UnoProperty.OWNPAR;
    	}
    	else if ("Store".equals(str)) {
    		return UnoProperty.STORE;
    	}
    	else if ("NEscField".equals(str)) {
    		return UnoProperty.NESCFIELD;
    	}
    	else if ("OwnField".equals(str)) {
    		return UnoProperty.OWNFIELD;
    	}
    	if (str == null) {
    		throw new IllegalArgumentException("str is null");
    	}
    	throw new IllegalArgumentException(str + " is not a property");
     }
    
    private void addPropertyForField(String property, boolean holds, String className, String fieldName) {
    	UnoProperty p = getProptertyFromString(property);
    	String key = className + "." + fieldName;
    	HashMap<String, HashSet<UnoProperty>> map;
    	if (holds) {
    		map = trueFieldProperties;
    	}
    	else {
    		map = falseFieldProperties;
    	}
    	addPropertyToMap(p, key, map);
    }

	private void addPropertyToMap(UnoProperty p, String key,
			HashMap<String, HashSet<UnoProperty>> map) {
		HashSet<UnoProperty> set = map.get(key);
    	if (set != null) {
    		set.add(p);
    	}
    	else {
    		set = new HashSet<UnoProperty>();
    		set.add(p);
    		map.put(key, set);
    	}
	}
    
    private void addPropertyForMethod(String property, boolean holds, String className, String methodName, String returnType) {
    	UnoProperty p = getProptertyFromString(property);
    	String key = className + " " + returnType + " " + methodName;
    	HashMap<String, HashSet<UnoProperty>> map;
    	if (holds) {
    		map = trueMethodProperties;
    	}
    	else {
    		map = falseMethodProperties;
    	}
    	addPropertyToMap(p, key, map);
    }
    
    private void addPropertyForParameter(String property, boolean holds, String className, String methodName, int paramIndex, String returnType) {
    	UnoProperty p = getProptertyFromString(property);
    	String key = className + " " + returnType + " " + methodName + "[" + paramIndex + "]";
    	HashMap<String, HashSet<UnoProperty>> map;
    	if (holds) {
    		map = trueParameterProperties;
    	}
    	else {
    		map = falseParameterProperties;
    	}
    	addPropertyToMap(p, key, map);
    }
    
	private void parseLine(String line, AnalysisContext ctx) {
		line = removeCommentAndTrim(line);
		if (line.length() < 2) {
			throw new IllegalArgumentException("line should be longer");
		}
		// First char tells if its a method annotation or a parameter annotation
		char firstChar = line.charAt(0);
		
		
		line = line.substring(2).trim();
		String[] parts = line.split(" ");
		if (parts.length < 4) {
			String text = "UNO information should have 4 parts";
			ctx.info("UNO: " + text);
			throw new IllegalArgumentException(text);
		}

		String property = parts[0];		

		// We are not interested in results in which UNO is unsure
		if (!("true?".equals(parts[1].toLowerCase()))) {
			Boolean holds = Boolean.parseBoolean(parts[1]); 
			String className = parts[2];
			String methodOrFieldName;
			String returnType;
			
			switch (firstChar) {
			case 'm':  // Method annotation: m UniqRet True org.javagrok.test.TestMain void main(java.lang.String[])
				returnType = parts[3];
				methodOrFieldName = parts[4];
				addPropertyForMethod(property, holds, className, methodOrFieldName, returnType);
				break;
			case 'p':  // Parameter annotation: p NEscPar True org.javagrok.test.TestMain void main(java.lang.String[]) 0 java.lang.String[] [True] <-- last one optional
				if (parts.length < 6) {
					String text = "Parameter information should have 6 or 7 parts";
					ctx.info("UNO: " + text);
					throw new IllegalArgumentException(text);
				}
				try {
					returnType = parts[3];
					methodOrFieldName = parts[4];
					int parameterNumber = Integer.parseInt(parts[5]);
					String parameterType = parts[6];
					String nofield = "";
					if (parts.length == 8) {
						nofield = parts[7];
					}
					
					/* If nofield is false it means that the reference might get retained in a local field.
					 * In the case of LentPar it means that the property of lending gets weakened and
					 * it also includes now that the reference might be retained in a local field and UNO
					 * would still consider it to be only lent and not retained.
					 * Note: We are not interested in those properties so we do not add it to the properties
					 * data structure.
					 */
					if (Boolean.parseBoolean(nofield) || nofield.equals("")) {
						addPropertyForParameter(property, holds, className, methodOrFieldName, parameterNumber, returnType);					
					}
				} 
				catch (NumberFormatException e) {
					String text = "Parameter number was not an integer";
					ctx.info("UNO: " + text);
					throw new IllegalArgumentException(text);
				}
				break;
			case 'f':  // Field annotation: f NEscField True org.javagrok.test.TestMain _box
				methodOrFieldName = parts[3];
				addPropertyForField(property, holds, className, methodOrFieldName);
				break;
			default:
				String text = "line has to begin with either m or p";
				ctx.info("UNO: " + text);
				throw new IllegalArgumentException(text);
			}
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
