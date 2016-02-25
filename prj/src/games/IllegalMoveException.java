package games;

/* Describe an illegal move.
*/
public class IllegalMoveException extends RuntimeException {

private static final long serialVersionUID = 1L;

public IllegalMoveException() {
   super();
 }  

 public IllegalMoveException(String message) {
   super(message);
 }  
}
