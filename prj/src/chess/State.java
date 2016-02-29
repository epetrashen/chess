package chess;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.IllegalMoveException;
import games.Move;
import games.PlayerColor;
import games.Position;
import chess.Piece;



public class State {
  
  static final int BOARDLENGTH = 8;
  
  private PlayerColor whoseTurn = PlayerColor.WHITE;

  /**
   * Reasons why the chess game ended: http://en.wikipedia.org/wiki/Chess#End_of_the_game
   */
  public static enum GameOverReason {
    FIFTY_MOVE_RULE,
    THREEFOLD_REPETITION_RULE,
    NO_AVAILABLE_MOVES,
    CHECK_MATE,
  }
  
  private GameOverReason gameover = null;
  
  
  protected Piece[][] board = new Piece[8][8];
  private boolean isCastling = false; //http://www.chesscorner.com/tutorial/basic/castling/castle.htm
  /**
   * https://en.wikipedia.org/wiki/En_passant this Position serves to mark if the last state an opponents 
   * pawn moved "first long move" as this is the only situation when enpassant capture can be applied
   */
  private Position enpassantPiecePosition = null; 
  /**
   * Counter for the amount of moves made if no capture has been made 
   * and no pawn has been moved in the last fifty moves   
   * http://en.wikipedia.org/wiki/Fifty-move_rule
   */
  protected int movesWithoutCaptureNorPawn = 0;
  
  public PlayerColor getPlayerColor() {
	    return whoseTurn;
  }
  
  public void setPlayerColor(PlayerColor pc){
	  this.whoseTurn = pc;
  }

  public GameOverReason getGameOverReason() {
	    return this.gameover;
  }
  
  public void setGameOverReason(GameOverReason gameover){
	  this.gameover = gameover;
  }

  // initialization in the beginning of the game
  public State() {
	  
	this.whoseTurn = PlayerColor.WHITE;
	
	for (int i=2; i< BOARDLENGTH-1; i++){
	//for (int i=0; i< BOARDLENGTH; i++){
		for (int j=0; j< BOARDLENGTH; j++){
			board[j][i]= new Piece();
		}
	}
	
	for (int j=0; j< BOARDLENGTH; j++){
		board[j][1]=new Piece(PlayerColor.WHITE, PieceKind.PAWN);
		board[j][BOARDLENGTH-2]=new Piece(PlayerColor.BLACK, PieceKind.PAWN);
	}
	
    board[0][0]=new Piece(PlayerColor.WHITE, PieceKind.ROOK);
    board[1][0]=new Piece(PlayerColor.WHITE, PieceKind.KNIGHT);
    board[2][0]=new Piece(PlayerColor.WHITE, PieceKind.BISHOP);
    board[3][0]=new Piece(PlayerColor.WHITE, PieceKind.QUEEN);
    board[4][0]=new Piece(PlayerColor.WHITE, PieceKind.KING);
    board[5][0]=new Piece(PlayerColor.WHITE, PieceKind.BISHOP);
    board[6][0]=new Piece(PlayerColor.WHITE, PieceKind.KNIGHT);
    board[7][0]=new Piece(PlayerColor.WHITE, PieceKind.ROOK);
    
    board[0][BOARDLENGTH-1]=new Piece(PlayerColor.BLACK, PieceKind.ROOK);
    board[1][BOARDLENGTH-1]=new Piece(PlayerColor.BLACK, PieceKind.KNIGHT);
    board[2][BOARDLENGTH-1]=new Piece(PlayerColor.BLACK, PieceKind.BISHOP);
    board[3][BOARDLENGTH-1]=new Piece(PlayerColor.BLACK, PieceKind.QUEEN);
    board[4][BOARDLENGTH-1]=new Piece(PlayerColor.BLACK, PieceKind.KING);
    board[5][BOARDLENGTH-1]=new Piece(PlayerColor.BLACK, PieceKind.BISHOP);
    board[6][BOARDLENGTH-1]=new Piece(PlayerColor.BLACK, PieceKind.KNIGHT);
    board[7][BOARDLENGTH-1]=new Piece(PlayerColor.BLACK, PieceKind.ROOK);
    
	board[3][3]=new Piece(PlayerColor.BLACK, PieceKind.PAWN);
	board[3][1]=new Piece(PlayerColor.BLACK, PieceKind.PAWN);
	
    board[5][0]=new Piece(PlayerColor.WHITE, PieceKind.KING);
    board[4][0]=new Piece(PlayerColor.WHITE, PieceKind.BISHOP);
  }
 
  
  public State(State original) { //deep copy
		for (int i=0; i < BOARDLENGTH; i++){
			for (int j=0; j < BOARDLENGTH; j++){
				board[i][j] = new Piece (original.board[i][j].getColor(),
						original.board[i][j].getKind(),original.board[i][j].getIfMoved());
			}
		}
	    this.whoseTurn = original.whoseTurn;
	    this.movesWithoutCaptureNorPawn = original.movesWithoutCaptureNorPawn;
	    this.gameover = original.gameover;
	    this.enpassantPiecePosition= original.enpassantPiecePosition;
	    this.isCastling = original.isCastling;
  }
  
