/**
 * 
 */
package test.net.jawr.web.resource.bundle.locale;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.jawr.web.resource.bundle.locale.LocaleUtils;

/**
 * Test case class for Local utils
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class LocalUtilsTestCase extends TestCase {

	public void testGetLocaleAvailablePrefixes(){
		
		List result = LocaleUtils.getAvailableLocaleSuffixesForBundle("bundleLocale.messages", null);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("",result.get(0));
		Assert.assertEquals("es",result.get(1));
	}
	
	public void testGetLocaleAvailablePrefixesWithNamespace(){
		
		List result = LocaleUtils.getAvailableLocaleSuffixesForBundle("bundleLocale.messages(mynamespace)", null);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("",result.get(0));
		Assert.assertEquals("es",result.get(1));
	}
	
	public void testGetLocaleAvailablePrefixesWithFilter(){
		
		List result = LocaleUtils.getAvailableLocaleSuffixesForBundle("bundleLocale.messages[ui.msg]", null);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("",result.get(0));
		Assert.assertEquals("es",result.get(1));
	}
	
	public void testGetLocaleAvailablePrefixesWithFilterAndNamespace(){
		
		List result = LocaleUtils.getAvailableLocaleSuffixesForBundle("bundleLocale.messages(mynamespace)[ui.msg]", null);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("",result.get(0));
		Assert.assertEquals("es",result.get(1));
	}
	
	// TODO test for Grails with servlet context
}
