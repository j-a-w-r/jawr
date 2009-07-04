/**
 * 
 */
package test.net.jawr.web.resource.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.JoinableResourceBundlePropertySerializer;
import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * Test case for JoinableResourceBundle  serializer
 * @author Ibrahim Chaehoi
 *
 */
public class JoinableResourceBundleSerializerTestCase extends TestCase {

	public void testGlobalBundleSerialization(){
		
		String bundleName = "myBundle";
		String resourceType = "js";
		List mappings = Arrays.asList(new String[]{"/bundle/content/**", "/bundle/myScript.js"});
		
		ResourceHandler handler = new TestResourceHandler();
		InclusionPattern inclusionPattern = new InclusionPattern(true, 0);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, ".js", inclusionPattern, handler);
		bundle.setMappings(mappings);
		bundle.setBundleDataHashCode(null, 123456);
		
		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle, resourceType, props);
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(props, resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));
		
		Set expectedMappings = new HashSet(Arrays.asList(new String[]{"/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"}));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));
		
		assertEquals("true", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		
	}

	public void testStdBundleSerialization(){
		
		String bundleName = "myBundle";
		String resourceType = "js";
		List mappings = Arrays.asList(new String[]{"/bundle/content/**", "/bundle/myScript.js"});
		
		ResourceHandler handler = new TestResourceHandler();
		InclusionPattern inclusionPattern = new InclusionPattern(false, 3, true, false);
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
		
		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle, resourceType, props);
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(props, resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));
		
		Set expectedMappings = new HashSet(Arrays.asList(new String[]{"/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"}));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));
		
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG, "false"));
		assertEquals("3", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));
		assertEquals("true", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));
		assertEquals("http://hostname/scripts/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		assertEquals("if lt IE 6", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));
		assertEquals("myFilePostProcessor1,myFilePostProcessor2", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		
		Set expectedLocales = new HashSet(Arrays.asList(new String[]{"fr", "en_US"}));
		assertEquals(expectedLocales, helper.getCustomBundlePropertyAsSet(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_LOCALE_VARIANTS));
		
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+"fr"));
		assertEquals("789", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+"en_US"));
		
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
