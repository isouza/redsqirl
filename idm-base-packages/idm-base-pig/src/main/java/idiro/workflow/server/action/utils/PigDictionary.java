package idiro.workflow.server.action.utils;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.EditorInteraction;
import idiro.workflow.server.action.AbstractDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Utilities for writing Pig Latin operations. The class can: - generate a help
 * for editing operations - check an operation
 * 
 * @author etienne
 * 
 */
public class PigDictionary extends AbstractDictionary {

	private static Logger logger = Logger.getLogger(PigDictionary.class);
	/**
	 * Key for logical operators
	 */
	private static final String logicalOperators = "logicalOperators";
	/** Key for relational operators */
	private static final String relationalOperators = "relationalOperators";
	/** Key for a cast operator */
	private static final String castOperator = "castOperator";
	/** Key for arithmetic operation */
	private static final String arithmeticOperators = "arithmeticOperators";
	/** Key for utils methods */
	private static final String utilsMethods = "utilsMethods";
	/** Key for math methods */
	private static final String mathMethods = "mathMethods";
	/** Key for string Methods */
	private static final String stringMethods = "stringMethods";
	/** Key for aggregation methods */
	private static final String agregationMethods = "agregationMethods";
	/** Key for aggregation methods */
	private static final String conditionalOperator = "conditionalOperator";
	/** Instance */
	private static PigDictionary instance;

	/**
	 * Get an instance of the dictionary
	 * 
	 * @return instance
	 */
	public static PigDictionary getInstance() {
		if (instance == null) {
			instance = new PigDictionary();
		}
		return instance;
	}

	/**
	 * Constructor
	 */
	private PigDictionary() {
		super();
	}

	/**
	 * Get the file name of the where the functions are stored
	 * 
	 * @return file name
	 */
	@Override
	protected String getNameFile() {
		return "functionsPig.txt";
	}

