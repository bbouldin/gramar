package org.gramar.plugin;

import java.io.File;
import java.util.HashMap;

import org.gramar.IFileStore;
import org.gramar.IGramar;
import org.gramar.IPluginSource;
import org.gramar.ITemplatingExtension;
import org.gramar.exception.NoSuchFileStoreException;
import org.gramar.exception.NoSuchTemplatingExtensionException;
import org.gramar.gramar.JarGramar;
import org.gramar.gramar.ZipFileGramar;


public class FileSystemPluginSource extends PluginSource implements IPluginSource {

	private String rootDir;
	
	public FileSystemPluginSource(String rootDir) {
		this.rootDir = rootDir;
	}

	@Override
	public void gather(HashMap<String, IGramar> map) {
		super.gather(map);
		File root = new File(rootDir);
		if (root.isDirectory()) {
			gather(root,map);
		}
	}

	private void gather(File dir, HashMap<String, IGramar> map) {
		if (!dir.isDirectory()) {
			return;
		}
		String entry[] = dir.list();
		for (String name : entry) {
			File f = new File(dir,name);
			if (f.isDirectory()) {
				gather(f,map);
			} else if (f.isFile()) {
				int offset = name.lastIndexOf(".");
				if (offset > -1) {
					String allegedId = name.substring(0, offset);
					String type = name.substring(offset+1);
					if (type.equals("zip")) {
						try {
							IGramar pattern = new ZipFileGramar(f);
							String id = pattern.getId();
							if (!map.containsKey(id)) {
								map.put(pattern.getId(), pattern);
							}
						} catch (Exception e) {

						}
					}
					if (type.equals("jar")) {
						try {
							IGramar pattern = new JarGramar(allegedId,f.getAbsolutePath());
							String id = pattern.getId();
							if (!map.containsKey(id)) {
								map.put(pattern.getId(), pattern);
							}
						} catch (Exception e) {

						}
					}
				}
			}
		}
	}

	@Override
	public ITemplatingExtension getTemplatingExtension(String extensionId)
			throws NoSuchTemplatingExtensionException {
		// TODO Auto-generated method stub
		return null;
	}

}
