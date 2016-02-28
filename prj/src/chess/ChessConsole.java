package chess;

import java.util.Scanner;

import games.PlayerColor;
import games.Position;

public class ChessConsole {
	
	private ChessConsole(){};
	
	static final int CONST_FOR_ALPHANUMERIC_CONVERSION = 96;
	
	public static Move getMovePosition(PlayerColor c, int number){
		Move m = new Move();
		Scanner input = new Scanner (System.in);
		System.out.println ("\nIt's time for the move #"+number+" by "+c+".");
		System.out.println ("Please input the current coordinates of the piece and the coordinates you wish" +
				" the piece to move to, separating them by a dash (i.e. e2-e4; move the king appropriately for castling): ");
		String in = input.nextLine().toLowerCase();
		//checking for possible incorrect input + if it's out of the board
		while (!(in.matches("[a-i][1-8]-[a-i][1-8]$"))){
			System.out.println ("Bad input. Please try again:");
			in = input.nextLine().toLowerCase();
		}
		// conversion on the user input to int corresponding to the possible range of the board array
		m.from = new Position ((int)in.charAt(0)-CONST_FOR_ALPHANUMERIC_CONVERSION-1, Character.getNumericValue(in.charAt(1))-1); 
		m.to = new Position ((int)in.charAt(3)-CONST_FOR_ALPHANUMERIC_CONVERSION-1, Character.getNumericValue(in.charAt(4))-1);

		return m;
	}
	
	public static void printBoardState(State s){
		System.out.println (s.toString());
	}
	
	
	public static void gameOverMessage(State st){
		switch (st.gameover){
			case CHECK_MATE:
				System.out.println ("The check cannot be escaped. "+ st.whoseTurn.getOpposite() +" won.");
				break;
			case FIFTY_MOVE_RULE:
				System.out.println ("The 50-move rule is broken. Draw.");
				break;
			case THREEFOLD_REPETITION_RULE:
				System.out.println ("The threefold repetition rule is broken. Draw.");
				break;
			case NO_AVAILABLE_MOVES:
				System.out.println ("There's no available moves for "+ st.whoseTurn +". Draw.");
				break;
		}
	}
	
	public static PieceKind callForPromotion(){
		System.out.println ("You now can promote you pawn. Please input the desired piece kind, i.e. QUEEN");
		Scanner input = new Scanner (System.in);
		String in = input.nextLine().toUpperCase();
		PieceKind res = null;
		
		while (res == null){
			for (PieceKind pk : PieceKind.values()) {
		        if (pk.name().equals(in.toUpperCase())) {
		        	if (pk!=PieceKind.KING){
		        		res = pk;}
		        	else {
		        		System.out.println ("You cannot promote your pawn to KING. Please try again.");
		        	}
		        }
		    }
			if (res == null){
				System.out.println ("Bad input. Please try again:");
				in = input.nextLine().toUpperCase();
			}
		}
	    
	    return res;

	}
	
	public static void printMessage (String s){
		System.out.println (s);
	}

}
