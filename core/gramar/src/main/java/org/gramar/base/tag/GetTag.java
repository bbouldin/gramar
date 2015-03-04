package org.gramar.base.tag;

import org.gramar.ITagHandler;
import org.gramar.IGramarContext;
import org.gramar.filestore.MergeStream;
import org.gramar.tag.TagHandler;


public class GetTag extends TagHandler implements ITagHandler {

	public GetTag() {

	}

	@Override
	public String getTagName() {
		return "get";
	}

	@Override
	public void mergeTo(MergeStream stream, IGramarContext context) {

		try {
			String select = getAttributes().get("select");
			String value = context.resolveToString(select);
			stream.append(value);
		} catch (Exception e) {
			context.error(e);
		}

	}

}
