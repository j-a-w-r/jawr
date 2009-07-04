package test.net.jawr.web.resource.bundle.handler;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Properties;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import test.net.jawr.web.resource.bundle.PredefinedBundlesHandlerUtil;



public class ResourceBundlesHandlerImplTest  extends  ResourceHandlerBasedTest {
	
	private static final String ROOT_DEFAULT_FOLDER = "/collectionshandler/default/";
	private static final String ROOT_DEFAULT_DEBUG_FOLDER = "/collectionshandler/debug/";
	private static final String ROOT_SIMPLE_FOLDER = "/collectionshandler/simple/";
	private ResourceBundlesHandler defaultHandler;
	private ResourceBundlesHandler defaultDebugCollection;
	private ResourceBundlesHandler simpleHandler;

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
			
			defaultHandler = PredefinedBundlesHandlerUtil.buildSingleBundleHandler(handler, config);
			simpleHandler = PredefinedBundlesHandlerUtil.buildSimpleBundles(handlerSimple,"/js","js", config);
			defaultDebugCollection = PredefinedBundlesHandlerUtil.buildSimpleBundles(handlerDebug,"/js","js", configDebug);
			
		} catch (Exception e) {
			System.out.println("Error in test constructor");
			e.printStackTrace();
		}
	}
	
	public void testGetSingleFilePath() {
		
		assertTrue("The collection path was not initialized properly", 
					defaultHandler.getBundlePaths("/script.js",null,null).next().toString().endsWith("/script.js"));
		
	}
	public void testGetNormalCollectionPaths() {
		
		ResourceBundlePathsIterator globalSimplePaths = simpleHandler.getGlobalResourceBundlePaths(null,null);
		assertTrue("Path ordering does not match expected. ", globalSimplePaths.next().toString().endsWith("/library.js"));
		assertTrue("Path ordering does not match expected. ", globalSimplePaths.next().toString().endsWith("/global.js"));
		assertTrue("Path ordering does not match expected. ", globalSimplePaths.next().toString().endsWith("/debugOff.js"));
		
		ResourceBundlePathsIterator simplePaths = simpleHandler.getBundlePaths("/js/one.js",null,null);
		//assertEquals("Invalid number of paths returned",new Integer(4), new Integer(simplePaths.size()));
		assertTrue("Path ordering does not match expected. ", simplePaths.next().toString().endsWith("js/one.js"));
		
	}
	public void testGetDebugCollectionPaths() {
		
		ResourceBundlePathsIterator simplePaths = defaultDebugCollection.getGlobalResourceBundlePaths(null,null);
		assertEquals("Path ordering does not match expected. ","/js/lib/prototype/protoype.js", simplePaths.next());
		assertEquals("Path ordering does not match expected. ","/js/lib/lib2.js", simplePaths.next());
		assertEquals("Path ordering does not match expected. ","/js/lib/scriptaculous/scriptaculous.js", simplePaths.next());
		
	}

	public void testWriteCollectionTo() {
		StringWriter writer = new StringWriter();
		try {
			defaultHandler.writeBundleTo("/dummy/script.js", writer);
		} catch (ResourceNotFoundException e) {
			fail("File was not found:" + e.getRequestedPath());
		}
		assertTrue("Nothing was written to the file", writer.getBuffer().length() > 0);
		
		writer = new StringWriter();
		try {
			simpleHandler.writeBundleTo("/dummy/js/one.js", writer);
		} catch (ResourceNotFoundException e) {
			fail("File was not found:" + e.getRequestedPath());
		}
		assertTrue("Nothing was written to the file", writer.getBuffer().length() > 0);
	}

	public void testResolveCollectionForPath() {
		assertEquals("Get script by id failed","/script.js", defaultHandler.resolveBundleForPath("/script.js").getId());
		assertEquals("Get script by script name failed","/script.js", defaultHandler.resolveBundleForPath("/js/script1.js").getId());

		assertEquals("Get script by id failed","/library.js", simpleHandler.resolveBundleForPath("/library.js").getId());
		assertEquals("Get script by script name failed","/global.js", simpleHandler.resolveBundleForPath("/js/global/global.js").getId());
	}

}
