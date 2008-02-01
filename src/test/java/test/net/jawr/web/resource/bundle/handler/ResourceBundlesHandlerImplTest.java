package test.net.java.jawr.web.resource.bundle.handler;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import net.java.jawr.web.config.JawrConfig;
import net.java.jawr.web.exception.ResourceNotFoundException;
import net.java.jawr.web.resource.ResourceHandler;
import net.java.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

import test.net.java.jawr.web.resource.bundle.PredefinedBundlesHandlerUtil;



public class ResourceBundlesHandlerImplTest  extends  ResourceHandlerBasedTest {
	
	private static final String ROOT_DEFAULT_FOLDER = "/collectionshandler/default/";
	private static final String ROOT_DEFAULT_DEBUG_FOLDER = "/collectionshandler/debug/";
	private static final String ROOT_SIMPLE_FOLDER = "/collectionshandler/simple/";
	private ResourceBundlesHandler defaultHandler;
	private ResourceBundlesHandler defaultDebugCollection;
	private ResourceBundlesHandler simpleHandler;
	private static final String RESOURCES_PREFIX = "/test";
	private static final String RESOURCES_GLOBAL_PREFIX = "/global00";
	private static final String RESOURCES_LIB_PREFIX = "/lib00";

	public ResourceBundlesHandlerImplTest() {
		try {
			Charset charsetUtf = Charset.forName("UTF-8"); 
			
			ResourceHandler handler = createResourceHandler(ROOT_DEFAULT_FOLDER,charsetUtf);
			ResourceHandler handlerSimple = createResourceHandler(ROOT_SIMPLE_FOLDER,charsetUtf);
			ResourceHandler handlerDebug = createResourceHandler(ROOT_DEFAULT_DEBUG_FOLDER,charsetUtf);
			JawrConfig config = new JawrConfig(new Properties());
			config.setCharsetName("UTF-8");
			config.setDebugModeOn(false);
			config.setGzipResourcesModeOn(false);
			//config.setURLPrefix(RESOURCES_PREFIX);
			
			
			JawrConfig configDebug = new JawrConfig(new Properties());
			configDebug.setCharsetName("UTF-8");
			configDebug.setDebugModeOn(true);
			//configDebug.setURLPrefix(RESOURCES_PREFIX);
			
			defaultHandler = PredefinedBundlesHandlerUtil.buildSingleBundleHandler(handler,RESOURCES_PREFIX, config);
			simpleHandler = PredefinedBundlesHandlerUtil.buildSimpleBundles(handlerSimple,"/js","js", config,RESOURCES_PREFIX,RESOURCES_GLOBAL_PREFIX,RESOURCES_LIB_PREFIX);
			defaultDebugCollection = PredefinedBundlesHandlerUtil.buildSimpleBundles(handlerDebug,"/js","js", configDebug,RESOURCES_PREFIX,RESOURCES_GLOBAL_PREFIX,RESOURCES_LIB_PREFIX);
			
		} catch (Exception e) {
			System.out.println("Error in test constructor");
			e.printStackTrace();
		}
	}
	
	public void testGetSingleFilePath() {
		
		assertTrue("The collection path was not initialized properly", 
					defaultHandler.getBundlePaths("/script.js").contains(RESOURCES_PREFIX +"/script.js"));
		
	}
	public void testGetNormalCollectionPaths() {
		
		List simplePaths = simpleHandler.getBundlePaths("/js/one.js");
		assertEquals("Invalid number of paths returned",new Integer(4), new Integer(simplePaths.size()));
		assertEquals("Path ordering does not match expected. ",RESOURCES_LIB_PREFIX +"/library.js", simplePaths.get(0));
		assertEquals("Path ordering does not match expected. ",RESOURCES_GLOBAL_PREFIX +"/global.js", simplePaths.get(1));
		assertEquals("Path ordering does not match expected. ",RESOURCES_PREFIX +"/debugOff.js", simplePaths.get(2));
		assertEquals("Path ordering does not match expected. ",RESOURCES_PREFIX +"/js/one.js", simplePaths.get(3));
		
	}
	public void testGetDebugCollectionPaths() {
		
		
		List simplePaths = defaultDebugCollection.getBundlePaths("/library.js");
		assertEquals("Path ordering does not match expected. ","/js/lib/prototype/protoype.js", simplePaths.get(0));
		assertEquals("Path ordering does not match expected. ","/js/lib/lib2.js", simplePaths.get(1));
		assertEquals("Path ordering does not match expected. ","/js/lib/scriptaculous/scriptaculous.js", simplePaths.get(2));
		
	}

	public void testWriteCollectionTo() {
		StringWriter writer = new StringWriter();
		try {
			defaultHandler.writeBundleTo(RESOURCES_PREFIX +"/script.js", writer);
		} catch (ResourceNotFoundException e) {
			fail("File was not found:" + e.getRequestedPath());
		}
		assertTrue("Nothing was written to the file", writer.getBuffer().length() > 0);
		
		writer = new StringWriter();
		try {
			simpleHandler.writeBundleTo(RESOURCES_PREFIX +"/js/one.js", writer);
		} catch (ResourceNotFoundException e) {
			fail("File was not found:" + e.getRequestedPath());
		}
		assertTrue("Nothing was written to the file", writer.getBuffer().length() > 0);
	}

	public void testResolveCollectionForPath() {
		assertEquals("Get script by id failed","/script.js", defaultHandler.resolveBundleForPath("/script.js"));
		assertEquals("Get script by script name failed","/script.js", defaultHandler.resolveBundleForPath("/js/script1.js"));

		assertEquals("Get script by id failed","/library.js", simpleHandler.resolveBundleForPath("/library.js"));
		assertEquals("Get script by script name failed","/global.js", simpleHandler.resolveBundleForPath("/js/global/global.js"));
	}

}
