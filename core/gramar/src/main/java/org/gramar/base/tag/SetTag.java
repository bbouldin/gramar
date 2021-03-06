package org.gramar.base.tag;

import org.gramar.ITagHandler;
import org.gramar.IGramarContext;
import org.gramar.resource.MergeStream;
import org.gramar.tag.TagHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SetTag extends TagHandler implements ITagHandler {

	public SetTag() {

	}

	@Override
	public void mergeTo(MergeStream stream, IGramarContext context) {
		
		try {

			Node node = getNodeAttribute("select", context);
			String name = getStringAttribute("name", context);

			MergeStream newStream = processChildren(context);
			String content = newStream.toString();
			
			Element element = (Element) node;
			if (element != null) {
				element.setAttribute(name, content);
			}
			
		} catch (Exception e) {
			context.error(e);
			logStackTrace(context);
		}

	}

	@Override
	public String getTagName() {
		return "set";
	}

}
