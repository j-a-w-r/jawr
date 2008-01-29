package test.net.java.jawr.web.resource.bundle.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import net.java.jawr.web.resource.FileSystemResourceHandler;

import test.net.java.jawr.web.FileUtils;

/**
 *
 * @author jhernandez
 */
public abstract class ResourceHandlerBasedTest  extends  TestCase {
    
    protected static final String TMP_DIR = "tmp/";
	protected static final String WORK_DIR = "work/";
	
	protected FileSystemResourceHandler createResourceHandler(String rootDir,Charset charset) {
	try {
	    FileUtils.createDir(rootDir);

	    File tmp = FileUtils.createDir(rootDir + TMP_DIR);

	    String work = FileUtils.createDir(rootDir + WORK_DIR).getCanonicalPath().replaceAll("%20", " ");
	    return new FileSystemResourceHandler(work, tmp, charset);
	} catch (Exception ex) {
	     ex.printStackTrace();
	   throw new RuntimeException(ex);
	}
	}
	

	protected String fullyReadReader(Reader rd)  throws Exception {
		BufferedReader brd = new BufferedReader(rd);
		StringBuffer sb = new StringBuffer();
		String t;
		while((t = brd.readLine()) != null)
			sb.append(t);
		return sb.toString();
	}
	

	protected String fullyReadChannel(FileChannel channel, String charsetName)  throws Exception {
		
		GZIPInputStream gzIn = new GZIPInputStream(Channels.newInputStream(channel));
		ReadableByteChannel chan = Channels.newChannel(gzIn);
		Reader rd = Channels.newReader(chan, charsetName);
		String res =  fullyReadReader(rd);
		rd.close();
		return res;
	}	
    
}
