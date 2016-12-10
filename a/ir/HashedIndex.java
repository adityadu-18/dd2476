/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
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
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    /** store the number of documents. */
    private int numDocuments;
    
    /**
     * a hashmap storing the tf_idf scores.
     * key: the terms
     * value: an arraylist w/form (docID, score)
     **/
    private HashMap<String, HashMap<Integer, Double>> tfIdfScores = new 
    		HashMap<String, HashMap<Integer, Double>>();

    /**
     *  Inserts this token in the index.
     */
    public void insert(String token, int docID, int offset) {
    	if (!index.containsKey(token)) {
    		// when the token in the hashmap is not found.
    		index.put(token, new PostingsList());
    		// create a new hashmap.
    	}
    	index.get(token).insert(docID, offset, 1.0);
    	// insert to the hashmap.
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
    public PostingsList getPostings( String token ) {
    	if (!index.containsKey(token)) {
    		return null;
    	} else {
    		return index.get(token);
    	}
    	// above for task 1.1~5.
    	// below for task 1.6.
    	/*PostingsList list = new PostingsList();
		File file = new File("" + "./index/" + token + ".index");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			// create a buffer reader.
			String line;
		    while ((line = br.readLine()) != null) {
		    	String[] str = line.split("\\s+");
		    	// split by space.
		    	list.insert(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
		    }
		    br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;*/
    }
    // above is the method version for task 1.1~5.
    // for task 1.6, the method is implemented below.
    
    public void writeFile() {
    	Iterator<String> stringSet = index.keySet().iterator();
    	// get the token set.
    	while (stringSet.hasNext()) {
    		String token = stringSet.next();
    		File file = new File("" + "./index/" + token + ".index");
    		if (!file.exists()) {
    			try {
    				file.createNewFile();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		
    		try {
	    		FileWriter fw = new FileWriter(file, true);
	    		// allow append mode.
	    		PostingsList list = index.get(token);
	    		for (int i = 0; i < list.size(); i++) {
	    			PostingsEntry entry = list.get(i);
	    			int id = entry.docID;
	    			int offset = entry.offset;
	    			fw.append(id + " " + offset);
	    			fw.append(System.lineSeparator());
	    		}
	    		// write to the file, one token for each line.
	    		fw.close();
	    		
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
		PostingsList list = new PostingsList();
		long startTime = System.nanoTime(); // the start time
		// initialize a list.
		if (queryType == Index.INTERSECTION_QUERY) {
			for (int i = 0; i < query.terms.size(); i++) {
				String token = query.terms.get(i);
				PostingsList listFoo = this.getPostings(token);
				if (list.size() == 0) {
					list.append(listFoo);
				} else {
					list.intersect(listFoo);
				}
			}
			// retrieve all entries.
			list.removeDuplicate(rankingType);
		} else if (queryType == Index.PHRASE_QUERY) {
			int flag = 0;
			for (int i = 0; i < query.terms.size(); i++) {
				String token = query.terms.get(i);
				PostingsList listFoo = this.getPostings(token);
				if (list.size() == 0) {
					list.append(listFoo);
				} else {
					list.appendPhrase(listFoo, flag);
				}
				flag++;
			}
			list.removeDuplicate(rankingType);
		} else if (queryType == Index.RANKED_QUERY) {
			Hashtable<Integer, Double> pagerank = new Hashtable<Integer, Double>();
			double idf = 0.0;
			double weight = 0.0;
			
			for (int i = 0; i < query.terms.size(); i++) {
				String token = query.terms.get(i);
				PostingsList listFoo = this.getPostings(token);
				PostingsList listBar = new PostingsList();
				listBar.append(listFoo);
				// copy w/protecting original data.
				listBar.removeDuplicate(rankingType);
				listBar.sortChampionList(10);
				// for task 3.3.
				HashMap<Integer, Double> scores = new HashMap<Integer, Double>();
				// this arrayList stores the tf_idf scores of the specified token.
				weight = query.weights.get(i);
				
				if (rankingType != Index.PAGERANK) {
					idf = Math.log((double)numDocuments / (double)listBar.size());
					// calcualte the idf.
					for (int j = 0; j < listBar.size(); j++) {
						PostingsEntry entry = listBar.get(j);
						entry.score = weight * idf * (1 + Math.log(entry.score)) / 
								Integer.valueOf(docLengths.get(new Integer(entry.docID).toString()));
						// the tf_idf score (normalized).
						scores.put(entry.docID, entry.score);
					} // for
				} // if
				
				if (tfIdfScores.get(token) == null) {
					tfIdfScores.put(token, scores);
				}
				// save the score into the hashMap.
				
				list.expand(listBar, 0);
			} // for
							
			if (rankingType != Index.TF_IDF) {
				File file = new File("" + "./ir/pagerank.score");
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					// create a buffer reader.
					String line;
				    while ((line = br.readLine()) != null) {
				    	String[] str = line.split("\\s+");
				    	// split by space.
				    	pagerank.put(Integer.parseInt(str[0]), Double.parseDouble(str[1]));
				    }
				    br.close();
				} catch (IOException e) {
					e.printStackTrace();
				} // try & catch
				// read the pagerank from file.
			}
			
			if (rankingType == Index.PAGERANK) {
				for (int i = 0; i < list.size(); i++) {
					PostingsEntry entry = list.get(i);
					entry.score = pagerank.get(entry.docID);
				} // for
			} else if (rankingType == Index.COMBINATION) {
				for (int i = 0; i < list.size(); i++) {
					PostingsEntry entry = list.get(i);
					entry.score *= pagerank.get(entry.docID);
				} // for
			}
			
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
    
    public double getTfIdfScore(String token, int docID) {
    	HashMap<Integer, Double> scores = tfIdfScores.get(token);
    	
    	if (!scores.containsKey(docID)) {
    		return 0;
    	} else {
    		return scores.get(docID);
    	}
    }
    
}
