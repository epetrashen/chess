package games;

public class Utils {
	
	private Utils() {
	  }

	
	  /**
	   * deepCopy() utility methods. Makes a copy of a 2D array by
	   * reference.
	   */
	  public static <T> void array2dCopy(T[][] src, T[][] target) {
	    for (int i = 0; i < target.length; i++) {
	      System.arraycopy(src[i], 0, target[i], 0, target[i].length);
	    }
	  }
	  
	  

}
