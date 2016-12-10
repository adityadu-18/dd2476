/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.Hashtable;
import java.util.LinkedList;
import java.io.Serializable;
import java.util.Collections;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();
    
    /**  Number of postings in this list  */
    public int size() {
        return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
    	return list.get(i);
    }

    public void insert (int docID, int offset, double score) {
    	PostingsEntry entry = new PostingsEntry();
    	entry.docID = docID;
    	entry.score = score;
    	entry.offset = offset;
    	// initialize a new PostingsEntry.
    	
    	list.add(entry);
    	// add to the PostingsList.
    }
    
    public void intersect (PostingsList listFoo) {
    	Hashtable table = new Hashtable();
    	for (int i = 0; i < listFoo.size(); i++) {
    		table.put(listFoo.get(i).docID, true);
    	}
    	// create a hashtable for storing the docIDs.
    		
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i) == null) {
    			break;
    		}
    		int foo = list.get(i).docID;
    		if (!table.containsKey(foo)) {
    			list.remove(i);
    			i--;
    		}
		}
    }
    
    public void append(PostingsList listFoo) {
    	if (listFoo == null) {
    		return;
    	}
	    for (int i = 0; i < listFoo.size(); i++) {
			PostingsEntry entry = listFoo.get(i);
			// fetch the entry.
			this.insert(entry.docID, entry.offset, entry.score);
			// insert to the list.
		}
    }
    
    public void expand(PostingsList listFoo, int structureType) {
    	if (listFoo == null) {
    		return;
    	}
    	if (this.list == null) {
    		this.append(listFoo);
    		return;
    	}
    	
    	Hashtable<Integer, Integer> table = new Hashtable<Integer, Integer>();
    	for (int i = 0; i < list.size(); i++) {
    		table.put(list.get(i).docID, i);
    	}
    	
    	for (int i = 0; i < listFoo.size(); i++) {
    		PostingsEntry entry = listFoo.get(i);
    		if (!table.containsKey(entry.docID)) {
    			this.insert(entry.docID, entry.offset, entry.score);
    		} else {
    			if (structureType == 2) { // Index.SUBPHRASE
    				list.get(table.get(entry.docID)).score += entry.score * 0.25;
    			} else { // common situations.
    				list.get(table.get(entry.docID)).score += entry.score;
    			}
    		}
    	}
    	//System.err.println("expanded size: " + list.size());
    }
    
    public void removeDuplicate(int rankingType) {
    	Hashtable<Integer, Integer> table = new Hashtable<Integer, Integer>();
    	// create a hash table for storing data.
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i) == null) {
    			break;
    		}
    		// when the list reaches its end.
    		int foo = list.get(i).docID;
    		if (table.containsKey(foo)) {
    			if (rankingType != 1) { // not Index.PAGERANK
    				list.get(table.get(foo).intValue()).score++;
    			} else {
    				list.get(table.get(foo).intValue()).score = 0;
    				// clear for pagerank
    			}
    			list.remove(i);
    			i--;
    			// return to the position.
    		} else {
    			table.put(foo, i);
    		}
    	}
    	
    	//System.err.println("reduced size: " + list.size());
    }
    
    public void appendPhrase (PostingsList listFoo, int flag) {
    	Hashtable<Integer, LinkedList<Integer>> table = new Hashtable<Integer, LinkedList<Integer>>();
    	for (int i = 0; i < listFoo.size(); i++) {
    		PostingsEntry foo = listFoo.get(i);
    		if (!table.containsKey(foo.docID)) {
    			table.put(foo.docID, new LinkedList<Integer>());
    		}
    		table.get(foo.docID).add(foo.offset);
    	}
    	// create a table for storing the document IDs.
    	
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i) == null) {
    			break;
    		}
    		// when the list reaches its end.
    		
    		PostingsEntry foo = list.get(i);
    		if (!table.containsKey(foo.docID)) {
    			list.remove(i);
    			i--;
    			// when no token in the same docID, delete directly.
    		} else {
    			// when docID is contained in listFoo.
    			boolean mark = false;
    			LinkedList<Integer> listOffset = table.get(foo.docID);
    			for (int bar : listOffset) {
    				if (bar == foo.offset + flag) {
    					mark = true;
    					break;
    				}
    				// when the tokens are adjacent.
    			}
    			if (!mark) {
    				list.remove(i);
    				i--;
    			}
    		}
    	}
    }
    
    public void generateByScore(Hashtable<Integer, Double> score) {
    	// for task 2.2.
    	// generate a new postingslist by the calculated scores.
    	for (Integer docID : score.keySet()) {
    		this.insert(docID, 0, score.get(docID));
    	}
    }
    
    public void sort() {
    	Collections.sort(list);
    }
    
    /**
     * for task 3.3.
     * sort out the most ranked numChampions documents.
     * for time saving purpose.
     */
    public void sortChampionList(int numChampions) {
    	this.sort();
    	while (this.list.size() > numChampions) {
    		this.list.remove(numChampions);
    	}
    }
    
}
	

			   
