/**
 * 
 */
package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import junit.framework.TestCase;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CSSImportPostProcessor;
import net.jawr.web.servlet.mock.MockServletContext;

/**
 * @author Ibrahim Chaehoi
 * 
 */
public class CSSImportPostProcessorTest extends TestCase {

	JoinableResourceBundle bundle;
	JawrConfig config;
	BundleProcessingStatus status;
	CSSImportPostProcessor processor;

	protected void setUp() throws Exception {
		super.setUp();
		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/css/bundle.css";
		// Bundle url prefix
		final String urlPrefix = "/v00";

		bundle = buildFakeBundle(bundlePath, urlPrefix);
		config = new JawrConfig(new Properties());
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");
		
		processor = new CSSImportPostProcessor();
	}

	

	public void testBasicRelativeURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	public void testRelativeURLImportWithSpaceAndSimpleQuote() {
		// basic test
		StringBuffer data = new StringBuffer("@import url( \n 'temp.css' \n );\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	public void testRelativeURLImportWithSpaceAndDoubleQuote() {
		// basic test
		StringBuffer data = new StringBuffer("@import url( \n \"temp.css\" \n );\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	public void testBasicAbsoluteURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(/style/myStyle/temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/style/myStyle/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}

	public void testClasspathCssRelativeURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(../rainbow/temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "jar:cssimportprocessor/style/myStyle/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "jar:cssimportprocessor/style/rainbow/temp.css");
		String result = processor.postProcessBundle(status, data).toString().replaceAll("\r", "");		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	public void testClasspathCssAbsoluteURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(jar:cssimportprocessor/style/rainbow/temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "jar:cssimportprocessor/style/rainbow/temp.css");
		String result = processor.postProcessBundle(status, data).toString().replaceAll("\r", "");		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}

	private BundleProcessingStatus getBundleProcessingStatus(String filePath, String expectedCssImportPath) {
		ResourceHandler rsHandler = getResourceHandler(expectedCssImportPath);
		BundleProcessingStatus status = new BundleProcessingStatus(bundle, rsHandler, config);
		status.setLastPathAdded(filePath);
		return status;
	}

	private JoinableResourceBundle buildFakeBundle(final String id,
			final String urlPrefix) {

		return new JoinableResourceBundle() {
			public boolean belongsToBundle(String itemPath) {
				return false;
			}

			public InclusionPattern getInclusionPattern() {
				return null;
			}

			public List getItemPathList() {
				return null;
			}

			public Set getLicensesPathList() {
				return null;
			}

			public String getId() {
				return id;
			}

			public String getURLPrefix(String variantKey) {
				return urlPrefix;
			}

			public ResourceBundlePostProcessor getBundlePostProcessor() {
				return null;
			}

			public ResourceBundlePostProcessor getUnitaryPostProcessor() {
				return null;
			}

			public void setBundleDataHashCode(String var, int bundleDataHashCode) {

			}

			public String getExplorerConditionalExpression() {
				return null;
			}

			public List getItemPathList(String variantKey) {
				return null;
			}

			public List getLocaleVariantKeys() {
				return null;
			}

			public String getAlternateProductionURL() {
				return null;
			}

			public String getBundleDataHashCode(String variantKey) {
				return null;
			}

			public String getName() {
				return null;
			}

			public boolean isComposite() {
				return false;
			}

			public void setBundleDataHashCode(String variantKey,
					String bundleDataHashCode) {

			}

			public void setMappings(List mappings) {

			}
		};

	}
	
	private ResourceHandler getResourceHandler(final String expectedResourcePath) {
		
		return new ResourceHandler(){

			public Reader getCssClasspathResource(String resourceName)
					throws ResourceNotFoundException {
				return null;
			}

			public Properties getJawrBundleMapping() {
				return null;
			}

			public Reader getResource(String resourceName)
					throws ResourceNotFoundException {
				
				if(!resourceName.equals(expectedResourcePath)){
					fail("The expected resource path was : '"+expectedResourcePath+"'; but we get : '"+resourceName);
				}
				return new StringReader(".test { align : left; \n" +
						"padding : 0 7px; \n" +
				"}");
			}

			public Reader getResource(String resourceName,
					boolean processingBundle) throws ResourceNotFoundException {
				return new StringReader(".test { align : left; \n" +
						"padding : 0 7px; \n" +
				"}");
			}

			public InputStream getResourceAsStream(String resourceName)
					throws ResourceNotFoundException {
				return null;
			}

			public ReadableByteChannel getResourceBundleChannel(
					String bundleName) throws ResourceNotFoundException {
				return null;
			}

			public Reader getResourceBundleReader(String bundleName)
					throws ResourceNotFoundException {
				return null;
			}

			public Set getResourceNames(String path) {
				return null;
			}

			public String getResourceType() {
				return null;
			}

			public InputStream getTemporaryResourceAsStream(String resourceName)
					throws ResourceNotFoundException {
				return null;
			}

			public boolean isDirectory(String path) {
				return false;
			}

			public boolean isExistingMappingFile() {
				return false;
			}

			public boolean isResourceGenerated(String path) {
				return false;
			}

			public void storeBundle(String bundleName,
					JoinableResourceBundleContent bundleResourcesContent) {
				
			}

			public void storeJawrBundleMapping(Properties bundleMapping) {
				
			}
			
		};
	}
}
