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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentox.toxotis.core.component.User;
import org.opentox.toxotis.database.DbWriter;
import org.opentox.toxotis.database.exception.DbException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public class AddUser extends DbWriter {

    private final User user;
    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AddUser.class);
    private PreparedStatement ps = null;

    public AddUser(final User user) {
        this.user = user;
    }

    @Override
    public int write() throws DbException {
        setTable("User");
        setTableColumns("uid", "name", "mail", "password", "maxParallelTasks", "maxModels", "maxBibTeX");
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException("SQLException happened while applying the method .setAutoCommit(boolean) on a fresh "
                    + "connection. This most probably means that the connection is closed.", ex);
        }
        try {
            ps = connection.prepareStatement(getSql());
            ps.setString(1, user.getUid());
            ps.setString(2, user.getName());
            ps.setString(3, user.getMail());
            ps.setString(4, user.getHashedPass());
            ps.setInt(5, user.getMaxParallelTasks());
            ps.setInt(6, user.getMaxModels());
            ps.setInt(7, user.getMaxBibTeX());
            int update = ps.executeUpdate();
            connection.commit();
            return update;
        } catch (final SQLException ex) {
            final String msg = "SQL statement execution failed while trying to add user in the database";
            logger.warn(msg, ex);
            if (connection != null) {
                try {
                    logger.info("Rolling back");
                    connection.rollback();
                } catch (final SQLException rollBackException) {
                    final String m1 = "Rolling back failed";
                    logger.error(m1);
                    throw new DbException(m1, rollBackException);
                }
            }
            throw new DbException(msg, ex);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (final SQLException ex) {
                final String msg = "SQL statement execution failed while trying to add a user in the database";
                logger.warn(msg, ex);
                throw new DbException(msg, ex);
            } finally {
                close();
            }
        }
    }
}
