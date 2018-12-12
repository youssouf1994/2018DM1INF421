// This is a test harness for an implementation of the Gale-Shapley
// stable matching algorithm.
// Author: François Pottier.
// Modified by Adrian Kosowski for the grouped variant.

import java.io.PrintStream;
import java.util.Random;
import java.util.Arrays;

public class StableMatchingTest {

    // This is the student's implementation of the stable matching algorithm.
    // We view it as a black box, to be tested.

    private StableMatchingInterface box;

    // Up to a certain size, known as SMALL, we try every value of n. This is
    // cheap and allows giving the box a hard time.

    public final static int SMALL = 128;

    // Between SMALL and LARGE, we double the value of n at each step. This
    // allows us to quickly try larger and larger instances.

    // At n = 8192, one preference matrix occupies 256 megabytes, so a problem
    // instance is 512 megabytes. The test harness then requires 1 gigabyte
    // (because it stores both the problem instance and its inverse) plus
    // whatever the box itself requires.

    public final static int LARGE = 8192;
    
    // Maximum dimension of matrix for all tests, except those with n=m=w.
	
    public final static int LARGE_NONUNIT = 4096;
    

    // At each size, we generate a certain number of random instances.

    public final static int R = 16;

    // The box must reply under a certain time limit (expressed in milliseconds).

    public final static long TIMEOUT = 30000;

    // Setting this flag causes us to abandon after the first failure.

    public final static boolean STOP = false;

    // These fields are used to hold the result produced by the matching
    // algorithm. One might think that they should be local variables of the
    // method "run(n,menPrefs,womenPrefs)" below, but can't be, because they
    // are mutated in an inner class.

    private int[][] mar;
    private Throwable exception;

    // This is the output stream that is used for logging.

    private PrintStream out;

    // Running counts of failures and successes.

    private int failures, successes;

    // A pseudo-random number generator.

    private Random random;

    // Constructor.

    StableMatchingTest (StableMatchingInterface box, PrintStream out, Random random)
    {
        this.box = box;
        this.out = out;
        failures = successes = 0;
        this.random = random;
    }

    // This method assumes that its argument is a permutation of [0,n), and
    // returns the inverse permutation.

    int[] inversePermutation (int[] p)
    {
        int n = p.length;
        int[] inverse = new int [n];
        for (int i = 0; i < n; i++)
            inverse[p[i]] = i;
        return inverse;
    }

    // This method prints a preference matrix.
    
    void printPreferences (int[][] prefs)
    {
        int n = prefs.length;
        for (int i = 0; i < n; i++)
            out.printf("%d prefers: %s\n", i, Arrays.toString(prefs[i]));
    }

    // These methods print the parameters and result of a test run.

    void printParameters (final int n,  final int[] menGroupCount, final int[] womenGroupCount,
            final int[][] menPrefs, final int[][] womenPrefs)
    {
        out.println("The parameters of this test run were:");
        out.printf("n = %d\n", n);
        out.printf("m = %d\n", menGroupCount.length);
        out.printf("w = %d\n", womenGroupCount.length);        
        out.println("menGroupCount =");
        out.println(Arrays.toString(menGroupCount));
        out.println("womenGroupCount =");
        out.println(Arrays.toString(womenGroupCount));
        out.println("menPrefs =");
        printPreferences(menPrefs);
        out.println("womenPrefs =");
        printPreferences(womenPrefs);
    }

    void printResult (int[][] mar)
    {
        out.println("The result of this test run was:");
        out.println("marriages =");
        out.println(Arrays.toString(mar));
    }

    int[][] deepCopy2D (int[][] arg)
    {
        int[][] copy = new int[arg.length][];
        for (int i=0; i<arg.length; i++)
          copy[i] = Arrays.copyOf(arg[i], arg[i].length);
        return copy;
    }


    // This method records a failure.

    void fail ()
    {
        failures++;
        // If the user wishes that we stop after the first failure, do so.
        if (STOP)
            System.exit(1);
    }

    // This method is a wrapper around the box's main method. It invokes the box,
    // controls its side effects (exceptions), and measures its time consumption.

