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
package org.opentox.toxotis.core.component;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.core.IHTMLSupport;
import org.opentox.toxotis.core.OTOnlineResource;
import org.opentox.toxotis.core.html.Alignment;
import org.opentox.toxotis.core.html.HTMLContainer;
import org.opentox.toxotis.core.html.HTMLDivBuilder;
import org.opentox.toxotis.core.html.HTMLForm;
import org.opentox.toxotis.core.html.HTMLInput;
import org.opentox.toxotis.core.html.HTMLTable;
import org.opentox.toxotis.core.html.impl.HTMLInputImpl;
import org.opentox.toxotis.core.html.impl.HTMLTextImpl;
import org.opentox.toxotis.exceptions.impl.ServiceInvocationException;
import org.opentox.toxotis.exceptions.impl.ToxOtisException;
import org.opentox.toxotis.util.aa.AuthenticationToken;
import org.opentox.toxotis.util.aa.EmailValidator;

/**
 * An individual User and its publicly available attributes.
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class User extends OTOnlineResource<User> implements IHTMLSupport {

    private String uid;
    private String name;
    private String mail;
    private String hashedPass;
    private int maxParallelTasks = 0;
    private int maxModels = 0;
    private int maxBibTeX = 0;
    private String tokenLogout;
    public static final User GUEST = new User("guest@opensso.in-silico.ch", "Guest", "anonymous@anonymous.org", "{SSHA}ficDnnD49QMLnwStKABXzDvFIgrd/c4H");

    public int getMaxBibTeX() {
        return maxBibTeX;
    }

    public void setMaxBibTeX(int maxBibTeX) {
        this.maxBibTeX = maxBibTeX;
    }

    public int getMaxModels() {
        return maxModels;
    }

    public void setMaxModels(int maxModels) {
        this.maxModels = maxModels;
    }

    public int getMaxParallelTasks() {
        return maxParallelTasks;
    }

    public void setMaxParallelTasks(int maxParallelTasks) {
        this.maxParallelTasks = maxParallelTasks;
    }

    public User() {
    }

    private User(String uid, String name, String mail, String hashedPass) {
        this.uid = uid;
        this.name = name;
        this.mail = mail;
        this.hashedPass = hashedPass;
    }

    public String getHashedPass() {
        return hashedPass;
    }

    public void setHashedPass(String hashedPass) {
        this.hashedPass = hashedPass;
    }

    /**
     * Retrieve the email of the user
     * @return
     *      User email
     */
    public String getMail() {
        return mail;
    }

    /**
     * Checks whether the provided e-mail address is RDF-2822 compliant and sets
     * the e-mail address of the user accordingly.
     * @param mail
     *      The e-mail address of the user (Must be RDF-2822 compliant).
     * @return 
     *      The current modifiable instance of User.
     * @throws ToxOtisException
     *      In case the provided e-mail address is not compliant to the
     *      specifications of RFC 2822.
     * @see EmailValidator
     */
    public User setMail(String mail) throws 
            ToxOtisException {
        if (!EmailValidator.validate(mail)) {
            throw new org.opentox.toxotis.exceptions.impl.ToxOtisException("Bad email address according to RFC 2822 : '" + mail + "'");
        }
        this.mail = mail;
        return this;

    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public User setUid(String uid) {
        this.uid = uid;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UID   : ");
        sb.append(uid);
        sb.append("\n");
        sb.append("Name  : ");
        sb.append(name);
        sb.append("\n");
        sb.append("Mail  : ");
        sb.append(mail);
        sb.append("\n");
        sb.append("Pass  : ");
        sb.append(hashedPass);
        return new String(sb);
    }

    @Override
    protected User loadFromRemote(VRI vri, AuthenticationToken token) throws ServiceInvocationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Individual asIndividual(OntModel model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeRdf(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HTMLContainer inHtml() {
        HTMLDivBuilder builder = new HTMLDivBuilder("jaqpot_user");
        builder.addComment("User Representation automatically generated by Jaqpot");
        builder.addSubHeading("User");
        builder.addSubSubHeading(getUri().toString());
        builder.getDiv().setAlignment(Alignment.justify).breakLine().horizontalSeparator();
        builder.addSubSubHeading("Information about the User");
        builder.getDiv().breakLine().breakLine();

        HTMLTable dataTable = builder.addTable(2);
        dataTable.setAtCursor(new HTMLTextImpl("URI").formatBold(true)).setTextAtCursor(getUri() != null ? getUri().toString() : "-").
                setAtCursor(new HTMLTextImpl("UID").formatBold(true)).setTextAtCursor(getUid()).
                setAtCursor(new HTMLTextImpl("Name").formatBold(true)).setTextAtCursor(getName() != null ? getName() : "No Name").
                setAtCursor(new HTMLTextImpl("Max allowed parallel tasks").formatBold(true)).setTextAtCursor(getMaxParallelTasks() + "").
                setAtCursor(new HTMLTextImpl("Max allowed models").formatBold(true)).setTextAtCursor(getMaxModels() + "").
                setAtCursor(new HTMLTextImpl("Max allowed BibTeX entries").formatBold(true)).setTextAtCursor(getMaxBibTeX() + "");

        dataTable.setCellPadding(5).
                setCellSpacing(2).
                setTableBorder(1).
                setColWidth(1, 150).
                setColWidth(2, 400);
        builder.getDiv().breakLine();

        builder.addParagraph("<p style=\"display:inline;\">If you are already logged in and you need to logout click :",Alignment.left);
        HTMLForm form = builder.addForm("/login", "POST","").setStyle("margin-left:20px;display:inline;");
        form.addComponent(new HTMLInputImpl().setType(HTMLInput.HTMLInputType.HIDDEN).
                setName("logout").setValue("logout"));
        form.addComponent(new HTMLInputImpl().setType(HTMLInput.HTMLInputType.HIDDEN).
                setName("token").setValue(tokenLogout));
        form.addComponent(new HTMLInputImpl().setType(HTMLInput.HTMLInputType.SUBMIT).setValue("Logout"));
        
        builder.addParagraph("Check out user's quota <a href=\"" + getUri().augment("quota") + "\">here</a>",
                Alignment.left);

        builder.addComponent(createLinksFooter());
        
        return builder.getDiv();
    }

    public void setTokenLogout(String tokenLogout) {
        this.tokenLogout = tokenLogout;
    }
    
    
}
