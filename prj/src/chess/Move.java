package chess;
import games.Position;

/**
 * When a pawn reaches the eighth rank, it can be promoted to another piece kind. It is null if no
 * promotion was made.
 */
public class Move{
	public Piece promoteToPiece;
	
	 public Position from;
	 public Position to;
	 
		public Move(){
			}

	public Move(Position from, Position to){//, Piece promoteToPiece) {
	  this.from = from;
	  this.to = to;
	 // this.promoteToPiece = promoteToPiece;
	}
}