    void run (final int[] menGroupCount, final int[] womenGroupCount,
            final int[][] menPrefs, final int[][] womenPrefs)
    {
        // Log the parameters of this test case.
        int n = 0;
        for (int s : menGroupCount)
            n += s;
        int womenCount = 0;
        for (int s : womenGroupCount)
            womenCount += s;
        assert (n == womenCount);
        
        out.println();
        out.printf("n = %d, m = %d, w = %d: running...\n", n, menGroupCount.length, womenGroupCount.length);

        // Perform garbage collection prior to running the algorithm, so as to
        // (hopefully) avoid the need for GC while the algorithm is running.
        // This should allow us to obtain a more accurate time measurement.
        // (Do this only at large sizes, as it is slow. We do not measure
        // performance at small sizes.)

        if (n >= SMALL)
            System.gc();

        // Run the student's method in a separate thread, so we can terminate it
        // if it does not finish in a reasonable time.

        mar = null;
        exception = null;

        Thread thread = new Thread () {

            public void run () {

                int[] mgc =  Arrays.copyOf(menGroupCount, menGroupCount.length);
                int[] wgc =  Arrays.copyOf(womenGroupCount, womenGroupCount.length);
                int[][] mp = deepCopy2D(menPrefs);
                int[][] wp = deepCopy2D(womenPrefs);
                
                long startTime = System.currentTimeMillis();
                try {
                    mar = box.constructStableMatching(mgc, wgc, mp, wp);
                } catch (Throwable e) {
                    // This is either an exception thrown by the student's code, or
                    // ThreadDeath, caused by a call to thread.stop() below.
                    // We record the exception and terminate.
                    exception = e;
                }
                if (exception == null) {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    out.printf("Elapsed time: %d milliseconds\n", duration);
                }

            }

        };

        // Start the student's thread, and wait for it to terminate.

        thread.start();
        try {
            thread.join(TIMEOUT);
        } catch (InterruptedException e) {
            // In principle, we cannot be interrupted, unless the student does
            // very weird things.
            assert (false);
        }
        // If the thread is still alive, kill it and declare a timeout.
        // In principle, the thread could terminate normally after isAlive() returns
        // true and before we kill it. This does not seem to be a problem -- the
        // outcome is still considered a timeout.
        if (thread.isAlive()) {
            thread.stop();
            out.println("FAILURE: TIMEOUT!");
            out.printf("Your code did not terminate within %d milliseconds.\n", TIMEOUT);
            if (n < SMALL)
                printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
            fail();
            return;
        }
        
        // The thread has terminated on its own.

        // If it has raised an exception, log it.
        if (exception != null) {
            out.println("FAILURE: EXCEPTION!");
            out.println("Your code unexpectedly throws an exception:");
            out.println(exception);
            printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
            fail();
            return;
        }

        // The thread did not throw an exception.

        if (mar == null) {
            out.println("FAILURE: NULL RESULT!");
            out.println("Your code returns a null array.");
            printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
            printResult(null);
            fail();
            return;
        }
        
        int m = menGroupCount.length;
        int w = womenGroupCount.length;
        
        if (mar.length != m)  {
            out.println("FAILURE: INVALID 1st DIMENSION OF RESULT!");
            out.printf("Your code returns an array of length %d,\nwhereas an array of length %d was expected.\n",
                    mar.length, m);
            printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
            printResult(mar);
            fail();
            return;
        }
      
        
        int sumMar = 0;
        int[] sumMarMen = new int[m];
        int[] sumMarWomen = new int[w];
        for (int i=0; i<m; i++) {
            if (mar[i].length != w)  {
                out.println("FAILURE: INVALID 2nd DIMENSION OF RESULT!");
                out.printf("Your code returns an array of length %d in row %d,\nwhereas an array of length %d was expected.\n",
                        mar[i].length, i, w);
                printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
                printResult(mar);
                fail();
                return;
            }
            for (int j=0; j<w; j++) {
                if (mar[i][j] < 0 || mar[i][j] > n) {
                    out.println("FAILURE: INVALID ENTRY IN RESULT!");
                    out.printf("Your code returns %d marriages between group of men %d and group of women %d,\n value in range [0,%d] expected.",
                            mar[i][j], i, j, n);
                    printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
                    printResult(mar);
                    fail();
                    return;
                }
                sumMar += mar[i][j];
                sumMarMen[i] += mar[i][j];
                sumMarWomen[j] += mar[i][j];
                if (sumMar > n){ // No integer overflows, thank you.  Check reliable for n < MAX_INT / 2.
                    out.println("FAILURE: TOO MANY PEOPLE ARE MARRIED!");
                    out.printf("Your code returns >= %d people married, but there are only %d.\n",
                            sumMar, n);
                    printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
                    printResult(mar);
                    fail();
                    return;
                }
            }
        }
        
        // Check if correct number of men and women are married in each group (matching condition)
        for (int i=0; i<m; i++)
            if (sumMarMen[i] != menGroupCount[i]){
                out.println("FAILURE: WRONG NUMBER OF MEN MARRIED!");
                out.printf("Your code returns %d men married in group %d, but there are %d in this group.\n",
                        sumMarMen[i], i, menGroupCount[i]);
                printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
                printResult(mar);
                fail();
                return;
            }
        
        for (int j=0; j<w; j++)
            if (sumMarWomen[j] != womenGroupCount[j]){
                out.println("FAILURE: WRONG NUMBER OF WOMEN MARRIED!");
                out.printf("Your code returns %d women married in group %d, but there are %d in this group.\n",
                        sumMarWomen[j], j, womenGroupCount[j]);
                printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
                printResult(mar);
                fail();
                return;
            }
        
        // Now, check matching for stability.

        int[][] revMenPrefs = new int[m][];
        for (int i = 0; i < m; i++)
            revMenPrefs[i] = inversePermutation(menPrefs[i]);
        int[][] revWomenPrefs = new int[w][];
        for (int j = 0; j < w; j++)
            revWomenPrefs[j] = inversePermutation(womenPrefs[j]);

        // Find least preferred wife (menPrefs[i][worstWifeIndex[i]]) for a man from every group i 
        int[] worstWifeIndex = new int[m];
        for (int i=0; i<m; i++)
            for (int jx=w-1; jx>=0; jx--)
                if (mar[i][menPrefs[i][jx]] > 0) {
                    worstWifeIndex[i] = jx;
                    break;
                }
        
        // Find least preferred husband (womenPrefs[j][worstHusbandIndex[j]]) for a woman from every group j 
        int[] worstHusbandIndex = new int[w];
        for (int j=0; j<w; j++)
            for (int ix=m-1; ix>=0; ix--)
                if (mar[womenPrefs[j][ix]][j] > 0) {
                    worstHusbandIndex[j] = ix;
                    break;
                }
        
        for (int i=0; i<m; i++)
            for (int j=0; j<w; j++) {
                if (revWomenPrefs[j][i] < worstHusbandIndex[j] && revMenPrefs[i][j] < worstWifeIndex[i]) {
                    out.println("FAILURE: NOT A STABLE MATCHING!");
                    out.printf("The pair formed by a man from group %d and a woman from group %d is unstable.\n",
                            i, j);
                    out.printf("Indeed, the man from group %d prefers the woman from group %d to his bride from group %d\n",
                            i, j, menPrefs[i][worstWifeIndex[i]]);
                    out.printf("and a woman from group %d prefers the woman from group %d to her groom from group %d.\n",
                            i, j, womenPrefs[j][worstHusbandIndex[j]]);
                    printParameters(n, menGroupCount, womenGroupCount, menPrefs, womenPrefs);
                    printResult(mar);
                    fail();
                    return;
                }
            }        

        out.println("SUCCESS!");
        successes++;
    }

