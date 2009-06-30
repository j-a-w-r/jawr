package test.net.jawr.web;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.jawr.web.BundleProcessor;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;

/**
 * Bundle processor test case
 * 
 * @author Ibrahim Chaehoi
 */
public class BundleProcessorTestCase extends TestCase {

	private BundleProcessor bundleProcessor = new BundleProcessor();
	
	public void testFinalGenerationBundlePath(){
	
		JawrConfig jawrConfig = new JawrConfig(new Properties());
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		jawrConfig.setGeneratorRegistry(generatorRegistry);
		
		// JS without servlet mapping
		// File path for production mode
		jawrConfig.setDebugModeOn(false);
		assertEquals("js/bundle/msg.js", bundleProcessor.getFinalBundlePath("/N1785986402/js/bundle/msg.js", jawrConfig, ""));
		assertEquals("js/bundle/msg_fr.js", bundleProcessor.getFinalBundlePath("/N1785986402.fr/js/bundle/msg.js", jawrConfig, "fr"));
		assertEquals("js/bundle/msg_en_US.js", bundleProcessor.getFinalBundlePath("/N1388754583.en_US/js/bundle/msg.js", jawrConfig, "en_US"));
		
		// File path for debug mode
		jawrConfig.setDebugModeOn(false);
		assertEquals("/jawr_generator/js/messages/messages.js", bundleProcessor.getFinalBundlePath("/jawr_generator.js?generationConfigParam=messages%3Amessages", jawrConfig, ""));
		assertEquals("/jawr_generator/js/messages/messages_fr.js", bundleProcessor.getFinalBundlePath("/jawr_generator.js?generationConfigParam=messages%3Amessages%40fr", jawrConfig, "fr"));
		
		// JS With servlet mapping
		jawrConfig.setServletMapping("jsJawrPath");
		// File path for production mode
		jawrConfig.setDebugModeOn(false);
		assertEquals("js/bundle/msg.js", bundleProcessor.getFinalBundlePath("/jsJawrPath/N1785986402/js/bundle/msg.js", jawrConfig, ""));
		assertEquals("js/bundle/msg_fr.js", bundleProcessor.getFinalBundlePath("/jsJawrPath/N1785986402.fr/js/bundle/msg.js", jawrConfig, "fr"));
		assertEquals("js/bundle/msg_en_US.js", bundleProcessor.getFinalBundlePath("/jsJawrPath/N1388754583.en_US/js/bundle/msg.js", jawrConfig, "en_US"));
		
		// File path for debug mode
		jawrConfig.setDebugModeOn(false);
		assertEquals("/jawr_generator/js/messages/messages.js", bundleProcessor.getFinalBundlePath("/jsJawrPath/jawr_generator.js?generationConfigParam=messages%3Amessages", jawrConfig, ""));
		assertEquals("/jawr_generator/js/messages/messages_fr.js", bundleProcessor.getFinalBundlePath("/jsJawrPath/jawr_generator.js?generationConfigParam=messages%3Amessages%40fr", jawrConfig, "fr"));
		
		// CSS Without servlet mapping
		// File path for production mode
		jawrConfig.setDebugModeOn(false);
		jawrConfig.setServletMapping("");
		generatorRegistry = new GeneratorRegistry(JawrConstant.CSS_TYPE);
		jawrConfig.setGeneratorRegistry(generatorRegistry);
		
		assertEquals("folder/core/component.css", bundleProcessor.getFinalBundlePath("/1414653084/folder/core/component.css", jawrConfig, ""));
		assertEquals("css/two.css", bundleProcessor.getFinalBundlePath("/N87509158/css/two.css", jawrConfig, ""));
		
		// File path for debug mode
		jawrConfig.setDebugModeOn(true);
		assertEquals("css/one.css", bundleProcessor.getFinalBundlePath("/css/one.css", jawrConfig, ""));
		assertEquals("/jawr_generator/css/jar/net/jawr/css/cpStyle.css", bundleProcessor.getFinalBundlePath("/jawr_generator.css?generationConfigParam=jar%3Anet%2Fjawr%2Fcss%2FcpStyle.css", jawrConfig, ""));
		
		// CSS With servlet mapping
		// File path for production mode
		jawrConfig.setDebugModeOn(false);
		jawrConfig.setServletMapping("cssJawrPath");
		generatorRegistry = new GeneratorRegistry(JawrConstant.CSS_TYPE);
		jawrConfig.setGeneratorRegistry(generatorRegistry);
		
		assertEquals("folder/core/component.css", bundleProcessor.getFinalBundlePath("/cssJawrPath/1414653084/folder/core/component.css", jawrConfig, ""));
		assertEquals("css/two.css", bundleProcessor.getFinalBundlePath("/cssJawrPath/N87509158/css/two.css", jawrConfig, ""));
		
		// File path for debug mode
		jawrConfig.setDebugModeOn(true);
		assertEquals("css/one.css", bundleProcessor.getFinalBundlePath("/cssJawrPath/css/one.css", jawrConfig, ""));
		assertEquals("/jawr_generator/css/jar/net/jawr/css/cpStyle.css", bundleProcessor.getFinalBundlePath("/cssJawrPath/jawr_generator.css?generationConfigParam=jar%3Anet%2Fjawr%2Fcss%2FcpStyle.css", jawrConfig, ""));
		
	}
	
	public void testGetFinalImageName(){
		
		JawrConfig jawrConfig = new JawrConfig(new Properties());
		
		// Without servlet mapping
		assertEquals("classpathResources/img/iconInformation.gif", bundleProcessor.getImageFinalPath("/cpCb3df496cbae960efd97933bdd50e5d454/classpathResources/img/iconInformation.gif", jawrConfig));
		assertEquals("img/appIcons/application_add.png", bundleProcessor.getImageFinalPath("/cb31d7ab9cdff1b9eafdf728250d5ea78a/img/appIcons/application_add.png", jawrConfig));
		
		// With servlet mapping
		jawrConfig.setServletMapping("jawrImg");
		assertEquals("classpathResources/img/iconInformation.gif", bundleProcessor.getImageFinalPath("/jawrImg/cpCb3df496cbae960efd97933bdd50e5d454/classpathResources/img/iconInformation.gif", jawrConfig));
		assertEquals("img/appIcons/application_add.png", bundleProcessor.getImageFinalPath("/jawrImg/cb31d7ab9cdff1b9eafdf728250d5ea78a/img/appIcons/application_add.png", jawrConfig));
		
	}
	
	public void testBundleProcessing() throws Exception{
		
		String baseDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/destDir";
		
		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);
		
		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, true);
		
		String bundlePath = FileUtils.getClasspathRootDir()+"/bundleProcessor/tmpDir/jawrTmp/text/bundle/global.js";
		Assert.assertTrue("Bundle has not been created", new File(bundlePath).exists());
	}
	
	
}
