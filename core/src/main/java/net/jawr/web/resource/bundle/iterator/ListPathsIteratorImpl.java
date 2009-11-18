package net.jawr.web.resource.bundle.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ListPathsIteratorImpl implements ResourceBundlePathsIterator {

	/**
	 * The path iterator 
	 */
	private Iterator pathIterator;
	
	/**
	 * Constructor
	 * @param paths the list of path
	 */
	public ListPathsIteratorImpl(List paths) {
		pathIterator = paths.iterator();
	}
	
	/**
	 * Constructor
	 * @param paths the array of path
	 */
	public ListPathsIteratorImpl(String[] paths) {
		pathIterator = Arrays.asList(paths).iterator();
	}
	
	/**
	 * Constructor
	 * @param path the path
	 */
	public ListPathsIteratorImpl(String path) {
		this(new String[]{path});
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator#nextPath()
	 */
	public String nextPath() {
		return (String) pathIterator.next();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return pathIterator.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		return pathIterator.next();
	}

	/**
	 * Unsupported method from the Iterator interface, will throw UnsupportedOperationException
	 * if called. 
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
