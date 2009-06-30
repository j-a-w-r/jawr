/**
 * Copyright 2009 Ibrahim Chaehoi
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
package net.jawr.web.config.jmx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This interface defines the MBean which manage the Jawr configuration for a we application, so it will affect all JawrConfigManagerMBean associated
 * to Jawr Servlets.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrApplicationConfigManager implements JawrApplicationConfigManagerMBean {

	/** The message of the property, when the values are not equals for the different configuration manager */
	private static String NOT_IDENTICAL_VALUES = "Value for this property are not identical";

	/** The configuration manager for the Javascript handler */
	private JawrConfigManagerMBean jsMBean;

	/** The configuration manager for the CSS handler */
	private JawrConfigManagerMBean cssMBean;

	/** The configuration manager for the image handler */
	private JawrConfigManagerMBean imgMBean;

	/** The set of session ID for which all requests will be executed in debug mode */
	private Set debugSessionIdSet = new HashSet();

	/**
	 * Constructor
	 */
	public JawrApplicationConfigManager() {

	}

	/**
	 * Sets the configuration manager for the Javascript handler
	 * 
	 * @param jsMBean the configuration manager to set
	 */
	public void setJsMBean(JawrConfigManagerMBean jsMBean) {
		this.jsMBean = jsMBean;
	}

	/**
	 * Sets the configuration manager for the CSS handler
	 * 
	 * @param cssMBean the configuration manager to set
	 */
	public void setCssMBean(JawrConfigManagerMBean cssMBean) {
		this.cssMBean = cssMBean;
	}

	/**
	 * Sets the configuration manager for the image handler
	 * 
	 * @param imgMBean the configuration manager to set
	 */
	public void setImgMBean(JawrConfigManagerMBean imgMBean) {
		this.imgMBean = imgMBean;
	}

	/**
	 * Returns the list of initialized configuration managers.
	 * 
	 * @return the list of initialized configuration managers.
	 */
	private List getInitializedConfigurationManagers() {

		List mBeans = new ArrayList();
		if (jsMBean != null) {
			mBeans.add(jsMBean);
		}

		if (cssMBean != null) {
			mBeans.add(cssMBean);
		}
		if (imgMBean != null) {
			mBeans.add(imgMBean);
		}

		return mBeans;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getCharsetName()
	 */
	public String getCharsetName() {

		List mBeans = getInitializedConfigurationManagers();
		if (mBeans.size() == 3) {

			if (areEquals(jsMBean.getCharsetName(), cssMBean.getCharsetName(), imgMBean.getCharsetName())) {

				return jsMBean.getCharsetName();
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		if (mBeans.size() == 2) {
			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
			JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans.get(1);

			if (areEquals(mBean1.getCharsetName(), mBean2.getCharsetName())) {
				return mBean1.getCharsetName();
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
		return mBean1.getCharsetName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getDebugOverrideKey()
	 */
	public String getDebugOverrideKey() {

		List mBeans = getInitializedConfigurationManagers();
		if (mBeans.size() == 3) {

			if (areEquals(jsMBean.getDebugOverrideKey(), cssMBean.getDebugOverrideKey(), imgMBean.getDebugOverrideKey())) {

				return jsMBean.getDebugOverrideKey();
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		if (mBeans.size() == 2) {
			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
			JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans.get(1);

			if (areEquals(mBean1.getDebugOverrideKey(),mBean2.getDebugOverrideKey())) {
				return mBean1.getDebugOverrideKey();
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
		return mBean1.getDebugOverrideKey();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isDebugModeOn()
	 */
	public String getDebugModeOn() {

		List mBeans = getInitializedConfigurationManagers();
		if (mBeans.size() == 3) {

			if (jsMBean.isDebugModeOn() == cssMBean.isDebugModeOn() && cssMBean.isDebugModeOn() == imgMBean.isDebugModeOn()) {

				return Boolean.toString(jsMBean.isDebugModeOn());
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		if (mBeans.size() == 2) {
			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
			JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans.get(1);

			if (mBean1.isDebugModeOn() == mBean2.isDebugModeOn()) {
				return Boolean.toString(mBean1.isDebugModeOn());
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
		return Boolean.toString(mBean1.isDebugModeOn());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isGzipResourcesForIESixOn()
	 */
	public String getGzipResourcesForIESixOn() {

		List mBeans = getInitializedConfigurationManagers();
		if (mBeans.size() == 3) {

			if (jsMBean.isGzipResourcesForIESixOn() == cssMBean.isGzipResourcesForIESixOn()
					&& cssMBean.isGzipResourcesForIESixOn() == imgMBean.isGzipResourcesForIESixOn()) {

				return Boolean.toString(jsMBean.isGzipResourcesForIESixOn());
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		if (mBeans.size() == 2) {
			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
			JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans.get(1);

			if (mBean1.isGzipResourcesForIESixOn() == mBean2.isGzipResourcesForIESixOn()) {
				return Boolean.toString(mBean1.isGzipResourcesForIESixOn());
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
		return Boolean.toString(mBean1.isGzipResourcesForIESixOn());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isGzipResourcesModeOn()
	 */
	public String getGzipResourcesModeOn() {

		List mBeans = getInitializedConfigurationManagers();
		if (mBeans.size() == 3) {

			if (jsMBean.isGzipResourcesModeOn() == cssMBean.isGzipResourcesModeOn()
					&& cssMBean.isGzipResourcesModeOn() == imgMBean.isGzipResourcesModeOn()) {

				return Boolean.toString(jsMBean.isGzipResourcesModeOn());
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		if (mBeans.size() == 2) {
			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
			JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans.get(1);

			if (mBean1.isGzipResourcesForIESixOn() == mBean2.isGzipResourcesModeOn()) {
				return Boolean.toString(mBean1.isGzipResourcesModeOn());
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
		return Boolean.toString(mBean1.isGzipResourcesModeOn());
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getContextPathOverride()
	 */
	public String getContextPathOverride() {
		List mBeans = getInitializedConfigurationManagers();
		if (mBeans.size() == 3) {

			if (areEquals(jsMBean.getContextPathOverride(), cssMBean.getContextPathOverride(), imgMBean.getContextPathOverride())) {

				return jsMBean.getContextPathOverride();
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		if (mBeans.size() == 2) {
			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
			JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans.get(1);

			if (areEquals(mBean1.getContextPathOverride(),mBean2.getContextPathOverride())) {
				return mBean1.getContextPathOverride();
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
		return mBean1.getContextPathOverride();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getContextPathSslOverride()
	 */
	public String getContextPathSslOverride() {
		
		List mBeans = getInitializedConfigurationManagers();
		if (mBeans.size() == 3) {

			if (areEquals(jsMBean.getContextPathSslOverride(), cssMBean.getContextPathSslOverride(), imgMBean.getContextPathSslOverride())) {

				return jsMBean.getContextPathSslOverride();
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		if (mBeans.size() == 2) {
			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
			JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans.get(1);

			if (areEquals(mBean1.getContextPathSslOverride(),mBean2.getContextPathSslOverride())) {
				return mBean1.getContextPathSslOverride();
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
		return mBean1.getContextPathSslOverride();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#getUseContextPathOverrideInDebugMode()
	 */
	public String getUseContextPathOverrideInDebugMode() {
		List mBeans = getInitializedConfigurationManagers();
		if (mBeans.size() == 3) {

			if (jsMBean.getUseContextPathOverrideInDebugMode() == cssMBean.getUseContextPathOverrideInDebugMode()
					&& cssMBean.getUseContextPathOverrideInDebugMode() == imgMBean.getUseContextPathOverrideInDebugMode()) {

				return Boolean.toString(jsMBean.getUseContextPathOverrideInDebugMode());
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		if (mBeans.size() == 2) {
			JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
			JawrConfigManagerMBean mBean2 = (JawrConfigManagerMBean) mBeans.get(1);

			if (mBean1.getUseContextPathOverrideInDebugMode() == mBean2.getUseContextPathOverrideInDebugMode()) {
				return Boolean.toString(mBean1.getUseContextPathOverrideInDebugMode());
			} else {
				return NOT_IDENTICAL_VALUES;
			}
		}

		JawrConfigManagerMBean mBean1 = (JawrConfigManagerMBean) mBeans.get(0);
		return Boolean.toString(mBean1.getUseContextPathOverrideInDebugMode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setCharsetName(java.lang.String)
	 */
	public void setCharsetName(String charsetName) {
		if (jsMBean != null) {
			jsMBean.setCharsetName(charsetName);
		}
		if (cssMBean != null) {
			cssMBean.setCharsetName(charsetName);
		}
		if (imgMBean != null) {
			imgMBean.setCharsetName(charsetName);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setDebugModeOn(boolean)
	 */
	public void setDebugModeOn(String debugMode) {
		if (jsMBean != null) {
			jsMBean.setDebugModeOn(Boolean.valueOf(debugMode).booleanValue());
		}
		if (cssMBean != null) {
			cssMBean.setDebugModeOn(Boolean.valueOf(debugMode).booleanValue());
		}
		if (imgMBean != null) {
			imgMBean.setDebugModeOn(Boolean.valueOf(debugMode).booleanValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setDebugOverrideKey(java.lang.String)
	 */
	public void setDebugOverrideKey(String debugOverrideKey) {
		if (jsMBean != null) {
			jsMBean.setDebugOverrideKey(debugOverrideKey);
		}
		if (cssMBean != null) {
			cssMBean.setDebugOverrideKey(debugOverrideKey);
		}
		if (imgMBean != null) {
			imgMBean.setDebugOverrideKey(debugOverrideKey);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setGzipResourcesForIESixOn(boolean)
	 */
	public void setGzipResourcesForIESixOn(String gzipResourcesForIESixOn) {
		if (jsMBean != null) {
			jsMBean.setGzipResourcesForIESixOn(Boolean.valueOf(gzipResourcesForIESixOn).booleanValue());
		}
		if (cssMBean != null) {
			cssMBean.setGzipResourcesForIESixOn(Boolean.valueOf(gzipResourcesForIESixOn).booleanValue());
		}
		if (imgMBean != null) {
			imgMBean.setGzipResourcesForIESixOn(Boolean.valueOf(gzipResourcesForIESixOn).booleanValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setGzipResourcesModeOn(boolean)
	 */
	public void setGzipResourcesModeOn(String gzipResourcesModeOn) {
		if (jsMBean != null) {
			jsMBean.setGzipResourcesModeOn(Boolean.valueOf(gzipResourcesModeOn).booleanValue());
		}
		if (cssMBean != null) {
			cssMBean.setGzipResourcesModeOn(Boolean.valueOf(gzipResourcesModeOn).booleanValue());
		}
		if (imgMBean != null) {
			imgMBean.setGzipResourcesModeOn(Boolean.valueOf(gzipResourcesModeOn).booleanValue());
		}
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setContextPathOverride(java.lang.String)
	 */
	public void setContextPathOverride(String contextPathOverride) {
		
		if (jsMBean != null) {
			jsMBean.setDebugOverrideKey(contextPathOverride);
		}
		if (cssMBean != null) {
			cssMBean.setDebugOverrideKey(contextPathOverride);
		}
		if (imgMBean != null) {
			imgMBean.setDebugOverrideKey(contextPathOverride);
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setContextPathSslOverride(java.lang.String)
	 */
	public void setContextPathSslOverride(String contextPathOverride) {
		
		if (jsMBean != null) {
			jsMBean.setDebugOverrideKey(contextPathOverride);
		}
		if (cssMBean != null) {
			cssMBean.setDebugOverrideKey(contextPathOverride);
		}
		if (imgMBean != null) {
			imgMBean.setDebugOverrideKey(contextPathOverride);
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#setUseContextPathOverrideInDebugMode(java.lang.String)
	 */
	public void setUseContextPathOverrideInDebugMode(String useContextPathOverrideInDebugMode) {
		if (jsMBean != null) {
			jsMBean.setGzipResourcesModeOn(Boolean.valueOf(useContextPathOverrideInDebugMode).booleanValue());
		}
		if (cssMBean != null) {
			cssMBean.setGzipResourcesModeOn(Boolean.valueOf(useContextPathOverrideInDebugMode).booleanValue());
		}
		if (imgMBean != null) {
			imgMBean.setGzipResourcesModeOn(Boolean.valueOf(useContextPathOverrideInDebugMode).booleanValue());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#refreshConfig()
	 */
	public void refreshConfig() {

		if (jsMBean != null) {
			jsMBean.refreshConfig();
		}
		if (cssMBean != null) {
			cssMBean.refreshConfig();
		}
		if (imgMBean != null) {
			imgMBean.refreshConfig();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#addDebugSessionId(java.lang.String)
	 */
	public void addDebugSessionId(String sessionId) {
		debugSessionIdSet.add(sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#removeDebugSessionId(java.lang.String)
	 */
	public void removeDebugSessionId(String sessionId) {
		debugSessionIdSet.remove(sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#removeAllDebugSessionId()
	 */
	public void removeAllDebugSessionId() {
		debugSessionIdSet.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean#isDebugSessionId(java.lang.String)
	 */
	public boolean isDebugSessionId(String sessionId) {
		return debugSessionIdSet.contains(sessionId);
	}

	/**
	 * Returns true if the 2 string are equals.
	 * 
	 * @param str1 the first string
	 * @param str2 the 2nd string
	 * @return true if the 2 string are equals.
	 */
	public boolean areEquals(String str1, String str2) {

		return (str1 == null && str2 == null || str1 != null && str2 != null && str1.equals(str2));
	}

	/**
	 * Returns true if the 3 string are equals.
	 * 
	 * @param str1 the first string
	 * @param str2 the 2nd string
	 * @param str3 the 3rd string
	 * @return true if the 3 string are equals.
	 */
	public boolean areEquals(String str1, String str2, String str3) {

		return (str1 == null && str2 == null && str3 == null || str1 != null && str2 != null && str3 != null && str1.equals(str2)
				&& str2.equals(str3));
	}
}
