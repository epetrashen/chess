package chess;
import games.Position;

public class Move{
	 public Position from;
	 public Position to;
	 
		public Move(){
			}

	public Move(Position from, Position to){//, Piece promoteToPiece) {
	  this.from = from;
	  this.to = to;
	}
}
