package org.opentox.toxotis.ontology.collection;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import org.opentox.toxotis.ontology.OTObjectProperty;
import org.opentox.toxotis.ontology.impl.OTObjectPropertyImpl;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class OTRestObjectProperties {

    public static final String NS = "http://opentox.org/opentox-rest.owl#";
    private static OTObjectProperty ms_hasHTTPMethod;
    private static OTObjectProperty ms_inputParam;
    private static OTObjectProperty ms_paramContent;
    private static OTObjectProperty ms_resource;
    private static OTObjectProperty ms_result;
    private static OTObjectProperty ms_uri;

    public static OTObjectProperty hasHTTPMethod() {
        if (ms_hasHTTPMethod == null) {
            OTObjectProperty clazz = new OTObjectPropertyImpl("hasHTTPMethod", NS);
            clazz.getDomain().add(OTRestClasses.RESTOperation());
            clazz.getRange().add(OTRestClasses.HTTPMethod());
            ms_hasHTTPMethod = clazz;
        }
        return ms_hasHTTPMethod;
    }

    public static OTObjectProperty inputParam() {
        if (ms_inputParam == null) {
            OTObjectProperty clazz = new OTObjectPropertyImpl("inputParam", NS);
            clazz.getDomain().add(OTRestClasses.RESTOperation());
            clazz.getDomain().add(OTRestClasses.POST_AlgorithmCreateModel());
            clazz.getRange().add(OTRestClasses.InputParameter());
            ms_inputParam = clazz;
        }
        return ms_inputParam;
    }

    public static OTObjectProperty result() {
        if (ms_result == null) {
            OTObjectProperty clazz = new OTObjectPropertyImpl("result", NS);
            clazz.getDomain().add(OTRestClasses.RESTOperation());
            clazz.getDomain().add(OTClasses.OpenToxResource());
            ms_result = clazz;
        }
        return ms_result;
    }
}