	/**
	 * Load the default funtions into a map
	 */
	protected void loadDefaultFunctions() {

		functionsMap = new HashMap<String, String[][]>();

		functionsMap
				.put(logicalOperators,
						new String[][] {
								new String[] {
										"AND",
										"BOOLEAN,BOOLEAN",
										"BOOLEAN",
										"@function:AND@short:Boolean AND@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the variables are equal@example:TRUE AND TRUE" },
								new String[] {
										"OR",
										"BOOLEAN,BOOLEAN",
										"BOOLEAN",
										"@function:OR@short:Boolean OR@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the varables are not the same@example:TRUE OR FALSE" },
								new String[] {
										"NOT",
										",BOOLEAN",
										"BOOLEAN",
										"@function:NOT@short:Boolean NOT@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the varables are  not equal@example:TRUE NOT FALSE" } });

		functionsMap
				.put(relationalOperators,
						new String[][] {
								new String[] {
										"<=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:<=@short:Less or equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the right value is less or equal to the right@example:1<=5 returns TRUE" },
								new String[] {
										">=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:>=@short:Greater or equal@param:Any value@param Any value@description:Compare the left value to the right and checks if the left value is greater or equal to the right@example:5>=1 returns TRUE" },
								new String[] {
										"<",
										"ANY,ANY",
										"BOOLEAN",
										"@function:<@short:Less than@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is less than the right@example:1<5 returns TRUE" },
								new String[] {
										">",
										"ANY,ANY",
										"BOOLEAN",
										"@function:>@short:Greater than@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is greater than the right@example:1>5 returns TRUE" },
								new String[] {
										"!=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:!=@short:Not Equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is not equal the right@example:1!=5 returns TRUE" },
								new String[] {
										"==",
										"ANY,ANY",
										"BOOLEAN",
										"@function:==@short:Equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is equal the right@example:5==5 returns TRUE" },
								new String[] {
										"IS NOT NULL",
										"ANY,",
										"BOOLEAN",
										"@function:IS NOT NULL@short:Is not empty/null@param:Any value@description:Checks the value if it is not null@example:x IS NOT NULL" },
								new String[] {
										"IS NULL",
										"ANY,",
										"BOOLEAN",
										"@function:IS NULL@short:Is empty/null@param:Any value@description:Checks the value if it is null@example:x IS NULL" },
								new String[] {
										"REGEX_EXTRACT",
										"STRING,STRING, INT",
										"STRING",
										"@function:REGEX_EXTRACT( MYSTRING , CHAR , INDEX )@short:Performs regular expression matching and extracts the matched group defined by an index parameter@param:MYSTRING string to search@param:CHAR The regular expression@param:INDEX The index of the matched group to return@description:Use the REGEX_EXTRACT function to perform regular expression matching and to extract the matched group defined by the index parameter (where the index is a 1-based parameter.) The function uses Java regular expression form."
												+ "The function returns a string that corresponds to the matched group in the position specified by the index. If there is no matched expression at that position, NULL is returned@example:REGEX_EXTRACT('helloworld#8020', '(.*)\\#(.*)', 1) returns 'helloworld'" } });

		functionsMap
				.put(castOperator,
						new String[][] { new String[] {
								"()",
								"TYPE,ANY",
								"TYPE",
								"@function:(TYPE)@short:Cast one type to another@param TYPE the type to cast the variable to@description:Cast one type to a target variable@example:(double)A.AGE" } });

		functionsMap.put(arithmeticOperators, new String[][] {
				new String[] { "+", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "-", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "*", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "/", "NUMBER,NUMBER", "NUMBER" },
				new String[] { "%", "NUMBER,NUMBER", "NUMBER" } });

		functionsMap
				.put(utilsMethods,
						new String[][] { new String[] {
								"RANDOM()",
								"",
								"DOUBLE",
								"@function:RANDOM()@short: Generate a random double@description:Generates a random double and returns it" }
						});

		functionsMap
				.put(mathMethods,
						new String[][] {
								new String[] {
										"ROUND()",
										"DOUBLE",
										"INT",
										"@function:ROUND()@short:Returns the value of an expression rounded to an integer@param:DOUBLE@description:Use the ROUND function to return the value of an expression rounded to an integer (if the result type is float) or rounded to a long (if the result type is double)@example:ROUND(4.6) returns 5@example:ROUND(2.3) returns 2" },
								new String[] {
										"FLOOR()",
										"DOUBLE",
										"INT",
										"@function:FLOOR()@short:Returns the value of an expression rounded down to the nearest integer@param:DOUBLE@description:Use the FLOOR function to return the value of an expression rounded down to the nearest integer. This function never increases the result value@example:FLOOR(4.6) returns 4@example:FLOOR(2.3) returns 2" },
								new String[] {
										"CEIL()",
										"DOUBLE",
										"INT",
										"@function:CEIL()@short:Returns the value of an expression rounded up to the nearest integer@param:DOUBLE@description:Use the CEIL function to return the value of an expression rounded up to the nearest integer. This function never decreases the result value@example:CEIL(4.6) returns 5@example:CEIL(2.3) returns 3" },
								new String[] {
										"ABS()",
										"NUMBER",
										"NUMBER",
										"@function:ABS()@short:Returns the absolute value of an expression@param:DOUBLE@description:Use the ABS function to return the absolute value of an expression. If the result is not negative (x ≥ 0), the result is returned. If the result is negative (x < 0), the negation of the result is returned@example:ABS(-36) returns 36@example:CEIL(5-7) returns 2" },
								new String[] {
										"ACOS()",
										"DOUBLE",
										"DOUBLE",
										"@function:ACOS()@short:Returns the arc cosine of an expression@param:DOUBLE@description:Use the ACOS function to return the arc cosine of an expression.@example:ACOS(0) returns 90@example:ACOS(0.5) returns 60" },
								new String[] {
										"ASIN()",
										"DOUBLE",
										"DOUBLE",
										"@function:ASIN()@short:Returns the arc sine of an expression@param:DOUBLE@description:Use the ASIN function to return the arc sine of an expression@example:ASIN(1) returns 90@example:ASIN(0.7071068) returns 45" },
								new String[] {
										"ATAN()",
										"DOUBLE",
										"DOUBLE",
										"@function:ATAN()@short:Returns the arc tangent of an expression@param:DOUBLE@description:Use the ASIN function to return the arc tangent of an expression@example:ATAN(1) returns 45@example:ATAN(-0.5) returns -26.56505118" },
								new String[] {
										"CBRT()",
										"DOUBLE",
										"DOUBLE",
										"@function:CBRT()@short:Returns the cube root of an expression@param:DOUBLE@description:Use the CBRT function to return the cube root of an expression@example:CBRT(3) returns  1.44225 @example:CBRT(726) returns 8.987637" },
								new String[] {
										"COS()",
										"DOUBLE",
										"DOUBLE",
										"@function:COS()@short:Returns the trigonometric cosine of an expression@param:DOUBLE@description:Use the COS function to return the trigonometric cosine of an expression@example:COS(45) returns  0.70710678 @example:COS(89) returns 0.01745241" },
								new String[] {
										"COSH()",
										"DOUBLE",
										"DOUBLE",
										"@function:COSH()@short:Returns the hyperbolic cosine of an expression@param:DOUBLE@description:Use the COSH function to return the hyperbolic cosine of an expression@example:COSH(45) returns  1.3246106846575 @example:COSH(89) returns 0.01745241" },
								new String[] {
										"EXP()",
										"DOUBLE",
										"DOUBLE",
										"@function:COSH()@short:Returns Euler's number e raised to the power of x@param:DOUBLE@description:Use the EXP function to return the value of Euler's number e raised to the power of x (where x is the result value of the expression)@example:EXP(2) returns  7.3890560991533 @example:EXP(89) returns  4.4896128251945E+38" },
								new String[] {
										"LOG()",
										"DOUBLE",
										"DOUBLE",
										"@function:LOG()@short:Returns the natural logarithm (base e) of an expression@param:DOUBLE@description:Use the LOG function to return the natural logarithm (base e) of an expression@example:LOG(1) returns  0 @example:EXP(89) returns  4.4886363697" },
								new String[] {
										"LOG10()",
										"DOUBLE",
										"DOUBLE",
										"@function:LOG10()@short:Returns the base 10 logarithm of an expression@param:DOUBLE@description:Use the LOG10 function to return the base 10 logarithm of an expression@example:LOG10(1) returns  0 @example:EXP10(89) returns  1.9493900066" },
								new String[] {
										"SIN()",
										"DOUBLE",
										"DOUBLE",
										"@function:SIN()@short:Returns the sine of an expression@param:DOUBLE@description:Use the SIN function to return the sine of an expession@example:SIN(90) returns  1 @example:SIN(45) returns  0.70710678" },
								new String[] {
										"SINH()",
										"DOUBLE",
										"DOUBLE",
										"@function:SINH()@short:Returns the hyperbolic sine of an expression@param:DOUBLE@description:Use the SINH function to return the hyperbolic sine of an expression@example:SINH(90) returns 2.301298902307@example:SIN(45) returns  0.868670961486" },
								new String[] {
										"SQRT()",
										"DOUBLE",
										"DOUBLE",
										"@function:SQRT()@short:Returns the positive square root of an expression@param:DOUBLE@description:Use the SQRT function to return the positive square root of an expression@example:SQRT(5) returns 2.2360679775@example:SQRT(45) returns  6.7082039325" },
								new String[] {
										"TAN()",
										"DOUBLE",
										"DOUBLE",
										"@function:TAN()@short:Returns the trignometric tangent of an angle@param:DOUBLE@description:Use the TAN function to return the trignometric tangent of an angle@example:TAN(45) returns 1@example:TAN(30) returns  0.57735027" },
								new String[] {
										"TANH()",
										"DOUBLE",
										"DOUBLE",
										"@function:TANH()@short:Returns the hyperbolic tangent of an expression@param:DOUBLE@description:Use the TANH function to return the hyperbolic tangent of an expression@example:TANH(45) returns 0.655794202633@example:TAN(90) returns  0.917152335667" }, });

		functionsMap
				.put(stringMethods,
						new String[][] {
								new String[] {
										"SUBSTRING()",
										"STRING,INT,INT",
										"STRING",
										"@function:SUBSTRING( MYSTRING , INDEX , LENGTH )@short:Returns a substring from a given string@param:MYSTRING The string from which a substring will be extracted@param: INDEX The index (type integer) of the first character of the substring."
												+ "The index of a string begins with zero (0)@param:LENGTH The index (type integer) of the character following the last character of the substring@description:Use the SUBSTRING function to return a substring from a given string."
												+ "Given a field named alpha whose value is ABCDEF, to return substring BCD use this statement: SUBSTRING(alpha,1,4). Note that 1 is the index of B (the first character of the substring) and 4 is the index of E (the character following the last character of the substring)@example:SUBSTR('help',1,4) returns 'elp'; @example:SUBSTR('example',6,7) returns  'le'" },
								new String[] {
										"UPPER()",
										"STRING",
										"STRING",
										"@function:UPPER( MYSTRING )@short:Returns a string converted to upper case@param:MYSTRING@description:Use the UPPER function to convert all characters in a string to upper case@example:UPPER('hello') returns 'HELLO'@example:UPPER('Example') returns  'EXAMPLE'" },
								new String[] {
										"LOWER()",
										"STRING",
										"STRING",
										"@function:LOWER( MYSTRING )@short:Converts all characters in a string to lower case@param:MYSTRING@description:Use the LOWER function to convert all characters in a string to lower case@example:LOWER('HELLO') returns 'hello'@example:LOWER('Example') returns  'example'" },
								new String[] {
										"LCFIRST()",
										"STRING",
										"STRING",
										"@function:LCFIRST( MYSTRING )@short:Converts the first character in a string to lower case@param:MYSTRING@description:Use the LCFIRST function to convert only the first character in a string to lower case@example:LCFIRST('HELLO') returns 'hELLO'@example:LCFIRST('Example') returns  'example'" },
								new String[] {
										"UCFIRST()",
										"STRING",
										"STRING",
										"@function:UCFIRST( MYSTRING )@short:Converts the first character in a string to upper case@param:MYSTRING@description:Use the UCFIRST function to convert only the first character in a string to upper case@example:UCFIRST('hELLO') returns 'HELLO'@example:UCFIRST('example') returns  'Example'" },
								new String[] {
										"TRIM()",
										"STRING",
										"STRING",
										"@function:TRIM( MYSTRING )@short:Returns a copy of a string with leading and trailing white space removed@param:MYSTRING@description:Use the TRIM function to remove leading and trailing white space from a string@example:TRIM(' hello ') returns 'hello'@example:TRIM(' example ') returns  'example'" },
								new String[] {
										"INDEXOF()",
										"STRING,STRING,INT",
										"INT",
										"@function:INDEXOF( MYSTRING , CHAR , INDEX )@short:Returns the index of the first occurrence of a character in a string. Searching forward from a start index@param:MYSTRING string to search@param:CHAR character to search for@param:INDEX index to start foreward search from@description:Use the INDEXOF function to determine the index of the first occurrence of a character in a string. The forward search for the character begins at the designated start index@example:INDEXOF('hello','e',0) returns 1@example:INDEXOF('example','l',1) returns  5" },
								new String[] {
										"LAST_INDEX_OF()",
										"STRING,CHAR,INT",
										"INT",
										"@function:LAST_INDEX_OF( MYSTRING , CHAR , INDEX )@short:Returns the index of the last occurrence of a character in a string. Searching backward from a start index@param:MYSTRING string to search@param:CHAR character to search for@param:INDEX index to start backward search from@description:Use the INDEXOF function to determine the index of the first occurrence of a character in a string. The forward search for the character begins at the designated start index@example:LAST_INDEX_OF('hello','l',0) returns 3@example:LAST_INDEX_OF('eeeee','e',4) returns  4" },
								new String[] {
										"REGEX_EXTRACT()",
										"STRING,STRING,INT",
										"STRING",
										"@function:REGEX_EXTRACT( MYSTRING , CHAR , INDEX )@short:Performs regular expression matching and extracts the matched group defined by an index parameter@param:MYSTRING string to search@param:CHAR The regular expression@param:INDEX The index of the matched group to return@description:Use the REGEX_EXTRACT function to perform regular expression matching and to extract the matched group defined by the index parameter (where the index is a 1-based parameter.) The function uses Java regular expression form."
												+ "The function returns a string that corresponds to the matched group in the position specified by the index. If there is no matched expression at that position, NULL is returned@example:REGEX_EXTRACT('helloworld#8020', '(.*)\\#(.*)', 1) returns 'helloworld'" },
								new String[] {
										"REPLACE()",
										"STRING,STRING,STRING",
										"INT",
										"@function:REPLACE(MYSTRING , OLDCHAR , NEWCHAR)@short:Replaces existing characters in a string with new characters@param:MYSTRING string to replace@param:OLDCHAR character to replace@param:NEWCHAR character to replace with@description:Use the REPLACE function to replace existing characters in a string with new characters@example:REPLACE('open source software','software','wiki') returns 'open source wiki'" }, });
		functionsMap
				.put(agregationMethods,
						new String[][] {
								new String[] {
										"COUNT_STAR()",
										"ANY",
										"NUMBER",
										"@function:COUNT_STAR( ELEMENT )@short:Computes the number of elements in a bag@param:ELEMENT item to count@description:Use the COUNT_STAR function to compute the number of elements in a bag. COUNT_STAR requires a preceding GROUP ALL statement for global counts and a GROUP BY statement for group counts."
												+ "COUNT_STAR includes NULL values in the count computation (unlike COUNT, which ignores NULL values)@example: COUNT_STAR(A) returns the frequency of A" },
								new String[] {
										"COUNT()",
										"ANY",
										"INT",
										"@function:COUNT( ELEMENT )@short:Computes the number of elements in a bag@param:ELEMENT item to count@description:Use the COUNT function to compute the number of elements in a bag. COUNT requires a preceding GROUP ALL statement for global counts and a GROUP BY statement for group counts."
												+ "The COUNT function follows syntax semantics and ignores nulls. What this means is that a tuple in the bag will not be counted if the FIRST FIELD in this tuple is NULL. If you want to include NULL values in the count computation, use COUNT_STAR."
												+ "Note: You cannot use the tuple designator (*) with COUNT; that is, COUNT(*) will not work.@example: COUNT(A) returns the frequency of A" },
								new String[] {
										"SUM()",
										"NUMBER",
										"NUMBER",
										"@function:SUM( ELEMENT )@short:Use the SUM function to compute the sum of a set of numeric values in a single-column bag@param: ELEMENT item to sum@description:Use the SUM function to compute the sum of the numeric values in a single-column bag. SUM requires a preceding GROUP ALL statement for global averages and a GROUP BY statement for group averages."
												+ "The SUM function now ignores NULL values.@example: SUM(A.id) returns the sum value of A.id" },
								new String[] {
										"AVG()",
										"NUMBER",
										"NUMBER",
										"@function:AVG( ELEMENT )@short:Use the AVG function to compute the average of a set of numeric values in a single-column bag@param: ELEMENT item to average@description:Computes the average of the numeric values in a single-column bag. AVG requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: AVG(A.id) returns the average value of A.id" },
								new String[] {
										"MIN()",
										"NUMBER",
										"NUMBER",
										"@function:MIN( ELEMENT )@short:Use the MIN function to compute the minimum of a set of numeric values in a single-column bag@param: ELEMENT item to get the minimum@description:Computes the minimum of the numeric values in a single-column bag. MIN requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: MIN(A.id) returns the minimum value of A.id" },
								new String[] {
										"MAX()",
										"NUMBER",
										"NUMBER",
										"@function:MAX( ELEMENT )@short:Use the MAX function to compute the maximum of a set of numeric values in a single-column bag@param: ELEMENT item to get the maximum@description:Computes the maximum of the numeric values in a single-column bag. MAX requires a preceding GROUP ALL statement for global sums and a GROUP BY statement for group sums@example: MAX(A.id) returns the maximum value of A.id" },
								new String[] {
										"COUNT_DISTINCT()",
										"ANY",
										"INT",
										"@function:COUNT_DISTINCT( ELEMENT )@short:Computes the number of distinct elements in a bag@param:ELEMENT item to count@description:Use the COUNT_DISTINCT function to compute the number of distinct elements in a bag. COUNT_DISTINCT requires a preceding GROUP ALL statement for global counts and a GROUP BY statement for group counts."
												+ "The COUNT_DISTINCT function follows syntax semantics and ignores nulls. What this means is that a tuple in the bag will not be counted if the FIRST FIELD in this tuple is NULL. If you want to include NULL values in the count computation, use COUNT_STAR."
												+ "Note: You cannot use the tuple designator (*) with COUNT_DISTINCT; that is, COUNT_DISTINCT(*) will not work.@example: COUNT_DISTINCT(A) returns the frequency of A"}});
		functionsMap
				.put(conditionalOperator,
							new String[][] {
								new String[] {
										"() ? () : ()",
										"BOOLEAN,BOOLEAN",
										"BOOLEAN",
										"@function:AND@short:Boolean AND@param:boolean variable@param:boolean variable@description:boolean logic that returns true if the variables are equal@example:TRUE AND TRUE" }
				});

		
		
	}
	
	
	

