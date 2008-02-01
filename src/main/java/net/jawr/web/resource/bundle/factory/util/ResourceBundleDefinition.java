/**
 * Copyright 2007 Jordi Hernández Sellés
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
package net.java.jawr.web.resource.bundle.factory.util;

import java.util.List;

/**
 * Transfer object meant for a factory to use to create a JoinableResourceBundle. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class ResourceBundleDefinition {
	
	private String bundleId;
	private List mappings;
	private String prefix;
	private boolean isGlobal;
	private boolean isComposite;
	private int inclusionOrder;
	private boolean debugOnly = false;
	private boolean debugNever = false;
	private String unitaryPostProcessorKeys;
	private String bundlePostProcessorKeys;
	private List children;
	
	public List getChildren() {
		return children;
	}
	public void setChildren(List children) {
		this.children = children;
	}
	public String getBundleId() {
		return bundleId;
	}
	public void setBundleId(String bundleId) {
		this.bundleId = bundleId;
	}
	public List getMappings() {
		return mappings;
	}
	public void setMappings(List mappings) {
		this.mappings = mappings;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public boolean isGlobal() {
		return isGlobal;
	}
	public void setGlobal(boolean isGlobal) {
		this.isGlobal = isGlobal;
	}
	public int getInclusionOrder() {
		return inclusionOrder;
	}
	public void setInclusionOrder(int inclusionOrder) {
		this.inclusionOrder = inclusionOrder;
	}
	public boolean isDebugOnly() {
		return debugOnly;
	}
	public void setDebugOnly(boolean debugOnly) {
		this.debugOnly = debugOnly;
	}
	public boolean isDebugNever() {
		return debugNever;
	}
	public void setDebugNever(boolean debugNever) {
		this.debugNever = debugNever;
	}
	public String getUnitaryPostProcessorKeys() {
		return unitaryPostProcessorKeys;
	}
	public void setUnitaryPostProcessorKeys(String unitaryPostProcessorKeys) {
		this.unitaryPostProcessorKeys = unitaryPostProcessorKeys;
	}
	public String getBundlePostProcessorKeys() {
		return bundlePostProcessorKeys;
	}
	public void setBundlePostProcessorKeys(String bundlePostProcessorKeys) {
		this.bundlePostProcessorKeys = bundlePostProcessorKeys;
	}
	public boolean isComposite() {
		return isComposite;
	}
	public void setComposite(boolean isComposite) {
		this.isComposite = isComposite;
	}
	
	

}
