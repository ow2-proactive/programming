/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.vfsprovider.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.objectweb.proactive.extensions.vfsprovider.gui.DataServer.Server;


/**
 * Displays a set of @link {@link DataServer} in a {@link JTable}
 * 
 * 
 * @author mschnoor
 *
 */
class ServerTableModel implements TableModel {

    public ServerTableModel() {
        this.data = new ArrayList<Server>();
        for (Server srv : DataServer.getInstance().getServers().values()) {
            this.data.add(srv);
        }
    }

    private ArrayList<Server> data;

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return 5;
    }

    public void addServer(Server server) {
        this.data.add(server);
    }

    public List<Server> getServers() {
        return this.data;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Path";
            case 1:
                return "Protocol";
            case 2:
                return "Name";
            case 3:
                return "Status";
            case 4:
                return "URL";
        }
        return "";
    }

    public Class<?> getColumnClass(int columnIndex) {
        return java.lang.String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Server srv = this.data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return srv.getRootDir();
            case 1:
                String proto = srv.getProtocol();
                if (proto == null)
                    proto = "default";
                return proto;
            case 2:
                return srv.getName();
            case 3:
                return new String("" + srv.isStarted());
            case 4:
                return srv.getUrl();
        }
        return "";
    }

    public Server getValueAt(int rowIndex) {
        return this.data.get(rowIndex);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }
}
