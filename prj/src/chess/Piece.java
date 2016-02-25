package chess;
import games.PlayerColor;

public class Piece {	
	 private PlayerColor color;
	 private PieceKind kind;
	 
public Piece() {
}

public Piece(PlayerColor color, PieceKind kind) {
    this.color = color;
    this.kind = kind;
  }

public Piece SetPiece(Piece p) {
    this.color = p.color;
    this.kind = p.kind;
    return this;
  }

public PlayerColor getColor() {
    return color;
  }
  
  public PieceKind getKind() {
    return kind;
  }
  
  public Piece PieceRemove(){
	  this.color = null;
	  this.kind = null;
	  return this;
  }
  
  @Override
  public String toString(){
	  return color+" "+kind;
  }
}
