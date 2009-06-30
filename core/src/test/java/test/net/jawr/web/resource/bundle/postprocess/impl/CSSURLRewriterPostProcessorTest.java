package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;
import net.jawr.web.servlet.mock.MockServletContext;

public class CSSURLRewriterPostProcessorTest extends TestCase {
	
	
	JoinableResourceBundle bundle;
	JawrConfig config;
	BundleProcessingStatus status;
	CSSURLPathRewriterPostProcessor processor;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/css/bundle.css";
		// Bundle url prefix
		final String urlPrefix = "/v00";
		
		bundle = buildFakeBundle(bundlePath, urlPrefix);
		config = new JawrConfig( new Properties());
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");		
		status = new BundleProcessingStatus(bundle,null,config);
		status.setLastPathAdded("/css/someCSS.css");
		processor = new CSSURLPathRewriterPostProcessor();
	}
	
	public void testBasicURLRewriting() {
		// basic test
		StringBuffer data = new StringBuffer("background-image:url(../../../../../images/someImage.gif);");
		//StringBuffer data = new StringBuffer("background-image:url(../../../../images/someImage.gif);");
		// the image is at /images
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		// Expected: goes 1 back for servlet mapping, 1 back for prefix, 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url(../../../images/someImage.gif);";
		status.setLastPathAdded(filePath);		
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	
	
	public void testBackReferenceAndSpaces() {
		// Now a back reference must be created, and there are quotes and spaces
		StringBuffer data = new StringBuffer("background-image:url( \n 'images/someImage.gif' );");
		status.setLastPathAdded("/someCSS.css");
		// Expected: goes 1 back for servlet mapping, 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../images/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly : " +expectedURL + "    \n:  " + result,expectedURL, result);
	}
	
	public void testBackReferenceNoUrlMapping() {
		StringBuffer data = new StringBuffer("background-image:url(  'images/someImage.gif' );");
		// Remove the url mapping from config, one back reference less expected
		config.setServletMapping("");
		String expectedURL = "background-image:url('../../css/images/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	
	public void testSameLevelUrl() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  'folder/subfolder/subfolder/someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/folder/subfolder/subfolder/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	
	public void testSameLineURLs() {
		// Now a back reference must be created, and there are quotes and spaces
		
		StringBuffer data = new StringBuffer(".rule1{background:url(some\\(Image\\).gif);background:url(hue_bg.png) no-repeat;top:4px;}");
		status.setLastPathAdded("/css/someCSS.css");
		// Expected: goes 1 back for servlet mapping, 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = ".rule1{background:url(../../../css/some\\(Image\\).gif);background:url(../../../css/hue_bg.png) no-repeat;top:4px;}";		
		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly : " +expectedURL + "    \n:  " + result,expectedURL, result);
	}
	
	
	public void testSameLevelUrlWithPartialBackreference() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  '../folder/subfolder/subfolder/someImage.gif' );");
		// Test several URLs
		data.append("background-image:url(  '../folder/subfolder/subfolder/someOtherImage.gif' );");
		
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 		
		String expectedURL = "background-image:url('../../../css/folder/subfolder/subfolder/someImage.gif');";
		expectedURL += "background-image:url('../../../css/folder/subfolder/subfolder/someOtherImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	

		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	public void testSameLevelResource() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  'someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/subpath/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	public void testSameLevelExtraPathMapping() {
		// Set a path with several contexts to test if backtracking is done right. 
		status.getJawrConfig().setServletMapping("/foo/bar/baz/");
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  'someImage.gif' );");
		// Expected: goes 3 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../../../css/subpath/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	public void testSameLevelResourceLeadingDotSlash() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  './someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/subpath/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	public void testSameLevelUrlWithComplexBackreference() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/anotherPath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  '../folder/subfolder/subfolder/someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/subpath/folder/subfolder/subfolder/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	public void testSameUrlWithDollarSymbol() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  'folder/subfolder/subfolder$/someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/folder/subfolder/subfolder$/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	public void testUpperCaseUrl() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:URL(  'folder/subfolder/subfolder/someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/folder/subfolder/subfolder/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	public void testSameUrlWithParens() {
		StringBuffer data = new StringBuffer("background-image:url(  'images/some\\(Image\\).gif' );");
		// Remove the url mapping from config, one back reference less expected
		config.setServletMapping("");
		String expectedURL = "background-image:url('../../css/images/some\\(Image\\).gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,expectedURL, result);
		
	}

	public void testSameUrlWithQuotes() {
		StringBuffer data = new StringBuffer("background-image:url(  'images/some\\'Image\\\".gif' );");
		// Remove the url mapping from config, one back reference less expected
		config.setServletMapping("");
		String expectedURL = "background-image:url('../../css/images/some\\'Image\\\".gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,expectedURL, result);
		
	}
	

	public void testDomainRelativeUrl() {
		StringBuffer data = new StringBuffer("background-image:url('/someImage.gif');");
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,data.toString(), result);
		
	}
	
	public void testDblSlashDomainRelativeUrl() {
		StringBuffer data = new StringBuffer("background-image:url('//someImage.gif');");
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,data.toString(), result);
		
	}
	
	public void testStaticUrlWithProtocol() {
		StringBuffer data = new StringBuffer("background-image:url('http://www.someSite.org/someImage.gif');");
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,data.toString(), result);
		
	}
	public void testStaticUrlWithProtocolAndParens() {
		StringBuffer data = new StringBuffer("background-image:url(http://www.someSite.org/some\\(Image.gif\\));");
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,data.toString(), result);
		
	}
	
	public void testImgURLFromClasspathCssRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
		config = new JawrConfig(props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(
				"background-image:url(../../images/logo.png);");
		
		// Css path
		String filePath = "jar:style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(../../../cssImg/cpCb2587531189/style/images/logo.png);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);

	}
	
	
	
	public void testURLImgClasspathCssRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
		config = new JawrConfig(props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(
				"background-image:url(jar:style/images/logo.png);");
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(../../../cssImg/cpCb2587531189/style/images/logo.png);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);

	}
	
	public void testImgURLRewritingForDataScheme() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
		config = new JawrConfig(props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(
				"background-image: url(data:image/gif;base64,AAAA);");
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image: url(data:image/gif;base64,AAAA);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);

	}
	
	
	public void testMultiLine() {
		StringBuffer data = new StringBuffer("\nsomeRule {");
		data.append("\n");
		data.append("\tfont-size:12pt;");
		data.append("\n");
		data.append("\tbackground: #00ff00 url('folder/subfolder/subfolder/someImage.gif') no-repeat fixed center; ");
		data.append("\n");
		data.append("}");
		data.append("\n");
		data.append("anotherRule");
		data.append("\n");
		data.append("{");
		data.append("\n");
		//data.append("\tbackground-image:url( ../../../../../images/someImage.gif );");
		data.append("\tbackground-image:url( ../images/someImage.gif );");
		data.append("\n");
		data.append("}");
		data.append("\n");
		data.append("otherRule");
		data.append("\n");
		data.append("{");
		data.append("\n");
		data.append("\tbackground-image:url( 'http://www.someSite.org/someImage.gif' );");
		data.append("\n");
		data.append("}\n");
		
		StringBuffer expected = new StringBuffer("\nsomeRule {");
		expected.append("\n");
		expected.append("\tfont-size:12pt;");
		expected.append("\n");
		expected.append("\tbackground: #00ff00 url('../../../css/folder/subfolder/subfolder/someImage.gif') no-repeat fixed center; ");
		expected.append("\n");
		expected.append("}");
		expected.append("\n");
		expected.append("anotherRule");
		expected.append("\n");
		expected.append("{");
		expected.append("\n");
		expected.append("\tbackground-image:url(../../../images/someImage.gif);");
		expected.append("\n");
		expected.append("}");
		expected.append("\n");
		expected.append("otherRule");
		expected.append("\n");
		expected.append("{");
		expected.append("\n");
		expected.append("\tbackground-image:url('http://www.someSite.org/someImage.gif');");
		expected.append("\n");
		expected.append("}\n");
		
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:",expected.toString(), result);
		
	}
	private JoinableResourceBundle buildFakeBundle(final String id, final String urlPrefix) {
		
		return new JoinableResourceBundle(){
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
				
			}};
		
	}
}
