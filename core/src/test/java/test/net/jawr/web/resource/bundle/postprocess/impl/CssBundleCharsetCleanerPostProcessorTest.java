/**
 * 
 */
package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.util.List;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CssCharsetFilterPostProcessor;
import test.net.jawr.log.AppenderForTesting;
import test.net.jawr.web.FileUtils;

/**
 * @author Ibrahim Chaehoi
 *
 */
public class CssBundleCharsetCleanerPostProcessorTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp(){
		PropertyConfigurator.configure(CssBundleCharsetCleanerPostProcessorTest.class.getResource("/postprocessor/cssbundlecharset/log4j-test.properties"));
		AppenderForTesting.clear();
	}
	
	public void testPostProcessBundle() throws Exception {
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/standard-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(getJoinableResourceBundle("/bundle1.css"),null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/standard-bundle-result.css"), ret.toString());
	}
	
	public void testPostProcessBundleWithDifferentCharsetWarning() throws Exception {
		
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/different-charset-decl-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(getJoinableResourceBundle("/bundle1.css"),null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/different-charset-decl-bundle-result.css"), ret.toString());
		
		String[] messages = AppenderForTesting.getMessages();
		assertEquals(2, messages.length);
		assertEquals("The bundle '/bundle1.css' contains CSS with different charset declaration.", messages[1]);
	}
	
	public void testPostProcessBundleWithCharsetNotAtTheBegining() throws Exception {
		
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/charset-decl-not-at-the-top-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(getJoinableResourceBundle("/bundle1.css"),null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/charset-decl-not-at-the-top-bundle-result.css"), ret.toString());
	}

	private JoinableResourceBundle getJoinableResourceBundle(final String id){
		
		JoinableResourceBundle bundle = new JoinableResourceBundle() {
			
			public void setMappings(List mappings) {
				
			}
			
			public void setBundleDataHashCode(String variantKey,
					String bundleDataHashCode) {
				
			}
			
			public void setBundleDataHashCode(String variantKey, int bundleDataHashCode) {
				
			}
			
			public boolean isComposite() {
				return false;
			}
			
			public ResourceBundlePostProcessor getUnitaryPostProcessor() {
				return null;
			}
			
			public String getURLPrefix(String variantKey) {
				return null;
			}
			
			public String getName() {
				return null;
			}
			
			public List getLocaleVariantKeys() {
				return null;
			}
			
			public Set getLicensesPathList() {
				return null;
			}
			
			public List getItemPathList(String variantKey) {
				return null;
			}
			
			public List getItemPathList() {
				return null;
			}
			
			public InclusionPattern getInclusionPattern() {
				return null;
			}
			
			public String getId() {
				return id;
			}
			
			public String getExplorerConditionalExpression() {
				return null;
			}
			
			public ResourceBundlePostProcessor getBundlePostProcessor() {
				return null;
			}
			
			public String getBundleDataHashCode(String variantKey) {
				return null;
			}
			
			public String getAlternateProductionURL() {
				return null;
			}
			
			public boolean belongsToBundle(String itemPath) {
				return false;
			}

			public List getDependencies() {
				return null;
			}

			public void setDependencies(List bundleDependencies) {
				
			}
		};
		return bundle;
		
	}
}
