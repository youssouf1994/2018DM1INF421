public class Main {

    public static void main (String[] args)
    {
	// The random seed must be fixed, so that the tests are the same for everyone
	// and are reproducible. 
	new StableMatchingTest (
	    new StableMatching (),
	    System.out,
	    new java.util.Random (0L) // The program should also work correctly for other values of random seed - try specifying a different integer
	).test('A'); // Set internal call parameter to 'A', 'B', or 'C' depending on the chosen variant. 
    }

}
