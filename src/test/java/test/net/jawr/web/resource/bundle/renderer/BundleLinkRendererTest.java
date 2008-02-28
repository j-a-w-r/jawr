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
	private static final String GLOBAL_PFX = "globalPfx";
	private static final String LIB_PFX = "libPfx";
	
	private static final String JS_PRE_TAG = "<script type=\"text/javascript\" src=\"";
    private static final String JS_POST_TAG = "\" ></script>";
	
	private static final String CSS_PRE_TAG = "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"";
	private static final String CSS_PRE_PR_TAG = "<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"";
    private static final String CSS_POST_TAG = "\"></link>";
	
	private CSSHTMLBundleLinkRenderer cssRenderer;
	private CSSHTMLBundleLinkRenderer cssPrintRenderer;
	private JavascriptHTMLBundleLinkRenderer jsRenderer;
	private JawrConfig jeesConfig;
	
	public BundleLinkRendererTest(){
	     
	    Charset charsetUtf = Charset.forName("UTF-8"); 
			
	    FileSystemResourceHandler rsHandler = createResourceHandler(ROOT_TESTDIR,charsetUtf);
	    jeesConfig = new JawrConfig(new Properties());
	    jeesConfig.setCharsetName("UTF-8");
	    jeesConfig.setServletMapping("/srvMapping");
	  //  jeesConfig.setURLPrefix("pfx");
	    ResourceBundlesHandler jsHandler = null;
	    ResourceBundlesHandler cssHandler = null;
	    try {
	    	jsHandler = PredefinedBundlesHandlerUtil.buildSimpleBundles(rsHandler,JS_BASEDIR,"js", jeesConfig,"pfx",GLOBAL_PFX,LIB_PFX);
	    	cssHandler = PredefinedBundlesHandlerUtil.buildSimpleBundles(rsHandler,CSS_BASEDIR,"css", jeesConfig,"pfx",GLOBAL_PFX,LIB_PFX);
		} catch (DuplicateBundlePathException e) {
			// 
			throw new RuntimeException(e);
		}
	    cssRenderer = new CSSHTMLBundleLinkRenderer(cssHandler,true,null);
	    cssPrintRenderer  = new CSSHTMLBundleLinkRenderer(cssHandler,true,"print");
	    jsRenderer = new JavascriptHTMLBundleLinkRenderer(jsHandler,true);
	}
	
	private String renderToString(BundleRenderer renderer, String path, String ctxPath,Set included, boolean gZip){
		ByteArrayOutputStream baOs = new ByteArrayOutputStream();
	    WritableByteChannel wrChannel = Channels.newChannel(baOs);
	    Writer writer = Channels.newWriter(wrChannel, jeesConfig.getResourceCharset().name());
	    String ret = null;
	    try {
			renderer.renderBundleLinks(path,ctxPath,included,gZip,writer);
		    writer.close();
		    ret = baOs.toString(jeesConfig.getResourceCharset().name());
		} catch (IOException e) {
			fail("Exception rendering tags:" + e.getMessage());
		}
	    return ret;
	}
	
	public void testWriteCCBundleLinks()
	{
		jeesConfig.setDebugModeOn(false);
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(cssRenderer,"/js/one/one2.css", CSS_CTX_PATH, includedBundles, false);
		
		assertNotSame("No css tag written ", "", result.trim());
			
		String libTag = CSS_PRE_TAG + "/ctxPathCss/srvMapping/libPfx/library.css" + CSS_POST_TAG;
		String globalTag = CSS_PRE_TAG + "/ctxPathCss/srvMapping/globalPfx/global.css" + CSS_POST_TAG;
		String debOffTag = CSS_PRE_TAG + "/ctxPathCss/srvMapping/pfx/debugOff.css" + CSS_POST_TAG;
		StringTokenizer tk = new StringTokenizer(result,"\n");
		
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		assertEquals("Unexpected tag added at position 0", libTag,tk.nextElement());
		assertEquals("Unexpected tag added at position 1", globalTag,tk.nextElement());
		assertEquals("Unexpected tag added at position 2", debOffTag,tk.nextElement());
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(cssRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false);
		assertEquals("Tags were repeated","", result.trim());
		
		includedBundles = new HashSet();
		result = renderToString(cssPrintRenderer,"/js/one/one2.css", CSS_CTX_PATH, includedBundles, false);

		assertNotSame("No css tag written ", "", result.trim());
			
		String libPrTag = CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/libPfx/library.css" + CSS_POST_TAG;
		String globaPrlTag = CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/globalPfx/global.css" + CSS_POST_TAG;
		String debOffPrTag = CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/pfx/debugOff.css" + CSS_POST_TAG;
		tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		assertEquals("Unexpected tag added at position 0", libPrTag,tk.nextElement());
		assertEquals("Unexpected tag added at position 1", globaPrlTag,tk.nextElement());
		assertEquals("Unexpected tag added at position 2", debOffPrTag,tk.nextElement());
		
	}
	
	public void testWriteJSBundleLinks()
	{
		jeesConfig.setDebugModeOn(false);
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false);
		
		assertNotSame("No script tag written ", "", result.trim());
			
		String libTag = JS_PRE_TAG + "/ctxPathJs/srvMapping/libPfx/library.js" + JS_POST_TAG;
		String globalTag = JS_PRE_TAG + "/ctxPathJs/srvMapping/globalPfx/global.js" + JS_POST_TAG;
		String debOffTag = JS_PRE_TAG + "/ctxPathJs/srvMapping/pfx/debugOff.js" + JS_POST_TAG;
		String oneTag = JS_PRE_TAG + "/ctxPathJs/srvMapping/pfx/js/one.js" + JS_POST_TAG;
		StringTokenizer tk = new StringTokenizer(result,"\n");
		
		
		assertEquals("Invalid number of tags written. ",4, tk.countTokens());
		assertEquals("Unexpected tag added at position 0", libTag,tk.nextElement());
		assertEquals("Unexpected tag added at position 1", globalTag,tk.nextElement());
		assertEquals("Unexpected tag added at position 2", debOffTag,tk.nextElement());
		assertEquals("Unexpected tag added at position 3", oneTag,tk.nextElement());
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false);
		assertEquals("Tags were repeated","", result.trim());
		

		// Test gzipped link creation
		String libZTag = JS_PRE_TAG+ "/ctxPathJs/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "libPfx/library.js" + JS_POST_TAG;
		String globalZTag = JS_PRE_TAG + "/ctxPathJs/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "globalPfx/global.js" + JS_POST_TAG;
		String debOffZTag = JS_PRE_TAG + "/ctxPathJs/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/debugOff.js" + JS_POST_TAG;
		String debOffoneTag = JS_PRE_TAG + "/ctxPathJs/srvMapping" + BundleRenderer.GZIP_PATH_PREFIX + "pfx/js/one.js" + JS_POST_TAG;
		includedBundles = new HashSet();
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true);
		assertNotSame("No gzip script tags written ", "", result.trim());
		tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of gzip script tags written. ",4, tk.countTokens());
		assertEquals("Unexpected tag added at position 0", libZTag,tk.nextToken());
		assertEquals("Unexpected tag added at position 1", globalZTag,tk.nextToken());
		assertEquals("Unexpected tag added at position 2", debOffZTag,tk.nextToken());
		assertEquals("Unexpected tag added at position 3", debOffoneTag,tk.nextToken());
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, true);
		assertEquals("Gzip tags were repeated","", result.trim());
	}
	
	public void testWriteJSDebugLinks() 
	{
		jeesConfig.setDebugModeOn(true);
		// Test regular link creation
	    Set includedBundles = new HashSet();
	    String result = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false);
	    // Reusing the set, we test that no repeats are allowed. 
	    String repeated = renderToString(jsRenderer,"/js/one/one2.js", JS_CTX_PATH, includedBundles, false);
	    
	    assertNotSame("No script tag written ", "", result.trim());

	    Pattern comment = Pattern.compile("<!--.*-->");
	   
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
		assertTrue("No match for /js/one/one.js", Pattern.matches(regX, tk.nextToken()));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/one/one2.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /js/one/one2.js", Pattern.matches(regX, tk.nextToken()));
		
		regX = JS_PRE_TAG + "/ctxPathJs/srvMapping/js/one/sub/one3.js\\?d=\\d*" + JS_POST_TAG;
		assertTrue("No match for /js/one/sub/one3.js", Pattern.matches(regX, tk.nextToken()));
		
		assertTrue("comment expected",comment.matcher(tk.nextToken()).matches());
		assertFalse("Repeated already included tag ", Pattern.matches(regX, repeated));
		
		
	}

	
}
