/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();
    
    /**
     * the parameters used for rocchio's algorithm.
     */
    private static final double ALPHA = 0.5;
    private static final double BETA = 0.5;

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
		StringTokenizer tok = new StringTokenizer( queryString );
		while ( tok.hasMoreTokens() ) {
		    terms.add( tok.nextToken() );
		    weights.add( new Double(1) );
		}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
    	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
		Query queryCopy = new Query();
		queryCopy.terms = (LinkedList<String>) terms.clone();
		queryCopy.weights = (LinkedList<Double>) weights.clone();
		return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	// results contain the ranked list from the current search
	// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
    	
    	if (!(indexer.index instanceof HashedIndex)) {
    		return;
    	}
    	
    	int numRelevantDocs = 0;
    	for (boolean b : docIsRelevant) {
    		if (b) {
    			numRelevantDocs++;
    		} // if
    	}
    	// calculate the number of relevant documents.
    	
    	HashMap<String, Double> scores = new HashMap<String, Double>();
    	for (int i = 0; i < this.terms.size(); i++) {
    		scores.put(this.terms.get(i), ALPHA * this.weights.get(i));
    	}
    	// ALPHA * q_0 (initially 1).
    	
    	for (int i = 0; i < docIsRelevant.length; i++) {
    		if (!docIsRelevant[i]) {
    			continue;
    		}
    		PostingsEntry entry = results.get(i);
    		
    		for (String token : this.terms) {
    			double foo = (BETA * ((HashedIndex)indexer.index).getTfIdfScore(token, entry.docID)) / numRelevantDocs;
    			// BETA * <weight of docs> (first normalization).
    			scores.put(token, scores.get(token) + foo);
    		} // for 2
    	} // for 1
    	
    	this.terms = new LinkedList(scores.keySet());
    	this.weights = new LinkedList(scores.values());
    	this.normalize();
    	// calculate the weights and then normalize them (second normalization).
    }
    
    public void normalize() {
    	double squareSum = 0.0;
    	for (double weight : this.weights) {
    		squareSum += weight * weight;
    	}
    	squareSum = Math.sqrt(squareSum);
    	
    	for (double weight : this.weights) {
    		weight /= squareSum;
    	}
    }
}

    
