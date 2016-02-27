package chess;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.IllegalMoveException;
import games.PlayerColor;
import games.Position;
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
  public int movesWithoutCaptureNorPawn = 0;
  

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

	//board[1][3]=new Piece(PlayerColor.BLACK, PieceKind.KING);
	
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
	/*board[0][6]=new Piece(PlayerColor.WHITE, PieceKind.ROOK);
	board[3][7]=new Piece(PlayerColor.BLACK, PieceKind.KING);
	board[3][5]=new Piece(PlayerColor.WHITE, PieceKind.KING);
	board[6][6]=new Piece(PlayerColor.WHITE, PieceKind.PAWN);*/
  }
 
  
  public State(State original) {
		for (int i=0; i < BOARDLENGTH; i++){
			for (int j=0; j < BOARDLENGTH; j++){
				board[i][j] = new Piece (original.board[i][j].getColor(),original.board[i][j].getKind());
			}
		}
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
  
  
  public List<Position> validMoves(PieceKind kind, Position starting, PlayerColor pc){
	  List<Position> moves = new LinkedList<Position>();
	  switch (kind){
		case PAWN:{
			// can move straight 1 board cell if the cell is not occupied // out of array boundaries
			Position to = new Position (starting.getRow(), starting.getCol()+1*pc.toInt());

			if ((this.board[to.getRow()][to.getCol()].getKind() == null) && to.isInRange(0, BOARDLENGTH)){
				moves.add (to);
			}
		
		  //en passant capture: starting position is 4/5 and there is an opponents pawn neighboring yours at the moment
			if (this.enpassantPiecePosition != null) {
				//if the conditions below are not valid, the en-passant opportunity is not followed
				if((starting.col*pc.toInt() == 4 || starting.col*pc.toInt() == -3) &&
					this.enpassantPiecePosition.getCol()==starting.col &&
					(this.enpassantPiecePosition.getRow() == starting.row+1 || this.enpassantPiecePosition.getRow() == starting.row-1 )){
						moves.add (new Position (this.enpassantPiecePosition.getRow(), this.enpassantPiecePosition.getCol()+1*whoseTurn.toInt()));
				} else{
					this.enpassantPiecePosition = null;
				}
			}
					
			//if starting position is horizontal 2(7) it's okay to move it to 4(5) if the way if not occupied
			to = new Position (starting.row, starting.col+2*pc.toInt());
			if ((starting.col*pc.toInt() == 1 || starting.col*pc.toInt() == -6)
					&& board [to.getRow()][to.getCol()].getKind() == null
					&& board [to.getRow()][to.getCol()-1*this.whoseTurn.toInt()].getKind() == null){
				moves.add (to);
				this.enpassantPiecePosition = to; //this pawn is potentially eligible to be captured via en passant 
			}
			// if it was a diagonal move  - OK when capturing
			to = new Position (starting.row+1, starting.col+1*pc.toInt());
			if (to.isInRange(0, BOARDLENGTH) && board [to.getRow()][to.getCol()].getKind() != null){
				moves.add (to);
			}
			to = new Position (starting.row-1, starting.col+1*pc.toInt());
			if (to.isInRange(0, BOARDLENGTH) && board [to.getRow()][to.getCol()].getKind() != null){
				moves.add (to);
			}
			
			break;
		}
		
		case ROOK:{
			//OK to move if same horizontal/vertical + the path is free
			moves.addAll(moveLine(starting, Direction.LEFT, pc));
			moves.addAll(moveLine(starting, Direction.DOWN, pc));
			moves.addAll(moveLine(starting, Direction.UP, pc));
			moves.addAll(moveLine(starting, Direction.RIGHT, pc));
			
			break;
		}

		case BISHOP:{
			//OK to move if same diagonal + the path is free
			moves.addAll(moveLine(starting, Direction.LEFTUP, pc));
			moves.addAll(moveLine(starting, Direction.LEFTDOWN, pc));
			moves.addAll(moveLine(starting, Direction.RIGHTUP, pc));
			moves.addAll(moveLine(starting, Direction.RIGHTDOWN,pc));
			
			break;
		}
		
		case QUEEN:{
			//OK to move if same horizontal/vertical/diagonal + the path is free
			moves.addAll(moveLine(starting, Direction.LEFT, pc));
			moves.addAll(moveLine(starting, Direction.DOWN, pc));
			moves.addAll(moveLine(starting, Direction.UP, pc));
			moves.addAll(moveLine(starting, Direction.RIGHT, pc));
			moves.addAll(moveLine(starting, Direction.LEFTUP, pc));
			moves.addAll(moveLine(starting, Direction.LEFTDOWN, pc));
			moves.addAll(moveLine(starting, Direction.RIGHTUP, pc));
			moves.addAll(moveLine(starting, Direction.RIGHTDOWN, pc));
			
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
		    
		    moves.addAll(moveOffset(starting, offsets, pc));
		    /*need to check that the king is not moving to endangered field
		    int n = moveOffset(starting, offsets).size();
		    for (int k=0; k<n; k++){
		    	System.out.println("p");
		    	if (! isUnderRiskOfCapture (moveOffset(starting, offsets).get(k), 
		    			this.getPlayerColor().getOpposite())){
		    		moves.add(moveOffset(starting, offsets).get(k));
		    	}
		    }
		    */
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
		    moves.addAll(moveOffset(starting, offsets, pc));
		    
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
  
  /*an auxiliary function checking whether you can move a figure within particular offsets
   *  - used for king and knight
   */
  private List<Position> moveOffset(Position starting, int[][] offsets, PlayerColor pc){
	    List<Position> moves = new LinkedList<Position>();
	    Position to;
	    for (int[] o : offsets) {
	    	to = new Position (starting.row+o[0], starting.col+o[1]);
	    	if (to.isInRange(0, BOARDLENGTH)){
	    		if (this.board[starting.row+o[0]][starting.col+o[1]].getColor()!=pc)
	    			moves.add(to);
	    	}
	    };
		return moves;
  }
  /*an auxiliary function checking whether you can move a figure in a particular direction
   * while not getting out of the board & not jumping over pieces - used for queen, rook, bishop
   */
  private List<Position> moveLine(Position starting, Direction dir, PlayerColor pc){
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
				if (board[to.row][to.col].getColor()!=pc)
					moves.add (to);
			} while (board[to.row][to.col].getKind()==null);
			
		return moves;
  }
 
 /* function returning KING position
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
				  res +="       | "; 
			  } else {
				  res += j.toString()+ " | ";
			  }
		  }
	  res += "\n";
	  }
	  return res;
  }
} 
