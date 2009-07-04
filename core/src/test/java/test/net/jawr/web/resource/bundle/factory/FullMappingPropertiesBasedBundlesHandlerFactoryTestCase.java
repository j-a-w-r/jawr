package test.net.jawr.web.resource.bundle.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.JoinableResourceBundlePropertySerializer;
import net.jawr.web.resource.bundle.factory.FullMappingPropertiesBasedBundlesHandlerFactory;
import net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * Test case for FullMappingPropertiesBasedBundlesHandlerFactory
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class FullMappingPropertiesBasedBundlesHandlerFactoryTestCase extends
		TestCase {

	public void testGetResourceBundles() {
		
		ResourceHandler rsHandler = new TestResourceHandler();
		PostProcessorChainFactory chainFactory = new TestPostProcessorChainFactory();
		
		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory("js", rsHandler, chainFactory);
		
		Properties props = new Properties();
		
		JoinableResourceBundle globalBundle = getGlobalBundle();
		JoinableResourceBundlePropertySerializer.serializeInProperties(globalBundle, "js", props);
		
		JoinableResourceBundle stdBundle = getStdBundle();
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);
		
		List resourcesBundles = factory.getResourceBundles(props);
		assertEquals(2, resourcesBundles.size());
		
		for (int i = 0; i < resourcesBundles.size(); i++) {
			JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(i);
			if(bundle.getId().equals("/bundle/myGlobalBundle.js")){
				
				Set expectedMappings = new HashSet(Arrays.asList(new String[]{"/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"}));
				assertEquals(expectedMappings, new HashSet(bundle.getItemPathList()));
				
				assertEquals(true, bundle.getInclusionPattern().isGlobal());
				assertEquals(false, bundle.getInclusionPattern().isExcludeOnDebug());
				assertEquals(false, bundle.getInclusionPattern().isIncludeOnDebug());
				assertEquals("123456", bundle.getBundleDataHashCode(null));
				
			}else if(bundle.getId().equals("/bundle/myBundle.js")){
				
				Set expectedMappings = new HashSet(Arrays.asList(new String[]{"/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"}));
				assertEquals(expectedMappings, new HashSet(bundle.getItemPathList()));
				
				assertEquals(true, bundle.getInclusionPattern().isGlobal());
				assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
				assertEquals(false, bundle.getInclusionPattern().isExcludeOnDebug());
				assertEquals(true, bundle.getInclusionPattern().isIncludeOnDebug());
				assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
				assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
				assertEquals("myBundlePostProcessor1,myBundlePostProcessor2", ((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
				assertEquals("myFilePostProcessor1,myFilePostProcessor2", ((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());
				
				Set expectedLocales = new HashSet(Arrays.asList(new String[]{"fr", "en_US"}));
				assertEquals(expectedLocales, new HashSet(bundle.getLocaleVariantKeys()));
				
				assertEquals("N123456", bundle.getBundleDataHashCode(null));
				assertEquals("123456", bundle.getBundleDataHashCode("fr"));
				assertEquals("789", bundle.getBundleDataHashCode("en_US"));
				
			}else{
				fail("The bundles don't contains the right Ids");
			}
		}
	}

	private JoinableResourceBundle getGlobalBundle(){
		String bundleName = "myGlobalBundle";
		List mappings = Arrays.asList(new String[]{"/bundle/content/**", "/bundle/myScript.js"});
		
		ResourceHandler handler = new TestResourceHandler();
		InclusionPattern inclusionPattern = new InclusionPattern(true, 0);
		JoinableResourceBundle bundle = new JoinableResourceBundleImpl("/bundle/myGlobalBundle.js", bundleName, ".js", inclusionPattern, handler);
		bundle.setMappings(mappings);
		bundle.setBundleDataHashCode(null, 123456);
		
		return bundle;
	}
	
	private JoinableResourceBundleImpl getStdBundle(){
		
		String bundleName = "myBundle";
		List mappings = Arrays.asList(new String[]{"/bundle/content/**", "/bundle/myScript.js"});
		
		ResourceHandler handler = new TestResourceHandler();
		InclusionPattern inclusionPattern = new InclusionPattern(true, 3, true, false);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, ".js", inclusionPattern, handler);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");
		
		bundle.setLocaleVariantKeys(Arrays.asList(new String[]{"fr", "en_US"}));
		bundle.setBundleDataHashCode(null, -123456);
		bundle.setBundleDataHashCode("fr", 123456);
		bundle.setBundleDataHashCode("en_US", 789);
		
		ResourceBundlePostProcessor bundlePostProcessor = new AbstractChainedResourceBundlePostProcessor("myBundlePostProcessor1,myBundlePostProcessor2"){

			protected StringBuffer doPostProcessBundle(
					BundleProcessingStatus status, StringBuffer bundleData)
					throws IOException {
				return null;
			}
		};
		bundle.setBundlePostProcessor(bundlePostProcessor);

		ResourceBundlePostProcessor filePostProcessor = new AbstractChainedResourceBundlePostProcessor("myFilePostProcessor1,myFilePostProcessor2"){

			protected StringBuffer doPostProcessBundle(
					BundleProcessingStatus status, StringBuffer bundleData)
					throws IOException {
				return null;
			}
		};
		bundle.setUnitaryPostProcessor(filePostProcessor);
	
		return bundle;
	}
	private class TestPostProcessorChainFactory implements PostProcessorChainFactory{

		public ResourceBundlePostProcessor buildDefaultProcessorChain() {
			return null;
		}

		public ResourceBundlePostProcessor buildDefaultUnitProcessorChain() {
			return null;
		}

		public ResourceBundlePostProcessor buildPostProcessorChain(
				String processorKeys) {
			
			return new TestChainedResourceBundlePostProcessor(processorKeys);
		}

		public void setCustomPostprocessors(Map keysClassNames) {
			
		}
	}
	
	private static class TestChainedResourceBundlePostProcessor extends AbstractChainedResourceBundlePostProcessor{
		
		public TestChainedResourceBundlePostProcessor(String id) {
			super(id);
		}
		
		protected StringBuffer doPostProcessBundle(
				BundleProcessingStatus status,
				StringBuffer bundleData) throws IOException {
			return null;
		}
	}
	
	private static class TestResourceHandler implements ResourceHandler{

		public Set getResourceNames(String path) {
			
			List paths = Arrays.asList(new String[]{"script1.js", "script2.js"});
			return new HashSet(paths);
		}

		public boolean isDirectory(String path) {
			
			return path.endsWith("/**");
		}

		public Reader getCssClasspathResource(String resourceName)
				throws ResourceNotFoundException {
			return null;
		}

		public Properties getJawrBundleMapping() {
			return null;
		}

		public Reader getResource(String resourceName)
				throws ResourceNotFoundException {
			return null;
		}

		public Reader getResource(String resourceName, boolean processingBundle)
				throws ResourceNotFoundException {
			return null;
		}

		public InputStream getResourceAsStream(String resourceName)
				throws ResourceNotFoundException {
			return null;
		}

		public ReadableByteChannel getResourceBundleChannel(String bundleName)
				throws ResourceNotFoundException {
			return null;
		}

		public Reader getResourceBundleReader(String bundleName)
				throws ResourceNotFoundException {
			return null;
		}

		public String getResourceType() {
			return null;
		}

		public InputStream getTemporaryResourceAsStream(String resourceName)
				throws ResourceNotFoundException {
			return null;
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
	}
}
