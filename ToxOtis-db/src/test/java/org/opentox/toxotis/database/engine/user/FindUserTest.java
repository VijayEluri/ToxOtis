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
package org.opentox.toxotis.database.engine.user;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentox.toxotis.core.component.User;
import org.opentox.toxotis.database.IDbIterator;
import org.opentox.toxotis.util.ROG;
import static org.junit.Assert.*;
import org.opentox.toxotis.database.exception.DbException;
import org.opentox.toxotis.database.pool.DataSourceFactory;

/**
 *
 * @author chung
 */
public class FindUserTest {

    public FindUserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        org.opentox.toxotis.database.TestUtils.setUpDB();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        DataSourceFactory.getInstance().close();
    }

    @Before
    public void setUp() {
    }

    @After
    public synchronized void tearDown() {
    }

    @Test
    public void testFindUser() throws DbException {
        ListUsers list = new ListUsers();
        list.setMode(ListUsers.ListUsersMode.BY_NAME);
        try {
            IDbIterator<String> userNames = list.list();
            boolean guestFound = false;
            while (userNames.hasNext()) {
                guestFound |= userNames.next().equals("Guest");
            }
            assertTrue(guestFound);
        } catch (final DbException ex) {
            throw new DbException(ex);
        } finally {
            list.close();
        }

        FindUser fu = new FindUser();
        fu.setWhere("name LIKE 'Gue%'");
        IDbIterator<User> users = fu.list();
        boolean userFound = false;
        while (users.hasNext()) {
            userFound = true;
            assertEquals(User.GUEST, users.next());
            assertEquals(User.GUEST.getMail(), users.next().getMail());
            assertEquals(User.GUEST.getName(), users.next().getName());
            System.out.println(users.next().getMail());
        }
        assertTrue(userFound);
        users.close();
        fu.close();
    }

    @Test
    public void testWriteAndRead() throws DbException {
        ROG rog = new ROG();
        User random = rog.nextUser();
        random.setMaxBibTeX(1001);
        random.setMaxModels(1001);
        random.setMaxParallelTasks(5);
        AddUser adder = new AddUser(random);
        adder.write();
        adder.close();

        FindUser fu = new FindUser();
        fu.setWhere("name='" + random.getName() + "'");
        IDbIterator<User> users = fu.list();
        while (users.hasNext()) {
            assertEquals(5, users.next().getMaxParallelTasks());
        }
        users.close();
        fu.close();
    }
}
