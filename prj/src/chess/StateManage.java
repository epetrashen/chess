package chess;

import chess.State.GameOverReason;
import games.IllegalMoveException;
import games.PlayerColor;
import games.Position;

public class StateManage {
	private StateManage(){};
	
	static final int FIFTY_MOVE_RULE_NUM = 50;
	
	 public static boolean ifWaysToAvoidMate(State state, PlayerColor pc){
		 for (int i=0; i < 8; i++){
				for (int j=0; j < 8; j++){ //TODO
					// for every piece of the player pc
					if (state.board[i][j].getColor()== pc ){
							Position from = new Position (i, j);
							// getting the list of the valid moves 
							for (Position p : state.validMoves(state.board[i][j].getKind(), from, state.getPlayerColor())){
								// and checking if the check condition can be avoided if the move is implemented
								State s = new State(state);
								s.whoseTurn = s.getPlayerColor().getOpposite();
								s = makeMove(s, new Move(from, p), true);
								if (! isUnderRiskOfCapture(s, s.kingPosition(pc), pc.getOpposite())){
									return true;
								}
							}
					}
				}
			}
		 return false;
	 }
	 
	 public static State makeMove(State st, Move move, boolean checkForCheck) {
		 	State state = new State (st);
			Piece moving = state.board[move.from.getRow()][ move.from.getCol()];
			try {
				//check to see whether there is a piece of the chosen board location
				if (moving.getColor()==null){
					throw new IllegalMoveException ("There's no piece at the selected board location");
				}
				// check to see if the piece to be moved belongs to the current player
				if (moving.getColor()==state.whoseTurn.getOpposite()){
					throw new IllegalMoveException ("You're trying to move another's player piece");
				}
				//if it's occupied -a) can't move b) capture
				if (state.board[move.to.row][move.to.col].getColor()==state.getPlayerColor()){
					//TODO OK for swapping the king and rook
					throw new IllegalMoveException ("You're trying to capture your own piece");
				}
				// Test to see if the move is valid for the particular piece
				if (!state.validMoves(moving.getKind(), move.from, state.getPlayerColor()).contains(move.to)){
					throw new IllegalMoveException ("This is an illegal move for this type of piece");
				}
			} catch (IllegalMoveException im) {
				// logging exceptions to console in case this is a real move
				if (!checkForCheck){
					System.out.println(im.toString());
				}
				return state;
			}
			
			
			//Check whether this is being a move with capture & by a pawn
			if (state.board[move.from.row][move.from.col].getKind() == PieceKind.PAWN 
					|| (state.board[move.to.row][move.to.col].getKind()!=null)){ 
				state.movesWithoutCaptureNorPawn = 0;
			} else {
				state.movesWithoutCaptureNorPawn ++;
			}

			//Check for the http://en.wikipedia.org/wiki/Fifty-move_rule
			if (state.movesWithoutCaptureNorPawn == FIFTY_MOVE_RULE_NUM){
				state.gameover = GameOverReason.FIFTY_MOVE_RULE;
			}
			
			
			//execute the move
			State nextState = new State(state);
			nextState.board[move.to.getRow()][ move.to.getCol()] = nextState.board[move.to.getRow()][ move.to.getCol()].SetPiece(moving);
			nextState.board[move.from.getRow()][ move.from.getCol()]=moving.PieceRemove();
			
			//if the pawn reaches the diagonal 8 of the other player it should be promoted
			if ( nextState.board[move.from.getRow()][move.from.getCol()].getKind()==PieceKind.PAWN && 
					(nextState.whoseTurn.toInt()*move.from.getCol()==6 || nextState.whoseTurn.toInt()*move.from.getCol()==-1)){
				nextState.board[move.to.getRow()][move.to.getCol()].setKind(ChessConsole.callForPromotion());
			}
			
			//checking if the opponent's king is endangered - only according to bool to avoid getting into the infinite loop
			if (checkForCheck){
			if (StateManage.isUnderRiskOfCapture(nextState, nextState.kingPosition(nextState.whoseTurn.getOpposite()), nextState.whoseTurn)){
				ChessConsole.printMessage("Check - king is endangered");
				// check whether there is a move that would save the king
				//State temp = new State (this);
				//temp.board
				if (!StateManage.ifWaysToAvoidMate(nextState, nextState.whoseTurn.getOpposite())){
					nextState.gameover = GameOverReason.CHECK_MATE;
				}
			}
			}
			
			// if en passant happened we also need to remove the opponents pawn piece
			if (nextState.enpassantPiecePosition != null 
					&& nextState.whoseTurn.getOpposite() == nextState.board[nextState.enpassantPiecePosition.getRow()][nextState.enpassantPiecePosition.getCol()].getColor()){
				nextState.board[nextState.enpassantPiecePosition.getRow()][nextState.enpassantPiecePosition.getCol()].PieceRemove();
				nextState.enpassantPiecePosition = null; 
			}

			nextState.whoseTurn = state.whoseTurn.getOpposite();

		    return nextState; 
		  }
	 
	 // auxiliary function which establishes whether the field is under possible capture

	 public static boolean isUnderRiskOfCapture (State state, Position position, PlayerColor opponent){
		 
		for (int i=0; i < 8; i++){
			for (int j=0; j < 8; j++){
				// if the piece is opponent's piece and the position is question is among its valid moves
				  if (state.board[i][j].getColor()==opponent &&
						  state.validMoves(state.board[i][j].getKind(), new Position(i,j), opponent).contains(position)){
					  return true;}
			}
		}
		 return false;
	 }

}
