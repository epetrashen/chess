package chess;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.IllegalMoveException;
import games.PlayerColor;
import games.Position;
import games.Utils;
import chess.Piece;



public class State {
  
  static final int BOARDLENGTH = 8;
  static final int FIFTY_MOVE_RULE_NUM = 50;
  
  public PlayerColor whoseTurn = PlayerColor.WHITE;

	  public PlayerColor getPlayerColor() {
	    return whoseTurn;
	  }

  /**
   * Reasons why the chess game ended: http://en.wikipedia.org/wiki/Chess#End_of_the_game
   */
  public static enum GameOverReason {
    FIFTY_MOVE_RULE,
    THREEFOLD_REPETITION_RULE,
    NO_AVAILABLE_MOVES,
    CHECK_MATE,
  }
  
  public GameOverReason gameover = null;
  
  
  public Piece[][] board = new Piece[8][8];
  public boolean canCastle = true; //http://www.chesscorner.com/tutorial/basic/castling/castle.htm
  /*
   * https://en.wikipedia.org/wiki/En_passant this Position serves to mark if the last state an opponents 
   * pawn moved "first long move" as this is the only situation when enpassant capture can be applied
   */
  public Position enpassantPiecePosition = null; 
  /**
   * Counter for the amount of moves made if no capture has been made 
   * and no pawn has been moved in the last fifty moves   
   * http://en.wikipedia.org/wiki/Fifty-move_rule
   */
  public int movesWithoutCaptureNorPawn = 0;//TODO
  

  // initialization in the beginning of the game
  public State() {
	this.whoseTurn = PlayerColor.WHITE;
	for (int i=2; i< BOARDLENGTH-1; i++){
		for (int j=0; j< BOARDLENGTH; j++){
			board[j][i]= new Piece();
		}
	}
	
	for (int j=0; j< BOARDLENGTH; j++){
		board[j][1]=new Piece(PlayerColor.WHITE, PieceKind.PAWN);
		board[j][BOARDLENGTH-2]=new Piece(PlayerColor.BLACK, PieceKind.PAWN);
	}

	board[1][3]=new Piece(PlayerColor.BLACK, PieceKind.PAWN);
	
    board[0][0]=new Piece(null,null);
    board[0][1]=new Piece(PlayerColor.WHITE, PieceKind.ROOK);
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
  }
  
  public State(PlayerColor whoseTurn, Piece[][] boardstate){
	  this.whoseTurn = whoseTurn;
	  this.board = boardstate;
  }
  
