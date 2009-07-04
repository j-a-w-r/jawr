/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.net.jawr.web.resource.bundle.renderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.FileSystemResourceHandler;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;
import test.net.jawr.web.resource.bundle.PredefinedBundlesHandlerUtil;
import test.net.jawr.web.resource.bundle.handler.ResourceHandlerBasedTest;
/**
 *
 * @author jhernandez
 */
public class BundleLinkRendererTest  extends ResourceHandlerBasedTest{
	private static final String ROOT_TESTDIR = "/bundleLinkRenderer/";
	private static final String JS_BASEDIR = "js";
	private static final String JS_CTX_PATH = "/ctxPathJs";
	private static final String CSS_BASEDIR = "css/";
	private static final String CSS_CTX_PATH = "/ctxPathCss";
	
	private static final String JS_PRE_TAG = "<script type=\"text/javascript\" src=\"";
    private static final String JS_POST_TAG = "\" ></script>";
	
	private static final String CSS_PRE_TAG = "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"";
	private static final String CSS_PRE_PR_TAG = "<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"";
    private static final String CSS_POST_TAG = "\" />";
	
	private CSSHTMLBundleLinkRenderer cssRenderer;
	private CSSHTMLBundleLinkRenderer cssPrintRenderer;
	private JavascriptHTMLBundleLinkRenderer jsRenderer;
	private JawrConfig jawrConfig;
	private boolean globalBundleAdded = false;
	
	public BundleLinkRendererTest(){
	     
	    Charset charsetUtf = Charset.forName("UTF-8"); 
			
	    FileSystemResourceHandler rsHandler = createResourceHandler(ROOT_TESTDIR,charsetUtf);
	    jawrConfig = new JawrConfig(new Properties());
	    jawrConfig.setCharsetName("UTF-8");
	    jawrConfig.setServletMapping("/srvMapping");
	    ResourceBundlesHandler jsHandler = null;
	    ResourceBundlesHandler cssHandler = null;
	    try {
	    	jsHandler = PredefinedBundlesHandlerUtil.buildSimpleBundles(rsHandler,JS_BASEDIR,"js", jawrConfig);
	    	cssHandler = PredefinedBundlesHandlerUtil.buildSimpleBundles(rsHandler,CSS_BASEDIR,"css", jawrConfig);
		} catch (DuplicateBundlePathException e) {
			// 
			throw new RuntimeException(e);
		}
	    cssRenderer = new CSSHTMLBundleLinkRenderer(cssHandler,true,null);
	    cssPrintRenderer  = new CSSHTMLBundleLinkRenderer(cssHandler,true,"print");
	    jsRenderer = new JavascriptHTMLBundleLinkRenderer(jsHandler,true);
	}
	
	private String renderToString(BundleRenderer renderer, String path, String ctxPath,Set included, boolean gZip, boolean isSslRequest){
		ByteArrayOutputStream baOs = new ByteArrayOutputStream();
	    WritableByteChannel wrChannel = Channels.newChannel(baOs);
	    Writer writer = Channels.newWriter(wrChannel, jawrConfig.getResourceCharset().name());
	    String ret = null;
	    try {
			renderer.renderBundleLinks(path,ctxPath,null,included,globalBundleAdded, gZip, isSslRequest, writer);
		    writer.close();
		    ret = baOs.toString(jawrConfig.getResourceCharset().name());
		    globalBundleAdded = true;
		} catch (IOException e) {
			fail("Exception rendering tags:" + e.getMessage());
		}
	    return ret;
	}
	
