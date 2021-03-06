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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.IOException;
import java.net.URISyntaxException;
import org.opentox.toxotis.client.ClientFactory;
import org.opentox.toxotis.client.IGetClient;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.client.collection.Media;
import org.opentox.toxotis.core.component.Task;
import org.opentox.toxotis.exceptions.impl.ToxOtisException;
import org.opentox.toxotis.ontology.collection.OTDatatypeProperties;
import org.opentox.toxotis.ontology.collection.OTObjectProperties;
import org.opentox.toxotis.util.aa.AuthenticationToken;
import org.opentox.toxotis.core.component.User;
import org.opentox.toxotis.exceptions.impl.ConnectionException;
import org.opentox.toxotis.exceptions.impl.RemoteServiceException;
import org.opentox.toxotis.exceptions.impl.ServiceInvocationException;

/**
 *
 * @author Charalampos Chomenides
 * @author Pantelis Sopasakis
 */
public class TaskSpider extends Tarantula<Task> {

    private VRI vri;
    private AuthenticationToken token;
    private int httpStatus = -1;
    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TaskSpider.class);

    public TaskSpider() {
    }

    public TaskSpider(VRI vri) throws ServiceInvocationException {
        this(vri, (AuthenticationToken) null);
    }

    public TaskSpider(VRI vri, AuthenticationToken token) throws ServiceInvocationException {
        this.vri = vri;
        this.token = token;
        IGetClient client = ClientFactory.createGetClient(vri);
        client.authorize(token);
        client.setMediaType(Media.APPLICATION_RDF_XML);
        try {
            final int status = client.getResponseCode();
            assessHttpStatus(status, vri);
            httpStatus = status;
            setOntModel(client.getResponseOntModel());
            setResource(getOntModel().getResource(vri.getStringNoQuery()));
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException ex) {
                    throw new ConnectionException(
                            "StreamCouldNotClose: Error while trying to close the stream "
                            + "with the remote location at :'" + ((vri != null)
                            ? vri.toString() : null) + "'", ex);
                }
            }
        }
    }

    public TaskSpider(Resource resource, OntModel model) {
        super(resource, model);
    }

    @Override
    public Task parse() throws ServiceInvocationException {
        Task task = new Task(vri);
        task.setMeta(new MetaInfoSpider(getResource(), getOntModel()).parse());
        task.setHttpStatus(httpStatus);

        if (token != null) {
            try {
                User u = token.getUser();
                task.setCreatedBy(u);
            } catch (final ToxOtisException ex) {
                logger.info("BAD USER EMAIL!!!");
                throw new RemoteServiceException("User with RFC-invalid email returned from remote SSO service", ex);
            } catch (ServiceInvocationException ex) {
                logger.info(null, ex);
                throw ex;
            }
        }

        Statement hasStatusProp = getResource().getProperty(
                OTDatatypeProperties.hasStatus().asProperty(getOntModel()));
        if (hasStatusProp != null) {
            Literal hasStatus = hasStatusProp.getObject().as(Literal.class);
            if (hasStatus != null) {
                task.setStatus(Task.Status.valueOf(hasStatus.getString().toUpperCase()));
            }
        }

        Statement resultUriStmt = getResource().getProperty(
                OTDatatypeProperties.resultURI().asDatatypeProperty(getOntModel()));
        Literal resultUri = null;
        if (resultUriStmt != null) {
            resultUri = resultUriStmt.getObject().as(Literal.class);
        }

        if (resultUri != null) {
            try {
                task.setResultUri(new VRI(resultUri.getString()));
            } catch (URISyntaxException ex) {
                logger.debug(null, ex);
            }
        }
        Statement percentageStmt = getResource().getProperty(
                OTDatatypeProperties.percentageCompleted().asDatatypeProperty(getOntModel()));
        Literal percentageCompleted = percentageStmt != null ? percentageStmt.getObject().as(Literal.class) : null;

        if (percentageCompleted != null) {
            task.setPercentageCompleted(percentageCompleted.getFloat());
        }

        Statement errorReportStmt = getResource().getProperty(
                OTObjectProperties.errorReport().asObjectProperty(getOntModel()));
        Resource errorReport = errorReportStmt != null ? errorReportStmt.getObject().as(Resource.class) : null;

        if (errorReport != null) {
            task.setErrorReport(new ErrorReportSpider(errorReport, getOntModel()).parse());
        }
        return task;
    }
}