	/**
	 * Get the Pig type of the variable passed
	 * 
	 * @param String
	 *            of the variable type
	 * @return FeatureType of Pig variable
	 */
	public static FeatureType getType(String pigType) {
		FeatureType ans = null;
		logger.debug("Type of: " + pigType);
		if (pigType.equalsIgnoreCase("CHARARRAY")) {
			ans = FeatureType.STRING;
		} else if (pigType.equalsIgnoreCase("NUMBER")) {
			ans = FeatureType.DOUBLE;
		} else {
			ans = FeatureType.valueOf(pigType);
		}
		return ans;
	}

	/**
	 * Return the Type that is accepted by pig
	 * 
	 * @param feat
	 *            a feature type that will be checked
	 * @return Pig Type of the feature type given
	 */
	public static String getPigType(FeatureType feat) {
		String featureType = feat.name();
		switch (feat) {
		case BOOLEAN:
			break;
		case INT:
			break;
		case FLOAT:
			break;
		case LONG:
			break;
		case DOUBLE:
			break;
		case STRING:
			break;
		}
		return featureType;
	}

	/**
	 * Get the return type of a pig based expression
	 * 
	 * @param expr
	 *            operation to check return type
	 * @param features
	 *            list of features to check
	 * @param nonAggregFeats
	 *            set of non aggregated features
	 * @return type of the expression
	 * @throws Exception
	 */
	public String getReturnType(String expr, final FeatureList features,
			final Set<String> nonAggregFeats) throws Exception {
		if (expr == null || expr.trim().isEmpty()) {
			logger.error("No expressions to test");
			throw new Exception("No expressions to test");
		}
		logger.info("expression is ok");
		if (nonAggregFeats != null
				&& !features.getFeaturesNames().containsAll(nonAggregFeats)) {
			logger.error("Aggregation features unknown");
			throw new Exception("Aggregation features unknown("
					+ nonAggregFeats.toString() + "): "
					+ features.getFeaturesNames().toString());
		}
		logger.info("aggreg and feats ok");

		expr = expr.trim().toUpperCase();
		logger.debug("expression : " + expr);
		if (expr.startsWith("(") && expr.endsWith(")")) {
			int count = 1;
			int index = 1;
			while (index < expr.length() && count > 0) {
				if (expr.charAt(index) == '(') {
					++count;
				} else if (expr.charAt(index) == ')') {
					--count;
				}
				++index;
			}
			if (count != 0) {
				String error = "Not the right number of bracket in: " + expr;
				logger.debug(error);
				throw new Exception(error);
			}
			if (index == expr.length()) {
				expr = expr.substring(1, expr.length() - 1);
				logger.debug("expresion after manipulations:" + expr);
			}
		}
		String type = null;
		if (expr.equalsIgnoreCase("TRUE") || expr.equalsIgnoreCase("FALSE")) {
			logger.debug("expression is boolean: " + expr);
			type = "BOOLEAN";
		} else if (expr.startsWith("'")) {
			if (expr.endsWith("'") && expr.length() > 1) {
				type = "STRING";
			} else {
				String error = "string quote \"'\" not closed";
				logger.debug(error);
				throw new Exception(error);
			}
		} else {
			try {
				Integer.valueOf(expr);
				type = "INT";
			} catch (Exception e) {
			}
			if (type == null) {
				try {
					Double.valueOf(expr);
					type = "DOUBLE";
				} catch (Exception e) {
				}
			}
		}

		logger.debug("getting feature type if null " + type + " " + expr);
		if (type == null) {
			Iterator<String> itS = null;
			if (nonAggregFeats != null) {
				logger.debug("feataggreg is not empty : "
						+ nonAggregFeats.toString());
				itS = nonAggregFeats.iterator();
			} else {
				itS = features.getFeaturesNames().iterator();
				// logger.debug("using features list "+features.getSize());
			}
			while (itS.hasNext() && type == null) {
				String feat = itS.next();
				logger.debug("feat " + feat + " expr " + expr);
				if (feat.equalsIgnoreCase(expr)) {
					type = getPigType(features.getFeatureType(feat));
					logger.debug("type : " + type);
				}
			}
		}

		logger.debug("if expression is an operator or function if type null : "
				+ type + " " + expr);
		if (type == null) {
			logger.debug("checking all types of functions");
			if (isLogicalOperation(expr)) {
				logger.debug(expr + ", is a logical operation");
				if (runLogicalOperation(expr, features, nonAggregFeats)) {
					type = "BOOLEAN";
				}
			} else if (isRelationalOperation(expr)) {
				logger.debug(expr + ", is a relational operation");
				if (runRelationalOperation(expr, features, nonAggregFeats)) {
					type = "BOOLEAN";
				}
			} else if (isArithmeticOperation(expr)) {
				logger.debug(expr + ", is an arithmetic operation");
				if (runArithmeticOperation(expr, features, nonAggregFeats)) {
					type = "NUMBER";
				}
			} else if (isAggregatorMethod(expr)) {
				if (nonAggregFeats == null) {
					throw new Exception("Cannot use aggregation method");
				}
				logger.debug(expr + ", is an agg method");
				FeatureList fl = new OrderedFeatureList();
				List<String> l = new LinkedList<String>();
				l.addAll(features.getFeaturesNames());
				l.removeAll(nonAggregFeats);
				logger.debug("feats list size " + l.size());
				Iterator<String> lIt = l.iterator();
				while (lIt.hasNext()) {
					String nameF = lIt.next();
					logger.debug("name " + nameF);
					fl.addFeature(nameF, features.getFeatureType(nameF));
				}
				type = runMethod(expr, fl, true);
			} else if (isNonAggMethod(expr)) {
				logger.debug(expr + ", is a method");

				if (nonAggregFeats != null && nonAggregFeats.isEmpty()) {
					throw new Exception("Cannot use non aggregation method");
				}

				FeatureList fl = features;
				if (nonAggregFeats != null) {
					fl = new OrderedFeatureList();
					Iterator<String> featureAggIterator = nonAggregFeats
							.iterator();
					while (featureAggIterator.hasNext()) {
						String nameF = featureAggIterator.next().toUpperCase();
						fl.addFeature(nameF, features.getFeatureType(nameF));
					}
				}
				type = runMethod(expr, fl, false);
			} else if (isConditionalOperation(expr)){
				logger.debug(expr + ", is an cast operation");
				type = runConditionalOperation(expr, features, nonAggregFeats);
			} else if (isCastOperation(expr)) {
				logger.debug(expr + ", is an cast operation");
				type = runCastOperation(expr, features, nonAggregFeats);
			}
		}

		logger.debug("type returning: " + type);
		return type;

	}

