import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Stack;

public class TheoremProver {

	//List of variables (true and false)
	public static HashMap<String, Variable> VariableList = new HashMap<String, Variable>();
	public static ArrayList<Variable> VariableListObjects = new ArrayList<Variable>();

	//List of variables (true) - the facts "database"
	public static LinkedHashSet<String> FactList = new LinkedHashSet<String>();
	
	//List of rules
	public static HashMap<String, ArrayList<ArrayList<String>>> RuleList = new HashMap<String, ArrayList<ArrayList<String>>>();
	public static ArrayList<String> RuleListStrings = new ArrayList<String>();
	public static ArrayList<ArrayList<String>> RuleListStacks = new ArrayList<ArrayList<String>>(); //keeps the rules in the order they were added
	

	public static void main(String[] args) {

		/*Scanner input = new Scanner(System.in);
		System.out.println("Theorem Prover - Please enter your commands: ");

		while (input.hasNextLine()) {
			String s = input.nextLine();
			if (s.equalsIgnoreCase("Quit")) {
				break;
			}
			ParseCommand(s);

		}*/
		//String filename = "ai_hw_1_test_albert.txt";
		//String filename = "ai_hw_1_test.txt";
		String filename = "test14.txt";
		//String filename = "ai_hw_1_test6.txt";
		File file = new File(filename);
		Scanner reader;


		try {
			reader = new Scanner(file);
			while (reader.hasNextLine()) {
				String cmd = reader.nextLine();
				if (cmd.equals("Exit")) {
					break;
				}
				System.out.println(cmd);
				ParseCommand(cmd);


			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Prints out Variables in the order they were added
	 * Print out facts 
	 * Prints out Rules in the order they were added
	 * **/
	public static void ListCommand() {
		System.out.println("Variables:");
		for (int i = 0; i<VariableListObjects.size(); i++) {
			Variable tmp = VariableListObjects.get(i);
			System.out.println("\t " + tmp.name + " = " + tmp.value);
		}
		System.out.println("Facts:");
		for (String s: FactList) {
			System.out.println("\t" + s);
		}
		System.out.println("Rules:");
		for (int i = 0; i<RuleListStrings.size(); i++) {
			System.out.println("\t" + RuleListStrings.get(i));
		}
	}

	/**
	 * Parses the user input and calls appropriate method
	 * Or shows error message if there's a problem
	 * **/
	public static void ParseCommand(String s) {
		String[] cmd = s.split(" ");

		if (cmd[0].equalsIgnoreCase("Teach")) {
			
			if (cmd[2].equals("=")) {
				//Case 2: Teach variable True/False value
				if (cmd[3].equalsIgnoreCase("true") || cmd[3].equalsIgnoreCase("false")) {
					//System.out.println("Teaching variable t/f");
					TeachBoolean(cmd[1], cmd[3]);
				}
				else {
					//Case 1: Teach new variable
					//System.out.println("Teaching new variable");
					String[] tmp = s.split("=");
					TeachNewVariable(cmd[1], tmp[tmp.length-1].substring(1));
				}
			}
			else if (cmd[2].equals("->")) {
				if (!VariableList.containsKey(cmd[3])) {
					System.out.println(cmd[3] + " does not exist in the Variable List");
					return;
				}
				if (!ValidInput(cmd[1])) {
					return;
				}
				else {
					//Case 3: Teach system new rule
					//System.out.println("Teaching new rule");
					RuleListStrings.add(cmd[1] + " " + cmd[2] + " " + cmd[3]);
					CreateNewRule(cmd[1], cmd[3]);
				}

			}
			else {
				System.out.println("Invalid Teach command entered");
			}

		}
		else if (cmd[0].equalsIgnoreCase("List")) {
			ListCommand();
		}
		else if (cmd[0].equalsIgnoreCase("Learn")) {
			Learn();
		}
		else if (cmd[0].equalsIgnoreCase("Query")) {
			Query(cmd[1]);
			//System.out.println(cmd[1] + " is " + value);
		}
		else if (cmd[0].equalsIgnoreCase("Why")) {
			if (cmd.length > 1) {
				Why(cmd[1]); 	
			}
			else {
				System.out.println("Why has invalid format. Enter another command.");
			}
		}
		else {
			System.out.println("The command " + s + " you entered is not defined in this system.");
		}

	}

	public static void TeachNewVariable(String variableName, String variableValue) {
		//Check to make sure the variable is not already in the database
		//If it is, report an error
		if (VariableList.containsKey(variableName)) {
			System.out.println("The variable already exists in the system, so it will not be added");
		}
		else {
			Variable tmp = new Variable(variableName, variableValue);
			VariableList.put(variableName, tmp);
			VariableListObjects.add(tmp);
		}
	}

	public static void TeachBoolean(String s, String bool) {
		boolean tf_value = (bool.equalsIgnoreCase("true")) ? true : false;

		//Find the variable name in the variable database
		if (VariableList.containsKey(s)) {
			Variable tmp = VariableList.get(s);
			if (tmp.fact != tf_value) {
				//Set the new value for if it is a fact or not
				tmp.fact = tf_value;
				if (tf_value == true) { 
					//Changed a variable from false to true
					//Add the variable to the facts database
					FactList.add(tmp.name);
				}
				else {
					//Changed a variable from true to false
					//Remove it from the facts database
					FactList.remove(tmp.name);
				}
			}
			//Do nothing if bool = same as the variable's bool. 
		}
		else { //If not there, print out an error message 
			System.out.println("The variable " + s + " you are trying to set to " + bool + " does not exist in the variable list.");
		}
	}

	public static boolean ValidInput(String expr) {
		//String[] parsedExpr = expr.split("[()&|!]"); //"a|b&c!(d)
		String[] parsedExpr = expr.split("(?<=[|!&()])|(?=[!|&()])");
		//System.out.println("INPUT: " + Arrays.toString(parsedExpr));
		for (int i = 0; i<parsedExpr.length; i++) {
			String x = parsedExpr[i];
			if (x.equals("")) {
				continue;
			}
			if (!x.equals("|") && !x.equals("&") && !x.equals("(") && !x.equals(")") && !x.equals("!")) {
				if (!VariableList.containsKey(x)) {
					System.out.println("Variable " + x + " in your input has not been defined!");
					return false;
				}
			}

		}
		return true;
	}

	public static ArrayList<String> ConvertToPostFixStack(String expr) {
		String[] parsedExpr = expr.split("(?<=[|!&()])|(?=[!|&()])");

		ArrayList<String> output = new ArrayList<String>();
		Stack<String> stack = new Stack<String>();

		Stack<Integer> numANDs = new Stack<Integer>();
		Stack<Integer> numORs = new Stack<Integer>();
		Stack<Integer> numNOTs = new Stack<Integer>();


		int numberOfANDs = 0;
		int numberOfORs = 0;
		int numberOfNOTs = 0;

		for (int i = 0; i<parsedExpr.length; i++) {
			//If token is operand, push token onto output array
			if (parsedExpr[i].equals("")) {
				continue;
			}
			if (parsedExpr[i].equals("!")) {
				stack.push(parsedExpr[i]);
				numberOfNOTs++;
			}
			else if (parsedExpr[i].equals("(")) {
				stack.push(parsedExpr[i]);
				numANDs.push(numberOfANDs);
				numORs.push(numberOfORs);
				numNOTs.push(numberOfNOTs);
				numberOfANDs = 0;
				numberOfORs = 0;
				numberOfNOTs = 0;
			}
			else if (parsedExpr[i].equals(")")) {
				//System.out.println("popping");
				while(!stack.peek().equals("(")) {
					
					String tmp = stack.pop();
					if (tmp.equals("|")) {
						numberOfORs--;
					}
					if (tmp.equals("&")) {
						numberOfANDs--;
					}
					if (tmp.equals("!")) {
						numberOfNOTs--;
					}
					output.add(tmp);
				}
				stack.pop();
				output.add("(");
				output.add(")");
				numberOfANDs = numANDs.pop();
				numberOfORs = numORs.pop();
				numberOfNOTs = numNOTs.pop();

			}
			else if (parsedExpr[i].equals("&")) {
				if (numberOfANDs == 0 && numberOfNOTs == 0) {
					stack.push(parsedExpr[i]);
					numberOfANDs++;
				}
				else {
					while (numberOfANDs > 0 || numberOfNOTs > 0) {
						String tmp = stack.pop();

						if (tmp.equals("&")) {
							numberOfANDs--;
						}
						if (tmp.equals("|")) {
							numberOfORs--;
						}
						if (tmp.equals("!")) {
							numberOfNOTs--;
						}
						output.add(tmp);
					}
					if (numberOfANDs==0 && numberOfNOTs==0) {
						stack.add(parsedExpr[i]);
						numberOfANDs++;
					}
				}
			}
			else if (parsedExpr[i].equals("|")) {
				if (numberOfANDs == 0 && numberOfORs == 0 && numberOfNOTs == 0) {
					stack.push(parsedExpr[i]);
					numberOfORs++;
				}
				else {
					while (numberOfANDs > 0 || numberOfORs > 0 || numberOfNOTs > 0) {
						String tmp = stack.pop();
						if (tmp.equals("&")) {
							numberOfANDs--;
						}
						if (tmp.equals("|")) {
							numberOfORs--;
						}
						if (tmp.equals("!")) {
							numberOfNOTs--;
						}
						output.add(tmp);

					}
					if (numberOfORs==0 && numberOfNOTs == 0 && numberOfANDs == 0) {
						stack.add(parsedExpr[i]);
						numberOfORs++;
					}
				}
			}
			else {
				output.add(parsedExpr[i]);
			}
		}

		//Pop remaining stuff on stack to output
		while (!stack.isEmpty()) {
			output.add(stack.pop());
		}
		//System.out.println(output.toString());
		return output;
	}

	public static void CreateNewRule(String expr, String variable) {

		ArrayList<String> output = ConvertToPostFixStack(expr);

		ArrayList<String> output2 = ConvertToPostFixStack(expr);
		ArrayList<ArrayList<String> > tmp = RuleList.get(variable);
		
		if (tmp==null) {
			tmp = new ArrayList<ArrayList<String> >();
			tmp.add(output);
			RuleList.put(variable, tmp);
		}
		else {
			tmp.add(output);
		}
		
		// for learning in the right order. add the "evals to" as the last thing on the stack.
		output2.add(">");
		output2.add(variable);
		RuleListStacks.add(output2);
	}

	public static void EvaluatePostfixExpression(ArrayList<String> expr, String evalsTo) {
		Stack<Boolean> stack = new Stack<Boolean>();
		//System.out.println(Arrays.toString(expr.toArray()));
		for (int i = 0; i<expr.size(); i++) {
			if (expr.get(i).equals("&")) {
				boolean tmp1 = stack.pop();
				boolean tmp2 = stack.pop();
				stack.push(tmp1&&tmp2);
			}
			else if (expr.get(i).equals("|")) {
				boolean tmp1 = stack.pop();
				boolean tmp2 = stack.pop();
				stack.push(tmp1||tmp2);
			}
			else if (expr.get(i).equals("!")) {
				boolean tmp1 = stack.pop();
				stack.push(!tmp1);
			}
			else if (expr.get(i).equals("(")) {
				i++;
			}
			else if (expr.get(i).equals(">")) {
				break;
			}
			else {
				Variable v = VariableList.get(expr.get(i));
				stack.push(v.fact);
			}
		}

		if (stack.peek()) { // if true
			Variable v = VariableList.get(evalsTo);
			v.fact = true; // set it to true
			if (!FactList.add(v.name)) {
				//System.out.println(v.name + " Variable is already true.");
			}
			else {
				//System.out.println(v.name + "New fact has been added.");

			}

		}
		//Variable v = VariableList.get(evalsTo);
		//System.out.println(v.name + "  Rule evaluated to false");
		//System.out.println(stack.peek());

	}

	/**
	 * Method description: evaluates the expression in postfix notation
	 * Stops forward chaining when size of the facts database doesn't change anymore. 
	 * That means no new facts can be learned.   
	 **/
	public static void Learn() {
		int originalFactDBSize = -1;
		if (RuleList.size()==0) {
			System.out.println("Cannot learn new things. No Rules are in the Database. Sorrys.");
		}
		while (originalFactDBSize!=FactList.size()) {
			originalFactDBSize = FactList.size();

			for (int i = 0; i<RuleListStacks.size(); i++) {
				ArrayList<String> stack = RuleListStacks.get(i);
				EvaluatePostfixExpression(stack, stack.get(stack.size()-1));
			}
			/*for (String evalsTo : RuleList.keySet()) {
				ArrayList<ArrayList<String> > expressions = RuleList.get(evalsTo);
				for (int i = 0; i < expressions.size();i++) {
					EvaluatePostfixExpression(expressions.get(i), evalsTo);
				}
			}*/
		}
	}

	public static boolean Query(String expression) {
		if (!ValidInput(expression)) {
			System.out.println("Returning false because of invalid input");
			return false;
		}
		ArrayList<String> expr = ConvertToPostFixStack(expression);
		Stack<Boolean> stack = new Stack<Boolean>();
		Stack<String> stringStack = new Stack<String>();
		Stack<String> exprStack = new Stack<String>();

		for (int i = 0; i<expr.size(); i++) {
			if (expr.get(i).equals("&")) {
				String tmp1 = exprStack.pop();
				String tmp2 = exprStack.pop();
				String tmp3 = stringStack.pop();
				String tmp4 = stringStack.pop();
				boolean tmp5 = stack.pop();
				boolean tmp6 = stack.pop();
				stack.push(tmp5&&tmp6);
				stringStack.push(tmp3 + "&" + tmp4);
				exprStack.push(tmp2 + " AND " + tmp1);

			}
			else if (expr.get(i).equals("|")) {
				String tmp1 = exprStack.pop();
				String tmp2 = exprStack.pop();
				String tmp3 = stringStack.pop();
				String tmp4 = stringStack.pop();
				boolean tmp5 = stack.pop();
				boolean tmp6 = stack.pop();
				stack.push(tmp5||tmp6);
				stringStack.push(tmp3 + "|" + tmp4);
				exprStack.push(tmp1 + " OR " + tmp2);
			}
			else if (expr.get(i).equals("!")) {
				String tmp1 = stringStack.pop();
				boolean tmp2 = stack.pop();
				stack.push(!tmp2);
				stringStack.push("!" + tmp1);
				exprStack.push("NOT " + exprStack.pop());
			}
			else if (expr.get(i).equals("(")) {
				exprStack.push("(" + exprStack.pop() + ")");
				if (i+1 < expr.size()) {
					i++;	
				}
			}
			else {
				stack.push(QueryForFact(expr.get(i)));
				stringStack.push(expr.get(i));
				exprStack.push(VariableList.get(expr.get(i)).value);
			}
		}

		if (stack.peek()) {
			System.out.println("true");
			return true;
		}
		else {
			System.out.println("false");
			return false;
		}

	}

	public static boolean QueryForFact(String f) {
		//Base case - look up in facts database. If true return true; 
		if (FactList.contains(f)) {
			return true;
		}
		else {
			//Find rules with f as a consequence
			ArrayList<ArrayList<String>> rules = RuleList.get(f);
			if (rules == null) {
				return false;
			}
			else {
				for (int i = 0; i< rules.size(); i++) {
					Stack<Boolean> stack = new Stack<Boolean>();
					Stack<String> stringStack = new Stack<String>();
					Stack<String> exprStack= new Stack<String>();
					//System.out.println(Arrays.toString(rules.get(i).toArray()));
					for (int j = 0; j<rules.get(i).size(); j++) {
						if (rules.get(i).get(j).equals("&")) {
							String tmp1 = exprStack.pop();
							String tmp2 = exprStack.pop();
							String tmp3 = stringStack.pop();
							String tmp4 = stringStack.pop();
							boolean tmp5 = stack.pop();
							boolean tmp6 = stack.pop();
							stack.push(tmp5 && tmp6);
							stringStack.push(tmp3 + "&" + tmp4);
							exprStack.push(tmp2 + " AND " + tmp1);
						}
						else if (rules.get(i).get(j).equals("|")) {
							String tmp1 = exprStack.pop();
							String tmp2 = exprStack.pop();
							String tmp3 = stringStack.pop();
							String tmp4 = stringStack.pop();
							boolean tmp5 = stack.pop();
							boolean tmp6 = stack.pop();
							stack.push(tmp5||tmp6);
							stringStack.push(tmp3 + "|" + tmp4);
							exprStack.push(tmp2 + " OR " + tmp1);
						}
						else if (rules.get(i).get(j).equals("!")) {
							boolean tmp1 = stack.pop();
							String tmp2 = stringStack.pop();
							stack.push(!tmp1);
							stringStack.push("(!" + tmp2 + ")");
							exprStack.push("NOT " + exprStack.pop());
						}
						else if (rules.get(i).get(j).equals("(")) {
							exprStack.push("(" + exprStack.pop() + ")");
							if (j+1 < rules.get(i).size()) {
								j++;
							}
						}
						else {
							exprStack.push(VariableList.get(rules.get(i).get(j)).value);
							stack.push(QueryForFact(rules.get(i).get(j)));
							stringStack.push(rules.get(i).get(j));
						}

					}
					//String factVal = VariableList.get(f).value;
					//Top of the stack is what the expression evaluates to. 
					if (stack.peek()) {
						return true;
					}

				}//end for loop
				//if the function didn't returned, that means none of the rules with f as a consequence evaluated to true. 
			}//end of else
		}//end of outer else
		return false;
	}

	public static boolean Why(String expression) {
		if (!ValidInput(expression)) {
			System.out.println("Returning false because of invalid input");
			return false;
		}
		ArrayList<String> expr = ConvertToPostFixStack(expression);
		Stack<Boolean> stack = new Stack<Boolean>();
		Stack<String> stringStack = new Stack<String>();
		Stack<String> exprStack = new Stack<String>();

		for (int i = 0; i<expr.size(); i++) {
			if (expr.get(i).equals("&")) {
				
				String tmp1 = exprStack.pop();
				String tmp2 = exprStack.pop();
				String tmp3 = stringStack.pop();
				String tmp4 = stringStack.pop();
				boolean tmp5 = stack.pop();
				boolean tmp6 = stack.pop();
				stack.push(tmp5&&tmp6);
				if (stack.peek()) {
					System.out.println("I THUS KNOW THAT " + tmp2 + " AND " + tmp1);
				}
				else {
					System.out.println("THUS I CANNOT PROVE " + tmp2 + " AND " + tmp1);
				}
				stringStack.push(tmp3 + "&" + tmp4);
				exprStack.push(tmp2 + " AND " + tmp1);

			}
			else if (expr.get(i).equals("|")) {
				String tmp1 = exprStack.pop();
				String tmp2 = exprStack.pop();
				String tmp3 = stringStack.pop();
				String tmp4 = stringStack.pop();
				boolean tmp5 = stack.pop();
				boolean tmp6 = stack.pop();
				stack.push(tmp5||tmp6);
				if (stack.peek()) {
					
					System.out.println("I THUS KNOW THAT " + tmp2 + " OR " + tmp1);
				}
				else {
					System.out.println("THUS I CANNOT PROVE " + tmp2 + " OR " + tmp1);
				}
				stringStack.push(tmp3 + "|" + tmp4);
				exprStack.push(tmp2 + " OR " + tmp1);
			}
			else if (expr.get(i).equals("!")) {
				String tmp1 = stringStack.pop();
				String tmp2 = exprStack.pop();
				boolean tmp3 = stack.pop();
				stack.push(!tmp3);
				if (stack.peek()) {
					System.out.println("I THUS KNOW THAT NOT " + tmp2);
					
				}
				else {
					System.out.println("THUS I CANNOT PROVE NOT " + tmp2);
					
				}
				stringStack.push("!" + tmp1);
				exprStack.push("NOT " +tmp2);
			}
			else if (expr.get(i).equals("(")) {
				exprStack.push("(" + exprStack.pop() + ")");
				if (i+1 < expr.size()) {
					i++;	
				}
			}
			else {
				stack.push(Why_recurse(expr.get(i)));
				stringStack.push(expr.get(i));
				exprStack.push(VariableList.get(expr.get(i)).value);
			}
		}

		if (stack.peek()) {
			System.out.println("I THUS KNOW THAT " + exprStack.peek());
			System.out.println("true");
			return true;
		}
		else {
			System.out.println("THUS I CANNOT PROVE " + exprStack.peek());
			System.out.println("false");
			return false;
		}
	}
	public static boolean Why_recurse(String f) {
		//Base case
		//If the fact is true, and there are no rules that lead to it. 
		if (!RuleList.containsKey(f)) {
			if (FactList.contains(f)) {
				System.out.println("I KNOW THAT " + VariableList.get(f).value);
				return true;
			}
			else {
				System.out.println("I KNOW THAT IT IS NOT TRUE THAT " + VariableList.get(f).value);
				return false;
			}
		}
		else {
			//Find rules with f as a consequence
			ArrayList<ArrayList<String>> rules = RuleList.get(f);

			for (int i = 0; i< rules.size(); i++) {
				Stack<Boolean> stack = new Stack<Boolean>();
				Stack<String> stringStack = new Stack<String>();
				Stack<String> exprStack= new Stack<String>();

				for (int j = 0; j<rules.get(i).size(); j++) {

					//System.out.println("Boolean stack rule: " + Arrays.toString(stack.toArray()));
					if (rules.get(i).get(j).equals("&")) {
						String tmp1 = exprStack.pop();
						String tmp2 = exprStack.pop();
						String tmp3 = stringStack.pop();
						String tmp4 = stringStack.pop();
						boolean tmp6 = stack.pop();
						boolean tmp7 = stack.pop();
						stack.push(tmp6 && tmp7);
						if (stack.peek()) {
							System.out.println("I THUS KNOW THAT " + tmp2 + " AND " + tmp1);
						}
						else {
							System.out.println("THUS I CANNOT PROVE " + tmp2 + " AND " + tmp1);
						}
						stringStack.push(tmp3 + "&" + tmp4);
						exprStack.push(tmp2 + " AND " + tmp1);
					}
					else if (rules.get(i).get(j).equals("|")) {
						boolean t1 = stack.pop();
						boolean t2 = stack.pop();
						String tmp1 = exprStack.pop();
						String tmp2 = exprStack.pop();
						String tmp3 = stringStack.pop();
						String tmp4 = stringStack.pop();
						stack.push(t1||t2);
						if (stack.peek()) {

							System.out.println("I THUS KNOW THAT " + tmp2 + " OR " + tmp1);
						}
						else {
							System.out.println("THUS I CANNOT PROVE " + tmp2 + " OR " + tmp1);
						}
						stringStack.push(tmp3 + "|" + tmp4);
						exprStack.push(tmp2 + " OR " + tmp1);
					}
					else if (rules.get(i).get(j).equals("!")) {
						String tmp1 = stringStack.pop();
						String tmp2 = exprStack.pop();
						boolean tmp3 = stack.pop();
						//System.out.println(tmp2);
						//System.out.println(tmp3);
						stack.push(!tmp3);
						//System.out.println("Stack after ! " + Arrays.toString(stack.toArray()));
						if (stack.peek()) {
							
							System.out.println("I THUS KNOW THAT NOT " + tmp2);
							
						}
						else {
							System.out.println("THUS I CANNOT PROVE NOT " + tmp2);
							
						}
						stringStack.push("!" + tmp1);
						exprStack.push("NOT " + tmp2);
					}
					else if (rules.get(i).get(j).equals("(")) {
						exprStack.push("(" + exprStack.pop() + ")");
						if (j+1 < rules.get(i).size()) {
							j++;
						}
					}
					else {
						exprStack.push(VariableList.get(rules.get(i).get(j)).value);
						stack.push(Why_recurse(rules.get(i).get(j)));
						stringStack.push(rules.get(i).get(j));
					}

				}
				String factVal = VariableList.get(f).value;
				//When rule is "applied" (ie it evals to true)
				if (stack.peek()) {
					System.out.println("BECAUSE " + exprStack.peek() + ", I KNOW THAT " + factVal);
					return true;
				}
				//When a rule cannot be proven
				else {
					System.out.println("BECAUSE IT IS NOT TRUE THAT " + exprStack.peek() + ", I CANNOT PROVE " + factVal);
				}
				//go on to the next rule. 
			}//end for loop
			
		}//end of outer else
		//System.out.println("I KNOW THAT IT IS NOT TRUE THAT " + VariableList.get(f).value);
		//Return false when 
		//1) variable is NOT in the rule list 
		//2) Not in the fact list
		//3) Can't be concluded as true by going through the list of rules that have that variable as a consequence
		
		return false;
	}
}
