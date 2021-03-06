package org.opentox.toxotis.core.html.impl;

import java.util.ArrayList;
import java.util.List;
import org.opentox.toxotis.core.html.HTMLComponent;
import org.opentox.toxotis.core.html.HTMLForm;

/**
 * An HTML Form is a container of the form:
 *<pre>
 *  &lt;form action="URI" method="GET/POST"&gt;
 *   Some content goes here (e.g. HTML Inputs)....
 *  &lt;/form&gt;
 *</pre>
 * @author Pantelis Sopasakis
 */
public class HTMLFormImpl implements HTMLForm {

    private String actionUrl;
    private String mediaType;
    private String method;
    private String style="";
    private List<HTMLComponent> content = new ArrayList<HTMLComponent>();

    
    @Override
    public HTMLForm setMediaType(String m_type) {
        this.mediaType = m_type;
        return this;
    }

    
    @Override
    public HTMLForm setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
        return this;
    }

    @Override
    public List<HTMLComponent> getComponents() {
        return content;
    }

    @Override
    public HTMLForm setMethod(String method) {
        this.method = method;
        return this;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getActionUrl() {
        return actionUrl;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }
    
    @Override
    public void addComponent(HTMLComponent component) {
        this.content.add(component);
    }
    
    
    @Override
    public HTMLForm setStyle(String varStyle) {
        this.style = varStyle;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("<form method=\"%s\" style=\"%s\" action=\"%s\" enctype=\"%s\">", method != null ? method : "POST",style, actionUrl != null ? actionUrl : ".",mediaType != null ? mediaType : ""));
        for (HTMLComponent component : content) {
            builder.append(component.toString());
        }
        builder.append("</form>");
        return builder.toString();
    }
}