	/**
	 * Run a cast operation on an expression
	 * 
	 * @param expr
	 * @param features
	 * @param featureAggreg
	 * @return type
	 * @throws Exception
	 */
	private String runCastOperation(String expr, FeatureList features,
			Set<String> featureAggreg) throws Exception {
		logger.debug("casting");
		String type = null;
		List<String[]> methodsFound = findAll(functionsMap.get(castOperator),
				expr);
		if (!methodsFound.isEmpty()) {
			String arg = expr.substring(expr.indexOf("(") + 1,
					expr.length() - 1).replace(")", ",");
			String[] argSplit = null;
			// Find a method with the same number of argument
			Iterator<String[]> it = methodsFound.iterator();
			String[] method = null;
			while (it.hasNext() && method == null) {
				method = it.next();
				if (method[0].equals("()")) {
					argSplit = arg.split(escapeString(",") + "(?![^\\(]*\\))");
					break;
				}
			}

			if (method != null) {
				// Special case for CAST because it returns a dynamic type
				logger.debug(expr.trim());
				logger.debug(method[0].trim());
				getReturnType(argSplit[0], features);
				if (check("TYPE", argSplit[0])) {
					type = argSplit[0];
				}
			}
		} else {
			String error = "No method matching " + expr;
			logger.debug(error);
			throw new Exception(error);
		}

		return type;
	}
	
