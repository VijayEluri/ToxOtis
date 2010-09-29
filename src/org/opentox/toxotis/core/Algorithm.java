package org.opentox.toxotis.core;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.opentox.toxotis.ToxOtisException;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.ontology.MetaInfo;
import org.opentox.toxotis.ontology.OntologicalClass;
import org.opentox.toxotis.ontology.collection.OTClasses;
import org.opentox.toxotis.ontology.collection.OTObjectProperties;
import org.opentox.toxotis.ontology.impl.SimpleOntModelImpl;
import org.opentox.toxotis.util.aa.AuthenticationToken;
import org.opentox.toxotis.util.spiders.AlgorithmSpider;

/**
 * Provides access to different types of algorithms. An algorithm object contains
 * very general metadata about an algorithm and in fact describes its input and
 * output requirements and gives information about its parametrization. The different
 * types of algorithms used in <a href="http://opentox.org">OpenTox</a> and the
 * connection to each other is formally established throught the 
 * <a href="http://opentox.org/dev/apis/api-1.1/Algorithms">OpenTox Algorithm Ontology</a>.
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class Algorithm extends OTOnlineResource<Algorithm> implements OntologyServiceSupport<Algorithm>{

    /** Set of parameters of the algorithm. Specify the way the algorithm is parametrized */
    private Set<Parameter> parameters = new HashSet<Parameter>();
    /** Set of ontological classes that characterize the algorithm*/
    private Collection<OntologicalClass> ontologies;

    /**
     * Create a new instance of Algorithm providing its identifier as a {@link VRI }.
     * @param uri
     *      The URI of the algorithm as a {@link VRI }.
     */
    public Algorithm(VRI uri) throws ToxOtisException {
        super(uri);
        if (uri != null) {
            if (!Algorithm.class.equals(uri.getOpenToxType())) {
                throw new ToxOtisException("The provided URI : '" + uri.getStringNoQuery()
                        + "' is not a valid Algorithm uri according to the OpenTox specifications.");
            }
        }
    }

    /**
     * Constructs a new instance of Algorithm with given URI.
     * @param uri
     *      URI of the algorithm
     * @throws URISyntaxException
     *      In case the provided string cannot be cast as a {@link VRI }.
     */
    public Algorithm(String uri) throws URISyntaxException {
        super(new VRI(uri));
    }

    /**
     * Get the ontological classes of the algorithm
     * @return
     *      The collection of ontological classes for this algorithm.
     */
    public Collection<OntologicalClass> getOntologies() {
        return ontologies;
    }

    /**
     * Specify the ontologies for this algorithm.
     * @param ontologies
     *      A collection of ontological classes that characterize this algorithm.
     */
    public void setOntologies(Collection<OntologicalClass> ontologies) {
        this.ontologies = ontologies;
    }

    /**
     * Retrieve the set of parameters for this algorithm.
     * @return
     *      Set of parameters.
     */
    public Set<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Set the parameters of the algorithm.
     * @param parameters
     *      Set of parameters.
     */
    public Algorithm setParameters(Set<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    public Individual asIndividual(OntModel model) {
        Individual indiv = model.createIndividual(getUri().getStringNoQuery(), OTClasses.Algorithm().inModel(model));
        indiv.addComment(model.createTypedLiteral("Representation automatically generated by ToxOtis.",
                XSDDatatype.XSDstring));

        final MetaInfo metaInfo = getMeta();
        if (metaInfo != null) {
            metaInfo.attachTo(indiv, model);
        }

        if (ontologies != null && !ontologies.isEmpty()) {
            final Iterator<OntologicalClass> ontClassIter = ontologies.iterator();
            while (ontClassIter.hasNext()) {
                indiv.addRDFType(ontClassIter.next().inModel(model));
            }
        }
        if (parameters != null) {
            for (Parameter param : parameters) {
                indiv.addProperty(OTObjectProperties.parameters().asObjectProperty(model), param.asIndividual(model));
            }
        }
        return indiv;
    }

    /**
     * Allows algorithms to be created from arbitrary input sources
     * (in general files, URLs or other).Note that this is still an experimental
     * method.
     *
     * @param stream
     *      Input stream used to create the algorithm
     * @param uri
     *      The URI of the algorithm resource. To obfuscate any misunderstanding
     *      we underline that this URI needs not be a URL, i.e. it will not be used
     *      to retrieve any information from the corresponding (remote) location
     *      but serves exclusively as a reference. It indicates which individual
     *      or the data model should be parsed. If set to <code>null</code>, then
     *      an arbitrary individual is chosen. This is not a good practise in cases
     *      where more than one instances of <code>ot:Algorithm</code> might be
     *      present in the same data model.
     *
     * @return
     *      Updates this algorithm object and returns the updated instance.
     *
     * @throws ToxOtisException
     *      In case the input stream does not provide a valid data model for
     *      an algorithm
     */
    public Algorithm loadFromRemote(InputStream stream, VRI uri) throws ToxOtisException {
        com.hp.hpl.jena.ontology.OntModel om = new SimpleOntModelImpl();
        om.read(stream, null);
        AlgorithmSpider spider = new AlgorithmSpider(null, om);
        throw new UnsupportedOperationException();
    }

    @Override
    protected Algorithm loadFromRemote(VRI uri) throws ToxOtisException {
        AlgorithmSpider spider = new AlgorithmSpider(uri);
        Algorithm algorithm = spider.parse();
        setMeta(algorithm.getMeta());
        setOntologies(algorithm.getOntologies());
        setParameters(algorithm.getParameters());
        return this;
    }

    public Algorithm publishToOntService(AuthenticationToken token) throws ToxOtisException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
