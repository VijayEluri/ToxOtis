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
package org.opentox.toxotis.ontology;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.Serializable;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.ontology.collection.OTClasses;

/**
 * A wrapper for a Resource assigned to some other resource through an Object
 * property acting as a value for that property. A resource value is characterized
 * by its URI and its type which in that case is an Ontological Class.
 * 
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class ResourceValue implements Serializable {

    private VRI uri;
    private OntologicalClass ontologicalClass;
    private static final int HASH_OFFSET = 19, HASH_MOD = 1;

    private ResourceValue() {
    }

    public ResourceValue(VRI uri, OntologicalClass ontologicalClass) {
        this.uri = uri;
        this.ontologicalClass = ontologicalClass;
    }

    public OntologicalClass getOntologicalClass() {
        return ontologicalClass;
    }

    public void setOntologicalClass(OntologicalClass ontologicalClass) {
        this.ontologicalClass = ontologicalClass;
    }

    public VRI getUri() {
        return uri;
    }

    public void setUri(VRI uri) {
        this.uri = uri;
    }

    public Resource inModel(OntModel model) {
        return model.createResource(getUri() != null ? getUri().toString() : null,
                getOntologicalClass() != null ? getOntologicalClass().inModel(model) : OTClasses.thing().inModel(model));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ResourceValue other = (ResourceValue) obj;
        return getHash() == other.getHash();
    }

    @Override
    public int hashCode() {
        return (int) getHash();
    }

    public long getHash() {
        return HASH_OFFSET + HASH_MOD * (this.uri != null ? uri.toString().trim().hashCode() : 0);
    }

    public void setHash(long hashCode) {/* Do nothing! */ }
}
