package games;

public enum PlayerColor {
		  C1, C2;

		  public static final PlayerColor WHITE = C1;
		  public static final PlayerColor BLACK = C2;
		  public static final PlayerColor W = WHITE;
		  public static final PlayerColor B = BLACK;

		  public PlayerColor getOpposite() {
		    if (this == WHITE || this == BLACK)
		    	return this == WHITE ? BLACK : WHITE;
		    return null;
		  }

		  public boolean isWhite() {
		    return this == WHITE;
		  }

		  public boolean isBlack() {
		    return this == BLACK;
		  }
		  
		  public int toInt(){
			return isWhite() ? 1 : isBlack() ? -1 : null;
		  }

		  @Override
		  public String toString() {
		    return isWhite() ? "W" : isBlack() ? "B" : null;
		  }
}
