package test.net.jawr.web.resource.bundle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.factory.BundlesHandlerFactory;
import net.jawr.web.resource.bundle.factory.util.ResourceBundleDefinition;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

public class PredefinedBundlesHandlerUtil {

	public static final ResourceBundlesHandler buildSingleBundleHandler(ResourceReaderHandler handler, ResourceBundleHandler rsBundleHandler, JawrConfig config) throws DuplicateBundlePathException, BundleDependencyException {

		BundlesHandlerFactory factory = new BundlesHandlerFactory();
		factory.setResourceReaderHandler(handler);
		factory.setResourceBundleHandler(rsBundleHandler);
		factory.setBaseDir("/js");
		factory.setUseSingleResourceFactory(true);			
		factory.setSingleFileBundleName("script");
		factory.setBundlesType("js");
		factory.setJawrConfig(config);		
		return factory.buildResourceBundlesHandler();
		
	}
	
	public static final ResourceBundlesHandler buildSimpleBundles(ResourceReaderHandler handler, ResourceBundleHandler rsBundleHandler, String baseDir, String type,
																JawrConfig config) throws DuplicateBundlePathException, BundleDependencyException {		
		
		config.setGeneratorRegistry(new GeneratorRegistry(type));
		BundlesHandlerFactory factory = new BundlesHandlerFactory();
		factory.setResourceReaderHandler(handler);
		factory.setResourceBundleHandler(rsBundleHandler);
		factory.setBaseDir(baseDir);
		factory.setBundlesType(type);
		
		Set customBundles = new HashSet();
		
		ResourceBundleDefinition def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/lib/**"));
		def.setBundleId("/library." + type);
		def.setGlobal(true);
		def.setInclusionOrder(0);			
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/global/**"));
		def.setBundleId("/global." + type);
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
