/*
 *
 * ToxOtis
 *
 * ToxOtis is the Greek word for Sagittarius, that actually means ‘archer’. ToxOtis
 * is a Java interface to the predictive toxicology services of OpenTox. ToxOtis is
 * being developed to help both those who need a painless way to consume OpenTox
 * services and for ambitious service providers that don’t want to spend half of
 * their time in RDF parsing and creation.
 *
 * Copyright (C) 2009-2010 Pantelis Sopasakis & Charalampos Chomenides
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact:
 * Pantelis Sopasakis
 * chvng@mail.ntua.gr
 * Address: Iroon Politechniou St. 9, Zografou, Athens Greece
 * tel. +30 210 7723236
 *
 */
package org.opentox.toxotis.util.spiders;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import org.opentox.toxotis.ErrorCause;
import org.opentox.toxotis.ToxOtisException;
import org.opentox.toxotis.client.ClientFactory;
import org.opentox.toxotis.client.IGetClient;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.client.collection.Media;
import org.opentox.toxotis.core.component.Compound;

/**
 * Downloader and parser for a compound resource available in RDF.
 * @author Charalampos Chomenides
 * @author Pantelis Sopasakis
 */
public class CompoundSpider extends Tarantula<Compound> {

    VRI uri;
    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CompoundSpider.class);

    public CompoundSpider(VRI uri) throws ToxOtisException {
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
        } catch (IOException ex) {
            throw new ToxOtisException("Communication Error with the remote service at :" + uri, ex);
        } finally { // Have to close the client (disconnect)
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

    public CompoundSpider(Resource resource, OntModel model) {
        super(resource, model);
        try {
            uri = new VRI(resource.getURI());
        } catch (URISyntaxException ex) {
            logger.debug(null, ex);
        }
    }

    public CompoundSpider(OntModel model, String uri) {
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
    public Compound parse() throws ToxOtisException {
        Compound compound = new Compound(uri);
        compound.setMeta(new MetaInfoSpider(resource, model).parse());
        return compound;
    }
}