  State(State original) {
	    Utils.array2dCopy(original.board, this.board);
	    this.whoseTurn = original.whoseTurn;
	    this.movesWithoutCaptureNorPawn = original.movesWithoutCaptureNorPawn;
	    this.gameover = original.gameover;
	    this.enpassantPiecePosition= original.enpassantPiecePosition;
  }
  /* an auxiliary function required for THREEFOLD_REPETITION_RULE 
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
  

  /**
   * Applies the move.
   * @return The resulting state
   * @throws IllegalMoveException if the move is not legal
   */
  public State makeMove(Move move) {

	Piece moving = board[move.from.getRow()][ move.from.getCol()];
	try {
		//check to see whether there is a piece of the chosen board location
		if (moving.getColor()!=this.whoseTurn){
			throw new IllegalMoveException ("There's no piece at the selected board location");
		}
		// check to see if the piece to be moved belongs to the current player
		if (moving.getColor()!=this.whoseTurn){
			throw new IllegalMoveException ("You're trying to move another's player piece");
		}
		//if it's occupied -a) can't move b) capture
		if (board[move.to.row][move.to.col].getColor()==this.getPlayerColor()){
			//TODO OK for swapping the king and rook
			throw new IllegalMoveException ("You're trying to capture your own piece");
		}
		// Test to see if the move is valid for the particular piece
		if (!validMoves(moving.getKind(), move.from).contains(move.to)){
			throw new IllegalMoveException ("This is an illegal move for this type of piece");
		}
	} catch (IllegalMoveException im) {
		// logging exceptions to console
		System.out.println(im.toString());
		return this;
	}
	
	
	//Check whether this is being a move with capture & by a pawn
	if (board[move.from.row][move.from.col].getKind() == PieceKind.PAWN 
			|| (board[move.to.row][move.to.col].getKind()!=null)){ //TODO enpassant
		this.movesWithoutCaptureNorPawn = 0;
	} else {
		this.movesWithoutCaptureNorPawn ++;
	}

	//Check for the http://en.wikipedia.org/wiki/Fifty-move_rule
	if (this.movesWithoutCaptureNorPawn == FIFTY_MOVE_RULE_NUM){
		this.gameover = GameOverReason.FIFTY_MOVE_RULE;
	}
	
	//checking if the king is being captured TODO
	if (board[move.to.row][move.to.col].getColor()==this.getPlayerColor().getOpposite() &&
			board[move.to.row][move.to.col].getKind() == PieceKind.KING){
		//this means game is over - king is captured
		this.gameover = GameOverReason.CHECK_MATE;
		return this;
	}
	//execute the move
	State nextState = new State(this);
	nextState.board[move.to.getRow()][ move.to.getCol()] = nextState.board[move.to.getRow()][ move.to.getCol()].SetPiece(moving);
	nextState.board[move.from.getRow()][ move.from.getCol()]=moving.PieceRemove();
	//if the pawn reaches the diagonal 8 of the other player it should be promoted
	if ( nextState.board[move.from.getRow()][move.from.getCol()].getKind()==PieceKind.PAWN && 
			(this.whoseTurn.toInt()*move.from.getCol()==6 || this.whoseTurn.toInt()*move.from.getCol()==-1)){
		nextState.board[move.to.getRow()][move.to.getCol()].setKind(ChessConsole.callForPromotion());
	}
	
	
	// if en passant happened we also need to remove the opponents pawn piece
	if (nextState.enpassantPiecePosition != null 
			&& nextState.whoseTurn.getOpposite() == nextState.board[nextState.enpassantPiecePosition.getRow()][nextState.enpassantPiecePosition.getCol()].getColor()){
		nextState.board[nextState.enpassantPiecePosition.getRow()][nextState.enpassantPiecePosition.getCol()].PieceRemove();
		nextState.enpassantPiecePosition = null; 
	}

	nextState.whoseTurn = this.whoseTurn.getOpposite();

    return nextState; 
  }
  
