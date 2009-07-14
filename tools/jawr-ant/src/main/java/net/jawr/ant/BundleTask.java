/**
 * 
 */
package net.jawr.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.jawr.web.BundleProcessor;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Delete;

/**
 * @author ibrahim
 *
 */
public class BundleTask extends Task {

	/**
	 * The path to the root of the web application.
	 * <p/>
	 * That's where the resources are loaded from.
	 * 
	 */
	private String rootPath;
	
	/**
	 * The path to the root of the web application.
	 * <p/>
	 * That's where the resources are loaded from and where the new files are generated.
	 * 
	 */
	private String tempDirPath;
	
	/**
	 * The path to the root of the web application.
	 * <p/>
	 * That's where the resources are loaded from and where the new files are generated.
	 * 
	 */
	private String destDirPath;

	/**
	 * The path to the root of the web application.
	 * <p/>
	 * That's where the resources are loaded from and where the new files are generated.
	 * 
	 */
	private String servletsToInitialize;

	/**
	 * The flag indicating if we  should generate the CDN files or not
	 * 
	 * @parameter default-value="true"
	 */
	private boolean generateCDNFiles;
	
	/**
	 * @param servletsToInitialize the servletsToInitialize to set
	 */
	public void setServletsToInitialize(String servletsToInitialize) {
		this.servletsToInitialize = servletsToInitialize;
	}

	/**
	 * @param generateCDNFiles the generateCDNFiles to set
	 */
	public void setGenerateCDNFiles(boolean generateCDNFiles) {
		this.generateCDNFiles = generateCDNFiles;
	}

	/**
	 * @param rootPath the rootPath to set
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * @param tempDirPath the tempDirPath to set
	 */
	public void setTempDirPath(String tempDirPath) {
		this.tempDirPath = tempDirPath;
	}

	/**
	 * @param destDirPath the destDirPath to set
	 */
	public void setDestDirPath(String destDirPath) {
		this.destDirPath = destDirPath;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() {
		
		try {
			createBundles();
		} catch (Exception ex) {
			Logger logger = Logger.getLogger(getClass().getName());
			if(ex instanceof ServletException){
				logger.log(Level.SEVERE, null, ((ServletException)ex).getRootCause());
			}else{
				logger.log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Create the bundles.
	 * 
	 * @throws Exception if an exception occurs
	 */
	public void createBundles() throws Exception {

		File tempDir = new File(tempDirPath);
		if(!tempDir.exists()){
			tempDir.mkdirs();
		}else{
			cleanDirectory(tempDir);
		}
		File destDir = new File(destDirPath);
		if(!destDir.exists()){
			destDir.mkdirs();
		}else{
			cleanDirectory(destDir);
		}
		
		List servlets = new ArrayList();
		if(servletsToInitialize != null){
			
			String[] servletNames = servletsToInitialize.split(",");
			for (int i = 0; i < servletNames.length; i++) {
				servlets.add(servletNames[i].trim());
			}
		}
		
		BundleProcessor bundleProcessor = new BundleProcessor();
		bundleProcessor.process(rootPath, tempDirPath, destDirPath, servlets, generateCDNFiles);
	}

	/**
     * Clean a directory without deleting it.
     */
    public void cleanDirectory( final File directory )
        throws IOException
    {
        if ( !directory.exists() )
        {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException( message );
        }

        if ( !directory.isDirectory() )
        {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException( message );
        }

        Delete deleteTask = new Delete();
        deleteTask.setProject(getProject());
		deleteTask.setDir(directory);
		deleteTask.execute();
		
		if(!directory.exists()){
			directory.mkdirs();
		}
    }
  
}