	/**
	 * Run a cast operation on an expression
	 * 
	 * @param expr
	 * @param features
	 * @param featureAggreg
	 * @return type
	 * @throws Exception
	 */
	private String runConditionalOperation(String expr, FeatureList features,
			Set<String> featureAggreg) throws Exception {
		String type = null;
		
		String[] args = getBracketsContent(expr);
		
		if (args.length != 3){
			String error = "Wrong number of arguments.";
			logger.debug(error);
			throw new Exception(error);
		}
		
		if (!getReturnType(args[0], features).equals("BOOLEAN")){
			String error = "First argument of conditional expression must return a boolean";
			logger.debug(error);
			throw new Exception(error);
		} 
		
		type = getReturnType(args[1], features);
		
		if (!type.equals(getReturnType(args[2], features))){
			String error = "Types returned must be the same";
			logger.debug(error);
			throw new Exception(error);
		}

		return type;
	}

	/**
	 * Get the return type using an empty list for aggregation
	 * 
	 * @param expr
	 * @param features
	 * @return type
	 * @throws Exception
	 */
	public String getReturnType(String expr, FeatureList features)
			throws Exception {
		return getReturnType(expr, features, null);
	}

	/**
	 * Check if a type given is the same type as the type expected
	 * 
	 * @param typeToBe
	 * @param typeGiven
	 * @return <code>true</code> if types are equal else <code>false</code>
	 */
	public static boolean check(String typeToBe, String typeGiven) {
		boolean ok = false;
		logger.debug("type to be : " + typeToBe + " given " + typeGiven);
		if (typeGiven == null || typeToBe == null) {
			return false;
		}
		typeGiven = typeGiven.trim();
		typeToBe = typeToBe.trim();

		if (typeToBe.equalsIgnoreCase("ANY")) {
			ok = true;
		} else if (typeToBe.equalsIgnoreCase("NUMBER")) {
			ok = !typeGiven.equals("STRING") && !typeGiven.equals("BOOLEAN");
		} else if (typeToBe.equalsIgnoreCase("DOUBLE")) {
			ok = !typeGiven.equals("STRING") && !typeGiven.equals("BOOLEAN");
		} else if (typeToBe.equalsIgnoreCase("INT")) {
			ok = typeGiven.equalsIgnoreCase("NUMBER");
		} else if (typeToBe.equalsIgnoreCase("FLOAT")) {
			ok = typeGiven.equalsIgnoreCase("NUMBER");
		} else if (typeToBe.equalsIgnoreCase("TYPE")) {
			ok = typeGiven.equalsIgnoreCase("BOOLEAN")
					|| typeGiven.equalsIgnoreCase("INT")
					|| typeGiven.equalsIgnoreCase("LONG")
					|| typeGiven.equalsIgnoreCase("FLOAT")
					|| typeGiven.equalsIgnoreCase("DOUBLE")
					|| typeGiven.equalsIgnoreCase("STRING");

		} else if (typeToBe.equalsIgnoreCase("STRING")) {
			ok = false;
		} else if (typeToBe.equalsIgnoreCase("BOOLEAN")) {
			ok = false;
		}

		if (!ok && typeToBe.equalsIgnoreCase(typeGiven)) {
			ok = true;
		}
		return ok;
	}

