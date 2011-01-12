package org.opentox.toxotis.util.spiders;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import org.opentox.toxotis.ErrorCause;
import org.opentox.toxotis.ToxOtisException;
import org.opentox.toxotis.client.ClientFactory;
import org.opentox.toxotis.client.IGetClient;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.client.collection.Media;
import org.opentox.toxotis.core.component.Feature;
import org.opentox.toxotis.ontology.LiteralValue;
import org.opentox.toxotis.ontology.collection.OTClasses;
import org.opentox.toxotis.ontology.collection.OTDatatypeProperties;

/**
 * Downloader and parser for a Feature resource available in RDF.s
 * @author Charalampos Chomenides
 * @author Pantelis Sopasakis
 */
public class FeatureSpider extends Tarantula<Feature> {

    VRI uri;

    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FeatureSpider.class);

    public FeatureSpider(VRI uri) throws ToxOtisException {
        super();
        this.uri = uri;
        IGetClient client = ClientFactory.createGetClient(uri);
        try {
            client.setMediaType(Media.APPLICATION_RDF_XML.getMime());
            client.setUri(uri);
            int status = client.getResponseCode();
            assessHttpStatus(status, uri);
            model = client.getResponseOntModel();
            resource = model.getResource(uri.toString());
        } catch (final IOException ex) {
            throw new ToxOtisException("Communication Error with the remote service at :" + uri, ex);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException ex) {
                    throw new ToxOtisException(ErrorCause.StreamCouldNotClose,
                            "Error while trying to close the stream "
                            + "with the remote location at :'" + ((uri != null) ? uri.clearToken().toString() : null) + "'", ex);
                }
            }
        }
    }

    public FeatureSpider(Resource resource, OntModel model) {
        super(resource, model);
        try {
            uri = new VRI(resource.getURI());
        } catch (URISyntaxException ex) {
            logger.debug(null, ex);
        }
    }

    public FeatureSpider(OntModel model, String uri) {
        super();
        this.model = model;
        try {
            this.uri = new VRI(uri);
        } catch (URISyntaxException ex) {
            logger.debug(null, ex);
        }
        this.resource = model.getResource(uri);
    }

    @Override
    public Feature parse() {
        Feature feature = new Feature();
        feature.setMeta(new MetaInfoSpider(resource, model).parse()); // Parse meta-info
        feature.setUri(uri);
        feature.setOntologicalClasses(getOntologicalTypes(resource));
        Statement unitsStatement = resource.getProperty(OTDatatypeProperties.units().asDatatypeProperty(model));
        if (unitsStatement != null) {
            feature.setUnits(unitsStatement.getString());
        }

        if (feature.getOntologicalClasses() != null && feature.getOntologicalClasses().contains(OTClasses.NominalFeature())) {
            // Gather 'accept' values from the RDF and add them to the feature
            Set<LiteralValue> admissibleValues = new HashSet<LiteralValue>();
            StmtIterator acceptIt = resource.listProperties(OTDatatypeProperties.acceptValue().asDatatypeProperty(model));
            while (acceptIt.hasNext()) {
                Literal acceptValueLiteral = acceptIt.nextStatement().getObject().as(Literal.class);
                LiteralValue acceptValue = new LiteralValue(acceptValueLiteral.getValue().toString(),
                        (XSDDatatype) acceptValueLiteral.getDatatype());
                admissibleValues.add(acceptValue);
            }
            feature.setAdmissibleValues(admissibleValues);
        }

        return feature;
    }
}