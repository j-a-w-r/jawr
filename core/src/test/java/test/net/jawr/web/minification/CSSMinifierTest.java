package test.net.jawr.web.minification;

import java.io.File;

import junit.framework.TestCase;
import net.jawr.web.minification.CSSMinifier;
import test.net.jawr.web.FileUtils;

public class CSSMinifierTest extends TestCase {
	private StringBuffer source;
	private StringBuffer expected;
	private static final String TEST_FOLDER = "/cssminifier";
	
	protected void setUp() throws Exception {
		source = FileUtils.readFile(new File(FileUtils.getClasspathRootDir() + TEST_FOLDER + "/source.css"));
		expected = FileUtils.readFile(new File(FileUtils.getClasspathRootDir() +  TEST_FOLDER +"/expected.css"));
	}
	
	public void testMinifyCSS() {
		CSSMinifier minifier = new CSSMinifier();
		StringBuffer actual = minifier.minifyCSS(source);
		assertEquals("Error in minifier",expected.toString(), actual.toString());
	}
	
	
	
}
