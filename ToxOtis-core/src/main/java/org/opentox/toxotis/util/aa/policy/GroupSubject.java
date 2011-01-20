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
package org.opentox.toxotis.util.aa.policy;


/**
 * A collection of standard groups of Users used to identify certain access levels.
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class GroupSubject extends PolicySubject {

    public static final GroupSubject DEVELOPMENT = new GroupSubject("development");
    public static final GroupSubject PARTNER = new GroupSubject("partner");

    public GroupSubject() {
        LDAP_Type = "LDAPGroups";
    }

    private GroupSubject(String groupName) {
        this();
        setSubjectName(groupName);
    }

    @Override
    public String getValue() {
        return "cn=" + getSubjectName() + ", ou=groups, dc=opentox,dc=org";
    }
}