    // This method constructs an identity array.

    int[] identityArray (int n)
    {
        int[] p = new int [n];
        for (int i = 0; i < n; i++)
            p[i] = i;
        return p;
    }

    // This method constructs a reverse identity array.

    int[] reverseIdentityArray (int n)
    {
        int[] p = new int [n];
        for (int i = 0; i < n; i++)
            p[i] = n-1-i;
        return p;
    }

    // This method constructs a preferences matrix where all people agree.

    int[][] uniformPrefs (int m, int[] individualPrefs)
    {
        int[][] prefs = new int[m][];
        for (int i = 0; i < m; i++)
            prefs[i] = individualPrefs;
        return prefs;
    }

    // This method constructs a preferences matrix where (at most) two distinct
    // visions exist.

    int[][] mixedPrefs (int m, int[] individualPrefsOne, int[] individualPrefsTwo)
    {
        int[][] prefs = new int[m][];
        for (int i = 0; i < m; i++)
            prefs[i] = (i % 2 == 0) ? individualPrefsOne : individualPrefsTwo;
        return prefs;
    }

    // This method constructs a random permutation of [0,n).
    // Source: Wikipedia, "inside-out" variant of the Fisher-Yates shuffle.

    int[] randomPermutation (int n)
    {
        int[] p = new int [n];
        // p[0] = 0;
        for (int i = 1; i < n; i++) {
            // Pick a value j so that 0 <= j <= i.
            int j = random.nextInt(i+1);
            // Initialize p[i] to i, and swap p[i] and p[j].
            // These two operations can be done in only two instructions, as follows.
            p[i] = p[j];
            p[j] = i;
        }
        return p;
    }

