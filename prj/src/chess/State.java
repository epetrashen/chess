package chess;

import java.util.Arrays;

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
} 