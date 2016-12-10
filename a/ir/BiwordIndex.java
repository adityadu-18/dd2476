/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  

/*
 * the biword index.
 * partly inherited from the original hashedIndex.java file.
 * by yimingf, 21/03/2016.
 */

package ir;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class BiwordIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String, HashMap<String, PostingsList>> index = new 
    		HashMap<String, HashMap<String, PostingsList>> ();
    private String previousTokenName = null;
    private int previousDocID = -1;
    
    /** store the number of documents. */
    private int numDocuments;
    /** store the number of bigrams. */
    private int numBigrams;

    /**
     *  Inserts this token in the index.
     */
    public void insert(String token, int docID, int offset) {
    	if (previousDocID != docID) {
    		previousTokenName = null;
    		previousDocID = docID;
    	} // for first word in the document.
    	
    	if (previousTokenName == null) {
    		previousTokenName = token;
    		return;
    	} // first word does not participate in the game.
    	
    	if (!index.containsKey(previousTokenName)) {
    		HashMap<String, PostingsList> map = new HashMap<String, PostingsList>();
    		index.put(previousTokenName, map);
    	} // create a hashmap to store the previous token.
    	
    	HashMap<String, PostingsList> map = index.get(previousTokenName);
    	if (!map.containsKey(token)) {
    		map.put(token, new PostingsList());
    		numBigrams++;
    		// create a new hashmap.
    	}
    	map.get(token).insert(docID, offset, 1.0);
    	
    	previousTokenName = token;
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
		return index.keySet().iterator();
    }

    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings (String foo, String bar) {
    	if (!index.containsKey(foo)) {
    		return null;
    	} else {
    		return index.get(foo).get(bar);
    	}
    }
    
    public PostingsList getPostings (String token) {
    	return null;
    } // a deprecated method.

    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
		PostingsList list = new PostingsList();
		long startTime = System.nanoTime(); // the start time
		// initialize a list.
		if (queryType == Index.RANKED_QUERY) {
			double idf = 0.0;
			String foo = null; // foo -> bar -> list, the naming rule.
			String bar = null;
			
			for (int i = 0; i < query.terms.size(); i++) {
				bar = query.terms.get(i);
				if (foo == null) {
					foo = bar;
					continue;
				} // the first token does not participate in the game.
				
				PostingsList listFoo = this.getPostings(foo, bar);
				PostingsList listBar = new PostingsList();
				listBar.append(listFoo);
				// copy w/protecting original data.
				listBar.removeDuplicate(rankingType);
				HashMap<Integer, Double> scores = new HashMap<Integer, Double>();
				// this arrayList stores the tf_idf scores of the specified token.
				
				if (rankingType != Index.PAGERANK) {
					idf = Math.log((double)numBigrams / (double)listBar.size());
					// calcualte the idf.
					for (int j = 0; j < listBar.size(); j++) {
						PostingsEntry entry = listBar.get(j);
						entry.score = idf * (1 + Math.log(entry.score)) / 
								Integer.valueOf(docLengths.get(new Integer(entry.docID).toString()));
						// the tf_idf score (normalized).
						scores.put(entry.docID, entry.score);
					} // for
				} // if
				
				list.expand(listBar, 0);
			} // for
			
			list.sort();
		}
		System.out.println("Elapsed time: " + (System.nanoTime() - startTime) + " nanosecond(s)");
		return list;
    }
    
    public void setNumDocuments() {
    	// increase the number by 1.
    	this.numDocuments++;
    }
    
    public int getNumDocuments() {
    	return numDocuments;
    }

    /**
     *  No need for cleanup in a HashedIndex.
     *  *** actually this method is needed in task 1.6 ***
     */
    public void cleanup() {
    	index.clear();
    }
    
    public double getTfIdfScore(String foo, int bar) { return 0.0; }
    public void writeFile() { }
    
}
