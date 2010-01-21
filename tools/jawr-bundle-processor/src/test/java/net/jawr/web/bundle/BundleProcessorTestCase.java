package net.jawr.web.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.bundle.processor.BundleProcessor;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.ServletContextResourceReaderHandler;
import net.jawr.web.servlet.mock.MockServletContext;

/**
 * Bundle processor test case
 * 
 * @author Ibrahim Chaehoi
 */
public class BundleProcessorTestCase extends TestCase {

	private BundleProcessor bundleProcessor = new BundleProcessor();
	
	public void testFinalGenerationBundlePath() throws IOException{
	
		JawrConfig jawrConfig = new JawrConfig(new Properties());
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		jawrConfig.setGeneratorRegistry(generatorRegistry);
		MockServletContext servletContext = new MockServletContext();
		File tmpDir = new File("temp");
		servletContext.setAttribute(JawrConstant.SERVLET_CONTEXT_TEMPDIR, tmpDir);
		ResourceReaderHandler handler = new ServletContextResourceReaderHandler(servletContext, jawrConfig, generatorRegistry);
		
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
		generatorRegistry.setResourceReaderHandler(handler);
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
		generatorRegistry.setResourceReaderHandler(handler);
		
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
		checkGeneratedContent(destDirPath);
	}
	
	public void testSpringBundleProcessing() throws Exception{
		
		String baseDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/destDir";
		
		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);
		
		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, "classpath:/spring-JawrConfig.xml,/WEB-INF/dispatcher-servlet.xml", new ArrayList(), true);
		checkGeneratedContent(destDirPath);
	}

	public void testSpringBundleProcessingWithPlaceHolders() throws Exception{
		
		String baseDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/with-placeholders/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/with-placeholders/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/with-placeholders/destDir";
		
		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);
		
		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, "classpath:/spring-JawrConfig.xml,/WEB-INF/dispatcher-servlet.xml", new ArrayList(), true);
		checkGeneratedContent(destDirPath);
	}
	
	
	public void testSpringBundleProcessingWithNoSpringConfigSet() throws Exception{
		
		String baseDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/destDir";
		
		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);
		
		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, true);
		checkGeneratedContent(destDirPath);
	}
	
	public void testSpringProcessingWithoutMappingBundle() throws Exception{
		
		String baseDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/without-mapping/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/without-mapping/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()+"/bundleProcessor/spring/without-mapping/destDir";
		
		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);
		
		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, "/WEB-INF/dispatcher-servlet.xml", new ArrayList(), true);
		checkGeneratedContent(destDirPath);
	}
	
	private void checkGeneratedContent(String destDirPath){
		
		checkContentCreated(destDirPath+"/CDN/bundle/js/global.js");
		checkContentCreated(destDirPath+"/CDN/bundle/css/component.css");
		checkContentCreated(destDirPath+"/CDN/classpathResources/img/clock.png");
		checkContentCreated(destDirPath+"/CDN/classpathResources/img/iconInformation.gif");
		checkContentCreated(destDirPath+"/CDN/css/one.css");
		checkContentCreated(destDirPath+"/CDN/img/mysprite.png");
		checkContentCreated(destDirPath+"/CDN/img/appIcons/application.png");
		checkContentCreated(destDirPath+"/CDN/img/appIcons/application_add.png");
		checkContentCreated(destDirPath+"/CDN/img/appIcons/application_cascade.png");
		checkContentCreated(destDirPath+"/CDN/img/appIcons/application_delete.png");
		checkContentCreated(destDirPath+"/CDN/img/appIcons/application_double.png");
		checkContentCreated(destDirPath+"/CDN/img/appIcons/application_edit.png");
		checkContentCreated(destDirPath+"/CDN/img/calendarIcons/calendar.png");
		checkContentCreated(destDirPath+"/CDN/jawr_generator/css/jar/classpathResources/css/temp.css");
		checkContentCreated(destDirPath+"/CDN/js/global/jawr.js");
		checkContentCreated(destDirPath+"/CDN/js/global/module.js");
		checkContentCreated(destDirPath+"/CDN/js/index/index.js");
	}

	private void checkContentCreated(String filePath) {
		File file = new File(filePath);
		Assert.assertTrue("File '"+filePath+"' has not been created", file.exists());
		Assert.assertTrue("File '"+filePath+"'is empty", file.length() > 0);
	}
}
