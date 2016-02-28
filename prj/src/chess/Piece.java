package chess;

import games.PlayerColor;

public class Piece {	
	 private PlayerColor color;
	 private PieceKind kind;
	 private boolean hasMoved = false;
	 
public Piece() {
}

public boolean getIfMoved(){
	return hasMoved;
}

public void setIfMoved(boolean hasMoved){
	this.hasMoved = hasMoved;
}

public Piece(PlayerColor color, PieceKind kind) {
    this.color = color;
    this.kind = kind;
  }

public Piece(PlayerColor color, PieceKind kind, boolean hasMoved) {
    this.color = color;
    this.kind = kind;
    this.hasMoved = hasMoved;
  }

public Piece SetPiece(Piece p) {
    this.color = p.color;
    this.kind = p.kind;
    this.hasMoved = p.hasMoved;
    return this;
  }

public PlayerColor getColor() {
    return color;
  }
  
  public PieceKind getKind() {
    return kind;
  }
  
  public void setKind(PieceKind kind) {
	    this.kind = kind;
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
  
  @Override
  public boolean equals(Object o) 
  {
      if (o instanceof Piece) 
      {
        Piece p = (Piece) o;
        if (this.kind == p.kind && this.color == p.color ) {	  
           return true;
        }
      }
      return false;
  }
}
