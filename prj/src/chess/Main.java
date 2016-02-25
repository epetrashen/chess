package chess;

import chess.ChessConsole;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		State state = new State(), newState;
		ChessConsole.printBoardState(state);
		
		int i = 1;
		while (state.gameover == null){
			newState = state.makeMove(ChessConsole.getMovePosition(state.whoseTurn, i));
			if (state.whoseTurn != newState.whoseTurn){
				//printing and incrementing auxiliary counter only if the move was valid
				i++;
				ChessConsole.printBoardState(newState);
				state = newState;
			}
		}
		
		ChessConsole.gameOverMessage (state);
	}

}
