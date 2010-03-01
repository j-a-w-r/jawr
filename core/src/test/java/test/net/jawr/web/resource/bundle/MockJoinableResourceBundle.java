/**
 * Copyright 2010 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.bundle;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * @author Ibrahim Chaehoi
 *
 */
public class MockJoinableResourceBundle implements JoinableResourceBundle {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#belongsToBundle(java.lang.String)
	 */
	public boolean belongsToBundle(String itemPath) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getAlternateProductionURL()
	 */
	public String getAlternateProductionURL() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundleDataHashCode(java.lang.String)
	 */
	public String getBundleDataHashCode(String variantKey) {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundlePostProcessor()
	 */
	public ResourceBundlePostProcessor getBundlePostProcessor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getDependencies()
	 */
	public List getDependencies() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getExplorerConditionalExpression()
	 */
	public String getExplorerConditionalExpression() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getId()
	 */
	public String getId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getInclusionPattern()
	 */
	public InclusionPattern getInclusionPattern() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList()
	 */
	public List getItemPathList() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList(java.util.Map)
	 */
	public List getItemPathList(Map variants) {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLicensesPathList()
	 */
	public Set getLicensesPathList() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getName()
	 */
	public String getName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getURLPrefix(java.util.Map)
	 */
	public String getURLPrefix(Map variants) {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getUnitaryPostProcessor()
	 */
	public ResourceBundlePostProcessor getUnitaryPostProcessor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getVariantKeys()
	 */
	public List getVariantKeys() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getVariants()
	 */
	public Map getVariants() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#isComposite()
	 */
	public boolean isComposite() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setBundleDataHashCode(java.lang.String, int)
	 */
	public void setBundleDataHashCode(String variantKey, int bundleDataHashCode) {
	
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setBundleDataHashCode(java.lang.String, java.lang.String)
	 */
	public void setBundleDataHashCode(String variantKey,
			String bundleDataHashCode) {
	
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setDependencies(java.util.List)
	 */
	public void setDependencies(List bundleDependencies) {
	
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setMappings(java.util.List)
	 */
	public void setMappings(List mappings) {
	
	}

}
