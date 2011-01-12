package org.opentox.toxotis.core.component;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.client.collection.Services;
import org.opentox.toxotis.core.IHTMLSupport;
import org.opentox.toxotis.core.OTComponent;
import org.opentox.toxotis.core.html.Alignment;
import org.opentox.toxotis.core.html.HTMLContainer;
import org.opentox.toxotis.core.html.HTMLDivBuilder;
import org.opentox.toxotis.core.html.impl.HTMLParagraphImpl;
import org.opentox.toxotis.core.html.impl.HTMLTextImpl;
import org.opentox.toxotis.ontology.collection.OTClasses;
import org.opentox.toxotis.ontology.collection.OTDatatypeProperties;
import org.opentox.toxotis.ontology.impl.MetaInfoImpl;

/**
 * Error Reports are part of the OpenTox API since version 1.1. Error Reports define a
 * formal way to handle exceptional situations while invoking a service or during
 * inter-service communication thus facilitating debugging. They are sufficiently
 * documented online at <a href="http://opentox.org/dev/apis/api-1.1/Error%20Reports">
 * http://opentox.org/dev/apis/api-1.1/Error Reports</a>.
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class ErrorReport extends OTComponent<ErrorReport>
        implements IHTMLSupport {

    /** The HTTP status that accompanied the Error Report */
    private int httpStatus;
    /** The peer that threw the exception or reported an exceptional event */
    private String actor;
    /** Brief explanatory message */
    private String message;
    /** Technical Details */
    private String details;
    /** Error Cause Identification Code */
    private String errorCode;
    /** Trace... */
    private ErrorReport errorCause;
    private UUID uuid = UUID.randomUUID();
    private static final String DISCRIMINATOR = "error";
    private static Map<Integer, String> errorCodeReference;

    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ErrorReport.class);

    static {
        errorCodeReference = new HashMap<Integer, String>();
        errorCodeReference.put(400, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1");// Bad request
        errorCodeReference.put(401, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.2");// Unauthorized
        errorCodeReference.put(403, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.4");// Forbidden
        errorCodeReference.put(404, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5");// Not found
        errorCodeReference.put(405, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6");// Method not allowed
        errorCodeReference.put(406, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.7");// Not acceptable

        errorCodeReference.put(500, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.1");// Internal server error
        errorCodeReference.put(501, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.2");// Not implemented
        errorCodeReference.put(502, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.3");// Bad Gateway
        errorCodeReference.put(503, "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.4");// Service Unavailable

    }

    public ErrorReport() {
    }

    public ErrorReport(VRI uri) {
        super(uri);
    }

    public ErrorReport(int httpStatus, String actor, String message, String details, String errorCode) {
        this.httpStatus = httpStatus;
        this.actor = actor;
        this.message = message;
        this.details = details;
        this.errorCode = errorCode;
    }

    @Override
    public VRI getUri() {
        if (uri == null) {
            uri = Services.anonymous().augment(DISCRIMINATOR, uuid.toString());
        }
        return uri;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public ErrorReport getErrorCause() {
        return errorCause;
    }

    public void setErrorCause(ErrorReport errorCause) {
        this.errorCause = errorCause;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int errorCode) {
        this.httpStatus = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public Individual asIndividual(OntModel model) {
        if (getUri() == null) {
            try {
                long hc = (long) hashCode();
                long sgn = (long) Math.signum((double) hc);
                sgn = (sgn == 1L) ? 1L : 2L;
                hc *= sgn;
                hc = Math.abs(hc);
                String URI = "http://opentox.org/errorReport/#" + hc;
                setUri(new VRI(URI));
            } catch (URISyntaxException ex) {
                logger.debug(null, ex);
                throw new RuntimeException(ex);
            }
        }
        Individual indiv = model.createIndividual(getUri() != null ? getUri().getStringNoQuery()
                : null, OTClasses.ErrorReport().inModel(model));
        if (getMeta() == null) {
            setMeta(new MetaInfoImpl());
        }
        getMeta().addIdentifier(getUri() != null ? getUri().toString() : null);
        getMeta().addTitle("Error report produced by '" + getActor() + "'");
        getMeta().attachTo(indiv, model);
        if (message != null) {
            indiv.addLiteral(OTDatatypeProperties.message().asDatatypeProperty(model),
                    model.createTypedLiteral(message, XSDDatatype.XSDstring));
        }
        if (details != null) {
            indiv.addLiteral(OTDatatypeProperties.details().asDatatypeProperty(model),
                    model.createTypedLiteral(details, XSDDatatype.XSDstring));
        }
        if (actor != null) {
            indiv.addLiteral(OTDatatypeProperties.actor().asDatatypeProperty(model),
                    model.createTypedLiteral(actor, XSDDatatype.XSDstring));
        }
        if (errorCode != null) {
            indiv.addLiteral(OTDatatypeProperties.errorCode().asDatatypeProperty(model),
                    model.createTypedLiteral(errorCode, XSDDatatype.XSDstring));
        }
        if (httpStatus != 0) {
            indiv.addLiteral(OTDatatypeProperties.httpStatus().asDatatypeProperty(model),
                    model.createTypedLiteral(httpStatus, XSDDatatype.XSDint));
        }
        return indiv;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (uri != null) {
            builder.append("URI     : ");
            builder.append(uri);
            builder.append("\n");
        }
        if (actor != null) {
            builder.append("Actor   : ");
            builder.append(actor);
            builder.append("\n");
        }
        if (errorCode != null) {
            builder.append("Code    : ");
            builder.append(errorCode);
            builder.append("\n");
        }
        if (httpStatus != 0) {
            builder.append("Status  : ");
            builder.append(httpStatus);
            builder.append("\n");
        }
        if (message != null) {
            builder.append("Message : ");
            builder.append(message);
            builder.append("\n");
        }
        if (details != null) {
            builder.append("Details : ");
            builder.append("\n");
            builder.append(details);
        }
        return new String(builder);
    }

    @Override
    public void writeRdf(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public HTMLContainer inHtml() {
        HTMLDivBuilder builder = new HTMLDivBuilder("yaqp_algorithm");
        builder.addComment("Error Report Representation automatically generated by YAQP");
        builder.addSubHeading("Error Report").
                breakLine().horizontalSeparator();
        builder.getDiv().setAlignment(Alignment.justify).setId(getUri().toString());
        builder.addSubSubSubHeading("Information about the Exceptional Event");
        builder.addTable(2).
                setTextAtCursor("Report URI").setTextAtCursor(getUri().toString()).
                setTextAtCursor("Error Code").setTextAtCursor(getErrorCode()).
                setTextAtCursor("Message").setAtCursor(new HTMLParagraphImpl(htmlNormalize(getMessage())).setAlignment(Alignment.justify)).
                setTextAtCursor("HTTP Code").setAtCursor(
                new HTMLParagraphImpl("<a href= \"" + errorCodeReference.get(getHttpStatus()) + "\" >" + Integer.toString(getHttpStatus()) + "</a>").setAlignment(Alignment.justify)).
                setTextAtCursor("Who is to Blame").setAtCursor(new HTMLParagraphImpl(htmlNormalize(getActor())).setAlignment(Alignment.justify)).
                setCellPadding(5).
                setCellSpacing(2).
                setTableBorder(0).
                setColWidth(1, 150).
                setColWidth(2, 550);

        builder.getDiv().breakLine();
        if (details != null) {
            builder.addSubSubSubHeading("Details that will help debugging");
            builder.addComponent(new HTMLTextImpl(htmlNormalize(getDetails())).formatPRE(true));
        }
        builder.getDiv().breakLine().horizontalSeparator();

        return builder.getDiv();
    }

    private String htmlNormalize(String in) {
        if (in == null) {
            return null;
        }
        return in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}