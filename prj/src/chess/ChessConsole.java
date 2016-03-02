package chess;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import chess.State.GameOverReason;

import games.Move;
import games.PlayerColor;
import games.Position;


/**
 * main bunch of rules implemented 
 * 1. players move pieces according to turns
 * 2.pieces are moved according to rules and the way the board is occupied (can't "jump over")
 * 3. if the move exposes check, it's disallowed
 * 4. if the move was previously under check, the move that does not relieve the situation is undone
 * 5. castling is allowed if the conditions required are in place
 * 6. if the piece is a pawn reaching the back rank, it's promoted
 * 7. pieces are captured according to game's rules (including en passant)
 * 8. if the move results in a stalemate or checkmate, the game is over
 * 9. 50-move rule is implemented
 * 10.3-fold repetition rule is implemented
 * 
 * @author elena.petrashen@gmail.com
 */

public class ChessConsole {
	
	private ChessConsole(){};
	
	static final int CONST_FOR_ALPHANUMERIC_CONVERSION = 96;
	static final int CONST_FOR_3FOLD_RULE = 3;
	
	public static void main(String[] args) {
		State state = new State(), newState;
		ChessConsole.printBoardState(state);
		// this structure is required to watch for threefold repetition rule
		List <String> prevStates = new LinkedList<String> ();
		
		int i = 1;
		while (state.getGameOverReason() == null){
			newState = State.makeMove(state, ChessConsole.getMovePosition(state.getPlayerColor(), i), false);
			if (state.getPlayerColor() != newState.getPlayerColor()){
				
				/* we need to check to for threefold repetition rule - whether there already existed 2
				 * overall pieces disposition exactly like this one during the current game. 
				 * if yes - game ends with a draw 
				 */
				
				int numSameStates = 0;
				for (String s : prevStates){
					if (s.equals(newState.toString())){
						numSameStates++;
					}
				}
				if (numSameStates == CONST_FOR_3FOLD_RULE)
					newState.setGameOverReason(GameOverReason.THREEFOLD_REPETITION_RULE);
				else numSameStates =0;
				
				// remembering to later on check for https://en.wikipedia.org/wiki/Threefold_repetition
				if (newState.numPieces()!=state.numPieces()){
					prevStates.clear(); //if the number of pieces changed, all the previous states cannot reooccur
				} else {
					prevStates.add(state.toString()); //adding state
				}
				
				//printing and incrementing auxiliary counter only if the move was valid
				i++;
				ChessConsole.printBoardState(newState);
				state = newState;
			} 
		}
		
		ChessConsole.gameOverMessage (state);
	}
	
	/**
	 * receiving player's input from keyboard in e2-e4 format
	 * @param c - player making a move
	 * @param number - move #
	 * @return current move (before checking it's appropriate)
	 */
	public static Move getMovePosition(PlayerColor c, int number){
	
		Scanner input = new Scanner (System.in);
		System.out.println ("\nIt's time for the move #"+number+" by "+c+".");
		System.out.println ("Please input the current coordinates of the piece and the coordinates you wish" +
				" the piece to move to, separating them by a dash (i.e. e2-e4; move the king appropriately for castling): ");
		String in = input.nextLine().toLowerCase();
		//checking for possible incorrect input + if it's out of the board
		while (!(in.matches("[a-i][1-8]-[a-i][1-8]$"))){
			System.out.println ("Bad input. Please try again:");
			in = input.nextLine().toLowerCase();
		}
		// conversion on the user input to ints corresponding to the possible range of the board array
		Move m = new Move (new Position ((int)in.charAt(0)-CONST_FOR_ALPHANUMERIC_CONVERSION-1, Character.getNumericValue(in.charAt(1))-1), 
			new Position ((int)in.charAt(3)-CONST_FOR_ALPHANUMERIC_CONVERSION-1, Character.getNumericValue(in.charAt(4))-1));

		return m;
	}
	
	public static void printBoardState(State s){
		System.out.println (s.toString());//WithSymbols());
	}
	
	/**
	 * a message related to the reason of the game being over
	 */
	public static void gameOverMessage(State st){
		switch (st.getGameOverReason()){
			case CHECK_MATE:
				System.out.println ("The check cannot be escaped. "+ st.getPlayerColor().getOpposite() +" won.");
				break;
			case FIFTY_MOVE_RULE:
				System.out.println ("The 50-move rule is broken. Draw.");
				break;
			case THREEFOLD_REPETITION_RULE:
				System.out.println ("The threefold repetition rule is broken. Draw.");
				break;
			case NO_AVAILABLE_MOVES:
				System.out.println ("There's no available moves for "+ st.getPlayerColor() +". Draw.");
				break;
		}
	}
	
	/**
	 * getting user's input from console to be able to promote a piece to received PieceKind value
	 */
	public static PieceKind callForPromotion(){
		System.out.println ("You now can promote you pawn. Please input the desired piece kind, i.e. QUEEN");
		Scanner input = new Scanner (System.in);
		String in = input.nextLine().toUpperCase();
		PieceKind res = null;
		
		while (res == null){
			for (PieceKind pk : PieceKind.values()) {
		        if (pk.name().equals(in.toUpperCase())) {
		        	if (pk!=PieceKind.KING){
		        		res = pk;}
		        	else {
		        		System.out.println ("You cannot promote your pawn to KING. Please try again.");
		        	}
		        }
		    }
			if (res == null){
				System.out.println ("Bad input. Please try again:");
				in = input.nextLine().toUpperCase();
			}
		}
	    
	    return res;

	}
	
	public static void printMessage (String s){
		System.out.println (s);
	}

}
