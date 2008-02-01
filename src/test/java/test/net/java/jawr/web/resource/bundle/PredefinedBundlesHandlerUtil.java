package test.net.java.jawr.web.resource.bundle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.java.jawr.web.config.JawrConfig;
import net.java.jawr.web.exception.DuplicateBundlePathException;
import net.java.jawr.web.resource.ResourceHandler;
import net.java.jawr.web.resource.bundle.factory.BundlesHandlerFactory;
import net.java.jawr.web.resource.bundle.factory.util.ResourceBundleDefinition;
import net.java.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

public class PredefinedBundlesHandlerUtil {

	public static final ResourceBundlesHandler buildSingleBundleHandler(ResourceHandler handler, String commonPfx, JawrConfig config) throws DuplicateBundlePathException {

		BundlesHandlerFactory factory = new BundlesHandlerFactory();
		factory.setResourceHandler(handler);
		factory.setBaseDir("/js");
		factory.setUseSingleResourceFactory(true);			
		factory.setSingleFileBundleName("script");
		factory.setBundlesType("js");
		factory.setCommonURLPrefix(commonPfx);
		factory.setJawrConfig(config);		
		return factory.buildResourceBundlesHandler();
		
	}
	
	public static final ResourceBundlesHandler buildSimpleBundles(ResourceHandler handler,String baseDir, String type,
																JawrConfig config, String commonPfx,String globalPfx, String libPfx) throws DuplicateBundlePathException {		
		BundlesHandlerFactory factory = new BundlesHandlerFactory();
		factory.setCommonURLPrefix(commonPfx);
		factory.setResourceHandler(handler);
		factory.setBaseDir(baseDir);
		factory.setBundlesType(type);
		
		
		Set customBundles = new HashSet();
		
		ResourceBundleDefinition def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/lib/**"));
		def.setBundleId("/library." + type);
		def.setPrefix(libPfx);
		def.setGlobal(true);
		def.setInclusionOrder(0);			
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/global/**"));
		def.setBundleId("/global." + type);
		def.setPrefix(globalPfx);
		def.setGlobal(true);
		def.setInclusionOrder(1);		
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/debug/on/**"));
		def.setBundleId("/debugOn." + type);
		def.setGlobal(true);
		def.setInclusionOrder(2);	
		def.setDebugOnly(true);
		def.setDebugNever(false);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/debug/off/**"));
		def.setBundleId("/debugOff." + type);			
		def.setGlobal(true);
		def.setInclusionOrder(2);		
		def.setDebugOnly(false);
		def.setDebugNever(true);
		customBundles.add(def);
		
		factory.setBundleDefinitions(customBundles);
		factory.setUseDirMapperFactory(true);
		Set excludedPaths = new HashSet();
		excludedPaths.add(baseDir + "/lib");
		excludedPaths.add(baseDir + "/global");
		excludedPaths.add(baseDir + "/debug");
		factory.setExludedDirMapperDirs(excludedPaths);
		factory.setJawrConfig(config);
		return factory.buildResourceBundlesHandler();
	}
}
