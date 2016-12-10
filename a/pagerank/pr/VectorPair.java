package pr;

import java.util.*;
import java.io.*;

public class VectorPair implements Comparable<VectorPair> {
	public final int index;
	public final double value;
	
	public VectorPair(int index, double value) {
		super();
		this.index = index;
		this.value = value;
	}
	
	@Override
	public int compareTo(VectorPair other) {
		return -1 * Double.valueOf(this.value).compareTo(other.value);
	}
}