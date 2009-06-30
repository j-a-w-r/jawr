/**
 * 
 */
package test.net.jawr.web.resource;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

/**
 * @author ibrahim
 *
 */
public class FileNameUtilsTestCase extends TestCase {

//	public void testConcat() throws URISyntaxException{
//	
//		String folder = "/css/folder/subfolder/subfolder/temp.cs/";
//		String relativePath = "../../../../../images/someImage.gif";
//		URI uri = new URI(folder+relativePath); 
//		//String result = uri.normalize().toString();
//		String result = FileNameUtils.concat(folder, relativePath);
//		String expected = "/images/someImage.gif";
//		assertEquals(expected, result);
//		
//	}
//	
	
	public void testConcat(){
		String oldPath = "/Root/css/folder/temp.css";
		String imgPath = "../../images/someImage.gif";
//		String result = FileNameUtils.concat(PathTool.getParentPath(oldPath)+"/", imgPath);
//		System.out.println(result);
		
		// basic test
		imgPath = "../../../../../images/someImage.gif";
		// the image is at /images
		oldPath = "/Root/css/folder/subfolder/subfolder/someCSS.css";
		
		
		String result = PathNormalizer.concatWebPath(oldPath, imgPath);
		System.out.println(result);
	}
	
//	public void testRelativize() throws URISyntaxException{
//		
//		String oldPath = "/Root/css/folder/subfolder/subfolder/temp.css";
//		String imgPath = "../../../../../images/someImage.gif";
//		String newPath = "/Root/servletMapping/prefix"+"/css/some.css";
//		
//		String result = FileNameUtils.concat(PathTool.getParentPath(oldPath)+"/", imgPath);
//		result = PathTool.getRelativeWebPath(PathTool.getParentPath(newPath), result);
//		System.out.println(result);
//		assertEquals("../../../images/someImage.gif", result);
//		
//	}
//	
//	
//	public void testGetDirectoryComponent(){
//		String filePath = "/servletMapping/prefix"+"/css/some.css";
//		String result = PathTool.getParentPath(filePath);
//		System.out.println(result);
//	}
}
