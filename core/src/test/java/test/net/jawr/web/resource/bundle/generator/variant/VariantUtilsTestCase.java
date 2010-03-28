/**
 * Copyright 2010 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package test.net.jawr.web.resource.bundle.generator.variant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.bundle.variant.VariantUtils;

/**
 * @author Ibrahim Chaehoi
 *
 */
public class VariantUtilsTestCase extends TestCase {

	public void testConcatVariantsBasic(){
		
		Map variantSet1 = new HashMap();
		Map variantSet2 = new HashMap();
		variantSet2.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr_FR",asSet(new String[]{"fr_FR", "es_ES"})));
		variantSet2.put(JawrConstant.SKIN_VARIANT_TYPE, new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, "default", asSet(new String[]{"default", "summer"})));
		Map result = VariantUtils.concatVariants(variantSet1, variantSet2);
		Map expectedResult = new HashMap();
		expectedResult.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr_FR",asSet(new String[]{"fr_FR", "es_ES"})));
		expectedResult.put(JawrConstant.SKIN_VARIANT_TYPE, new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, "default", asSet(new String[]{"default", "summer"})));
		assertEquals(expectedResult, result);
		
		result = VariantUtils.concatVariants(variantSet2, variantSet1);
		assertEquals(expectedResult, result);
		
		result = VariantUtils.concatVariants(null, variantSet2);
		assertEquals(expectedResult, result);
		
		result = VariantUtils.concatVariants(variantSet2, null);
		assertEquals(expectedResult, result);
		
	}

	public void testConcatVariants(){
		
		Map variantSet1 = new HashMap();
		variantSet1.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr_FR", asSet(new String[]{"fr_FR", "en_US"})));
		variantSet1.put(JawrConstant.SKIN_VARIANT_TYPE, new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, "default", asSet(new String[]{"default", "winter"})));
		Map variantSet2 = new HashMap();
		variantSet2.put(JawrConstant.LOCALE_VARIANT_TYPE,  new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr_FR", asSet(new String[]{"fr_FR", "es_ES"})));
		variantSet2.put(JawrConstant.SKIN_VARIANT_TYPE, new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, "default", asSet(new String[]{"default", "summer"})));
		Map result = VariantUtils.concatVariants(variantSet1, variantSet2);
		Map expectedResult = new HashMap();
		expectedResult.put(JawrConstant.LOCALE_VARIANT_TYPE,  new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr_FR", asSet(new String[]{"fr_FR", "en_US", "es_ES"})));
		expectedResult.put(JawrConstant.SKIN_VARIANT_TYPE, new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, "default", asSet(new String[]{"winter", "default", "summer"})));
		assertEquals(expectedResult, result);
		
	}
	
	public void testGetAllVariantKeys(){
		
		Map variants = new HashMap();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, asSet(new String[]{"", "fr_FR", "en_US"}));
		variants.put(JawrConstant.SKIN_VARIANT_TYPE, asSet(new String[]{"winter", "summer"}));
		variants.put("browser", asSet(new String[]{null, "ie", "firefox"}));
		List variantKeys = VariantUtils.getAllVariantKeys(variants);
		Set expectedResult = asSet(new String[]{"@@winter", "@@summer", 
				"ie@@winter", "ie@@summer",
				"firefox@@winter", "firefox@@summer",
				"@fr_FR@winter", "@fr_FR@summer",
				"@en_US@winter", "@en_US@summer", 
				"ie@fr_FR@winter", "ie@fr_FR@summer",
				"ie@en_US@winter", "ie@en_US@summer", 
				"firefox@fr_FR@winter", "firefox@fr_FR@summer",
				"firefox@en_US@winter", "firefox@en_US@summer"});
		
		assertEquals(expectedResult, new HashSet(variantKeys));
		
	}
	
	public void testGetAllVariantKeysWithNullValues(){
		
		Map variants = new HashMap();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, asSet(new String[]{null, "fr_FR", "en_US"}));
		variants.put(JawrConstant.SKIN_VARIANT_TYPE, asSet(new String[]{"winter", "summer"}));
		List variantKeys = VariantUtils.getAllVariantKeys(variants);
		Set expectedResult = asSet(new String[]{"@winter", "@summer", "fr_FR@winter", "fr_FR@summer", "en_US@winter",
				"en_US@summer"});
		assertEquals(expectedResult, new HashSet(variantKeys));
	}
	
	public void testGetAllVariantsWithNullValues(){
		
		Map variants = new HashMap();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, asSet(new String[]{null, "fr_FR", "en_US"}));
		variants.put(JawrConstant.SKIN_VARIANT_TYPE, asSet(new String[]{"winter", "summer"}));
		List variantMaps = VariantUtils.getAllVariants(variants);
		
		Set expectedResult = new HashSet();
		Map aMap = asMap(new String[]{JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"fr_FR", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"fr_FR", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"en_US", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"en_US", "summer"});
		expectedResult.add(aMap);
		
		assertEquals(expectedResult, new HashSet(variantMaps));
	}

	
	public void testGetAllVariants(){
		
		Map variants = new HashMap();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, asSet(new String[]{"", "fr_FR", "en_US"}));
		variants.put(JawrConstant.SKIN_VARIANT_TYPE, asSet(new String[]{"winter", "summer"}));
		variants.put("browser", asSet(new String[]{null, "ie", "firefox"}));
		List variantMaps = VariantUtils.getAllVariants(variants);
		
		Set expectedResult = new HashSet();
		Map aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"firefox", "", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"firefox", "", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "fr_FR", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "fr_FR", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "en_US", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "en_US", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "fr_FR", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "fr_FR", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "en_US", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "en_US", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"firefox", "fr_FR", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"firefox", "fr_FR", "summer"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"firefox", "en_US", "winter"});
		expectedResult.add(aMap);
		
		aMap = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"firefox", "en_US", "summer"});
		expectedResult.add(aMap);
		
		assertEquals(expectedResult, new HashSet(variantMaps));
		
	}
	
	public void testGetVariantBundleNameFromVariantMap(){
		
		Map variants = null;
		String result = VariantUtils.getVariantBundleName("myBundle.js", variants);
		String expected = "myBundle.js";
		assertEquals(expected, result);
		
		variants = new HashMap();
		result = VariantUtils.getVariantBundleName("myBundle.js", variants);
		expected = "myBundle.js";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "", "winter"});
		
		result = VariantUtils.getVariantBundleName("myBundle.js", variants);
		expected = "myBundle@@@winter.js";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"", null, "summer"});
		
		result = VariantUtils.getVariantBundleName("myBundle.js", variants);
		expected = "myBundle@@@summer.js";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "fr_FR", "summer"});
		
		result = VariantUtils.getVariantBundleName("myBundle.js", variants);
		expected = "myBundle@ie@fr_FR@summer.js";
		assertEquals(expected, result);
		
	}
	
	public void testGetVariantBundleNameFromSuffix(){
		
		String result = VariantUtils.getVariantBundleName("myBundle.js", "@@");
		String expected = "myBundle@@@.js";
		assertEquals(expected, result);
		
		result = VariantUtils.getVariantBundleName("myBundle.js", "@@summer");
		expected = "myBundle@@@summer.js";
		assertEquals(expected, result);
		
		result = VariantUtils.getVariantBundleName("myBundle.js", (String)null);
		expected = "myBundle.js";
		assertEquals(expected, result);
		
		result = VariantUtils.getVariantBundleName("myBundle.js", "");
		expected = "myBundle.js";
		assertEquals(expected, result);
		
		result = VariantUtils.getVariantBundleName("message:message", "en_US");
		expected = "message:message@en_US";
		assertEquals(expected, result);
		
		
	}
	
	public void testGetVariantKey(){
		
		Map variants = null;
		String result = VariantUtils.getVariantKey(variants);
		String expected = "";
		assertEquals(expected, result);
		
		variants = new HashMap();
		result = VariantUtils.getVariantKey(variants);
		expected = "";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "", "winter"});
		
		result = VariantUtils.getVariantKey(variants);
		expected = "@@winter";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"", null, "summer"});
		
		result = VariantUtils.getVariantKey(variants);
		expected = "@@summer";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "fr_FR", "summer"});
		
		result = VariantUtils.getVariantKey(variants);
		expected = "ie@fr_FR@summer";
		assertEquals(expected, result);
	}
	
	public void testGetVariantKeyFromVariantType(){
		
		Map variants = null;
		Set variantTypes = asSet(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE});
		String result = VariantUtils.getVariantKey(variants, variantTypes);
		String expected = "";
		assertEquals(expected, result);
		
		variants = new HashMap();
		result = VariantUtils.getVariantKey(variants, variantTypes);
		expected = "";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{null, "", "winter"});
		
		result = VariantUtils.getVariantKey(variants, variantTypes);
		expected = "@@winter";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"", null, "summer"});
		
		result = VariantUtils.getVariantKey(variants, variantTypes);
		expected = "@@summer";
		assertEquals(expected, result);
		
		variants = asMap(new String[]{"browser",JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE},
				new String[]{"ie", "fr_FR", "summer"});
		
		result = VariantUtils.getVariantKey(variants, variantTypes);
		expected = "ie@fr_FR@summer";
		assertEquals(expected, result);
		
		variantTypes = asSet(new String[]{"browser", JawrConstant.SKIN_VARIANT_TYPE});
		result = VariantUtils.getVariantKey(variants, variantTypes);
		expected = "ie@summer";
		assertEquals(expected, result);
		
		variantTypes = asSet(new String[]{JawrConstant.LOCALE_VARIANT_TYPE, JawrConstant.SKIN_VARIANT_TYPE});
		result = VariantUtils.getVariantKey(variants, variantTypes);
		expected = "fr_FR@summer";
		assertEquals(expected, result);
		
		variantTypes = asSet(new String[]{JawrConstant.LOCALE_VARIANT_TYPE});
		result = VariantUtils.getVariantKey(variants, variantTypes);
		expected = "fr_FR";
		assertEquals(expected, result);
		
		variantTypes = asSet(new String[]{});
		result = VariantUtils.getVariantKey(variants, variantTypes);
		expected = "";
		assertEquals(expected, result);
		
		result = VariantUtils.getVariantKey(variants, null);
		expected = "";
		assertEquals(expected, result);
		
	}

	private Map asMap(String[] keys, String[] values){
		Map map = new HashMap();
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}
		return map;
	}
	private Set asSet(String[] values){
		return new HashSet(Arrays.asList(values));
	}
}
