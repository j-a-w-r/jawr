/**
 * 
 */
package test.net.java.jawr.web.resource.bundle.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import net.java.jawr.web.resource.bundle.InclusionPattern;
import net.java.jawr.web.resource.bundle.JoinableResourceBundle;
import net.java.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.java.jawr.web.resource.bundle.sorting.GlobalResourceBundleComparator;

/**
 * @author jhernandez
 *
 */
public class GlobalResourceBundleComparatorTest extends TestCase {
	
	public void testSortGlobalResourceBundles() {
		
		List unsorted = new ArrayList();
		unsorted.add(createMockBundle("four",4));
		unsorted.add(createMockBundle("two",2));
		unsorted.add(createMockBundle("one",1));
		unsorted.add(createMockBundle("three",3));
		Collections.sort(unsorted, new GlobalResourceBundleComparator());
		
		String name = ((JoinableResourceBundle) unsorted.get(0)).getName();
		assertTrue("Sorted list at position 0 does not match expected",name.equals("one"));
		name = ((JoinableResourceBundle) unsorted.get(1)).getName();
		assertTrue("Sorted list at position 1 does not match expected",name.equals("two"));
		name = ((JoinableResourceBundle) unsorted.get(2)).getName();
		assertTrue("Sorted list at position 2 does not match expected",name.equals("three"));
		name = ((JoinableResourceBundle) unsorted.get(3)).getName();
		assertTrue("Sorted list at position 3 does not match expected",name.equals("four"));
		
	}
	
	private JoinableResourceBundle createMockBundle(final String name, final int index) {
		
		return new JoinableResourceBundle(){

			public boolean belongsToBundle(String itemPath) {
				return false;
			}

			public InclusionPattern getInclusionPattern() {
				return new InclusionPattern(true,index);
			}

			public List getItemPathList() {
				return null;
			}

			public Set getLicensesPathList() {
				return null;
			}

			public String getName() {
				return name;
			}

			public String getURLPrefix() {
				return null;
			}

			public ResourceBundlePostProcessor getBundlePostProcessor() {
				return null;
			}

			public ResourceBundlePostProcessor getUnitaryPostProcessor() {
				return null;
			}};
	}

}
