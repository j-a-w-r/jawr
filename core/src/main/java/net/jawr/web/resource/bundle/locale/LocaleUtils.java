package net.jawr.web.resource.bundle.locale;

public class LocaleUtils {
	
	public static String getLocalizedBundleName(String bundleName, String localeKey) {
		
		String newName = bundleName.substring(0, bundleName.lastIndexOf('.'));
		newName += '_' + localeKey;
		newName += bundleName.substring(bundleName.lastIndexOf('.'));
		
		return newName;
	}

}
