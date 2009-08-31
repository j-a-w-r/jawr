/**
 * 
 */
package net.jawr.web.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.jawr.web.test.utils.Utils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * The base class for integration tests in Jawr.
 * This class is responsible of creating the Jetty server at the beginning of the tests defined 
 * in the current class.
 * The configuration files (Web.xml and jawr.properties) are updated before the start of the tests.
 *
 * To launch the tests, we use the maven command : mvn integration-test
 * The root folder of the application is defined in "target/jawr-integration-test".
 * 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractPageTest {

	/** The logger */
	private static Logger LOGGER = Logger.getLogger(AbstractPageTest.class);

	/** The web application context path */
	protected static final String CONTEXT_PATH = "/jawr-integration-test";

	/** The port */
	protected static int PORT = 8080;
	
	/** The application URL */
	protected static final String SERVER_URL = "http://localhost:"+PORT;

	/** The web application directory */
	private static final String WEBAPP_DIR = "target/jawr-integration-test";

	/** The flag indicating if we have configured the web application for all the tests of the current test case class */
	private static boolean webAppConfigInitialized = false;

	/** The Jetty server */
	private static Server SERVER;
	
	/** The web application context */
	private static WebAppContext WEB_APP_CONTEXT;
	
	/** The path of the web.xml in the web application (The file will be overwritten by the current test configuration) */
	private static String WEB_APP_WEB_XML_PATH = "";
	
	/** The path of the jawr.properties in the web application (The file will be overwritten by the current test configuration) */
	private static String WEB_APP_JAWR_CONFIG_PATH = "";

	/** The path of the current web.xml which is used for the configuration */
	private static String WEB_XML_SRC_PATH = "";
	
	/** The path of the current jawr.properties which is used for the configuration */
	private static String JAWR_CONFIG_SRC_PATH = "";
	
	/** The web client */
	protected WebClient webClient;
	
	/** The list of alerts collected */
	protected List<String> collectedAlerts;
	
	/** The HTML page */
	protected HtmlPage page;
	
	@BeforeClass
	public static void setInitFlag() throws IOException{
		webAppConfigInitialized = false;
		String webAppRootDir = new File(WEBAPP_DIR).getCanonicalFile().getAbsolutePath();
		WEB_APP_WEB_XML_PATH =  webAppRootDir+"/WEB-INF/web.xml";
		WEB_APP_JAWR_CONFIG_PATH = webAppRootDir+"/WEB-INF/classes/jawr.properties";
	}
	
	@Before
	public void setup() throws Exception {

		if (!webAppConfigInitialized) {
			initializeWebAppConfig();
		}
		
		webClient = createWebClient();
		collectedAlerts = new ArrayList<String>();
		webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
		page = webClient.getPage(getPageUrl());
		
	}
	
	/**
	 * Starts the web application.
	 * The web application root directory will be define in target/jawr-integration-test, the directory used for the war generation.
	 * 
	 * @throws Exception if an exception occurs
	 */
	public void startWebApplication() throws Exception {
		if(SERVER == null){
			SERVER = new Server(PORT);
			SERVER.setStopAtShutdown(true);
			WEB_APP_CONTEXT = new WebAppContext(WEBAPP_DIR, "/jawr-integration-test");
			WEB_APP_CONTEXT.setConfigurationClasses(new String[] {
					"org.mortbay.jetty.webapp.WebInfConfiguration",
					"org.mortbay.jetty.webapp.WebXmlConfiguration", });
		}
		
		// Create a new class loader to take in account the changes of the jawr config file in the WEB-INF/classes
		WebAppClassLoader webAppClassLoader = new WebAppClassLoader(WEB_APP_CONTEXT);
		WEB_APP_CONTEXT.setClassLoader(webAppClassLoader);
		
		SERVER.setHandler(WEB_APP_CONTEXT);
		
		if(SERVER.isStopped()){
			LOGGER.info("Start jetty server....");
				SERVER.start();
		}
		if(WEB_APP_CONTEXT.isStopped()){
			LOGGER.info("Start jetty webApp context....");
			WEB_APP_CONTEXT.start();
		}
		
	}

	/**
	 * Initialize the web application configuration for the tests
	 * @throws Exception if an exception occurs.
	 */
	public void initializeWebAppConfig() throws Exception {
		
		JawrTestConfigFiles annotationConfig = (JawrTestConfigFiles) getClass()
				.getAnnotation(JawrTestConfigFiles.class);

		String currentJawrConfigPath = annotationConfig.jawrConfig();
		boolean configChange = false;
		if(!AbstractPageTest.JAWR_CONFIG_SRC_PATH.equals(currentJawrConfigPath)){
			
			OutputStream outFile = new FileOutputStream(new File(WEB_APP_JAWR_CONFIG_PATH));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentJawrConfigPath), outFile);
			IOUtils.closeQuietly(outFile);
			AbstractPageTest.JAWR_CONFIG_SRC_PATH = currentJawrConfigPath;
			configChange = true;
		}
		
		String currentWebXmlPath = annotationConfig.webXml();
		if(!AbstractPageTest.WEB_XML_SRC_PATH.equals(currentWebXmlPath)){
			
			OutputStream outFile = new FileOutputStream(new File(WEB_APP_WEB_XML_PATH));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentWebXmlPath), outFile);
			IOUtils.closeQuietly(outFile);
			
			AbstractPageTest.WEB_XML_SRC_PATH = currentWebXmlPath;
			configChange = true;
		}
		
		// Starts the web application if there were changes in the configuration 
		if(configChange){
			startWebApplication();
		}
		
		webAppConfigInitialized = true;
	}

	/**
	 * Creates the web client
	 * 
	 * @return the web client
	 */
	protected WebClient createWebClient() {
		WebClient webClient = new WebClient();
		
		// Defines the accepted language for the web client.
		webClient.addRequestHeader("Accept-Language", getAcceptedLanguage());
		return webClient;
	}

	/**
	 * Resets the test configuration. 
	 * @throws Exception if an exception occurs
	 */
	@AfterClass
	public static void resetTestConfiguration() throws Exception {
		
		webAppConfigInitialized = false;
		// Stop the web application context at the end of the tests associated to the current class.
		LOGGER.info("Stop jetty webApp context....");
		WEB_APP_CONTEXT.stop();
	}
	
	/**
	 * Returns the locale used for the test
	 * 
	 * @return the locale used for the test
	 */
	public String getAcceptedLanguage() {
		return "en-us";
	}

	/**
	 * Returns the url of the page to test
	 * 
	 * @return the url of the page to test
	 */
	protected abstract String getPageUrl();

	/**
	 * Assert that the content of the file name equals to the response of the page.
	 * 
	 * @param fileName the file name
	 * @param page the web page
	 * @throws Exception if an exception occurs.
	 */
	protected void assertContentEquals(String fileName, Page page)
			throws Exception {
		Utils.assertContentEquals(getClass(), fileName, page);
	}

	/**
	 * Returns the list of HTML script tags which have an src attribute.
	 * @return the list of HTML script tag.
	 */
	@SuppressWarnings("unchecked")
	protected List<HtmlScript> getJsScriptTags() {
		return (List<HtmlScript>) page.getByXPath("html/head/script[@src]");
	}

	/**
	 * Returns the list of HTML link tags.
	 * @return the list of HTML script tags.
	 */
	@SuppressWarnings("unchecked")
	protected List<HtmlLink> getHtmlLinkTags() {
		return (List<HtmlLink>) page.getByXPath("html/head/link");
	}
	
	/**
	 * Returns the list of HTML link tags.
	 * @return the list of HTML script tags.
	 */
	@SuppressWarnings("unchecked")
	protected List<HtmlImage> getHtmlImageTags() {
		return (List<HtmlImage>) page.getByXPath("//img");
	}
	
	/**
	 * Returns the list of HTML link tags.
	 * @return the list of HTML script tags.
	 */
	@SuppressWarnings("unchecked")
	protected List<HtmlImageInput> getHtmlImageInputTags() {
		return (List<HtmlImageInput>) page.getByXPath("//input[@type='image']");
	}
	
	/**
	 * Call the webserver to retrieve the javascript page associated to the Html script object.
	 * 
	 * @param script the Html script
	 * @return the javascript page
	 * @throws IOException if an IOException occurs
	 * @throws MalformedURLException if a MalformedURLException occurs
	 */
	protected JavaScriptPage getJavascriptPage(final HtmlScript script)
			throws IOException, MalformedURLException {
		return webClient.getPage(SERVER_URL + script.getSrcAttribute());
	}

	/**
	 * Call the webserver to retrieve the css page associated to the Html link object.
	 * 
	 * @param css the Html link
	 * @return the css page
	 * @throws IOException if an IOException occurs
	 * @throws MalformedURLException if a MalformedURLException occurs
	 */
	protected TextPage getCssPage(final HtmlLink css) throws IOException,
			MalformedURLException {
		return webClient.getPage(SERVER_URL + css.getHrefAttribute());
	}

}