  public List<Position> validMoves(PieceKind kind, Position starting){
	  List<Position> moves = new LinkedList<Position>();
	  switch (kind){
		case PAWN:{
			// can move straight 1 board cell if the cell is not occupied // out of array boundaries
			Position to = new Position (starting.getRow(), starting.getCol()+1*this.whoseTurn.toInt());

			if ((this.board[to.getRow()][to.getCol()].getKind() == null) && to.isInRange(0, BOARDLENGTH)){
				moves.add (to);
			}
		
		  //en passant capture: starting position is 4/5 and there is an opponents pawn neighboring yours at the moment
			if (this.enpassantPiecePosition != null) {
				//if the conditions below are not valid, the en-passant opportunity is not followed
				if((starting.col*this.whoseTurn.toInt() == 4 || starting.col*this.whoseTurn.toInt() == -3) &&
					this.enpassantPiecePosition.getCol()==starting.col &&
					(this.enpassantPiecePosition.getRow() == starting.row+1 || this.enpassantPiecePosition.getRow() == starting.row-1 )){
						moves.add (new Position (this.enpassantPiecePosition.getRow(), this.enpassantPiecePosition.getCol()+1*whoseTurn.toInt()));
				} else{
					this.enpassantPiecePosition = null;
				}
			}
					
			//if starting position is horizontal 2(7) it's okay to move it to 4(5) if the way if not occupied
			to = new Position (starting.row, starting.col+2*this.whoseTurn.toInt());
			if ((starting.col*this.whoseTurn.toInt() == 1 || starting.col*this.whoseTurn.toInt() == -6)
					&& board [to.getRow()][to.getCol()].getKind() == null
					&& board [to.getRow()][to.getCol()-1*this.whoseTurn.toInt()].getKind() == null){
				moves.add (to);
				this.enpassantPiecePosition = to; //this pawn is potentially eligible to be captured via en passant 
			}
			// if it was a diagonal move  - OK when capturing
			to = new Position (starting.row+1, starting.col+1*this.whoseTurn.toInt());
			if (to.isInRange(0, BOARDLENGTH) && board [to.getRow()][to.getCol()].getKind() != null){
				moves.add (to);
			}
			to = new Position (starting.row-1, starting.col+1*this.whoseTurn.toInt());
			if (to.isInRange(0, BOARDLENGTH) && board [to.getRow()][to.getCol()].getKind() != null){
				moves.add (to);
			}
			
			break;
		}
		
		case ROOK:{
			//OK to move if same horizontal/vertical + the path is free
			moves.addAll(moveLine(starting, Direction.LEFT));
			moves.addAll(moveLine(starting, Direction.DOWN));
			moves.addAll(moveLine(starting, Direction.UP));
			moves.addAll(moveLine(starting, Direction.RIGHT));
			
			break;
		}

		case BISHOP:{
			//OK to move if same diagonal + the path is free
			moves.addAll(moveLine(starting, Direction.LEFTUP));
			moves.addAll(moveLine(starting, Direction.LEFTDOWN));
			moves.addAll(moveLine(starting, Direction.RIGHTUP));
			moves.addAll(moveLine(starting, Direction.RIGHTDOWN));
			
			break;
		}
		
		case QUEEN:{
			//OK to move if same horizontal/vertical/diagonal + the path is free
			moves.addAll(moveLine(starting, Direction.LEFT));
			moves.addAll(moveLine(starting, Direction.DOWN));
			moves.addAll(moveLine(starting, Direction.UP));
			moves.addAll(moveLine(starting, Direction.RIGHT));
			moves.addAll(moveLine(starting, Direction.LEFTUP));
			moves.addAll(moveLine(starting, Direction.LEFTDOWN));
			moves.addAll(moveLine(starting, Direction.RIGHTUP));
			moves.addAll(moveLine(starting, Direction.RIGHTDOWN));
			
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
		    moves.addAll(moveOffset(starting, offsets));
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
		    moves.addAll(moveOffset(starting, offsets));
		    break;
		}
	  }
	  
	  // this means en passant possibility was not used
	  if ((this.enpassantPiecePosition != null) &&( kind!=PieceKind.PAWN)){
		  this.enpassantPiecePosition = null;
	  }

	  return moves;
  }
  
  /*
   * auxiliary enum containing direction a figure can move
   */
  private enum Direction {RIGHT, LEFT, UP, DOWN, LEFTUP, RIGHTUP, LEFTDOWN, RIGHTDOWN} 
  
  /*an auxiliary function checking whether you can move a figure within particular offestes
   *  - used for king and knight
   */
  private List<Position> moveOffset(Position starting, int[][] offsets){
	    List<Position> moves = new LinkedList<Position>();
	    Position to;
	    for (int[] o : offsets) {
	    	to = new Position (starting.row+o[0], starting.col+o[1]);
	    	if (to.isInRange(0, BOARDLENGTH)){
	    		moves.add(to);
	    	}
	    };
		return moves;
  }
  /*an auxiliary function checking whether you can move a figure in a particular direction
   * while not getting out of the board & not jumping over pieces - used for queen, rook, bishop
   */
  private List<Position> moveLine(Position starting, Direction dir){
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
				to = new Position (starting.row+i, starting.col+j);
				if (!to.isInRange(0, BOARDLENGTH))
					break;
				moves.add (to);
			} while (board[to.row][to.col].getKind()==null);
			
		return moves;
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
	

  @Override
  public String toString(){
	  String res = " ";
	  char alphabet = 'a';
	  for (int num = 1; num<=BOARDLENGTH;num ++){
		  res +="        "+num;  
	  }
	  res +="\n";
	  for (Piece[] i : board){
		  res+= alphabet++ +" | ";
		  for (Piece j : i){
			  if (j.getColor()==null){
				  res +="      | "; 
			  } else {
				  res += j.toString()+ " | ";
			  }
		  }
	  res += "\n";
	  }
	  return res;
  }
}
