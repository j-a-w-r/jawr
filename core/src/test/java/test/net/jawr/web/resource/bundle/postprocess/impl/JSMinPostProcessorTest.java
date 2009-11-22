package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.JSMinPostProcessor;

public class JSMinPostProcessorTest extends TestCase {

    /**
     * Test the ability to compress javascript using JSMin. 
     */
    public void testPostProcessBundle() {
		String script = "//comment\n        \talert('αιρν')";
		Charset charset = Charset.forName("UTF-8");
		JawrConfig config = new JawrConfig(new Properties());
		config.setCharsetName("UTF-8");
		JSMinPostProcessor processor = new JSMinPostProcessor();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(script.getBytes(charset.name()));
		} catch (UnsupportedEncodingException ignore) {
			fail("UnsupportedEncodingException that will never be thrown");
		}
	    BundleProcessingStatus status = new BundleProcessingStatus(getBundle("/myBundle.js"),null,config);
		StringBuffer ret = processor.postProcessBundle(status, new StringBuffer(script));
		
		// Not really testing JSMin, that is supposed to work. 
		assertEquals("\nalert('αιρν')", ret.toString());
	}
    
    public JoinableResourceBundle getBundle(final String id){
    	return new JoinableResourceBundle() {
			
			public void setMappings(List mappings) {
				
			}
			
			public void setDependencies(List bundleDependencies) {
				
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
			
			public List getDependencies() {
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
		};
    }

}
