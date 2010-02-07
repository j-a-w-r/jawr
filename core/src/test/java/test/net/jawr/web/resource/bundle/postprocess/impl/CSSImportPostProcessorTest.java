/**
 * 
 */
package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CSSImportPostProcessor;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.servlet.mock.MockServletContext;

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
		config.setCssClasspathImageHandledByClasspathCss(true);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(JawrConstant.CSS_TYPE);
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		
		JawrConfig imgConfig = new JawrConfig(new Properties());
		GeneratorRegistry imgGeneratorRegistry = new GeneratorRegistry(JawrConstant.IMG_TYPE);
		generatorRegistry.setConfig(imgConfig);
		imgConfig.setGeneratorRegistry(imgGeneratorRegistry);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgConfig, null, null);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		processor = new CSSImportPostProcessor();
	}

	

	public void testBasicRelativeURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+ 
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
						"background : url('../img/rainbow.png'); \n"+ 
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
						"background : url('../img/rainbow.png'); \n"+ 
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
						"background : url('../../../../style/img/rainbow.png'); \n"+ 
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
						"background : url('jar:cssimportprocessor/style/img/rainbow.png'); \n"+ 
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
						"background : url('jar:cssimportprocessor/style/img/rainbow.png'); \n"+ 
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "jar:cssimportprocessor/style/rainbow/temp.css");
		String result = processor.postProcessBundle(status, data).toString().replaceAll("\r", "");		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}

	private BundleProcessingStatus getBundleProcessingStatus(String filePath, String expectedCssImportPath) {
		ResourceReaderHandler rsHandler = getResourceReaderHandler(expectedCssImportPath);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		
		ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) config.getContext().getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
		imgRsHandler.getJawrConfig().getGeneratorRegistry().setResourceReaderHandler(rsHandler);
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
			
			public List getDependencies() {
				return null;
			}

			public void setDependencies(List bundleDependencies) {
				
			}
		};

	}
	
	private ResourceReaderHandler getResourceReaderHandler(final String expectedResourcePath) {
		
		return new ResourceReaderHandler() {
			
			public void setWorkingDirectory(String workingDir) {
				
			}
			
			public boolean isResourceGenerated(String path) {
				return false;
			}
			
			public boolean isDirectory(String resourcePath) {
				return false;
			}
			
			public String getWorkingDirectory() {
				return null;
			}
			
			public Set getResourceNames(String dirPath) {
				return null;
			}
			
			public Reader getResource(String resourceName)
					throws ResourceNotFoundException {
				
				if(!resourceName.equals(expectedResourcePath)){
					fail("The expected resource path was : '"+expectedResourcePath+"'; but we get : '"+resourceName);
				}
				return new StringReader(".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+
				"}");
			}
		
			public Reader getResource(String resourceName,
					boolean processingBundle) throws ResourceNotFoundException {
				return new StringReader(".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+
				"}");
			}
			
			public InputStream getResourceAsStream(String resourceName,
					boolean processingBundle) throws ResourceNotFoundException {
				return null;
			}
			
			public InputStream getResourceAsStream(String resourceName)
					throws ResourceNotFoundException {
				return null;
			}
			
			public void addResourceReaderToStart(ResourceReader rd) {
				
			}
			
			public void addResourceReaderToEnd(ResourceReader rd) {
				
			}
		};
	}
}
