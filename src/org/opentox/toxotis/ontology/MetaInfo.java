package org.opentox.toxotis.ontology;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.Collection;
import java.util.Date;
import org.opentox.toxotis.util.spiders.TypedValue;

/**
 * <p align=justify width=80%>
 * MetaInfo provides access to a set of meta-information about
 * a resource. Such are the identifier, the title and other related meta. Formally
 * these are included in the Dublin Core ontology for modeling meta-data and partially
 * in the OpenTox ontology (e.g. hasSource) for modeling some OT-specific properties.
 * </p>
 *
 * @author Sopasakis Pantelis
 *
 * @see http://dublincore.org/documents/usageguide/elements.shtml
 */
public interface MetaInfo extends java.io.Serializable {
//TODO: add bibtex?

    /**
     * The property <code>rdfs:comment</code> is used to provide a human-readable
     * description of a resource.
     *
     * @return
     *      A comment on the described entity.
     */
    TypedValue<String> getComment();

    /**
     * An account of the content of the resource. Description may include but is not
     * limited to: an abstract, table of contents, reference to a graphical representation
     * of content or a free-text account of the content.
     * @return
     *      Description as a typed value
     */
    TypedValue<String> getDescription();

    /**
     * Get the identifier (ID) of the described entity. An identifier is an unambiguous
     * reference to the resource within a given context. Recommended best practice is
     * to identify the resource by means of a string or number conforming to a
     * formal identification system. Examples of formal identification systems include
     * the Uniform Resource Identifier (URI) (including the Uniform Resource Locator (URL),
     * the Digital Object Identifier (DOI) and the International Standard Book Number (ISBN).
     * @return
     *      Identifier as a Typed Value
     * @see http://dublincore.org/documents/usageguide/elements.shtml
     */
    TypedValue<String> getIdentifier();

    /**
     * The built-in OWL property <code>owl:sameAs</code> links an individual to an individual.
     * Such an <code>owl:sameAs</code> statement indicates that two URI references
     * actually refer to the same thing: the individuals have the same "identity".
     * For individuals such as "people" this notion is relatively easy to understand.
     * For example, we could state that the following two URI references actually refer
     * to the same person:
     * <pre>
     * &lt;rdf:Description rdf:about="#William_Jefferson_Clinton"&gt;
     * &lt;owl:sameAs rdf:resource="#BillClinton"/&gt;
     * &lt;/rdf:Description&gt;
     * </pre>
     * The <code>owl:sameAs</code> statements are often used in defining mappings between ontologies.
     * It is unrealistic to assume everybody will use the same name to refer to individuals.
     * That would require some grand design, which is contrary to the spirit of the web.
     * @return
     *      A link to a resource that resembles the described entity
     *
     */
    TypedValue<String> getSameAs();

    /**
     * Related to the datatype ontological property <code>rdfs:seeAlso</code>. The property
     * <code>rdfs:seeAlso</code> specifies a resource that might provide additional information
     * about the subject resource. This property may be specialized using rdfs:subPropertyOf
     * to more precisely indicate the nature of the information the object resource has about the
     * subject resource. The object and the subject resources are constrained only to be instances
     * of the class rdfs:Resource.
     *
     * @return
     *      A reference to some other entity as a typed value.
     */
    TypedValue<String> getSeeAlso();

    /**
     * Get the name given to the resource.
     * Typically, a Title will be a name by which the resource is formally known.
     * @return
     *      Tile as a Typed Value
     */
    TypedValue<String> getTitle();

    /**
     * Ge the topic of the content of the resource. Typically, a Subject will be
     * expressed as keywords or key phrases or classification codes that describe the
     * topic of the resource. Recommended best practice is to select a value from
     * a controlled vocabulary or formal classification scheme.
     * @return
     *      Subject as a typed value
     */
    TypedValue<String> getSubject();

    TypedValue<String> getVersionInfo();

    TypedValue<String> getPublisher();

    /**
     * The <code>creator</code> is an entity primarily responsible for making the content
     * of the resource. Examples of a Creator include a person, an organization, or a service.
     * Typically the name of the Creator should be used to indicate the entity.
     * @return
     *      Creator as a typed value
     */
    //TODO: Change this into a list of creators
    TypedValue<String> getCreator();

