package org.gramar.extension;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPathFunction;

import org.gramar.IFileStore;
import org.gramar.IModelAdaptor;
import org.gramar.ITagHandler;
import org.gramar.ITemplatingExtension;
import org.gramar.exception.InvalidTemplateExtensionException;
import org.gramar.exception.NoSuchCustomTagException;
import org.gramar.exception.NoSuchFileStoreException;
import org.gramar.exception.NoSuchModelAdaptorException;
import org.gramar.model.DocumentHelper;
import org.gramar.model.ModelAccess;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public abstract class TemplatingExtension implements ITemplatingExtension {

	/**
	 * The extension ID in the form of a java package name.
	 */
	protected String extensionId;

	/**
	 * The default abbreviation (namespace) to use when referencing specific
	 * custom tags and xpath functions in this platform extension
	 */
	protected String abbreviation;
	
	protected String label;
	
	protected String provider;
	
	/**
	 * A cache of XPath functions defined in this extension.  The functions are 
	 * wrapped as a DefinedFunction which has a reference to the implementation
	 * of the function as well as metadata about the function, including arity,
	 * whether the function has a variable number of arguments and the name of
	 * the defined XPath function.
	 */
	protected ArrayList<DefinedFunction> functions = new ArrayList<DefinedFunction>();
	
	/**
	 * A cache of custom tag handlers defined in this extension, keyed by the name
	 * of the tag each enables
	 */
	private HashMap<String,DefinedTag> tags = new HashMap<String,DefinedTag>();
	
	/**
	 * A set of filestore classes defined in this extension, keyed by the filestore ID
	 */
	private HashMap<String,Class<IFileStore>> filestores = new HashMap<String,Class<IFileStore>>();
	
	/**
	 * A set of model adaptor classes defined in this extension, keyed by the model adaptor ID
	 */
	private HashMap<String,IModelAdaptor> modelAdaptors = new HashMap<String,IModelAdaptor>();
	 
	public static final String META_FILE_NAME = "extension.config";

	/**
	 * Constructs an extension with the given ID
	 * 
	 * @param extensionID
	 * @throws InvalidTemplateExtensionException
	 */
	public TemplatingExtension(String extensionID) throws InvalidTemplateExtensionException {
		this.extensionId = extensionID;
		loadMeta();
	}

	public void loadMeta() throws InvalidTemplateExtensionException {
		try {
			
			String source = getConfig();
			Document doc = DocumentHelper.buildModel(source);
			
			extensionId = ModelAccess.getDefault().getText(doc, "/extension/id");
			label = ModelAccess.getDefault().getText(doc, "/extension/label");
			provider = ModelAccess.getDefault().getText(doc, "/extension/provider");
			
			ClassLoader loader = getExtensionClassloader();
			
			Node[] node = ModelAccess.getDefault().getNodes(doc, "/extension/tags/tag", true, null);
			for (Node n : node) {
				String name = ModelAccess.getDefault().getAttribute(n, "@name");
				String fqn = ModelAccess.getDefault().getAttribute(n, "@handler");
				String control = ModelAccess.getDefault().getAttribute(n, "@controlTag");
				boolean controlTag = Boolean.parseBoolean(control);
				tags.put(name,new DefinedTag(name, fqn, controlTag, extensionId));
			}
			
			node = ModelAccess.getDefault().getNodes(doc, "/extension/functions/function", true, null);
			for (Node n : node) {
				String name = ModelAccess.getDefault().getAttribute(n, "@name");
				String parms = ModelAccess.getDefault().getAttribute(n, "@parms");
				String variable = ModelAccess.getDefault().getAttribute(n, "@variableNumber");
				String impl = ModelAccess.getDefault().getAttribute(n, "@impl");
				int arity = Integer.parseInt(parms);
				boolean variableParms = Boolean.parseBoolean(variable);
				XPathFunction function = (XPathFunction) loader.loadClass(impl).newInstance();
				functions.add(new DefinedFunction(name, arity, variableParms, function));
			}
			
			node = ModelAccess.getDefault().getNodes(doc, "/extension/filestores/filestore", true, null);
			for (Node n : node) {
				String id = ModelAccess.getDefault().getAttribute(n, "@id");
				String impl = ModelAccess.getDefault().getAttribute(n, "@impl");
				Class<IFileStore> filestore = (Class<IFileStore>) loader.loadClass(impl);
				filestores.put(id, filestore);
			}
			
			node = ModelAccess.getDefault().getNodes(doc, "/extension/adaptors/adaptor", true, null);
			for (Node n : node) {
				String id = ModelAccess.getDefault().getAttribute(n, "@id");
				String impl = ModelAccess.getDefault().getAttribute(n, "@impl");
				Class<IModelAdaptor> modelAdaptor = (Class<IModelAdaptor>) loader.loadClass(impl);
				modelAdaptors.put(id, (IModelAdaptor)modelAdaptor.newInstance());
			}

			
		} catch (Exception e) {
			throw new InvalidTemplateExtensionException(e);
		}
	}

	protected abstract ClassLoader getExtensionClassloader() throws Exception;

	@Override
	public Class loadClass(String fullyQualifiedName) throws ClassNotFoundException, Exception {
		return getExtensionClassloader().loadClass(fullyQualifiedName);
	}

	protected abstract String getConfig() throws Exception;

	public void addFunction(DefinedFunction definedFunction) {
		functions.add(definedFunction);
	}

	public XPathFunction getFunction(String name, int arity) {
		for (DefinedFunction df : functions) {
			if (df.matches(name, arity)) {
				return df.getFunction();
			}
		}
		return null;
	}

	public void setDefaultAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}
	
	@Override
	public String getDefaultAbbreviation() {
		return abbreviation;
	}

	public void setExtensionId(String id) {
		this.extensionId = id;
	}
	
	@Override
	public String getExtensionId() {
		return extensionId;
	}

	@Override
	public ITagHandler getCustomTagHandler(String tagName) throws NoSuchCustomTagException {
		DefinedTag dt = tags.get(tagName);
		if (dt == null) {
			throw new NoSuchCustomTagException();
		}
		ITagHandler handler;
		try {
			ClassLoader loader = getExtensionClassloader();
			handler = (ITagHandler) loader.loadClass(dt.getFQClassName()).newInstance();
		} catch (Exception e) {
			throw new NoSuchCustomTagException(e);
		}
		return handler;
	}

	@Override
	public DefinedTag getTagDef(String tagName) throws NoSuchCustomTagException {
		DefinedTag dt = tags.get(tagName);
		if (dt == null) {
			throw new NoSuchCustomTagException();
		}
		return dt;
	}

	@Override
	public boolean hasCustomTagHandler(String tagName) {
		return tags.containsKey(tagName);
	}

	@Override
	public IFileStore getFileStore(String fileStoreId) throws NoSuchFileStoreException {
		if (filestores.containsKey(fileStoreId)) {
			try {
				return filestores.get(fileStoreId).newInstance();
			} catch (InstantiationException e) {
				throw new NoSuchFileStoreException(e);
			} catch (IllegalAccessException e) {
				throw new NoSuchFileStoreException(e);
			}
		}
		throw new NoSuchFileStoreException();
	}

	@Override
	public IModelAdaptor getModelAdaptor(String adaptorID) throws NoSuchModelAdaptorException {
		if (modelAdaptors.containsKey(adaptorID)) {
			return modelAdaptors.get(adaptorID);
		}
		throw new NoSuchModelAdaptorException();
	}

	@Override
	public IModelAdaptor getModelAdaptorFor(String type) throws NoSuchModelAdaptorException {
		for (IModelAdaptor adaptor: modelAdaptors.values()) {
			if (adaptor.adaptsType(type)) {
				return adaptor;
			}
		}
		throw new NoSuchModelAdaptorException();
	}
	
}
