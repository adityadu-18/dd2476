/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

package pr;

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */

    /**
	 * method: the number indicating which ranking method to use.
	 * 0 -> computing w/power iteration.
	 * 1 -> mc, end-point w/random start
	 * 2 -> mc, end-point w/cyclic start
	 * 3 -> mc, cmoplete path
	 * 4 -> mc, complete path stopping at dangling nodes
	 * 5 -> mc, complete path with random start
	 * 6 -> cpt w/pi, write to file.
	 */
    public PageRank(String filename, String method) {
    	int noOfDocs = readDocs( filename );
    	computePagerank(noOfDocs, method);
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
		int fileIndex = 0;
		try {
		    System.err.print( "Reading file... " );
		    BufferedReader in = new BufferedReader( new FileReader( filename ));
		    String line;
		    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
				int index = line.indexOf( ";" );
				String title = line.substring( 0, index );
				Integer fromdoc = docNumber.get( title );
				//  Have we seen this document before?
				if ( fromdoc == null ) {	
				    // This is a previously unseen doc, so add it to the table.
				    fromdoc = fileIndex++;
				    docNumber.put( title, fromdoc );
				    docName[fromdoc] = title;
				}
				// Check all outlinks.
				StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
				while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
				    String otherTitle = tok.nextToken();
				    Integer otherDoc = docNumber.get( otherTitle );
				    if ( otherDoc == null ) {
					// This is a previousy unseen doc, so add it to the table.
						otherDoc = fileIndex++;
						docNumber.put( otherTitle, otherDoc );
						docName[otherDoc] = otherTitle;
				    }
				    // Set the probability to 0 for now, to indicate that there is
				    // a link from fromdoc to otherDoc.
				    if ( link.get(fromdoc) == null ) {
				    	link.put(fromdoc, new Hashtable<Integer,Boolean>());
				    }
				    if ( link.get(fromdoc).get(otherDoc) == null ) {
						link.get(fromdoc).put( otherDoc, true );
						out[fromdoc]++;
				    }
				}
		    }
		    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		    	System.err.print( "stopped reading since documents table is full. " );
		    }
		    else {
		    	System.err.print( "done. " );
		    }
		    // Compute the number of sinks.
		    for ( int i=0; i<fileIndex; i++ ) {
				if ( out[i] == 0 )
				    numberOfSinks++;
		    }
		}
		catch ( FileNotFoundException e ) {
		    System.err.println( "File " + filename + " not found!" );
		}
		catch ( IOException e ) {
		    System.err.println( "Error reading file " + filename );
		}
		System.err.println( "Read " + fileIndex + " number of documents" );
		return fileIndex;
    }
    
    double diffOfArray(double[] foo, double[] bar) {
    	// compare the difference of two arrays.
    	// use "sum of difference" method.
    	double length = 0;
    	for (int i = 0; i < foo.length; i++) {
    		length += Math.abs(foo[i] - bar[i]);
    	}
    	return length;
    }
    
    void sortVector(double[] foo) {
    	VectorPair[] bar = new VectorPair[foo.length];
    	for (int i = 0; i < foo.length; i++) {
    		bar[i] = new VectorPair(i, foo[i]);
    	}
    	Arrays.sort(bar);
    	// create a pair and sort it.
    	int i = 0;
    	for (VectorPair hoge: bar) {
    		System.out.println(++i + ": " + docName[hoge.index] + " " + hoge.value);
    		if (i == 50) {
    			break;
    		}
    	}
    	// print the result.
    }
    
    double[] monteCarlo(int numberOfDocs, int numWalks, int method) {
    	double[] bar = new double[numberOfDocs];
    	// the probability vector.
    	Random hoge = new Random();
    	boolean isDangling = false;
    	// for mc method 4.
    	
    	if (method == 1) {
    		// random start, N = numWalks.
	    	for (int i = 0; i < numWalks; i++) {
	    		int initPage = hoge.nextInt(numberOfDocs);
	    		LinkedList<Integer> list = randomWalk(numberOfDocs, initPage, isDangling);
	    		bar[list.removeLast().intValue()]++;
	    	}
	    	for (int i = 0; i < numberOfDocs; i++) {
	    		bar[i] = bar[i] / (double)numWalks;
	    	}
    	} else if (method == 2) {
    		// cyclic start, N = numberOfDocs * numWalks.
    		for (int i = 0; i < numberOfDocs; i++) {
    			for (int j = 0; j < numWalks; j++) {
    				LinkedList<Integer> list = randomWalk(numberOfDocs, i, isDangling);
    				bar[list.removeLast().intValue()]++;
    			}
    		}
    		for (int i = 0; i < numberOfDocs; i++) {
	    		bar[i] = bar[i] / (double) (numWalks * numberOfDocs);
	    	}
    	} else if (method == 3) {
    		// complete path, N = numberOfDocs * numWalks.
    		int length = 0;
    		for (int i = 0; i < numberOfDocs; i++) {
    			for (int j = 0; j < numWalks; j++) {
    				LinkedList<Integer> list = randomWalk(numberOfDocs, i, isDangling);
    	    		length += list.size();
    	    		while (list.size() != 0) {
    	    			bar[list.removeLast().intValue()]++;
    	    		}
    			} // for 2
    		} // for 1
    		
    		for (int i = 0; i < numberOfDocs; i++) {
    			bar[i] = bar[i] / (double)length;
    		}
    	} else if (method == 4) {
    		// complete path (sadn), N = numberOfDocs * numWalks.
    		isDangling = true;
    		int length = 0;
    		for (int i = 0; i < numberOfDocs; i++) {
    			for (int j = 0; j < numWalks; j++) {
    				LinkedList<Integer> list = randomWalk(numberOfDocs, i, isDangling);
    				length += list.size();
    				while (list.size() != 0) {
    					bar[list.removeLast().intValue()]++;
    				}
    			} // for 2
    		} // for 1
    		
    		for (int i = 0; i < numberOfDocs; i++) {
    			bar[i] = bar[i] / (double)length;
    		}
    	} else if (method == 5) {
    		// complete path (sadn, rs), N = numWalks.
    		isDangling = true;
    		int length = 0;
    		for (int i = 0; i < numWalks; i++) {
    			int initPage = hoge.nextInt(numberOfDocs);
    			LinkedList<Integer> list = randomWalk(numberOfDocs, initPage, isDangling);
    			length += list.size();
    			while (list.size() != 0) {
    				bar[list.removeLast().intValue()]++;
    			}
    		} // for
    		
    		for (int i = 0; i < numberOfDocs; i++) {
    			bar[i] = bar[i] / (double)length;
    		}
    	}
    	
    	return bar;
    }
    
    private LinkedList<Integer> randomWalk(int numberOfDocs, int initPage, boolean isDangling) {
    	// return the end page using monte-carlo method.
    	Random hoge = new Random();
    	int page = initPage;
    	LinkedList<Integer> list = new LinkedList<Integer>();
    	list.add(page);
    	
    	while (true) {
    		// start a new walk.
    		if (hoge.nextDouble() < BORED) {
    			break;
    		} else {    			
				Hashtable<Integer, Boolean> table = link.get(page);
				if (table == null) {
					if (isDangling) {
						break;
					} // in method 4, no dangling pages are allowed.
					page = hoge.nextInt(numberOfDocs);
		 		} else {
					ArrayList<Integer> listFoo = new ArrayList<Integer>(table.keySet());
					page = listFoo.get(hoge.nextInt(table.size())).intValue();
				}
				
				list.add(page);				
    		}
    	} // while
    	
    	return list;
    }

    /* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank(int numberOfDocs, String str) {
    	int method = Integer.parseInt(str);
    	double[] foo = new double[numberOfDocs];
    	double[] bar = new double[numberOfDocs];
    	
    	long dateFoo = new Date().getTime();
    	if (method == 0 || method == 6) {
    		// power iteration method.
	    	bar[0] = 1.0;
	    	// and bar an initial state, say (1,0,...,0)
	    	int iteration = 0;
	    	while (diffOfArray(foo, bar) >= EPSILON && iteration <= MAX_NUMBER_OF_ITERATIONS) {
	    		foo = bar;
	    		//bar = computeProbabilityVector(link, foo);
	    		int length = foo.length;
	        	
	        	for (int i = 0; i < length; i++) {
	        		bar[i] = BORED / length;
	        		for (int j = 0; j < length; j++) {
	        			Hashtable<Integer, Boolean> outlinks = link.get(j);
	        			if (outlinks == null) {
	        				bar[i] += foo[j] * (1 - BORED) / length;
	        			} else {
	        				if (outlinks.get(i) != null) {
	        					bar[i] += foo[j] * (1 - BORED) / outlinks.size();
	        				} // if
	        			}
	        		} // for 2
	        	} // for 1
	    		iteration++;
	    	}
    	} else {
    		bar = monteCarlo(numberOfDocs, 50, method);
    		// 5 monte-carlo methods.
    	}
    		
    	if (method == 6) {
    		// write to file.
    		File file = new File("" + "./pagerank.score");
    		if (!file.exists()) {
    			try {
    				file.createNewFile();
    				FileWriter fw = new FileWriter(file, true);
    				for (int i = 0; i < bar.length; i++) {
    					fw.append("" + docName[i] + " " + bar[i]);
    					fw.append(System.lineSeparator());
    				}
    				fw.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	if (method != 6) {
    		// print to console.
    		sortVector(bar);
    	}
    	long dateBar = new Date().getTime();
    	System.err.println("time elapsed: " + (dateBar - dateFoo) + " ms");
    }


    /* --------------------------------------------- */


    public static void main( String[] args ) {
		if (args.length < 1) {
		    System.err.println( "Please give the name of the link file" );
		} else if (args.length < 2) {
			System.err.println("Please give the rank method");
		} else {
		    new PageRank(args[0], args[1]);
		}
    }
}