	/**
	 * Generate an editor interaction for single input
	 * 
	 * @param help
	 * @param in
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public static EditorInteraction generateEditor(Tree<String> help,
			DFEOutput in) throws RemoteException {
		List<DFEOutput> lOut = new LinkedList<DFEOutput>();
		lOut.add(in);
		return generateEditor(help, lOut);
	}

	/**
	 * Generate an editor interaction with a list
	 * 
	 * @param help
	 * @param in
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public static EditorInteraction generateEditor(Tree<String> help,
			List<DFEOutput> in) throws RemoteException {
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<DFEOutput> itIn = in.iterator();
		Set<String> featureName = new LinkedHashSet<String>();
		while (itIn.hasNext()) {
			DFEOutput inCur = itIn.next();
			Iterator<String> it = inCur.getFeatures().getFeaturesNames()
					.iterator();
			logger.debug("add features...");
			while (it.hasNext()) {
				String cur = it.next();
				logger.debug(cur);
				if (!featureName.contains(cur)) {
					Tree<String> word = new TreeNonUnique<String>("word");
					word.add("name").add(cur);
					word.add("info").add(
							inCur.getFeatures().getFeatureType(cur).name());
					keywords.add(word);
					featureName.add(cur);
				}
			}
		}
		editor.add(help);
		editor.add("output");

		EditorInteraction ei = new EditorInteraction("autogen", "auto-gen", "",
				0, 0);
		ei.getTree().removeAllChildren();
		ei.getTree().add(editor);
		return ei;
	}

	/**
	 * Generate an EditorInteraction with FeatureList
	 * 
	 * @param help
	 * @param inFeat
	 * @return EditorInteraction
	 * @throws RemoteException
	 */
	public static EditorInteraction generateEditor(Tree<String> help,
			FeatureList inFeat) throws RemoteException {
		logger.debug("generate Editor...");
		Tree<String> editor = new TreeNonUnique<String>("editor");
		Tree<String> keywords = new TreeNonUnique<String>("keywords");
		editor.add(keywords);
		Iterator<String> itFeats = inFeat.getFeaturesNames().iterator();
		while (itFeats.hasNext()) {
			String cur = itFeats.next();
			Tree<String> word = new TreeNonUnique<String>("word");
			word.add("name").add(cur);
			word.add("info").add(inFeat.getFeatureType(cur).name());
			keywords.add(word);
		}
		editor.add(help);
		editor.add("output");
		EditorInteraction ei = new EditorInteraction("autogen", "auto-gen", "",
				0, 0);
		ei.getTree().removeAllChildren();
		ei.getTree().add(editor);
		return ei;
	}

