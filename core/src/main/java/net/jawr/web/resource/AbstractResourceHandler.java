/**
 * Copyright 2007-2009   Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;

import org.apache.log4j.Logger;

/**
 * Abstract resourcehandler implementation with common functionality.  
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public abstract class AbstractResourceHandler  implements ResourceHandler{
	
	/** The logger */
	private static final Logger log = Logger.getLogger(AbstractResourceHandler.class.getName());
	
	/** The generator registry */
	private GeneratorRegistry generatorRegistry;

	/** The name of the Jawr temp directory */
	protected static final String TEMP_SUBDIR = "jawrTmp";
	
	/** The name of the directory which contain the bundles in text format */
	protected static final String TEMP_TEXT_SUBDIR = "text";
	
	/** The name of the directory which contain the bundles in gzip format */
	protected static final String TEMP_GZIP_SUBDIR = "gzip";
	
	/** The name of the directory which contain the CSS defined in classpath for the DEBUG mode */
	protected static final String TEMP_CSS_CLASSPATH_SUBDIR = "cssClasspath";
	
	/** The path of the directory which contain the bundles in text format */
	protected String textDirPath;
	
	/** The path of the directory which contain the bundles in gzip format */
	protected String gzipDirPath;
	
	/** The path of the directory which contain the CSS defined in classpath for the DEBUG mode */
	protected String cssClasspathDirPath;
	
	/** The charset to use for the files */
	protected Charset charset;
	
	/**
     * Build a resource handler based on the specified temporary files root path and charset. 
     * @param tempDirRoot Root dir for storing bundle files. 
     * @param charset Charset to read/write characters. 
     */
	protected AbstractResourceHandler(File tempDirRoot,Charset charset,GeneratorRegistry generatorRegistry) {
		super();
		this.charset = charset;
		this.generatorRegistry = generatorRegistry;
		try {
			String tempDirPath = tempDirRoot.getCanonicalPath() + File.separator + TEMP_SUBDIR;
             // In windows, pathnames with spaces are returned as %20
            if(tempDirPath.indexOf("%20") != -1)
                tempDirPath = tempDirPath.replaceAll("%20"," ");
            
            this.textDirPath = tempDirPath + File.separator + TEMP_TEXT_SUBDIR;
			this.gzipDirPath = tempDirPath + File.separator + TEMP_GZIP_SUBDIR;
			this.cssClasspathDirPath = tempDirPath + File.separator + TEMP_CSS_CLASSPATH_SUBDIR;
			createDir(tempDirPath);
			createDir(textDirPath);
			createDir(gzipDirPath);
			createDir(cssClasspathDirPath);
			
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException creating temporary jawr directory",e);
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.ResourceHandler#getResource(java.lang.String)
	 */
	public final Reader getResource(String resourceName) throws ResourceNotFoundException {
		return getResource(resourceName, false);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.ResourceHandler#getResource(java.lang.String, boolean)
	 */
	public final Reader getResource(String resourceName, boolean processingBundle) throws ResourceNotFoundException {
		if(generatorRegistry.isPathGenerated(resourceName)) {			
			return generatorRegistry.createResource(resourceName, this, processingBundle);
		}
		else return doGetResource(resourceName);
	}
	
	/**
	 * Retrieves a single resource using the implementation specifics. 
	 * Invoked by getResource() unless the requested item is generated. 
	 * 
	 * @param resourceName
	 * @return
	 * @throws ResourceNotFoundException
	 */
	protected abstract Reader doGetResource(String resourceName) throws ResourceNotFoundException;
	
	

    /* (non-Javadoc)
	 * @see net.jawr.web.resource.ResourceHandler#isResourceGenerated(java.lang.String)
	 */
	public boolean isResourceGenerated(String path) {
		return generatorRegistry.isPathGenerated(path);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.ResourceHandler#getCssClasspathResource(java.lang.String)
	 */
	public Reader getCssClasspathResource(String resourceName)
			throws ResourceNotFoundException {
		
		String filePath = getStoredBundlePath(cssClasspathDirPath, resourceName);
		FileReader reader = null;
		try {
			reader = new FileReader(filePath);
		} catch (FileNotFoundException e) {
			throw new ResourceNotFoundException(filePath);
		}
		return reader;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceHandler#getResourcebundleReader(java.lang.String)
	 */
	public Reader getResourceBundleReader(String bundleName)  throws ResourceNotFoundException {
		Reader rd = null;
		String tempFileName = null;
		try 
		{
			tempFileName = getStoredBundlePath(bundleName, false);
			
			File file = new File( tempFileName );
			
			// If file does not exist, throw an expection. 
			if(!file.exists())
				throw new  ResourceNotFoundException(tempFileName);
			
			FileInputStream fis = new FileInputStream( file );
	        FileChannel inchannel = fis.getChannel();
	        
	        rd = Channels.newReader(inchannel,charset.newDecoder (),-1);
			
		}catch (IOException e) {
			throw new RuntimeException("Unexpected IOException reading temporary jawr file with path:" + tempFileName,e);
		}
		return rd;
	}

	/**
	 * Resolves the file name with which a bundle is stored. 
	 * @param bundleName the bundle name
	 * @param asGzippedBundle the flag indicating if it's a gzipped bundle or not
	 * @return the file name.
	 */
	private String getStoredBundlePath(String bundleName, boolean asGzippedBundle) {
		String tempFileName;
		
		if(asGzippedBundle)
			tempFileName = gzipDirPath;
		else
			tempFileName = textDirPath;
		
		return getStoredBundlePath(tempFileName, bundleName);
	}


	/**
	 * Resolves the file path of the bundle from the root directory. 
	 * @param rootDir the rootDir
	 * @param bundleName the bundle name
	 * @return the file path
	 */
	private String getStoredBundlePath(String rootDir, String bundleName) {
		if(bundleName.indexOf('/') != -1) {
			bundleName = bundleName.replace('/', File.separatorChar);
		}
			
		if(!bundleName.startsWith(File.separator))
			rootDir += File.separator;
		
		return rootDir + bundleName;
	}

	
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceHandler#getResourceBundleBytes(java.lang.String)
	 */
	public FileChannel getResourceBundleChannel(String bundleName) throws ResourceNotFoundException {
		String tempFileName = null;
		try 
		{
			tempFileName = getStoredBundlePath(bundleName,true);
			
			File file = new File( tempFileName );
			
			// If file does not exist, throw an expection. 
			if(!file.exists())
				throw new  ResourceNotFoundException(tempFileName);
			
			FileInputStream fis = new FileInputStream( file );
			return fis.getChannel();
			
		}catch (IOException e) {
			throw new RuntimeException("Unexpected IOException reading temporary jawr file with path:" + tempFileName,e);
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceHandler#storebundle(java.lang.String, java.lang.StringBuffer)
	 */
	public void storeBundle(String bundleName, StringBuffer bundledResources) {
	
		JoinableResourceBundleContent bundleResourcesContent = new JoinableResourceBundleContent(bundledResources);
		storeBundle(bundleName, bundleResourcesContent);
	}


	/* (non-Javadoc)
	 * @see net.jawr.web.resource.ResourceHandler#storeBundle(java.lang.String, net.jawr.web.resource.bundle.JoinableResourceBundleContent)
	 */
	public void storeBundle(String bundleName,
			JoinableResourceBundleContent bundleResourcesContent) {
		
		// Text version
		String bundleContent = bundleResourcesContent.getContent().toString();
		storeBundle(bundleName,bundleContent,false, textDirPath);

		// binary version
		storeBundle(bundleName,bundleContent,true, gzipDirPath);
		
		// Store Css classpath debug files
		Map cssClasspathDebugContentMap = bundleResourcesContent.getCssClasspathDebugContentMap();
		Iterator keyIterator = cssClasspathDebugContentMap.keySet().iterator();
		while(keyIterator.hasNext()){
			String filePath = (String) keyIterator.next();
			storeBundle(filePath,(String) cssClasspathDebugContentMap.get(filePath),false, cssClasspathDirPath);
		}
	}
	
	/**
	 * Stores a resource bundle either in text or binary gzipped format. 
	 * @param bundleName the bundle name
	 * @param bundledResources the bundledRessources
	 * @param gzipFile a fag defining if the file is gzipped or not
	 * @param rootDir the root directory
	 */
	private void storeBundle(String bundleName, String bundledResources,boolean gzipFile, String rootdir) {
		if(log.isDebugEnabled()){
			String msg = "Storing a generated " + (gzipFile ? "and gzipped" : "") + " bundle with an id of:" + bundleName;
			log.debug(msg);
		}
		
		try {
			// Create subdirs if needed
			if(bundleName.indexOf('/') != -1) {
				StringTokenizer tk = new StringTokenizer(bundleName,"/");
				String pathName = rootdir;
				while(tk.hasMoreTokens()) {
					String name = tk.nextToken();
					if(tk.hasMoreTokens()){
						pathName += File.separator;
						pathName += name;
						createDir(pathName);
					}
				}
				bundleName = bundleName.replace('/', File.separatorChar);
			}
				

			File store = createNewFile(rootdir + File.separator + bundleName);
			
			if(gzipFile){
				FileOutputStream fos = new FileOutputStream(store);
				GZIPOutputStream gzOut = new GZIPOutputStream(fos);
				byte[] data = bundledResources.toString().getBytes(charset.name());
				gzOut.write(data, 0, data.length);
				gzOut.close();
			}
			else {
				FileOutputStream fos = new FileOutputStream(store);
		        FileChannel channel = fos.getChannel();
		        Writer wr = Channels.newWriter(channel,charset.newEncoder(),-1);
		        wr.write(bundledResources.toString());
		        wr.close();				
			}
		} catch (IOException e) {e.printStackTrace();
			throw new RuntimeException("Unexpected IOException creating temporary jawr file",e);
		}
	}

	/**
	 * Creates a directory. If dir is note created for some reson a runtimeexception is thrown. 
	 * @param dir
	 * @throws IOException
	 */
	private File createDir(String path) throws IOException {
        // In windows, pathnames with spaces are returned as %20
        if(path.indexOf("%20") != -1)
            path = path.replaceAll("%20"," ");		
        File dir = new File(path);
		if(!dir.exists() && !dir.mkdir())
			throw new RuntimeException("Error creating temporary jawr directory with path:" + dir.getPath());
		
		if(log.isDebugEnabled())
			log.debug("Created dir: " + dir.getCanonicalPath());
		return dir;
	}
	
	/**
	 * Creates a file. If dir is note created for some reson a runtimeexception is thrown. 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private File createNewFile(String path) throws IOException {
		
        // In windows, pathnames with spaces are returned as %20
        if(path.indexOf("%20") != -1)
            path = path.replaceAll("%20"," ");		
		File newFile = new File(path);
		
		if(!newFile.exists() && !newFile.createNewFile())
			throw new RuntimeException("Unable to create a temporary file at " + path);
		if(log.isDebugEnabled())
			log.debug("Created file: " + newFile.getCanonicalPath());
		return newFile;
	}
		

}