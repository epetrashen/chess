package games;

public class Move{
	 private Position from;
	 private Position to;
	 
	public Move(){
	}
	
	public Position getTo(){
		return this.to;
	}
	
	public Position getFrom(){
		return this.from;
	}

	public Move(Position from, Position to){
	  this.from = from;
	  this.to = to;
	}
}