    TypedValue<String> getHasSource();

    Collection<TypedValue<String>> getContributors();

    Collection<TypedValue<String>> getAudiences();

    MetaInfo setComment(String comment);

    MetaInfo setComment(TypedValue<String> comment);

    MetaInfo setDescription(String description);

    MetaInfo setDescription(TypedValue<String> description);

    MetaInfo setIdentifier(String identifier);

    MetaInfo setIdentifier(TypedValue<String> identifier);

    MetaInfo setSameAs(String sameAs);

    MetaInfo setSameAs(TypedValue<String> sameAs);

    MetaInfo setSeeAlso(String seeAlso);

    MetaInfo setSeeAlso(TypedValue<String> seeAlso);

    MetaInfo setTitle(String title);

    MetaInfo setTitle(TypedValue<String> title);

    MetaInfo setSubject(String subject);

    MetaInfo setSubject(TypedValue<String> subject);

    MetaInfo setVersionInfo(String versionInfo);

    MetaInfo setVersionInfo(TypedValue<String> versionInfo);

    MetaInfo setPublisher(String publisher);

    MetaInfo setPublisher(TypedValue<String> publisher);

    MetaInfo setCreator(String creator);

    MetaInfo setCreator(TypedValue<String> creator);

    MetaInfo setHasSource(String hasSource);

    MetaInfo setHasSource(TypedValue<String> hasSource);

    MetaInfo addContributor(String contributor);

    MetaInfo addContributor(TypedValue<String> contributor);

    MetaInfo addAudience(String audience);

    MetaInfo addAudience(TypedValue<String> audience);

    TypedValue<Date> getDate();

    MetaInfo setDate(TypedValue<Date> date);

    MetaInfo setDate(Date date);

    /**
     * Attaches the meta data to a given resource in an ontological model
     * returning the updated resource. The provided ontological model is also updated
     * with the meta data assigned to the given resource.
     * 
     * @param resource
     *      A resource from an ontological model to which meta data are to
     *      be assigned. The resource provided as input to this method is updated
     *      according to the metadata and various statements are assigned to it
     *      concerning the non-null fields of the metadata object to which the method
     *      is applied.<br/>
     * @param model
     *      The ontological model holding the individual provided in this method.
     * @return
     *      The updated resource with the metadata.
     */
    Resource attachTo(Resource resource, OntModel model);

    /**
     * Write the meta information in RDF/XML format using an XML Stream Writer. The
     * data are written at the current position of the cursor of the writer. The method
     * appends meta information about the underlying resource that is described in the
     * RDF/XML document. Note that the writter should include at least the following
     * namespaces before the invokation of this method:
     *
     * <pre>
     * ot   : http://www.opentox.org/api/1.1#
     * rdfs : http://www.w3.org/2000/01/rdf-schema#
     * rdf  : http://www.w3.org/1999/02/22-rdf-syntax-ns#
     * dc   : http://purl.org/dc/elements/1.1/
     * </pre>
     *
     * The following code can be used to declare the above namespaces:
     *
     * <pre>
     * writer.writeNamespace("ot", OTClasses.NS);
     * writer.writeNamespace("rdfs", RDFS.getURI());
     * writer.writeNamespace("rdf", RDF.getURI());
     * writer.writeNamespace("dc", DC.NS);
     * writer.writeNamespace("owl", OWL.NS);
     * </pre>
     *
     * Additionally you can declare the above as a set of predixes:
     *
     * <pre>
     * writer.setPrefix("ot", OTClasses.NS);
     * writer.setPrefix("rdfs", RDFS.getURI());
     * writer.setPrefix("rdf", RDF.getURI());
     * writer.setPrefix("dc", DC.NS);
     * writer.setPrefix("owl", OWL.NS);
     * </pre>
     * 
     * @param writer
     *      Writer used to write XML data to an Output Stream
     * @throws javax.xml.stream.XMLStreamException
     *      In case there the data that try to be written are not consisent. For example
     *      such an exception is thrown if you forget to close a non-empty element
     *      in the XML.
     */
    void writeToStAX(javax.xml.stream.XMLStreamWriter writer) throws javax.xml.stream.XMLStreamException;
}
