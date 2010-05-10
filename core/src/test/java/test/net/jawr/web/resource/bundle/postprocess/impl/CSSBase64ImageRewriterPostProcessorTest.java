package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.css.base64.Base64ImageEncoderPostProcessor;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;
import test.net.jawr.web.resource.bundle.handler.MockResourceReaderHandler;
import test.net.jawr.web.servlet.mock.MockServletContext;

public class CSSBase64ImageRewriterPostProcessorTest extends TestCase {
	
	
	JoinableResourceBundle bundle;
	JawrConfig config;
	BundleProcessingStatus status;
	Base64ImageEncoderPostProcessor processor;
	
	
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
		addGeneratorRegistryToConfig(config, "js");
		status.setLastPathAdded("/css/someCSS.css");
		processor = new Base64ImageEncoderPostProcessor();
	}

	private GeneratorRegistry addGeneratorRegistryToConfig(JawrConfig config, String type) {
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type){

			public boolean isHandlingCssImage(String cssResourcePath) {
				
				boolean result = false;
				if(cssResourcePath.startsWith("jar:")){
					result = true;
				}
				return result;
			}
		};
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		return generatorRegistry;
	}
	
	public void testBasicImgCssRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
		config = new JawrConfig(props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png);");
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(data:image/png;base64,RmFrZSB2YWx1ZQ==);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}
	
	public void testBasicImgAsolutURLCssRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
		props.setProperty(JawrConstant.JAWR_CSS_URL_REWRITER_CONTEXT_PATH, "/myApp");
		config = new JawrConfig(props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(/myApp/images/logo.png);");
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(data:image/png;base64,RmFrZSB2YWx1ZQ==);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}
	
	public void testEncodeTooBigImgCssRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
		config = new JawrConfig(props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo-bigImage.png);");
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(style/images/logo-bigImage.png);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}
	
	public void testBasicImgCssSkipRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
		config = new JawrConfig(props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png); /** jawr:base64-skip */");
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(../../../cssImg/cb3015770054/style/images/logo.png); /** jawr:base64-skip */";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}
	
	public void testMultipleImgCssSkipRewriting() throws Exception {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
		config = new JawrConfig(props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(FileUtils.readClassPathFile("base64Postprocessor/temp.css"));
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = FileUtils.readClassPathFile("base64Postprocessor/temp-result.css"); //"background-image:url(../../../cssImg/cb3015770054/style/images/logo.png); /** jawr:base64-skip */";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}
	
//	public void testImgURLFromClasspathCssRewriting() {
//
//		// Set the properties
//		Properties props = new Properties();
//		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
//		config = new JawrConfig(props);
//		ServletContext servletContext = new MockServletContext();
//		config.setContext(servletContext);
//		config.setServletMapping("/css");
//		config.setCharsetName("UTF-8");
//		addGeneratorRegistryToConfig(config, "css");
//		
//		// Set up the Image servlet Jawr config
//		props = new Properties();
//		JawrConfig imgServletJawrConfig = new JawrConfig(props);
//		imgServletJawrConfig.setServletMapping("/cssImg/");
//		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
//		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
//		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
//		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
//		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
//		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
//		
//		status = new BundleProcessingStatus(bundle, null, config);
//
//		// Css data
//		StringBuffer data = new StringBuffer(
//				"background-image:url(../../images/logo.png);");
//		
//		// Css path
//		String filePath = "jar:style/default/assets/someCSS.css";
//		
//		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
//		// then go to the image path
//		// the image is at classPath:/style/images/someImage.gif
//		String expectedURL = "background-image:url(../../../cssImg/jar_cb3015770054/style/images/logo.png);";
//		status.setLastPathAdded(filePath);
//
//		String result = processor.postProcessBundle(status, data).toString();
//		assertEquals("URL was not rewritten properly", expectedURL, result);
//
//	}
//	
//	
//	
//	public void testURLImgClasspathCssRewriting() {
//
//		// Set the properties
//		Properties props = new Properties();
//		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
//		config = new JawrConfig(props);
//		ServletContext servletContext = new MockServletContext();
//		config.setContext(servletContext);
//		config.setServletMapping("/css");
//		config.setCharsetName("UTF-8");
//		addGeneratorRegistryToConfig(config, "css");
//		
//		// Set up the Image servlet Jawr config
//		props = new Properties();
//		JawrConfig imgServletJawrConfig = new JawrConfig(props);
//		GeneratorRegistry generatorRegistry = addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
//		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
//		generatorRegistry.setResourceReaderHandler(rsHandler);
//		imgServletJawrConfig.setServletMapping("/cssImg/");
//		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
//		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
//		
//		status = new BundleProcessingStatus(bundle, null, config);
//
//		// Css data
//		StringBuffer data = new StringBuffer(
//				"background-image:url(jar:style/images/logo.png);");
//		
//		// Css path
//		String filePath = "style/default/assets/someCSS.css";
//		
//		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
//		// then go to the image path
//		// the image is at classPath:/style/images/someImage.gif
//		String expectedURL = "background-image:url(../../../cssImg/jar_cb3015770054/style/images/logo.png);";
//		status.setLastPathAdded(filePath);
//
//		String result = processor.postProcessBundle(status, data).toString();
//		assertEquals("URL was not rewritten properly", expectedURL, result);
//
//	}
//	
//	public void testImgURLRewritingForDataScheme() {
//
//		// Set the properties
//		Properties props = new Properties();
//		props.setProperty(JawrConfig.JAWR_CSS_IMG_USE_CLASSPATH_SERVLET, "true");
//		config = new JawrConfig(props);
//		ServletContext servletContext = new MockServletContext();
//		config.setContext(servletContext);
//		config.setServletMapping("/css");
//		config.setCharsetName("UTF-8");
//		
//		// Set up the Image servlet Jawr config
//		props = new Properties();
//		JawrConfig imgServletJawrConfig = new JawrConfig(props);
//		imgServletJawrConfig.setServletMapping("/cssImg/");
//		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, null, null);
//		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
//		
//		status = new BundleProcessingStatus(bundle, null, config);
//
//		// Css data
//		StringBuffer data = new StringBuffer(
//				"background-image: url(data:image/gif;base64,AAAA);");
//		
//		// Css path
//		String filePath = "style/default/assets/someCSS.css";
//		
//		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
//		// then go to the image path
//		// the image is at classPath:/style/images/someImage.gif
//		String expectedURL = "background-image: url(data:image/gif;base64,AAAA);";
//		status.setLastPathAdded(filePath);
//
//		String result = processor.postProcessBundle(status, data).toString();
//		assertEquals("URL was not rewritten properly", expectedURL, result);
//
//	}
//	
//	public void testBasicURLWithCachedImageRewriting() {
//		
//		// Set the properties
//		Properties props = new Properties();
//		config = new JawrConfig(props);
//		ServletContext servletContext = new MockServletContext();
//		config.setContext(servletContext);
//		config.setServletMapping("/css");
//		config.setCharsetName("UTF-8");
//		addGeneratorRegistryToConfig(config, "css");
//		status = new BundleProcessingStatus(bundle, null, config);
//
//		// Set up the Image servlet Jawr config
//		props = new Properties();
//		JawrConfig imgServletJawrConfig = new JawrConfig(props);
//		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, null, null);
//		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
//		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
//		imgRsHandler.addMapping("/images/someImage.gif", "/cp653321354/images/someImage.gif");
//		// basic test
//		StringBuffer data = new StringBuffer("background-image:url(../../../../images/someImage.gif);");
//		// the image is at /images
//		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
//		// Expected: goes 1 back for servlet mapping, 1 back for prefix, 1 back for the id having a subdir path. 
//		String expectedURL = "background-image:url(../../../cp653321354/images/someImage.gif);";
//		status.setLastPathAdded(filePath);		
//		
//		
//		String result = processor.postProcessBundle(status, data).toString();		
//		assertEquals("URL was not rewritten properly",expectedURL, result);
//		
//	}
//	
//	
//	public void testBasicURLWithNonExistingImageRewriting() {
//		
//		// Set the properties
//		Properties props = new Properties();
//		config = new JawrConfig(props);
//		ServletContext servletContext = new MockServletContext();
//		config.setContext(servletContext);
//		config.setServletMapping("/css");
//		config.setCharsetName("UTF-8");
//		addGeneratorRegistryToConfig(config, "css");
//		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
//		status = new BundleProcessingStatus(bundle, rsHandler, config);
//
//		// Set up the Image servlet Jawr config
//		props = new Properties();
//		JawrConfig imgServletJawrConfig = new JawrConfig(props);
//		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
//		
//		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
//		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
//		// basic test
//		StringBuffer data = new StringBuffer("background-image:url(../../../../images/someImage.gif);");
//		// the image is at /images
//		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
//		// Expected: goes 1 back for servlet mapping, 1 back for prefix, 1 back for the id having a subdir path. 
//		String expectedURL = "background-image:url(../../../images/someImage.gif);";
//		status.setLastPathAdded(filePath);		
//		
//		
//		String result = processor.postProcessBundle(status, data).toString();		
//		assertEquals("URL was not rewritten properly",expectedURL, result);
//		
//	}
//	
//	
//	public void testMultiLine() {
//		StringBuffer data = new StringBuffer("\nsomeRule {");
//		data.append("\n");
//		data.append("\tfont-size:12pt;");
//		data.append("\n");
//		data.append("\tbackground: #00ff00 url('folder/subfolder/subfolder/someImage.gif') no-repeat fixed center; ");
//		data.append("\n");
//		data.append("}");
//		data.append("\n");
//		data.append("anotherRule");
//		data.append("\n");
//		data.append("{");
//		data.append("\n");
//		//data.append("\tbackground-image:url( ../../../../../images/someImage.gif );");
//		data.append("\tbackground-image:url( ../images/someImage.gif );");
//		data.append("\n");
//		data.append("}");
//		data.append("\n");
//		data.append("otherRule");
//		data.append("\n");
//		data.append("{");
//		data.append("\n");
//		data.append("\tbackground-image:url( 'http://www.someSite.org/someImage.gif' );");
//		data.append("\n");
//		data.append("}\n");
//		
//		StringBuffer expected = new StringBuffer("\nsomeRule {");
//		expected.append("\n");
//		expected.append("\tfont-size:12pt;");
//		expected.append("\n");
//		expected.append("\tbackground: #00ff00 url('../../../css/folder/subfolder/subfolder/someImage.gif') no-repeat fixed center; ");
//		expected.append("\n");
//		expected.append("}");
//		expected.append("\n");
//		expected.append("anotherRule");
//		expected.append("\n");
//		expected.append("{");
//		expected.append("\n");
//		expected.append("\tbackground-image:url(../../../images/someImage.gif);");
//		expected.append("\n");
//		expected.append("}");
//		expected.append("\n");
//		expected.append("otherRule");
//		expected.append("\n");
//		expected.append("{");
//		expected.append("\n");
//		expected.append("\tbackground-image:url('http://www.someSite.org/someImage.gif');");
//		expected.append("\n");
//		expected.append("}\n");
//		
//		String result = processor.postProcessBundle(status, data).toString();	
//		assertEquals("URL was not rewritten properly:",expected.toString(), result);
//		
//	}
	private JoinableResourceBundle buildFakeBundle(final String id, final String urlPrefix) {
		
		return new MockJoinableResourceBundle(){
			
			/* (non-Javadoc)
			 * @see test.net.jawr.web.resource.bundle.MockJoinableResourceBundle#getId()
			 */
			public String getId() {
				return id;
			}

			public String getURLPrefix(Map variants) {
				return urlPrefix;
			}
		};
			
	}
	
	private static class FakeResourceReaderHandler extends MockResourceReaderHandler {

		public Reader getResource(String resourceName)
				throws ResourceNotFoundException {
			
			throw new ResourceNotFoundException(resourceName);
		}

		public Reader getResource(String resourceName, boolean processingBundle)
				throws ResourceNotFoundException {
			
			throw new ResourceNotFoundException(resourceName);
			
		}

		public InputStream getResourceAsStream(String resourceName,
				boolean processingBundle) throws ResourceNotFoundException {
			throw new ResourceNotFoundException(resourceName);
		}

		public InputStream getResourceAsStream(String resourceName)
				throws ResourceNotFoundException {
			
			if(resourceName.startsWith("sprite:")){
				throw new ResourceNotFoundException(resourceName);
			}
			
			InputStream is = null;
			if(resourceName.contains("bigImage")){
				
				int length = 400000;
				byte[] data = new byte[length];
				for(int i = 0; i < length; i++){
					data[i] = (byte) ((int)i%2);
				}
				is = new ByteArrayInputStream(data);
			}else{
				is = new ByteArrayInputStream("Fake value".getBytes());
			}
			
			return is;
		}
	}
}
