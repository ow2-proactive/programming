/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.dataspaces.api;

import java.util.Arrays;


/**
 * @author ActiveEon Team
 * @since 04/07/2020
 */
public class UserCredentials {

    private String login;

    private String password;

    private String domain;

    private byte[] privateKey;

    public UserCredentials() {

    }

    public UserCredentials(String login, String password, String domain, byte[] privateKey) {
        this.login = login;
        this.password = password;
        this.domain = domain;
        this.privateKey = privateKey;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public boolean isEmpty() {
        return login == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UserCredentials that = (UserCredentials) o;

        if (getLogin() != null ? !getLogin().equals(that.getLogin()) : that.getLogin() != null)
            return false;
        if (getPassword() != null ? !getPassword().equals(that.getPassword()) : that.getPassword() != null)
            return false;
        if (getDomain() != null ? !getDomain().equals(that.getDomain()) : that.getDomain() != null)
            return false;
        return Arrays.equals(getPrivateKey(), that.getPrivateKey());
    }

    @Override
    public int hashCode() {
        int result = getLogin() != null ? getLogin().hashCode() : 0;
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        result = 31 * result + (getDomain() != null ? getDomain().hashCode() : 0);
        result = 31 * result + Arrays.hashCode(getPrivateKey());
        return result;
    }

    @Override
    public String toString() {
        return "UserCredentials{" + "login='" + login + '\'' + ", password='" + password + '\'' + ", domain='" +
               domain + '\'' + '}';
    }
}
