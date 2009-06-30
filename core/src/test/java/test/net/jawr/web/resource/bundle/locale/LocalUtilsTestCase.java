/**
 * 
 */
package test.net.jawr.web.resource.bundle.locale;

import java.util.Iterator;
import java.util.List;

import net.jawr.web.resource.bundle.locale.LocaleUtils;
import junit.framework.TestCase;

/**
 * Test case class for Local utils
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class LocalUtilsTestCase extends TestCase {

	public void testGetLocaleAvailablePrefixes(){
		
		List result = LocaleUtils.getAvailableLocaleSuffixes("/bundleLocale/messages.properties");
		for (Iterator iterator = result.iterator(); iterator.hasNext();) {
			System.out.println("Suffix : '"+iterator.next()+"'");
		}
	}
}