    // This method constructs a random preference matrix.

    int[][] randomPrefs (int m, int w)
    {
        int[][] prefs = new int[m][];
        for (int i = 0; i < m; i++)
            prefs[i] = randomPermutation(w);
        return prefs;
    }

    
    // Returns integer array of 1s of length l
    int[] unit (int l)
    {
        int[] arr = new int[l];
        Arrays.fill(arr, 1);
        return arr;
    }
    
    // This method submits the box to a series of tests with given group size arrays.

    public void test (int[] menGroupCount, int[] womenGroupCount)
    {
        // Construct non-random instances of size n.

        int m = menGroupCount.length;
        int w = womenGroupCount.length;
        
        int[] identityM = identityArray(m);
        int[] identityW = identityArray(w);
        int[] reverseM = reverseIdentityArray(m);
        int[] reverseW = reverseIdentityArray(w);

        // An instance where all men agree and all women agree.
        run(menGroupCount, womenGroupCount, uniformPrefs(m,identityW), uniformPrefs(w,identityM));
        // Another instance where all men agree and all women agree.
        run(menGroupCount, womenGroupCount, uniformPrefs(m,identityW), uniformPrefs(w,reverseM));
        // An instance where all men agree and women have diverging visions.
        run(menGroupCount, womenGroupCount, uniformPrefs(m,identityW), mixedPrefs(w, identityM, reverseM));
        // An instance where all women agree and men have diverging visions.
        run(menGroupCount, womenGroupCount, mixedPrefs(m,identityW, reverseW), uniformPrefs(w,identityM));

        // Construct random instances of size n.

        for (int r = 0; r < R; r++)
            run(menGroupCount, womenGroupCount, randomPrefs(m,w), randomPrefs(w,m));
    }

    // Returns m groups of men and w groups of women, with a total of n people
    // n <= 2*nEstimate + m*w, group sizes are identically distributed with some random distribution
    // in expectation, n is close to nEstimate
    // Returns null if n > nBound.
    int[][] randomMW (int m, int w, int nEstimate, int nBound)
    {
        int[][] res = new int[2][];
        res[0] = new int[m];
        res[1] = new int[w];
        int n = 0;
        if (nEstimate > m*w) {
            int boundPerSquare = 1 + (2 * nEstimate / (m*w)); 
            for (int i=0; i < m; i++)
                for (int j=0; j < w; j++) {
                    int rv = random.nextInt(boundPerSquare) + 1;
                    res[0][i] += rv;
                    res[1][j] += rv;
                    n += rv;
                }
        }
        else {
            if (nEstimate < m || nEstimate < w)
                return null;
            Arrays.fill(res[0], 1);
            for (int k= nEstimate - m; k>0; k--)
                res[0][random.nextInt(m)]++;
            Arrays.fill(res[1], 1);            
            for (int k= nEstimate - w; k>0; k--)
                res[1][random.nextInt(w)]++;
            n = nEstimate;
        }
        if (n <= nBound)
            return res;
        else return null;
    }


    // This method submits the box to a series of tests at multiple sizes.

    public void test (char testType) 
    {
        out.printf("Starting test suite %c...\n\n", testType);

        // Test suite for m = w = n
        int n;

        // First, test at all sizes sequentially up to a certain size.
        for (n = 0; n <= SMALL; n++)
            test(unit(n), unit(n));
        // Then, test at larger and larger sizes, doubling the size at each step.
        // (Thus, all sizes will be powers of two; this might not be a good thing
        // in general, as we do not test the algorithm at other sizes, but it
        // should be OK here; we are interested mainly in the performance data).
        for (n = 2 * SMALL; n <= LARGE; n *= 2)
            test(unit(n), unit(n));

        int[] nRange = new int[]{50, 500, 3000, 6000, 16000, 1000000, 10000000, 100000000, 800000000};

        int nBound;
        switch (testType) {
            case 'A': nBound = 1000000000; break;
            case 'B': nBound = LARGE; break;
            default:  nBound = -1;                    
        }
        
        for (int nEstimate : nRange) {
            if (nEstimate < nBound)
                for (int m = 1; m <= LARGE_NONUNIT; m *= 4)
                    for (int w = 1; w <= LARGE_NONUNIT; w *= 4) {
                        int[][] mw = randomMW(m, w, nEstimate, nBound);
                        if (mw != null)
                            test (mw[0], mw[1]);
                    }                    
        }
        
        out.println();
        out.printf("Done test suite %c. In total, %d success(es) and %d failure(s).\n",
                testType, successes, failures);
   }
};

