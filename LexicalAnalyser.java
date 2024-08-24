import java.util.*;

public class LexicalAnalyser {
	enum State {
		START, REJECT_NUM, REJECT_EXPR, ZERO, ZERO_PNT, ZERO_DEC, INT, NUM_SPACE, NUM_PLUS
	};

	enum Type {
		Num, Op
	};

	private static String alphabet="1234567890+-/*. ";

	private static Map<State, Map<Character, State>> transitionMap = new HashMap<>();

	private static List<Token> tokens;



	public static List<Token> analyse(String input) throws NumberException, ExpressionException {
		State state = State.START;
		State nextState = state;
		tokens = new ArrayList<>();
		int startIndex = 0;

		initTransitions();
		//printTransitionTable();
		

		for (int i = 0; i < input.length(); i++) {
			char In = input.charAt(i);

			//lookup next transition
			Map<Character, State> stateTransitions = transitionMap.get(state);
			nextState = stateTransitions.get(In);

			//exceptions handling for rejects
			if (nextState == State.REJECT_NUM){
				throw new NumberException();
			}			
			if (nextState == State.REJECT_EXPR){
				throw new ExpressionException();
			}


			//tokenize
			//if at numspace or numplus, and coming from numplus, tokenize as op, but if not, tokenize as num
			if (nextState == State.NUM_SPACE | nextState == State.NUM_PLUS) {
				if (state == State.NUM_PLUS){tokenize(input, startIndex, i, Type.Op);}
				else {tokenize(input, startIndex, i, Type.Num);};
				startIndex = i;
			}
			//if creating number, and coming from numplus, then tokenise that operator!
			if ( ( nextState == State.ZERO | nextState == State.INT ) && state == State.NUM_PLUS){
				tokenize(input, startIndex, i, Type.Op);
				startIndex = i;
			}


			//perform state transition
			state = nextState;
		}


		//accept states:
		if (state == State.INT || state == State.ZERO || state == State.ZERO_DEC) {
			//tokenize last number
			tokenize(input, startIndex, input.length(), Type.Num);
			return tokens;
		}

		//ended on reject state (Exception handling)
		switch (state) {
			case NUM_SPACE:
			case NUM_PLUS:
				throw new ExpressionException();
			default: 
				//ZERO_PNT
				throw new NumberException();
		}

	}


	

	private static void tokenize(String input, int startIndex, int endIndex, Type type) {
		String str = input.substring(startIndex, endIndex);


		if (str.equals(" ")){return;}

		switch (type) {
			case Num:
				tokens.add(new Token(Double.parseDouble(str)));

			case Op:
				switch (str) {
					case "+":
						tokens.add(new Token(Token.TokenType.PLUS));
						break;
					case "-":
						tokens.add(new Token(Token.TokenType.MINUS));
						break;
					case "/":
						tokens.add(new Token(Token.TokenType.DIVIDE));
						break;
					case "*":
						tokens.add(new Token(Token.TokenType.TIMES));
						break;
				}
		}

		return;
	}





	//prints a transition table, not part of the assignment, but does look cool
	public static void printTransitionTable() {
		System.out.println("State Transition Table:");
		String border = new String(new char[(13*9)+9]).replace('\0', '-');
		System.out.println(border);
	
		// Print the header row with states
		System.out.printf("%-8s", "|     | ");
		for (State state : State.values()) {
			System.out.printf("%-13s", state);
		}
		System.out.println("|\n"+border);
	
		//print each input characters transitions
		for (char inputChar : alphabet.toCharArray()) {
			if (inputChar == ' '){System.out.print("| [ ] | "); } 
			else {System.out.printf("%-8s", "|  "+inputChar+"  | ");}
			
	
			for (State state : State.values()) {
				Map<Character, State> transitions = transitionMap.get(state);
				State nextState = transitions != null ? transitions.get(inputChar) : null;
				String nextStateStr = nextState != null ? nextState.name() : " ";
				System.out.printf("%-13s", nextStateStr);
			}
			System.out.println("|");
		}
	
		System.out.println(border);
	}





