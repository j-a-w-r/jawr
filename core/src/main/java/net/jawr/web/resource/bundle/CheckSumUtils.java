/**
 * Copyright 2009 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

import org.apache.log4j.Logger;

/**
 * This class defines utilities methods for Checksum.
 * 
 * @author Ibrahim Chaehoi
 */
public final class CheckSumUtils {

	/** The MD5 algorithm name */
	private static final String MD5_ALGORITHM = "MD5";

	/** The CRC32 algorithm name */
	private static final String CRC32_ALGORITHM = "CRC32";
	
	/**
	 * Return the cache busted url associated to the url passed in parameter,
	 * if the resource is not found, null will b returned. 
	 * @param url the url path to the resource file
	 * @param is the resource input stream 
	 * @param jawrConfig the jawrConfig
	 * @return the cache busted url
	 * @throws IOException if an IO exception occurs.
	 * @throws ResourceNotFoundException if the resource is not found.
	 */
	public static String getCacheBustedUrl(String url, ResourceHandler rsHandler, JawrConfig jawrConfig) throws IOException, ResourceNotFoundException {
		
		String checksum = null;
		String classpathResourceUrl = null;
		InputStream is = null;
		boolean fromClasspath = url.startsWith(JawrConstant.CLASSPATH_RESOURCE_PREFIX);
		try {
			
			if(fromClasspath){
				
				classpathResourceUrl = url.substring(JawrConstant.CLASSPATH_RESOURCE_PREFIX.length());
				is = ClassLoaderResourceUtils.getResourceAsStream(classpathResourceUrl, CheckSumUtils.class);
			}else{
				url = PathNormalizer.asPath(url);
				is = rsHandler.getResourceAsStream(url);
			}
			
			if(is != null){
				checksum = CheckSumUtils.getChecksum(is, jawrConfig.getImageHashAlgorithm());
			}else{
				throw new ResourceNotFoundException(url);
			}
		}catch (FileNotFoundException e) {
			throw new ResourceNotFoundException(url);
		}
		finally {
			IOUtils.close(is);
		}
		
		String result = "";
		
		if(fromClasspath){
			result = JawrConstant.CLASSPATH_CACHE_BUSTER_PREFIX;
		}else{
			result = JawrConstant.CACHE_BUSTER_PREFIX;
		}
		
		result = result+checksum;
		if(fromClasspath){
			url = classpathResourceUrl;
		}
		if(!url.startsWith("/")){
			result = result + "/";
		}
		// Add the cache buster extension
		return PathNormalizer.asPath(result+url);
	}
	
	/**
	 * Return the cache busted url associated to the url passed in parameter 
	 * @param url the url path to the resource file
	 * @param is the resource input stream 
	 * @param jawrConfig the jawrConfig
	 * @return the cache busted url
	 * @throws IOException if an IO exception occurs.
	 */
	public static String getCacheBustedUrl(String url, InputStream is, JawrConfig jawrConfig) throws IOException{
		
		String checksum = null;
		boolean fromClasspath = url.startsWith(JawrConstant.CLASSPATH_RESOURCE_PREFIX);
		
		try {
			checksum = CheckSumUtils.getChecksum(is, jawrConfig.getImageHashAlgorithm());
		}
		finally {
			IOUtils.close(is);
		}
		
		String result = "";
		
		if(fromClasspath){
			result = JawrConstant.CLASSPATH_CACHE_BUSTER_PREFIX;
		}else{
			result = JawrConstant.CACHE_BUSTER_PREFIX;
		}
		
		result = result+checksum;
		if(fromClasspath){
			url = url.substring("jar:".length());
		}
		if(!url.startsWith("/")){
			result = result + "/";
		}
		// Add the cache buster extension
		return PathNormalizer.asPath(result+url);
	}
	
	
	/**
	 * Returns the checksum value of the input stream taking in count the algorithm passed in parameter
	 * @param is the input stream
	 * @param algorithm the checksum algorithm
	 * @return the checksum value
	 * @throws IOException if an exception occurs.
	 */
	public static String getChecksum(InputStream is, String algorithm) throws IOException {
	
		if(algorithm.equals(CRC32_ALGORITHM)){
			return getCRC32Checksum(is);
		}else if(algorithm.equals(MD5_ALGORITHM)){
			return getMD5Checksum(is);
		}else{
			throw new RuntimeException("The checksum algorithm '"+algorithm+"' is not supported.\n" +
					"The only supported algorithm are 'CRC32' or 'MD5'.");
		}
	}
	
	/**
	 * Returns the CRC 32 Checksum of the input stream
	 * 
	 * @param is the input stream
	 * 
	 * @return the CRC 32 checksum of the input stream
	 * @throws IOException if an IO exception occurs
	 */
	public static String getCRC32Checksum(InputStream is) throws IOException {

		Checksum checksum = new CRC32();

		byte[] bytes = new byte[1024];
		int len = 0;

		while ((len = is.read(bytes)) >= 0) {
			checksum.update(bytes, 0, len);
		}

		return Long.toString(checksum.getValue());
	}

	/**
	 * Returns the MD5 Checksum of the input stream
	 * 
	 * @param is the input stream
	 * 
	 * @return the Checksum of the input stream
	 * @throws IOException if an IO exception occurs
	 */
	public static String getMD5Checksum(InputStream is) throws IOException {

		byte[] digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
			is = new DigestInputStream(is, md);
			// read stream to EOF as normal...
			while (is.read() != -1) {

			}
			digest = md.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 algorithm needs to be installed", e);
		}

		return new BigInteger(1, digest).toString(16);
	}
}