  public boolean getCastlingStatus (){
	  return this.isCastling;
  }
  
  public void setCastlingStatus (boolean isCastling){
	  this.isCastling = isCastling;
  }
  
  public Position getEnpassantPosition (){
	  return this.enpassantPiecePosition;
  }
  
  public void setEmpassantPosition (Position p){
	  this.enpassantPiecePosition = p;
  }
  
  /** an auxiliary function required for THREEFOLD_REPETITION_RULE 
   * which itself is realized outside the State class
   * @returns the number of pieces at the board
   */
  public int numPieces(){
	  int num =0;
	  for (Piece[] i : board){
		  for (Piece j : i){
			  if (j.getColor()!=null){
				  num++;
			  }
		  }
	  }
	  return num;
  }
  
 
 /** function returning KING position
  * @arg player color*/
 public Position kingPosition(PlayerColor pc){
	for (int i=0; i < BOARDLENGTH; i++){
		for (int j=0; j < BOARDLENGTH; j++){
			if (board[i][j].getColor()== pc &&
				board[i][j].getKind() == PieceKind.KING){
					return new Position (i,j);
			}
		}
	} 
	return null;
 }
  

@Override
public boolean equals(Object o) 
{
    if (o instanceof State) 
    {
      State s = (State) o;
      if (Arrays.deepEquals(this.board, s.board) && this.whoseTurn.equals(s.whoseTurn) ) {
    	  
         return true;
      }
    }
    return false;
}
	
/**
 * returns string with the state of the board where letters are used to indicate pieces
 */
  @Override
  public String toString(){
	  String res = "   ";
	  int num = 8;
	  for (char alphabet = 'a'; alphabet<='h';alphabet ++){
		  res +="    "+alphabet+"    ";  
	  }
	  res +="\n";
	  for (int i=BOARDLENGTH-1; i >=0; i--){
		  res += num-- +" | ";
		  for (int j=0; j <BOARDLENGTH; j++){
			  if (this.board[j][i].getColor()==null){
				  res +="_______|_"; 
			  } else {
				  res += this.board[j][i].toString().substring(0, 6)+ " | ";
			  }
		  }
	  res += "\n";
	  }
	  return res;
  }
/**
 * returns string with the state of the board where ascii chess characters are used
 */
public String toStringWithSymbols(){
	  String res = "    ";
	  int num = 8;
	  for (char alphabet = 'a'; alphabet<='h';alphabet ++){
		  res +="\u3000\u2009\u2006"+alphabet;  
	  }
	  res +="\n";
	  for (int i=BOARDLENGTH-1; i >=0; i--){
		  res += num-- +" |";
		  for (int j=0; j <BOARDLENGTH; j++){
			  if (this.board[j][i].getColor()==null){
				  res +="\u3000\u2009\u2006|"; 
			  } else {
			  	  switch (this.board[j][i].getKind()){
			  	  	case KING:
			  	  		res += ((this.board[j][i].getColor() == PlayerColor.WHITE) ? "\u2654" : "\u265A")+"|";
			  	  		break;
			  	  	case QUEEN:
			  	  		res += ((this.board[j][i].getColor() == PlayerColor.WHITE) ? "\u2655" : "\u265B" )+"|";
			  	  		break;
			  	  	case ROOK:
			  	  		res += ((this.board[j][i].getColor() == PlayerColor.WHITE) ? "\u2656" : "\u265C")+"|";
			  	  		break;
			  	  	case BISHOP:
			  	  		res += ((this.board[j][i].getColor() == PlayerColor.WHITE) ? "\u2657" : "\u265D")+"|";
			  	  		break;
			  	  	case KNIGHT:
			  	  		res += ((this.board[j][i].getColor() == PlayerColor.WHITE) ? "\u2658" : "\u265E")+"|";
			  	  		break;
			  	  	case PAWN:
			  	  		res += ((this.board[j][i].getColor() == PlayerColor.WHITE) ? "\u2659" : "\u265F")+"|";
			  	  		break;
			  	  }
			  }
		  }
		  res += "\n";
	  }
	  res += "    ";
	  for (char alphabet = 'a'; alphabet<='h';alphabet ++){
		  res +="\u3000\u2009\u2006"+alphabet;  
	  }
	  return res;
	}

static final int FIFTY_MOVE_RULE_NUM = 50;
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
						for (Position p : validMoves(state, state.board[i][j].getKind(), from, state.getPlayerColor().getOpposite(),true, false)){
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
			if (!validMoves(state, moving.getKind(), move.from, state.getPlayerColor(),true, true).contains(move.to)){
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
			if (State.noMoreMoves(nextState, nextState.getPlayerColor().getOpposite())){
				nextState.setGameOverReason(GameOverReason.NO_AVAILABLE_MOVES);
			} else if(State.isUnderRiskOfCapture(nextState, 
					nextState.kingPosition(nextState.getPlayerColor().getOpposite()), nextState.getPlayerColor())){
			//checking if the opponent's king is endangered - only according to bool to avoid getting into the infinite loop
				ChessConsole.printMessage("Check - king is endangered");
				// check whether there is a move that would save the king
				if (!State.ifWaysToAvoidMate(nextState, nextState.getPlayerColor().getOpposite())){
					nextState.setGameOverReason(GameOverReason.CHECK_MATE);
				}
			}
			//if the player's king is endangered - is he making the move to prevent it? (otherwise not valid)
			if(State.isUnderRiskOfCapture(nextState, 
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
					  validMoves(state, state.board[i][j].getKind(), new Position(i,j), opponent, false, false).contains(position)){
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
						  ! validMoves(state, state.board[i][j].getKind(), new Position (i,j), pc, true, false).isEmpty()){
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
  * @return the list of the valid moves for a specific piece of PieceKind kind from Position starting
  * boolean checkForCapture - whether we need in this call to check if the king is being captured 
  * boolean move -whether this call of the function refers to an actual move
  */
  public static List<Position> validMoves(State s, PieceKind kind, Position starting, PlayerColor pc, boolean checkForCapture, boolean move){
	  List<Position> moves = new LinkedList<Position>();
	  
	  //check for regular ways pieces can move
	  switch (kind){
		case PAWN:{
			// can move straight 1 board cell if the cell is not occupied // out of array boundaries
			Position to = new Position (starting.getRow(), starting.getCol()+1*pc.toInt());

			if (to.isInRange(0, BOARDLENGTH) &&(s.board[to.getRow()][to.getCol()].getKind() == null)){
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
					if (move)
						s.setEmpassantPosition(null);
				}
			}
					
			//if starting position is horizontal 2(7) it's okay to move it to 4(5) if the way if not occupied
			to = new Position (starting.getRow(), starting.getCol()+2*pc.toInt());
			if ((starting.getCol()*pc.toInt() == 1 || starting.getCol()*pc.toInt() == -6)
					&& s.board [to.getRow()][to.getCol()].getKind() == null
					&& s.board [to.getRow()][to.getCol()-1*s.getPlayerColor().toInt()].getKind() == null){
				moves.add (to);
				//adding only for the actual move, not if checking 
				if (move){
					s.setEmpassantPosition(to); //this pawn is potentially eligible to be captured via en passant 
				}
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
		    						!State.isUnderRiskOfCapture(s, new Position (ROOKROWCLOSE-1, starting.getCol()), pc.getOpposite()) &&
		    						!State.isUnderRiskOfCapture(s, new Position (ROOKROWCLOSE-2, starting.getCol()), pc.getOpposite()) &&
		    						!State.isUnderRiskOfCapture(s, s.kingPosition(pc), pc.getOpposite()) 
		    						){
		    			moves.add(new Position(ROOKROWCLOSE-1, starting.getCol()));
		    			s.setCastlingStatus(true);
		    		}
		    		
			    	//"long castling"
		    		if (!s.board[ROOKROWFAR][starting.getCol()].getIfMoved() &&
		    				s.board[ROOKROWFAR+1][starting.getCol()].getKind() == null &&
		    						s.board[ROOKROWFAR+2][starting.getCol()].getKind() == null &&
		    								s.board[ROOKROWFAR+3][starting.getCol()].getKind() == null &&
		    						!State.isUnderRiskOfCapture(s, new Position (ROOKROWFAR+2, starting.getCol()), pc.getOpposite()) &&
		    						!State.isUnderRiskOfCapture(s, new Position (ROOKROWFAR+3, starting.getCol()), pc.getOpposite()) &&
		    						!State.isUnderRiskOfCapture(s, s.kingPosition(pc), pc.getOpposite()) 
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
		    		if (! State.isUnderRiskOfCapture (s, kingsPossPositions.get(k), pc.getOpposite())){
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
	  if ((s.getEnpassantPosition() != null) &&( kind!=PieceKind.PAWN) && move){
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