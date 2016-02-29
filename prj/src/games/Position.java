package games;


public class Position {

	 private int row;
	 private int col;

     public Position(int row, int col) {
	 this.row = row;
	 this.col = col;
	}

	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}
	
	/**
	 * describes whether both row and column are within certain range (normally, board)
	 */
	public boolean isInRange (int min, int max){
		return row >=min && row <max && col >=min && col < max ? true : false;
	}

	@Override
	public String toString() {
		return "(" + row + "," + col + ")";
	}
	
	@Override
	public boolean equals(Object o) {
	    return o instanceof Position && this.getRow() == ((Position) o).getRow() && this.getCol() == ((Position) o).getCol();
	}
}