package test.net.jawr.web.resource.bundle.factory.util;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

/**
 * Test case class for PathNormalizer utility class
 * @author Ibrahim Chaehoi
 */
public class PathNormalizerTestCase extends TestCase {

	private static final String SEP = "/";

	public void testJoinDomainToPath(){
		
		assertEquals("https://mydomain.com/myContent/css/folder/myStyle.css",PathNormalizer.joinDomainToPath("https://mydomain.com/myContent","/css/folder/myStyle.css"));
		assertEquals("https://mydomain.com/myContent/css/folder/myStyle.css",PathNormalizer.joinPaths("https://mydomain.com/myContent","/css/folder/myStyle.css"));
		
	}
	
	public void testJoinPaths(){
		assertEquals("/myContent/css/folder/myStyle.css",PathNormalizer.joinPaths("/myContent","/css/folder/myStyle.css"));
	}
	
	public void testGetParentPath() {

		assertEquals("", PathNormalizer.getParentPath(null));
		assertEquals("/usr/", PathNormalizer.getParentPath("/usr/local/"));
		assertEquals("/usr/local/bin/", PathNormalizer.getParentPath("/usr/local/bin/java.sh"));
		assertEquals("/", PathNormalizer.getParentPath("/"));

	}

	public void testGetRelativePath() throws Exception {
		assertEquals(PathNormalizer.getRelativePath(null, null), "");
		assertEquals(PathNormalizer.getRelativePath(null, "/usr/local/java/bin"), "");
		assertEquals(PathNormalizer.getRelativePath("/usr/local/", null), "");
		assertEquals(PathNormalizer.getRelativePath("/usr/local/", "/usr/local/java/bin"), "..");
		assertEquals(PathNormalizer.getRelativePath("/usr/local/", "/usr/local/java/bin/java.sh"), "../..");
		assertEquals(PathNormalizer.getRelativePath("/usr/local/java/bin/java.sh", "/usr/local/"), "");
	}

	// -----------------------------------------------------------------------
	public void testConcat() {
		assertEquals(null, PathNormalizer.concatWebPath("", null));
		assertEquals(null, PathNormalizer.concatWebPath(null, null));
		assertEquals(null, PathNormalizer.concatWebPath(null, ""));
		assertEquals(null, PathNormalizer.concatWebPath(null, "a"));
		assertEquals(SEP + "a", PathNormalizer.concatWebPath(null, "/a"));

		assertEquals("/css/folder/subfolder/icons/img.png", PathNormalizer.concatWebPath("/css/folder/subfolder/", "icons/img.png"));
		assertEquals("/css/folder/subfolder/icons/img.png", PathNormalizer.concatWebPath("/css/folder/subfolder/style.css", "icons/img.png"));
		assertEquals("/css/icons/img.png", PathNormalizer.concatWebPath("/css/folder/", "../icons/img.png"));
		assertEquals("/css/icons/img.png", PathNormalizer.concatWebPath("/css/folder/style.css", "../icons/img.png"));

		assertEquals("f" + SEP, PathNormalizer.concatWebPath("", "f/"));
		assertEquals("f", PathNormalizer.concatWebPath("", "f"));
		assertEquals("a" + SEP + "f" + SEP, PathNormalizer.concatWebPath("a/", "f/"));
		assertEquals("a" + SEP + "f", PathNormalizer.concatWebPath("a", "f"));
		assertEquals("a" + SEP + "b" + SEP + "f" + SEP, PathNormalizer.concatWebPath("a/b/", "f/"));

		assertEquals("a" + SEP + "f" + SEP, PathNormalizer.concatWebPath("a/b/", "../f/"));
		assertEquals("f", PathNormalizer.concatWebPath("a/b", "../f"));
		assertEquals("a" + SEP + "c" + SEP + "g" + SEP, PathNormalizer.concatWebPath("a/b/../c/", "f/../g/"));
		assertEquals("a" + SEP + "g", PathNormalizer.concatWebPath("a/b/../c", "f/../g"));

		assertEquals("a" + SEP + "f", PathNormalizer.concatWebPath("a/c.txt", "f"));

		assertEquals(SEP + "f" + SEP, PathNormalizer.concatWebPath("", "/f/"));
		assertEquals(SEP + "f", PathNormalizer.concatWebPath("", "/f"));
		assertEquals("a" + SEP + "f" + SEP, PathNormalizer.concatWebPath("a/", "/f/"));
		assertEquals("a" + SEP + "f", PathNormalizer.concatWebPath("a", "/f"));

		assertEquals("a" + SEP + "b" + SEP + "c" + SEP + "d", PathNormalizer.concatWebPath("a/b/", "/c/d"));
	}

}
