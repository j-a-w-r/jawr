package test.net.jawr.web.minification;

import java.io.File;

import junit.framework.TestCase;
import net.jawr.web.minification.CSSMinifier;
import net.jawr.web.resource.bundle.postprocess.impl.yui.YUICSSCompressor;
import test.net.jawr.web.FileUtils;

public class CSSMinifierTest extends TestCase {
	private String source;
	private String expected;
	private static final String TEST_FOLDER = "/cssminifier";
	
	protected void setUp() throws Exception {
		source = FileUtils.readFile(new File(FileUtils.getClasspathRootDir() + TEST_FOLDER + "/source.css"));
		expected = FileUtils.readFile(new File(FileUtils.getClasspathRootDir() +  TEST_FOLDER +"/expected.css"));
	}
	
	public void testMinifyCSS() {
		CSSMinifier minifier = new CSSMinifier();
		StringBuffer actual = minifier.minifyCSS(new StringBuffer(source));
		assertEquals("Error in minifier",expected.toString(), actual.toString());
	}
	
	public void testMinifyCSSMultiLine() {
		CSSMinifier minifier = new CSSMinifier();
		//YUICSSCompressor compressor = new YUICSSCompressor();
		StringBuffer data = new StringBuffer(".some-class { \n" +
				"  background: transparent\n" +
				"url(image/path);\n" +
				"}");
		StringBuffer actual = minifier.minifyCSS(data);
		//StringBuffer actual = compressor.postProcessBundle(null, data);
		StringBuffer result = new StringBuffer(".some-class{background:transparent url(image/path);}");
		
		assertEquals("Error in minifier",result.toString(), actual.toString());
	}
	
}
