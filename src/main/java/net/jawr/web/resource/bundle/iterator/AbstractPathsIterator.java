/**
 * Copyright 2008 Jordi Hernández Sellés
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.resource.bundle.iterator;

/**
 * Abstract implementation of ResourceBundlePathsIterator that holds a 
 * reference to a ConditionalCommentCallbackHandler to signal the need 
 * to start or end wrapping the paths with a conditional comment for internet
 * explorer.  
 * 
 * @author Jordi Hernández Sellés
 */
public abstract class AbstractPathsIterator implements
		ResourceBundlePathsIterator {

	protected ConditionalCommentCallbackHandler commentCallbackHandler;
	protected String variantKey;


	/**
	 * Creates the iterator passing the reference to the ConditionalCommentCallbackHandler. 
	 * @param handler
	 */
	public AbstractPathsIterator(ConditionalCommentCallbackHandler handler,String variantKey) {
		super();
		commentCallbackHandler = handler;
		this.variantKey = variantKey;
	}

	/**
	 * Unsupported method from the Iterator interface, will throw UnsupportedOperationException
	 * if called. 
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		return nextPath();
	}
	

}