	/**
	 * Create a conditional help menu
	 * 
	 * @return Tree for Conditional Help Menu
	 * @throws RemoteException
	 */
	public Tree<String> createConditionHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("conditional_operator"),
				functionsMap.get(conditionalOperator)));
		logger.debug("create Condition Help Menu");
		return help;
	}

	/**
	 * Create the default Select Help Menu
	 * 
	 * @return Tree for Default Select Help Menu
	 * @throws RemoteException
	 */
	public Tree<String> createDefaultSelectHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("utils"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("conditional_operator"),
				functionsMap.get(conditionalOperator)));
		logger.debug("create Select Help Menu");
		return help;
	}
	/**
	 * Create a select help menu for a grouped action
	 * @return Grouped by tree
	 * @throws RemoteException
	 */
	public Tree<String> createGroupSelectHelpMenu() throws RemoteException {
		Tree<String> help = new TreeNonUnique<String>("help");
		help.add(createMenu(new TreeNonUnique<String>("operation_arithmetic"),
				functionsMap.get(arithmeticOperators)));
		help.add(createMenu(new TreeNonUnique<String>("aggregation"),
				functionsMap.get(agregationMethods)));
		help.add(createMenu(new TreeNonUnique<String>("string"),
				functionsMap.get(stringMethods)));
		help.add(createMenu(new TreeNonUnique<String>("math"),
				functionsMap.get(mathMethods)));
		help.add(createMenu(new TreeNonUnique<String>("integer"),
				functionsMap.get(utilsMethods)));
		help.add(createMenu(new TreeNonUnique<String>("operation_relation"),
				functionsMap.get(relationalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("operation_logic"),
				functionsMap.get(logicalOperators)));
		help.add(createMenu(new TreeNonUnique<String>("conditional_operator"),
				functionsMap.get(conditionalOperator)));
		logger.debug("create Group Select Help Menu");
		return help;
	}
	/**
	 *  Create Menu with help from list
	 * @param root
	 * @param list
	 * @return menu Tree
	 * @throws RemoteException
	 */

	protected static Tree<String> createMenu(Tree<String> root, String[][] list)
			throws RemoteException {

		for (String elStr[] : list) {
			Tree<String> suggestion = root.add("suggestion");
			suggestion.add("name").add(elStr[0]);
			suggestion.add("input").add(elStr[1]);
			suggestion.add("return").add(elStr[2]);
			suggestion.add("help").add(convertStringtoHelp(elStr[3]));
		}
		return root;
	}
	/**
	 * Check if expression is a logical operation
	 * @param expr
	 * @return <code>true</code> if expression is a logical operation else <code>false</code> 
	 */
	private static boolean isLogicalOperation(String expr) {
		if (expr.trim().isEmpty()) {
			return false;
		}
		String trimExp = expr.trim();
		if (trimExp.startsWith("(") && trimExp.endsWith(")")) {
			trimExp = trimExp.substring(1, trimExp.length() - 1);
		}
		String cleanUp = trimExp.replaceAll("\\(.*\\)", "()").trim();

		return cleanUp.startsWith("NOT ") || cleanUp.contains(" OR ")
				|| cleanUp.contains(" AND ");
	}
	/**
	 * Run a Logical operation and check if the operation ran ok 
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return <code>true</code> if operation ran ok else <code>false</code>
	 * @throws Exception
	 */
	private boolean runLogicalOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {

		logger.debug("logical operator ");
		String[] split = expr.split("OR|AND");
		boolean ok = true;
		int i = 0;
		while (ok && i < split.length) {
			String cur = split[i].trim();
			if (cur.startsWith("(")) {

				while (!cur.endsWith(")")
						&& countMatches(cur, "(") != countMatches(cur, ")")
						&& i < split.length) {
					cur += " and " + split[++i].trim();
				}

				ok = check(
						"BOOLEAN",
						getReturnType(cur.substring(1, cur.length() - 1),
								features, aggregFeat));
			} else if (cur.startsWith("NOT ")) {
				ok = check(
						"BOOLEAN",
						getReturnType(cur.substring(4, cur.length()).trim(),
								features, aggregFeat));
			} else {
				ok = check("BOOLEAN", getReturnType(cur, features, aggregFeat));
			}
			if (!ok) {
				String error = "Error in expression: '" + expr + "'";
				logger.debug(error);
				throw new Exception(error);
			}
			++i;
		}
		return ok;
	}

	/**
	 * Check if expression is a relational operation
	 * 
	 * @param expr
	 * @return <code>true</code> if the expression is a relataional operation
	 *         <code>false</code>
	 */
	private boolean isRelationalOperation(String expr) {
		return isInList(functionsMap.get(relationalOperators), expr);
	}

	/**
	 * Run a relational operation and check if the result is ok
	 * 
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return <code>true</code> if relational operation is ok else
	 *         <code>false</code>
	 * @throws Exception
	 */
	private boolean runRelationalOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		return runOperation(functionsMap.get(relationalOperators), expr,
				features, aggregFeat);
	}

	/**
	 * Check if expression is an arithmetic operation
	 * 
	 * @param expr
	 * @return <code>true</code> expression is aritmethic else
	 *         <code>false</code>
	 */
	private boolean isArithmeticOperation(String expr) {
		return isInList(functionsMap.get(arithmeticOperators), expr);
	}

	/**
	 * Check if an expression is a cast operation
	 * 
	 * @param expr
	 * @return <code>true</code> id operation is a cast operation else
	 *         <code>false</code>
	 */
	private boolean isCastOperation(String expr) {
		return isInList(functionsMap.get(castOperator), expr);
	}
	
	/**
	 * Check if an expression is a conditional operation
	 * 
	 * @param expr
	 * @return <code>true</code> id operation is a conditional operation else
	 *         <code>false</code>
	 */
	private boolean isConditionalOperation(String expr) {
		return isInList(functionsMap.get(conditionalOperator), expr);
	}

	/**
	 * Run arithmetic operation and check if result is ok
	 * 
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return <code>true</code> if operation ran ok else <code>false</code>
	 * @throws Exception
	 */
	private boolean runArithmeticOperation(String expr, FeatureList features,
			Set<String> aggregFeat) throws Exception {
		return runOperation(functionsMap.get(arithmeticOperators), expr,
				features, aggregFeat);
	}

	/**
	 * Check if an expression is an aggregative method
	 * 
	 * @param expr
	 * @return <code>true</code> if expression is aggregative else
	 *         <code>false</code>
	 */
	public boolean isAggregatorMethod(String expr) {
		return isInList(functionsMap.get(agregationMethods), expr);
	}
	
	
	public boolean isCountDistinctMethod(String expr) {
		return expr.startsWith("COUNT_DISTINCT(");
	}

	/**
	 * Check if a expression is a non aggregation method
	 * 
	 * @param expr
	 * @return <code>true</code> if it is non aggregative method else
	 *         <code>false</code>
	 */
	private boolean isNonAggMethod(String expr) {
		if (isInList(functionsMap.get(utilsMethods), expr)) {
			return true;
		} else if (isInList(functionsMap.get(mathMethods), expr)) {
			return true;
		} else if (isInList(functionsMap.get(stringMethods), expr)) {
			return true;
		}
		return false;

	}

	/**
	 * Run a method to check if it runs ok
	 * 
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return <cod>true</code> if method runs ok else <cod>false</code>
	 * @throws Exception
	 */
	private String runMethod(String expr, FeatureList features,
			boolean isAggregMethod) throws Exception {
		String type = null;
		List<String[]> methodsFound = findAllMethod(expr, isAggregMethod);
		if (!methodsFound.isEmpty()) {
			String arg = expr.substring(expr.indexOf("(") + 1,
					expr.lastIndexOf(")"));
			logger.debug("argument " + arg);
			String[] argSplit = null;
			int sizeSearched = -1;
			// Find a method with the same number of argument
			Iterator<String[]> it = methodsFound.iterator();
			String[] method = null;
			while (it.hasNext() && method == null) {
				method = it.next();
				logger.debug("method " + method[0] + " " + method[1] + " "
						+ method[2]);

				String delimiter = method[0].substring(
						method[0].indexOf("(") + 1, method[0].lastIndexOf(")"));
				logger.debug("delimiter " + delimiter);
				if (delimiter.isEmpty()) {
					delimiter = ",";
				}
				argSplit = arg
						.split(escapeString(delimiter) + "(?![^\\(]*\\))");
				sizeSearched = argSplit.length;
				logger.debug("argsplit last el" + argSplit[sizeSearched - 1]);
				logger.debug("argsplit size : " + sizeSearched);
				logger.debug("test " + method[1].trim().isEmpty());
				logger.debug("test "
						+ expr.trim().equalsIgnoreCase(method[0].trim()));
				if (method[1].trim().isEmpty()
						&& expr.trim().equalsIgnoreCase(method[0].trim())) {
					// Hard-copy method
					type = method[2];
				} else{
					int methodArgs = method[1].isEmpty() ? 0 : method[1].split(",").length;
					if (sizeSearched != methodArgs) {
						method = null;
					}
				}

			}

			if (method != null && type == null) {
				// Special case for CAST because it returns a dynamic type
				logger.debug(expr.trim());
				logger.debug(method[0].trim());
				if (removeBracketContent(method[0]).equalsIgnoreCase("CAST()")) {
					// Check the first argument
					getReturnType(argSplit[0], features);
					if (check("TYPE", argSplit[1])) {
						type = argSplit[1];
					}
				} else if (check(method, argSplit, features)) {
					type = method[2];
				}
			} else if (type == null) {
				String error = "No method " + methodsFound.get(0)[0] + " with "
						+ sizeSearched + " arguments, expr:" + expr;
				logger.debug(error);
			}
		} else {
			String error = "No method matching " + expr;
			logger.debug(error);
		}

		return type;
	}

	/**
	 * Run an operation to check if it runs ok
	 * 
	 * @param list
	 * @param expr
	 * @param features
	 * @param aggregFeat
	 * @return <cod>true</code> if operation runs ok else <cod>false</code>
	 * @throws Exception
	 */
	private boolean runOperation(String[][] list, String expr,
			FeatureList features, Set<String> aggregFeat) throws Exception {
		boolean ok = false;
		String[] method = PigDictionary.find(list, expr);
		if (method != null) {
			logger.debug("In " + expr + ", method found: " + method[0]);
			String[] splitStr = expr.split(escapeString(method[0]));
			if (aggregFeat == null) {
				ok = check(method, splitStr, features);
			} else if (aggregFeat.isEmpty()) {
				// No addition in a total aggregation
				ok = false;
			} else {
				FeatureList AF = new OrderedFeatureList();
				Iterator<String> itA = aggregFeat.iterator();
				while (itA.hasNext()) {
					String feat = itA.next();
					AF.addFeature(feat, features.getFeatureType(feat));
				}
				ok = check(method, splitStr, AF);
			}
		}

		if (!ok) {
			String error = "Error in expression: '" + expr + "'";
			logger.debug(error);
		}
		logger.debug("operation ok : " + ok);
		return ok;
	}

	/**
	 * Check if an expression is in a list
	 * 
	 * @param list
	 * @param expr
	 * @return <cod>true</code> if expression in list else <cod>false</code>
	 */
	private static boolean isInList(String[][] list, String expr) {
		String cleanUp = removeBracketContent(expr);
		boolean found = false;
		int i = 0;
		while (!found && list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0]
					.trim()));
			// logger.debug("Is " + cleanUp + " contains " + regex);
			found = cleanUp.matches(regex);
			++i;
		}

		return found;
	}

	/**
	 * Check if the arguments passed to a method are the same in the feature
	 * list and are acceptable by the method
	 * 
	 * @param method
	 * @param args
	 * @param features
	 * @return <cod>true</code> if arguments match else <cod>false</code>
	 * @throws Exception
	 */

	private boolean check(String[] method, String[] args, FeatureList features)
			throws Exception {
		boolean ok = false;
		String[] argsTypeExpected = method[1].split(",");
		logger.debug("check");
		if (argsTypeExpected[0].isEmpty()
				&& argsTypeExpected.length - 1 == args.length) {
			// Left operator
			logger.debug("left operaor");
			ok = true;
			for (int i = 1; i < argsTypeExpected.length; ++i) {
				ok &= check(argsTypeExpected[i],
						getReturnType(args[i - 1], features));
			}
		} else if (argsTypeExpected[argsTypeExpected.length - 1].isEmpty()
				&& argsTypeExpected.length - 1 == args.length) {
			// Right operator
			ok = true;
			logger.debug("right operator");
			for (int i = 0; i < argsTypeExpected.length - 1; ++i) {
				ok &= check(argsTypeExpected[i],
						getReturnType(args[i], features));
			}
		} else if (argsTypeExpected.length == args.length) {
			ok = true;
			for (int i = 0; i < argsTypeExpected.length; ++i) {
				logger.debug("only one arg : " + argsTypeExpected.length);
				logger.debug("features " + features.getFeaturesNames());
				logger.debug("arg " + args[i]);
				logger.info("return type : " + getReturnType(args[i], features));
				ok &= check(argsTypeExpected[i],
						getReturnType(args[i], features));
			}
		}
		if (!ok) {
			String arg = "";
			if (args.length > 0) {
				arg = args[0];
			}
			for (int i = 1; i < args.length; ++i) {
				arg += "," + args[i];
			}
			String error = "Method " + method[0]
					+ " does not accept parameter(s) " + arg;
			logger.debug(error);
		}

		return ok;
	}

	/**
	 * Find the expression in a list of methods
	 * 
	 * @param list
	 * @param expression
	 * @return method
	 */
	private static String[] find(String[][] list, String expression) {

		int i = 0;
		boolean found = false;
		String[] ans = null;
		String search = removeBracketContent(expression.trim());
		while (!found && list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0]
					.trim()));
			logger.trace("equals? " + search + " " + regex);

			if (found = search.matches(regex)) {
				ans = list[i];
			}

			++i;
		}
		if (ans != null) {
			logger.debug("expr " + expression + ", to search: " + search
					+ ", found: " + ans[0]);
		} else {
			logger.debug("expr " + expression + ", to search: " + search
					+ ", found: null");
		}
		return ans;
	}

	/**
	 * Find all methods for an Expression
	 * 
	 * @param list
	 * @param method
	 * @return List of Methods
	 */
	private static List<String[]> findAll(String[][] list, String method) {

		int i = 0;
		List<String[]> ans = new LinkedList<String[]>();
		String search = removeBracketContent(method.trim());
		while (list.length > i) {
			String regex = getRegexToFind(removeBracketContent(list[i][0]
					.trim()));
			if (search.matches(regex)) {
				ans.add(list[i]);
			}

			++i;
		}
		logger.debug("expr " + method + ", to search: " + search + ", found: "
				+ ans.size());
		return ans;
	}

	/**
	 * Find all methods for an expression checking for aggregation methods
	 * 
	 * @param expr
	 * @param aggregMethod
	 * @return List of methods
	 */
	private List<String[]> findAllMethod(String expr, boolean aggregMethod) {
		List<String[]> ans = null;
		if (aggregMethod) {
			ans = findAll(functionsMap.get(agregationMethods), expr);
		} else {
			ans = findAll(functionsMap.get(utilsMethods), expr);
			ans.addAll(findAll(functionsMap.get(mathMethods), expr));
			ans.addAll(findAll(functionsMap.get(stringMethods), expr));
		}
		logger.debug("found results for : " + expr + " with " + ans.size());
		return ans;
	}

	/**
	 * Count how many time an expression matches another on
	 * 
	 * @param str
	 * @param match
	 * @return match Count
	 */
	private static int countMatches(String str, String match) {
		int count = 0;
		while (!str.isEmpty()) {
			int index = str.indexOf(match);
			if (index == -1) {
				str = "";
			} else {
				++count;
				str = str.substring(index + match.length());
			}
		}
		return count;
	}

	/**
	 * Get the expression with escape characters
	 * 
	 * @param expr
	 * @return escapedString
	 */
	public static String escapeString(String expr) {
		return "\\Q" + expr + "\\E";
	}

	/**
	 * Remove the content that is in the expression
	 * 
	 * @param expr
	 * @return content
	 */
	public static String removeBracketContent(String expr) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while (index < expr.length()) {
			if (expr.charAt(index) == '(') {
				++count;
				if (count == 1) {
					cleanUp += '(';
				}
			} else if (expr.charAt(index) == ')') {
				--count;
				if (count == 0) {
					cleanUp += ')';
				}
			} else if (count == 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp;
	}

	/**
	 * Get the content of the expression that contains brackets
	 * 
	 * @param expr
	 * @return content
	 */
	public static String getBracketContent(String expr) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while (index < expr.length()) {
			if (expr.charAt(index) == '(') {
				++count;

			} else if (expr.charAt(index) == ')') {
				--count;

			} else if (count > 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp;
	}
	
	/**
	 * Get the content of the brackets delimited by comma
	 * 
	 * @param expr
	 * @return cleanUp
	 */

	public static String[] getBracketsContent(String expr) {
		int count = 0;
		int index = 0;
		String cleanUp = "";
		while (index < expr.length()) {
			if (expr.charAt(index) == '(') {
				++count;
				if (count > 1) {
					cleanUp += '(';
				}
			} else if (expr.charAt(index) == ')') {
				--count;
				if (count > 0) {
					cleanUp += ')';
				}
				else{
					cleanUp += ',';
				}
			} else if (count > 0) {
				cleanUp += expr.charAt(index);
			}
			++index;
		}
		return cleanUp.substring(0, cleanUp.length() - 1).split(",");
	}

	/**
	 * Get the regex that can be used to find the expression
	 * 
	 * @param expr
	 * @return regex
	 */
	public static String getRegexToFind(String expr) {
		String regex = escapeString(expr);
		if (!expr.matches("\\W.*")) {
			regex = "(^|.*\\s)" + regex;
		} else {
			regex = ".*" + regex;
		}
		if (!expr.matches(".*\\W")) {
			regex = regex + "(\\s.*|$)";
		} else {
			regex = regex + ".*";
		}
		return regex;
	}

	/**
	 * Check the name is a varialble name
	 * 
	 * @param name
	 * @return <code>true</code> if the name is the structure for a variable
	 *         </code>
	 */
	public static boolean isVariableName(String name) {
		String regex = "[a-zA-Z]+[a-zA-Z0-9_]*";
		return name.matches(regex);
	}
}