	private static void initTransitions() {
		// START State Transitions
		Map<Character, State> startTransitions = new HashMap<>();
		startTransitions.put('0', State.ZERO);
		for (char c : "123456789".toCharArray()) {
			startTransitions.put(c, State.INT);
		}
		for (char c : "+-/* ".toCharArray()) {
			startTransitions.put(c, State.REJECT_EXPR);
		}
		startTransitions.put('.', State.REJECT_NUM);
		transitionMap.put(State.START, startTransitions);

		// ZERO State Transitions
		Map<Character, State> zeroTransitions = new HashMap<>();
		for (char c : "1234567890".toCharArray()) {
			zeroTransitions.put(c, State.REJECT_NUM);
		}
		zeroTransitions.put('.', State.ZERO_PNT);
		zeroTransitions.put(' ', State.NUM_SPACE);
		for (char c : "+-/*".toCharArray()) {
			zeroTransitions.put(c, State.NUM_PLUS);
		}
		transitionMap.put(State.ZERO, zeroTransitions);

		// ZERO_PNT State Transitions
		Map<Character, State> zeroPointTransitions = new HashMap<>();
		for (char c : "1234567890".toCharArray()) {
			zeroPointTransitions.put(c, State.ZERO_DEC);
		}
		for (char c : "+-/* .".toCharArray()) {
			zeroPointTransitions.put(c, State.REJECT_NUM);
		}
		transitionMap.put(State.ZERO_PNT, zeroPointTransitions);

		// ZERO_DEC State Transitions
		Map<Character, State> zeroDecimalTransitions = new HashMap<>();
		for (char c : "1234567890".toCharArray()) {
			zeroDecimalTransitions.put(c, State.ZERO_DEC);
		}
		zeroDecimalTransitions.put('.', State.REJECT_NUM);
		zeroDecimalTransitions.put(' ', State.NUM_SPACE);
		for (char c : "+-/*".toCharArray()) {
			zeroDecimalTransitions.put(c, State.NUM_PLUS);
		}
		transitionMap.put(State.ZERO_DEC, zeroDecimalTransitions);

		// INT State Transitions
		Map<Character, State> intTransitions = new HashMap<>();
		for (char c : "1234567890".toCharArray()) {
			intTransitions.put(c, State.INT);
		}
		intTransitions.put('.', State.REJECT_NUM);
		intTransitions.put(' ', State.NUM_SPACE);
		for (char c : "+-/*".toCharArray()) {
			intTransitions.put(c, State.NUM_PLUS);
		}
		transitionMap.put(State.INT, intTransitions);

		// NUM_SPACE State Transitions
		Map<Character, State> numSpaceTransitions = new HashMap<>();
		numSpaceTransitions.put(' ', State.NUM_SPACE);
		for (char c : "1234567890.".toCharArray()) {
			numSpaceTransitions.put(c, State.REJECT_EXPR);
		}
		for (char c : "+-/*".toCharArray()) {
			numSpaceTransitions.put(c, State.NUM_PLUS);
		}
		transitionMap.put(State.NUM_SPACE, numSpaceTransitions);

		// NUM_PLUS State Transitions
		Map<Character, State> numPlusTransitions = new HashMap<>();
		numPlusTransitions.put(' ', State.NUM_PLUS);
		for (char c : "123456789".toCharArray()) {
			numPlusTransitions.put(c, State.INT);
		}
		numPlusTransitions.put('0', State.ZERO);
		for (char c : "+-/*.".toCharArray()) {
			numPlusTransitions.put(c, State.REJECT_EXPR);
		}
		transitionMap.put(State.NUM_PLUS, numPlusTransitions);

		// REJECT_NUM State Transitions
		Map<Character, State> rejectnumTrapTransitions = new HashMap<>();
		for (char c : "1234567890+-/* .".toCharArray()) {
			rejectnumTrapTransitions.put(c, State.REJECT_NUM);
		}
		transitionMap.put(State.REJECT_NUM, rejectnumTrapTransitions);

		// REJECT_EXPR State Transitions
		Map<Character, State> rejectexprTrapTransitions = new HashMap<>();
		for (char c : "1234567890+-/* .".toCharArray()) {
			rejectexprTrapTransitions.put(c, State.REJECT_EXPR);
		}
		transitionMap.put(State.REJECT_EXPR, rejectexprTrapTransitions);
	}
	



}
