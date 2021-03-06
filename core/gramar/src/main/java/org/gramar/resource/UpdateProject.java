package org.gramar.resource;

import java.io.IOException;

import org.gramar.IFileStore;

public class UpdateProject extends UpdateResource {

	private String altPath;
	
	public UpdateProject(String path, String altPath) {
		super(path);
		this.altPath = altPath;
	}

	@Override
	public void execute(IFileStore store) throws Exception {

		store.createProject(path, altPath);
		
	}
	
	public String toString() {
		return "UpdateProject: " + path;
	}

	public String report() {
		return "created project "+path;
	}
	
	public boolean isProject() {
		return true;
	}

}
