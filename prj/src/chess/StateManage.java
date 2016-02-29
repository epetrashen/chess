package chess;

import java.util.LinkedList;
import java.util.List;

import chess.State.GameOverReason;
import games.IllegalMoveException;
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

public class StateManage {
	private StateManage(){};
	
	static final int FIFTY_MOVE_RULE_NUM = 50;
	static final int BOARDLENGTH = 8;
	// 2 variables required for castling
	static final int ROOKROWCLOSE = 7;
	static final int ROOKROWFAR = 0;
	
	/**
	 * function called in case of check in a specific @param state
	 * @param pc - player color who's under check
	 * @return boolean describing if there are ways to avoid mate in case of check
	 */
	public static boolean ifWaysToAvoidMate(State state, PlayerColor pc){
		 for (int i=0; i < BOARDLENGTH ; i++){
				for (int j=0; j < BOARDLENGTH ; j++){ 
					// for every piece of the player pc
					if (state.board[i][j].getColor()== pc ){
							Position from = new Position (i, j);
							// getting the list of the valid moves 
							for (Position p : validMoves(state, state.board[i][j].getKind(), from, state.getPlayerColor().getOpposite(),true)){
								// and checking if the check condition can be avoided if the move is implemented
								State s = new State(state);
								s.setPlayerColor(s.getPlayerColor().getOpposite());
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
	

	  /**
	   * Applies the move.
	   * @return The resulting state
	   * @throws IllegalMoveException if the move is not legal
	   */
	  
	 public static State makeMove(State st, Move move, boolean checkForCheck) {
		 	State state = new State (st);
			Piece moving = state.board[move.from.getRow()][ move.from.getCol()];
			try {
				//check to see whether there is a piece at the chosen board location
				if (moving.getColor()==null){
					throw new IllegalMoveException ("There's no piece at the selected board location");
				}
				// check to see if the piece to be moved belongs to the current player
				if (moving.getColor()==state.getPlayerColor().getOpposite()){
					throw new IllegalMoveException ("You're trying to move another's player piece");
				}
				//if it's occupied -a) can't move b) capture
				if (state.board[move.to.getRow()][move.to.getCol()].getColor()==state.getPlayerColor()){
					//TODO OK for swapping the king and rook
					throw new IllegalMoveException ("You're trying to capture your own piece");
				}
				// Test to see if the move is valid for the particular piece
				if (!validMoves(state, moving.getKind(), move.from, state.getPlayerColor(),true).contains(move.to)){
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
			if (state.board[move.from.getRow()][move.from.getCol()].getKind() == PieceKind.PAWN 
					|| (state.board[move.to.getRow()][move.to.getCol()].getKind()!=null)){ 
				state.movesWithoutCaptureNorPawn = 0;
			} else {
				state.movesWithoutCaptureNorPawn ++;
			}

			//Check for the http://en.wikipedia.org/wiki/Fifty-move_rule
			if (state.movesWithoutCaptureNorPawn == FIFTY_MOVE_RULE_NUM){
				state.setGameOverReason(GameOverReason.FIFTY_MOVE_RULE);
			}
			
			//checking if the king is somehow being captured TODO 
			if (state.board[move.to.getRow()][move.to.getCol()].getColor()==state.getPlayerColor().getOpposite() &&
					state.board[move.to.getRow()][move.to.getCol()].getKind() == PieceKind.KING){
				//this means game is over - king is captured
				ChessConsole.printMessage("You somehow managed to capture your opponent's king!");
				state.setGameOverReason(GameOverReason.CHECK_MATE);
			}
			
			//setting the boolean piece parameter that it was moved 
			moving.setIfMoved(true);
			//execute the move
			State nextState = new State(state);
			nextState.board[move.to.getRow()][ move.to.getCol()].SetPiece(moving);
			nextState.board[move.from.getRow()][ move.from.getCol()]=moving.PieceRemove();
			
			//if the pawn reaches the diagonal 8 of the other player it should be promoted
			if ( nextState.board[move.from.getRow()][move.from.getCol()].getKind()==PieceKind.PAWN && 
					(nextState.getPlayerColor().toInt()*move.from.getCol()==6 || nextState.getPlayerColor().toInt()*move.from.getCol()==-1)){
				nextState.board[move.to.getRow()][move.to.getCol()].setKind(ChessConsole.callForPromotion());
			}
			
			// if en passant happened we also need to remove the opponents pawn piece
			if (nextState.getEnpassantPosition() != null 
					&& nextState.getPlayerColor().getOpposite() == nextState.board[nextState.getEnpassantPosition().getRow()][nextState.getEnpassantPosition().getCol()].getColor()){
				nextState.board[nextState.getEnpassantPosition().getRow()][nextState.getEnpassantPosition().getCol()].PieceRemove();
				nextState.setEmpassantPosition(null); 
			}
			
			// if castling is in progress we need to additionally move the rook 
			if (nextState.getCastlingStatus() && nextState.board[move.to.getRow()][ move.to.getCol()].getKind() == PieceKind.KING 
					&& Math.abs(move.to.getRow()-move.from.getRow()) == 2){
				if (move.to.getRow()== ROOKROWCLOSE-1){ //if it was "short castling"
					nextState.board[ROOKROWCLOSE-2][ move.to.getCol()].SetPiece(nextState.board[ROOKROWCLOSE][ move.to.getCol()]);
					nextState.board[ROOKROWCLOSE-2][ move.to.getCol()].setIfMoved(true);
					nextState.board[ROOKROWCLOSE][move.to.getCol()]=moving.PieceRemove();
				}
				if (move.to.getRow()== ROOKROWFAR+2){//"long castling"
					nextState.board[ROOKROWFAR+3][move.to.getCol()].SetPiece(nextState.board[ROOKROWFAR][ move.to.getCol()]);
					nextState.board[ROOKROWFAR+3][move.to.getCol()].setIfMoved(true);
					nextState.board[ROOKROWFAR][move.to.getCol()]=moving.PieceRemove();
				}
			}
			
			//if we are actually making a move, not just testing
			if (!checkForCheck){
				
				// checking if the opponent will have a chance to make a move next round
				if (StateManage.noMoreMoves(nextState, nextState.getPlayerColor().getOpposite())){
					nextState.setGameOverReason(GameOverReason.NO_AVAILABLE_MOVES);
				} else if(StateManage.isUnderRiskOfCapture(nextState, 
						nextState.kingPosition(nextState.getPlayerColor().getOpposite()), nextState.getPlayerColor())){
				//checking if the opponent's king is endangered - only according to bool to avoid getting into the infinite loop
					ChessConsole.printMessage("Check - king is endangered");
					// check whether there is a move that would save the king
					if (!StateManage.ifWaysToAvoidMate(nextState, nextState.getPlayerColor().getOpposite())){
						nextState.setGameOverReason(GameOverReason.CHECK_MATE);
					}
				}
				//if the player's king is endangered - is he making the move to prevent it? (otherwise not valid)
				if(StateManage.isUnderRiskOfCapture(nextState, 
						nextState.kingPosition(nextState.getPlayerColor()), nextState.getPlayerColor().getOpposite())){
						ChessConsole.printMessage(nextState.getPlayerColor()+"'s king is under check. A move which does not relieve the situation is invalid");
						return state; // otherwise returning old state
				}
			}
			// next time it will be another player's turn
			nextState.setPlayerColor(state.getPlayerColor().getOpposite());
			nextState.setCastlingStatus(false);
		    return nextState; 
		  }
	 
	 // auxiliary function which establishes whether the field is under possible capture

	 public static boolean isUnderRiskOfCapture (State state, Position position, PlayerColor opponent){
		 
		for (int i=0; i < 8; i++){
			for (int j=0; j < 8; j++){
				// if the piece is opponent's piece and the position is question is among its valid moves
				  if (state.board[i][j].getColor()==opponent &&
						  validMoves(state, state.board[i][j].getKind(), new Position(i,j), opponent, false).contains(position)){
					  return true;}
			}
		}
		 return false;
	 }
	 //checking whether the player has any valid moves left
	 public static boolean noMoreMoves (State state, PlayerColor pc){
		 for (int i=0; i < 8; i++){
				for (int j=0; j < 8; j++){
					// if the piece is player's piece and it has some valid moves
					  if (state.board[i][j].getColor()==pc &&
							  ! validMoves(state, state.board[i][j].getKind(), new Position (i,j), pc, true).isEmpty()){
						  return false;
					  }
				}
		 }
		 return true;
	 }
	 
	  /*
	   * auxiliary enum containing direction a figure can move
	   */
	  private enum Direction {RIGHT, LEFT, UP, DOWN, LEFTUP, RIGHTUP, LEFTDOWN, RIGHTDOWN} 
	 
	 /*
	  * @return the list of the valid moves for a specific piece
	  */
	  public static List<Position> validMoves(State s, PieceKind kind, Position starting, PlayerColor pc, boolean checkForCapture){
		  List<Position> moves = new LinkedList<Position>();
		  
		  //check for regular ways pieces can move
		  switch (kind){
			case PAWN:{
				// can move straight 1 board cell if the cell is not occupied // out of array boundaries
				Position to = new Position (starting.getRow(), starting.getCol()+1*pc.toInt());

				if ((s.board[to.getRow()][to.getCol()].getKind() == null) && to.isInRange(0, BOARDLENGTH)){
					moves.add (to);
				}
			
			  //en passant capture: starting position is 4/5 and there is an opponents pawn neighboring yours at the moment
				if (s.getEnpassantPosition() != null) {
					//if the conditions below are not valid, the en-passant opportunity is not followed
					if((starting.getCol()*pc.toInt() == 4 || starting.getCol()*pc.toInt() == -3) &&
						s.getEnpassantPosition().getCol()==starting.getCol() &&
						(s.getEnpassantPosition().getRow() == starting.getRow()+1 || s.getEnpassantPosition().getRow() == starting.getRow()-1 )){
							moves.add (new Position (s.getEnpassantPosition().getRow(), s.getEnpassantPosition().getCol()+1*s.getPlayerColor().toInt()));
					} else{
						s.setEmpassantPosition(null);
					}
				}
						
				//if starting position is horizontal 2(7) it's okay to move it to 4(5) if the way if not occupied
				to = new Position (starting.getRow(), starting.getCol()+2*pc.toInt());
				if ((starting.getCol()*pc.toInt() == 1 || starting.getCol()*pc.toInt() == -6)
						&& s.board [to.getRow()][to.getCol()].getKind() == null
						&& s.board [to.getRow()][to.getCol()-1*s.getPlayerColor().toInt()].getKind() == null){
					moves.add (to);
					s.setEmpassantPosition(to); //this pawn is potentially eligible to be captured via en passant 
				}
				// if it was a diagonal move  - OK when capturing
				to = new Position (starting.getRow()+1, starting.getCol()+1*pc.toInt());
				if (to.isInRange(0, BOARDLENGTH) && s.board [to.getRow()][to.getCol()].getKind() != null){
					moves.add (to);
				}
				to = new Position (starting.getRow()-1, starting.getCol()+1*pc.toInt());
				if (to.isInRange(0, BOARDLENGTH) && s.board [to.getRow()][to.getCol()].getKind() != null){
					moves.add (to);
				}
				
				break;
			}
			
			case ROOK:{
				//OK to move if same horizontal/vertical + the path is free
				moves.addAll(moveLine(s, starting, Direction.LEFT, pc));
				moves.addAll(moveLine(s, starting, Direction.DOWN, pc));
				moves.addAll(moveLine(s, starting, Direction.UP, pc));
				moves.addAll(moveLine(s, starting, Direction.RIGHT, pc));
				
				break;
			}

			case BISHOP:{
				//OK to move if same diagonal + the path is free
				moves.addAll(moveLine(s, starting, Direction.LEFTUP, pc));
				moves.addAll(moveLine(s, starting, Direction.LEFTDOWN, pc));
				moves.addAll(moveLine(s, starting, Direction.RIGHTUP, pc));
				moves.addAll(moveLine(s, starting, Direction.RIGHTDOWN,pc));
				
				break;
			}
			
			case QUEEN:{
				//OK to move if same horizontal/vertical/diagonal + the path is free
				moves.addAll(moveLine(s, starting, Direction.LEFT, pc));
				moves.addAll(moveLine(s, starting, Direction.DOWN, pc));
				moves.addAll(moveLine(s, starting, Direction.UP, pc));
				moves.addAll(moveLine(s, starting, Direction.RIGHT, pc));
				moves.addAll(moveLine(s, starting, Direction.LEFTUP, pc));
				moves.addAll(moveLine(s, starting, Direction.LEFTDOWN, pc));
				moves.addAll(moveLine(s, starting, Direction.RIGHTUP, pc));
				moves.addAll(moveLine(s, starting, Direction.RIGHTDOWN, pc));
				
				break;
			}
			
			case KING:{
				//length of possible move is 1
			    int[][] offsets = {
			            {1, 0},
			            {0, 1},
			            {-1, 0},
			            {0, -1},
			            {1, 1},
			            {-1, 1},
			            {-1, -1},
			            {1, -1}
			        };
			    
			  /*let's take a look at the castling situation. Castling may only be done if the king has never moved, 
			  * the rook involved has never moved, the squares between the king and the rook involved are unoccupied, 
			  * the king is not in check, and the king does not cross over or end on a square in which it would be in check. 
			  */
			    if (checkForCapture && !s.getCastlingStatus() &&
			    		!s.board[starting.getRow()][starting.getCol()].getIfMoved() ){//castling was not yet performed and the king was not moved
			    		
			    	//"short castling"
			    		if (!s.board[ROOKROWCLOSE][starting.getCol()].getIfMoved() &&
			    				s.board[ROOKROWCLOSE-1][starting.getCol()].getKind() == null &&
			    						s.board[ROOKROWCLOSE-2][starting.getCol()].getKind() == null &&
			    						!StateManage.isUnderRiskOfCapture(s, new Position (ROOKROWCLOSE-1, starting.getCol()), pc.getOpposite()) &&
			    						!StateManage.isUnderRiskOfCapture(s, new Position (ROOKROWCLOSE-2, starting.getCol()), pc.getOpposite()) &&
			    						!StateManage.isUnderRiskOfCapture(s, s.kingPosition(pc), pc.getOpposite()) 
			    						){
			    			moves.add(new Position(ROOKROWCLOSE-1, starting.getCol()));
			    			s.setCastlingStatus(true);
			    		}
			    		
				    	//"long castling"
			    		if (!s.board[ROOKROWFAR][starting.getCol()].getIfMoved() &&
			    				s.board[ROOKROWFAR+1][starting.getCol()].getKind() == null &&
			    						s.board[ROOKROWFAR+2][starting.getCol()].getKind() == null &&
			    								s.board[ROOKROWFAR+3][starting.getCol()].getKind() == null &&
			    						!StateManage.isUnderRiskOfCapture(s, new Position (ROOKROWFAR+2, starting.getCol()), pc.getOpposite()) &&
			    						!StateManage.isUnderRiskOfCapture(s, new Position (ROOKROWFAR+3, starting.getCol()), pc.getOpposite()) &&
			    						!StateManage.isUnderRiskOfCapture(s, s.kingPosition(pc), pc.getOpposite()) 
			    						){
			    			moves.add(new Position(ROOKROWFAR+2, starting.getCol()));
			    			//we are remembering the position of the rook to be moved to move it later
			    			s.setCastlingStatus(true);
			    		}
			    	
			    }
		
			    //*need to check that the king is not moving to endangered field
			    
			    if (checkForCapture){
			    	List <Position> kingsPossPositions = new LinkedList<Position>(moveOffset(s, starting, offsets, pc));
			    	for (int k=0; k<kingsPossPositions.size(); k++){
			    		if (! StateManage.isUnderRiskOfCapture (s, kingsPossPositions.get(k), pc.getOpposite())){
			    			moves.add(kingsPossPositions.get(k));
			    		}
			    	}
			    } else moves.addAll(moveOffset(s, starting, offsets, pc));
			    //*/
			    break;
			}
			
			case KNIGHT:{
				//knights can jump over other figures
			    int[][] offsets = {
			            {-2, 1},
			            {-1, 2},
			            {1, 2},
			            {2, 1},
			            {2, -1},
			            {1, -2},
			            {-1, -2},
			            {-2, -1}
			     };
			    moves.addAll(moveOffset(s, starting, offsets, pc));
			    
			    break;
			}
		  }
		  
		  // if the other kind of figure was moved - means en passant possibility was not used
		  if ((s.getEnpassantPosition() != null) &&( kind!=PieceKind.PAWN)){
			  s.setEmpassantPosition(null);
		  }

		  return moves;
	  }
	  
	  /*an auxiliary function checking whether you can move a figure within particular offsets
	   *  - used for king and knight
	   */
	  private static List<Position> moveOffset(State s, Position starting, int[][] offsets, PlayerColor pc){
		    List<Position> moves = new LinkedList<Position>();
		    Position to;
		    for (int[] o : offsets) {
		    	to = new Position (starting.getRow()+o[0], starting.getCol()+o[1]);
		    	if (to.isInRange(0, BOARDLENGTH)){
		    		if (s.board[starting.getRow()+o[0]][starting.getCol()+o[1]].getColor()!=pc)
		    			moves.add(to);
		    	}
		    };
			return moves;
	  }
	  /*an auxiliary function checking whether you can move a figure in a particular direction
	   * while not getting out of the board & not jumping over pieces - used for queen, rook, bishop
	   */
	  private static List<Position> moveLine(State s, Position starting, Direction dir, PlayerColor pc){
		    List<Position> moves = new LinkedList<Position>();
		    int i =0, j=0;
			Position to;

			do {
			    switch (dir.name().substring(0, 1)){
		    		case "R" : {
		    			i++;
		    			break;
		    		}
		    		case "L": {
		    			i--; 
		    			break;
		    		}
			    }
			    switch (dir.name().substring(dir.toString().length()-1, dir.toString().length())){
			    	case "P" : {
			    		j--;
			    		break;
			    	}
			    	case "N": {
			    		j++; 
			    		break;
			    	}
			    }
					to = new Position (starting.getRow()+i, starting.getCol()+j);
					if (!to.isInRange(0, BOARDLENGTH))
						break;
					if (s.board[to.getRow()][to.getCol()].getColor()!=pc)
						moves.add (to);
				} while (s.board[to.getRow()][to.getCol()].getKind()==null);
				
			return moves;
	  }
}
