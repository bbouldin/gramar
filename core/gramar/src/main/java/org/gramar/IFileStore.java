package org.gramar;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.gramar.exception.GramarException;
import org.gramar.exception.NoSuchResourceException;
import org.gramar.resource.UpdateResource;


/**
 * Represents a method to store and retrieve files.
 * 
 * @author chrisgerken
 *
 */
public interface IFileStore {

	/**
	 * Retrieve the content of the specified file.  The path is assumed to start at
	 * a project within the file store. 
	 * 
	 * Used mostly by user region and load support
	 */
	public Reader getFileContent(String path) throws NoSuchResourceException;
	
	/**
	 * Store the content of the specified file.  The path is assumed to start at
	 * a project within the file store. 
	 */
	public void setFileContent(String path, Reader reader) throws NoSuchResourceException, IOException;

	/**
	 * Retrieve the content of the specified file.  The path is assumed to start at
	 * a project within the file store. 
	 *
	 */
	public InputStream getFileByteContent(String path) throws NoSuchResourceException;
	
	/**
	 * Store the content of the specified file.  The path is assumed to start at
	 * a project within the file store. 
	 */
	public void setFileContent(String path, InputStream stream) throws NoSuchResourceException, IOException;
	
	/**
	 * Answers a boolean indicating that the resource at the given path exists (true) or
	 * that it does not (false)
	 */
	public boolean resourceExists(String path);
	
	/**
	 * Create a target project with the given name.  If non null or blank, the 
	 * altPath represents a store-dependent alternate or relative location to be used
	 * in the creation of the project
	 */
	public void createProject(String projectName, String altPath) throws Exception;
	
	public void createFolder(String pathName) throws NoSuchResourceException, IOException;
	
	/**
	 * Initialize to prepare for gramar application.  Does not change the state of any resources.
	 */
	public void reset();
	
	/**
	 * Commit to the file store all pending resource changes
	 */
	public void commit(String comment, IGramarContext context) throws GramarException;
	
	/**
	 * Add an update request to the set of changes that need to be made as a result of
	 * applying this gramar
	 */
	public void addUpdate(UpdateResource update);
	
	/***
	 * Logs a message in a FileStore-appropriate way
	 */
	public void log(String message);

	/**
	 * Free any cached resources
	 */
	public void free();
	
	/**
	 * Configure the filestore with values from the given properties file.
	 * 
	 * @param properties
	 * @throws GramarException 
	 */
	public void configure(Properties properties) throws GramarException;
	
}
