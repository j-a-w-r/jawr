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

import java.util.Iterator;
import java.util.List;

import net.jawr.web.resource.bundle.JoinableResourceBundle;

/**
 * Debug mode implementation of ResourceBundlePathsIterator. Uses a ConditionalCommentCallbackHandler
 * to signal the use of conditional comments. The paths returned are those of the individual 
 * members of the bundle. 
 * 
 * @author Jordi Hernández Sellés
 */
public class DebugModePathsIteratorImpl extends AbstractPathsIterator implements ResourceBundlePathsIterator {

	private Iterator bundlesIterator;
	private Iterator pathsIterator;
	private JoinableResourceBundle currentBundle;
	
	public DebugModePathsIteratorImpl(List bundles,ConditionalCommentCallbackHandler callbackHandler,String variantKey) {
		super(callbackHandler,variantKey);
		this.bundlesIterator = bundles.iterator();
		
	}
	

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator#nextPath()
	 */
	public String nextPath() {
		if(null == pathsIterator || !pathsIterator.hasNext()) {
			currentBundle = (JoinableResourceBundle) bundlesIterator.next();
			
			if(null != currentBundle.getExplorerConditionalExpression())
				commentCallbackHandler.openConditionalComment(currentBundle.getExplorerConditionalExpression());

			pathsIterator = currentBundle.getItemPathList(variantKey).iterator();
		}
		return pathsIterator.next().toString();
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(null != pathsIterator && !pathsIterator.hasNext()) {
			if(null != currentBundle && null != currentBundle.getExplorerConditionalExpression())
				commentCallbackHandler.closeConditionalComment();
		}
		boolean rets = false;
		if(null != pathsIterator) {
			rets = pathsIterator.hasNext() || bundlesIterator.hasNext();
		}
		else rets = bundlesIterator.hasNext();
			
		return rets;
	}


}