	public void testWriteCSSBundleLinks()
	{
		jawrConfig.setDebugModeOn(false);
		globalBundleAdded = false;
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(cssRenderer,"/css/lib/lib.css", CSS_CTX_PATH, includedBundles, false, false);
		
		assertNotSame("No css tag written ", "", result.trim());
			
		String libTag1 = CSS_PRE_TAG + "/ctxPathCss/srvMapping/";
		String libTag2 = "/library.css" + CSS_POST_TAG;
		String globalTag1 = CSS_PRE_TAG + "/ctxPathCss/srvMapping/";
		String globalTag2 = "/global.css" + CSS_POST_TAG;
		String debOffTag1 = CSS_PRE_TAG + "/ctxPathCss/srvMapping/";
		String debOffTag2 = "/debugOff.css" + CSS_POST_TAG;
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(libTag1));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith(libTag2));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(globalTag1));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith(globalTag2));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(debOffTag1));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith(debOffTag2));
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(cssRenderer,"/css/lib/lib.css", JS_CTX_PATH, includedBundles, false, false);
		assertEquals("Tags were repeated","", result.trim());
		
		includedBundles = new HashSet();
		globalBundleAdded = false;
		result = renderToString(cssPrintRenderer,"/css/lib/lib.css", CSS_CTX_PATH, includedBundles, false, false);

		assertNotSame("No css tag written ", "", result.trim());
			
		String libPrTag1 = CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/";
		String libPrTag2 = "library.css" + CSS_POST_TAG;
		
		String globaPrlTag1 = CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/";
		String globaPrlTag2 = "global.css" + CSS_POST_TAG;
		
		String debOffPrTag1 = CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/";
		String debOffPrTag2 = "debugOff.css" + CSS_POST_TAG;
		
		tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(libPrTag1));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith(libPrTag2));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(globaPrlTag1));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith(globaPrlTag2));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(debOffPrTag1));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith(debOffPrTag2));
		
	}
	
	public void testWriteJSBundleLinks()
	{
		jawrConfig.setDebugModeOn(false);
		globalBundleAdded = false;
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, false);
		
		assertNotSame("No script tag written ", "", result.trim());
			
		String libTag = JS_PRE_TAG + "/ctxPathJs/srvMapping/libPfx/library.js" + JS_POST_TAG;
		String globalTag = JS_PRE_TAG + "/ctxPathJs/srvMapping/globalPfx/global.js" + JS_POST_TAG;
		String debOffTag = JS_PRE_TAG + "/ctxPathJs/srvMapping/pfx/debugOff.js" + JS_POST_TAG;
		String oneTag = JS_PRE_TAG + "/ctxPathJs/srvMapping/pfx/js/one.js" + JS_POST_TAG;
		StringTokenizer tk = new StringTokenizer(result,"\n");
		
		
		assertEquals("Invalid number of tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1", assertStartEndSimmilarity(globalTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3", assertStartEndSimmilarity(oneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, false);
		assertTrue("Tags were repeated",assertStartEndSimmilarity(oneTag,"pfx",result.trim()));
		

		// Test gzipped link creation
		String libZTag = JS_PRE_TAG+ "/ctxPathJs/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "libPfx/library.js" + JS_POST_TAG;
		String globalZTag = JS_PRE_TAG + "/ctxPathJs/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "globalPfx/global.js" + JS_POST_TAG;
		String debOffZTag = JS_PRE_TAG + "/ctxPathJs/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/debugOff.js" + JS_POST_TAG;
		String debOffoneTag = JS_PRE_TAG + "/ctxPathJs/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/js/one.js" + JS_POST_TAG;
		includedBundles = new HashSet();
		globalBundleAdded = false;
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, false);
		assertNotSame("No gzip script tags written ", "", result.trim());
		tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of gzip script tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libZTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1",assertStartEndSimmilarity(globalZTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffZTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3",assertStartEndSimmilarity(debOffoneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, false);
		String one2Tag = JS_PRE_TAG + "/ctxPathJs/srvMapping"+BundleRenderer.GZIP_PATH_PREFIX + "pfx/js/one.js" + JS_POST_TAG;
		assertTrue("Gzip were not repeated",assertStartEndSimmilarity(one2Tag,"pfx",result.trim()));
	}
	
	private boolean assertStartEndSimmilarity(String toCompare, String splitter, String toMatch) {
		String[] parts = toCompare.split(splitter);
		return toMatch.startsWith(parts[0]) && toMatch.endsWith(parts[1]);
	}
	
	public void testWriteJSDebugLinks() 
	{
		jawrConfig.setDebugModeOn(true);
		globalBundleAdded = false;
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, false);
	    // Reusing the set, we test that no repeats are allowed. 
	    String repeated = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, false);
	    
	    assertNotSame("No script tag written ", "", result.trim());

	    Pattern comment = Pattern.compile("<script type=\"text/javascript\">/.*/</script>");
	   
		StringTokenizer tk = new StringTokenizer(result,"\n");
		
		// First item should be a comment		
		assertTrue("comment expected",comment.matcher(tk.nextToken()).matches());
		
	    String regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/lib/prototype/protoype.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /lib/protoype.js", Pattern.matches(regX, tk.nextToken()));
		assertFalse("Repeated already included tag ", Pattern.matches(regX, repeated));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/lib/lib2.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /lib/lib2.js", Pattern.matches(regX, tk.nextToken()));
		assertFalse("Repeated already included tag ", Pattern.matches(regX, repeated));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/lib/scriptaculous/scriptaculous.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /lib/scriptaculous/scriptaculous.js", Pattern.matches(regX, tk.nextToken()));
		assertFalse("Repeated already included tag ", Pattern.matches(regX, repeated));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/global/global2.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /global/global2.js", Pattern.matches(regX, tk.nextToken()));
		assertFalse("Repeated already included tag ", Pattern.matches(regX, repeated));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/global/global.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /global/global.js", Pattern.matches(regX, tk.nextToken()));
		assertFalse("Repeated already included tag ", Pattern.matches(regX, repeated));
		
		// Even though we set dbug to off, the handler was initialized in non-debug mode, thus we still get the debugoff.js file. 
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/debug/off/debugOff.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /debug/off/debugOff.js", Pattern.matches(regX, tk.nextToken()));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/one/one.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("comment expected",comment.matcher(tk.nextToken()).matches());
		assertTrue("comment expected",comment.matcher(tk.nextToken()).matches());
		assertTrue("No match for /js/one/one.js", Pattern.matches(regX, tk.nextToken()));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/one/one2.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /js/one/one2.js", Pattern.matches(regX, tk.nextToken()));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/one/sub/one3.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /js/one/sub/one3.js", Pattern.matches(regX, tk.nextToken()));
		
		assertTrue("comment expected",comment.matcher(tk.nextToken()).matches());
		assertFalse("Repeated already included tag ", Pattern.matches(regX, repeated));
		
	}

	public void testWriteJSBundleLinksWithHttpCdn()
	{
		jawrConfig.setDebugModeOn(false);
		globalBundleAdded = false;
		String contextPathOverride = "http://mydomain.com/aPath";
		String contextPathSslOverride = "http://mydomain.com/aPath";
		jawrConfig.setContextPathOverride(contextPathOverride+"/");
		jawrConfig.setContextPathSslOverride(contextPathSslOverride+"/");
		
		boolean isSslRequest = false;
		
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    
	    String result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, isSslRequest);
		
		assertNotSame("No script tag written ", "", result.trim());
			
		String libTag = JS_PRE_TAG + contextPathOverride+"/srvMapping/libPfx/library.js" + JS_POST_TAG;
		String globalTag = JS_PRE_TAG + contextPathOverride+"/srvMapping/globalPfx/global.js" + JS_POST_TAG;
		String debOffTag = JS_PRE_TAG + contextPathOverride+"/srvMapping/pfx/debugOff.js" + JS_POST_TAG;
		String oneTag = JS_PRE_TAG + contextPathOverride+"/srvMapping/pfx/js/one.js" + JS_POST_TAG;
		StringTokenizer tk = new StringTokenizer(result,"\n");
		
		
		assertEquals("Invalid number of tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1", assertStartEndSimmilarity(globalTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3", assertStartEndSimmilarity(oneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, isSslRequest);
		assertTrue("Tags were not repeated",assertStartEndSimmilarity(oneTag,"pfx",result.trim()));
		

		// Test gzipped link creation
		String libZTag = JS_PRE_TAG+ contextPathOverride+"/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "libPfx/library.js" + JS_POST_TAG;
		String globalZTag = JS_PRE_TAG + contextPathOverride+"/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "globalPfx/global.js" + JS_POST_TAG;
		String debOffZTag = JS_PRE_TAG + contextPathOverride+"/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/debugOff.js" + JS_POST_TAG;
		String debOffoneTag = JS_PRE_TAG + contextPathOverride+"/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/js/one.js" + JS_POST_TAG;
		includedBundles = new HashSet();
		globalBundleAdded = false;
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, isSslRequest);
		assertNotSame("No gzip script tags written ", "", result.trim());
		tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of gzip script tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libZTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1",assertStartEndSimmilarity(globalZTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffZTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3",assertStartEndSimmilarity(debOffoneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, isSslRequest);
		assertTrue("Gzip tags were not repeated",assertStartEndSimmilarity(debOffoneTag,"pfx",result.trim()));
	}
	
	public void testWriteJSBundleLinksWithHttpsCdn()
	{
		jawrConfig.setDebugModeOn(false);
		globalBundleAdded = false;
		String contextPathOverride = "http://mydomain.com/aPath";
		String contextPathSslOverride = "https://mydomain.com/aPath";
		jawrConfig.setContextPathOverride(contextPathOverride+"/");
		jawrConfig.setContextPathSslOverride(contextPathSslOverride+"/");
		
		boolean isSslRequest = true;
		
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, isSslRequest);
		
		assertNotSame("No script tag written ", "", result.trim());
			
		String libTag = JS_PRE_TAG + contextPathSslOverride+"/srvMapping/libPfx/library.js" + JS_POST_TAG;
		String globalTag = JS_PRE_TAG + contextPathSslOverride+"/srvMapping/globalPfx/global.js" + JS_POST_TAG;
		String debOffTag = JS_PRE_TAG + contextPathSslOverride+"/srvMapping/pfx/debugOff.js" + JS_POST_TAG;
		String oneTag = JS_PRE_TAG + contextPathSslOverride+"/srvMapping/pfx/js/one.js" + JS_POST_TAG;
		StringTokenizer tk = new StringTokenizer(result,"\n");
		
		
		assertEquals("Invalid number of tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1", assertStartEndSimmilarity(globalTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3", assertStartEndSimmilarity(oneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, isSslRequest);
		assertTrue("Tags were not repeated", assertStartEndSimmilarity(oneTag,"pfx",result.trim()));
		

		// Test gzipped link creation
		String libZTag = JS_PRE_TAG+ contextPathSslOverride+"/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "libPfx/library.js" + JS_POST_TAG;
		String globalZTag = JS_PRE_TAG + contextPathSslOverride+"/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "globalPfx/global.js" + JS_POST_TAG;
		String debOffZTag = JS_PRE_TAG + contextPathSslOverride+"/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/debugOff.js" + JS_POST_TAG;
		String debOffoneTag = JS_PRE_TAG + contextPathSslOverride+"/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/js/one.js" + JS_POST_TAG;
		includedBundles = new HashSet();
		globalBundleAdded = false;
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, isSslRequest);
		assertNotSame("No gzip script tags written ", "", result.trim());
		tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of gzip script tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libZTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1",assertStartEndSimmilarity(globalZTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffZTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3",assertStartEndSimmilarity(debOffoneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, isSslRequest);
		assertTrue("Gzip tags were not repeated",assertStartEndSimmilarity(debOffoneTag,"pfx",result.trim()));
	}
	
	public void testWriteJSBundleLinksWithHttpsRelativePath()
	{
		jawrConfig.setDebugModeOn(false);
		globalBundleAdded = false;
		String contextPathOverride = "http://mydomain.com/aPath";
		String contextPathSslOverride = "";
		jawrConfig.setContextPathOverride(contextPathOverride+"/");
		jawrConfig.setContextPathSslOverride(contextPathSslOverride);
		
		boolean isSslRequest = true;
		
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, isSslRequest);
		
		assertNotSame("No script tag written ", "", result.trim());
			
		String libTag = JS_PRE_TAG + "srvMapping/libPfx/library.js" + JS_POST_TAG;
		String globalTag = JS_PRE_TAG + "srvMapping/globalPfx/global.js" + JS_POST_TAG;
		String debOffTag = JS_PRE_TAG + "srvMapping/pfx/debugOff.js" + JS_POST_TAG;
		String oneTag = JS_PRE_TAG + "srvMapping/pfx/js/one.js" + JS_POST_TAG;
		StringTokenizer tk = new StringTokenizer(result,"\n");
		
		
		assertEquals("Invalid number of tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1", assertStartEndSimmilarity(globalTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3", assertStartEndSimmilarity(oneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, isSslRequest);
		assertTrue("Tags were not repeated",assertStartEndSimmilarity(oneTag,"pfx",result.trim()));
		

		// Test gzipped link creation
		String libZTag = JS_PRE_TAG+ "srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "libPfx/library.js" + JS_POST_TAG;
		String globalZTag = JS_PRE_TAG + "srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "globalPfx/global.js" + JS_POST_TAG;
		String debOffZTag = JS_PRE_TAG + "srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/debugOff.js" + JS_POST_TAG;
		String debOffoneTag = JS_PRE_TAG + "srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/js/one.js" + JS_POST_TAG;
		includedBundles = new HashSet();
		globalBundleAdded = false;
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, isSslRequest);
		assertNotSame("No gzip script tags written ", "", result.trim());
		tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of gzip script tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libZTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1",assertStartEndSimmilarity(globalZTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffZTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3",assertStartEndSimmilarity(debOffoneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, isSslRequest);
		assertTrue("Gzip tags were not repeated",assertStartEndSimmilarity(debOffoneTag,"pfx",result.trim()));
	}
	
	public void testWriteJSBundleLinksWithRelativePath()
	{
		jawrConfig.setDebugModeOn(false);
		globalBundleAdded = false;
		jawrConfig.setContextPathOverride("");
		jawrConfig.setContextPathSslOverride("https://mydomain.com/aPath/");
		
		boolean isSslRequest = false;
		
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, isSslRequest);
		
		assertNotSame("No script tag written ", "", result.trim());
			
		String libTag = JS_PRE_TAG + "srvMapping/libPfx/library.js" + JS_POST_TAG;
		String globalTag = JS_PRE_TAG + "srvMapping/globalPfx/global.js" + JS_POST_TAG;
		String debOffTag = JS_PRE_TAG + "srvMapping/pfx/debugOff.js" + JS_POST_TAG;
		String oneTag = JS_PRE_TAG + "srvMapping/pfx/js/one.js" + JS_POST_TAG;
		StringTokenizer tk = new StringTokenizer(result,"\n");
		
		
		assertEquals("Invalid number of tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1", assertStartEndSimmilarity(globalTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3", assertStartEndSimmilarity(oneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false, isSslRequest);
		assertTrue("Tags were not repeated",assertStartEndSimmilarity(oneTag,"pfx",result.trim()));
		

		// Test gzipped link creation
		String libZTag = JS_PRE_TAG+ "srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "libPfx/library.js" + JS_POST_TAG;
		String globalZTag = JS_PRE_TAG + "srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "globalPfx/global.js" + JS_POST_TAG;
		String debOffZTag = JS_PRE_TAG + "srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/debugOff.js" + JS_POST_TAG;
		String debOffoneTag = JS_PRE_TAG + "srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/js/one.js" + JS_POST_TAG;
		includedBundles = new HashSet();
		globalBundleAdded = false;
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, isSslRequest);
		assertNotSame("No gzip script tags written ", "", result.trim());
		tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of gzip script tags written. ",4, tk.countTokens());
		assertTrue("Unexpected tag added at position 0", assertStartEndSimmilarity(libZTag,"libPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 1",assertStartEndSimmilarity(globalZTag,"globalPfx",tk.nextToken()));
		assertTrue("Unexpected tag added at position 2",assertStartEndSimmilarity(debOffZTag,"pfx",tk.nextToken()) );
		assertTrue("Unexpected tag added at position 3",assertStartEndSimmilarity(debOffoneTag,"pfx",tk.nextToken()) );
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true, isSslRequest);
		assertTrue("Tags were not repeated",assertStartEndSimmilarity(debOffoneTag,"pfx",result.trim()));
	}
}
