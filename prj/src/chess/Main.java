package chess;

import chess.ChessConsole;
import chess.State.GameOverReason;

import java.util.List;
import java.util.LinkedList;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		State state = new State(), newState;
		ChessConsole.printBoardState(state);
		// this structure is required to watch for threefold repetition rule
		List <String> prevStates = new LinkedList<String> ();
		
		int i = 1;
		while (state.gameover == null){
			newState = state.makeMove(ChessConsole.getMovePosition(state.whoseTurn, i));
			if (state.whoseTurn != newState.whoseTurn){
				
				/* we need to check to for threefold repetition rule - whether there already existed 2
				 * overall pieces disposition exactly like this one during the current game. 
				 * if yes - game ends with a draw 
				 */
				
				int numSameStates = 0;
				for (String s : prevStates){
					System.out.println("_____________________________STORED____________");
					System.out.println(s.toString());
					if (s.equals(newState.toString())){
						numSameStates++;
					}
				}
				if (numSameStates == 2)
					newState.gameover = GameOverReason.THREEFOLD_REPETITION_RULE;
				else numSameStates =0;
				
				// remembering to later on check for https://en.wikipedia.org/wiki/Threefold_repetition
				if (newState.numPieces()!=state.numPieces()){
					prevStates.clear(); //if the number of pieces changed, all the previous states cannot reooccur
				} else {
					State toArray = new State (state);//we want to pass immutable state
					prevStates.add(toArray.toString()); //adding state, not toString() as the turn is important
				}
				
				//printing and incrementing auxiliary counter only if the move was valid
				i++;
				ChessConsole.printBoardState(newState);
				state = newState;
			} 
		}
		
		ChessConsole.gameOverMessage (state);
	}

}
