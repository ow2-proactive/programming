/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.objectweb.proactive.extensions.dataspaces.exceptions.DataSpacesException;
import org.objectweb.proactive.extensions.vfsprovider.gui.DataServer.Server;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.objectweb.proactive.utils.SecurityManagerConfigurator;


/**
 * Simple GUI to create dataservers
 * <p>
 * Everything runs in the same VM : closing the GUI shuts down the servers
 * 
 * 
 * @author mschnoor
 * @since 5.1.0
 *
 */
public class ServerBrowser implements ActionListener, WindowListener, KeyEventDispatcher, ClipboardOwner {

    /**
     * history file for root directory, contains up to 20 non duplicate entries
     */
    static final File rootHistoryFile = new File(System.getProperty("user.home") +
        "/.proactive/dataserver_root.history");

    /**
     * history file for DS name, contains up to 20 non duplicate entries
     */
    static final File nameHistoryFile = new File(System.getProperty("user.home") +
        "/.proactive/dataserver_name.history");

    /**
     * Entry point
     * 
     * @param no argument
     */
    public static void main(String[] args) {
        args = JVMPropertiesPreloader.overrideJVMProperties(args);

        SecurityManagerConfigurator.configureSecurityManager(ServerBrowser.class.getResource(
                "/all-permissions.security.policy").toString());

        setLF();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ServerBrowser s = new ServerBrowser();
                s.build();
            }
        });

    }

    private JTable table;
    private ServerTableModel model;
    private JFrame frame;

    private ServerDialog serverDialog;

    private Icon startedIcon, stoppedIcon;

    private Icon addServerIcon, removeServerIcon, startServerIcon, stopServerIcon, copyServerIcon, exitIcon;

    private JButton addServerBut, removeServerBut, startServerBut, stopServerBut, copyServerBut, exitBut;

    private JMenuItem addServerIt, removeServerIt, startServerIt, stopServerIt, copyServerIt;

    /**
     * Creates and displays the frame
     */
    void build() {
        model = new ServerTableModel();

        this.addServerIcon = new ImageIcon(this.getClass().getResource("icons/server_add.png"));
        this.removeServerIcon = new ImageIcon(this.getClass().getResource("icons/server_remove.png"));
        this.startServerIcon = new ImageIcon(this.getClass().getResource("icons/server_start.png"));
        this.stopServerIcon = new ImageIcon(this.getClass().getResource("icons/server_stop.png"));
        this.copyServerIcon = new ImageIcon(this.getClass().getResource("icons/copy.png"));
        this.exitIcon = new ImageIcon(this.getClass().getResource("icons/exit.png"));

        final JPopupMenu menu = new JPopupMenu();
        addServerIt = new JMenuItem("Add", addServerIcon);
        addServerIt.addActionListener(this);
        addServerIt.setActionCommand("addServer");
        removeServerIt = new JMenuItem("Remove", removeServerIcon);
        removeServerIt.addActionListener(this);
        removeServerIt.setActionCommand("removeServer");
        startServerIt = new JMenuItem("Start", startServerIcon);
        startServerIt.addActionListener(this);
        startServerIt.setActionCommand("startServer");
        stopServerIt = new JMenuItem("Stop", stopServerIcon);
        stopServerIt.addActionListener(this);
        stopServerIt.setActionCommand("stopServer");
        copyServerIt = new JMenuItem("Copy URL", copyServerIcon);
        copyServerIt.addActionListener(this);
        copyServerIt.setActionCommand("copyServer");
        menu.add(copyServerIt);
        menu.add(new JSeparator());
        menu.add(addServerIt);
        menu.add(removeServerIt);
        menu.add(new JSeparator());
        menu.add(startServerIt);
        menu.add(stopServerIt);

        table = new JTable(model) {

    private static final long serialVersionUID = 61L;
            private StatusCellRenderer statusRenderer = new StatusCellRenderer();

            /*
             * custom cell renderer to display colored icon next to the server status
             */
            final class StatusCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 61L;

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);

                    if (Boolean.parseBoolean(value.toString())) {
                        label.setIcon(startedIcon);
                        label.setText("Started");
                    } else {
                        label.setIcon(stoppedIcon);
                        label.setText("Stopped");
                    }
                    return label;
                }
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (getModel().getColumnName(column).equals("Status")) {
                    return this.statusRenderer;
                }
                return super.getCellRenderer(row, column);
            }
        };
        table.add(menu);
        table.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                JTable source = (JTable) e.getSource();
                int row = source.rowAtPoint(e.getPoint());

                int column = source.columnAtPoint(e.getPoint());
                if (!source.isRowSelected(row))
                    source.changeSelection(row, column, false, false);

                Server srv = model.getValueAt(row);
                boolean started = srv.isStarted();

                startServerBut.setEnabled(!started);
                startServerIt.setEnabled(!started);
                stopServerBut.setEnabled(started);
                stopServerIt.setEnabled(started);
                copyServerBut.setEnabled(started);
                copyServerIt.setEnabled(started);
                removeServerBut.setEnabled(table.getSelectedColumn() != -1);
                removeServerIt.setEnabled(table.getSelectedColumn() != -1);

                if (e.isPopupTrigger()) {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.setRowHeight(30);

        JScrollPane scroll = new JScrollPane(table);

        addServerBut = new JButton(addServerIcon);
        addServerBut.addActionListener(this);
        addServerBut.setActionCommand("addServer");

        removeServerBut = new JButton(removeServerIcon);
        removeServerBut.addActionListener(this);
        removeServerBut.setActionCommand("removeServer");
        removeServerBut.setEnabled(false);

        startServerBut = new JButton(startServerIcon);
        startServerBut.addActionListener(this);
        startServerBut.setActionCommand("startServer");
        startServerBut.setEnabled(false);

        stopServerBut = new JButton(stopServerIcon);
        stopServerBut.addActionListener(this);
        stopServerBut.setActionCommand("stopServer");
        stopServerBut.setEnabled(false);

        copyServerBut = new JButton(copyServerIcon);
        copyServerBut.addActionListener(this);
        copyServerBut.setActionCommand("copyServer");
        copyServerBut.setEnabled(false);

        exitBut = new JButton(exitIcon);
        exitBut.addActionListener(this);
        exitBut.setActionCommand("exit");

        JToolBar tools = new JToolBar(JToolBar.HORIZONTAL);
        tools.setFloatable(false);
        tools.add(addServerBut);
        tools.add(removeServerBut);
        tools.add(new JToolBar.Separator());
        tools.add(startServerBut);
        tools.add(stopServerBut);
        tools.add(copyServerBut);
        tools.add(new JToolBar.Separator());
        tools.add(exitBut);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(scroll, BorderLayout.CENTER);
        contentPane.add(tools, BorderLayout.NORTH);

        frame = new JFrame("DataServer browser");
        frame.addWindowListener(this);
        frame.setResizable(true);
        frame.setContentPane(contentPane);
        frame.setMinimumSize(new Dimension(300, 100));
        frame.setPreferredSize(new Dimension(500, 300));
        frame.setLocationRelativeTo(null);

        /* JFrame#setIconImages does not exist in jdk5 */
        //try {
        //	frame.setIconImages(getIcons());
        //} catch (Throwable t) {
        try {
            frame.setIconImage(getIcon());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
        frame.pack();
        frame.setVisible(true);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        this.startedIcon = new ImageIcon(this.getClass().getResource("icons/server_started.png"));
        this.stoppedIcon = new ImageIcon(this.getClass().getResource("icons/server_stopped.png"));
    }

    /**
     * Java6 and more
     * @return a list of icons for the WM
     */
    private List<Image> getIcons() {
        String[] icons = { "16", "24", "32", "48", "64", "128" };
        List<Image> images = new ArrayList<Image>(icons.length);

        for (String size : icons) {
            String name = "icons" + File.separator + size + ".png";
            URL url = this.getClass().getResource(name);
            try {
                Image img = ImageIO.read(url);
                images.add(img);
            } catch (IOException e) {
                System.out.println("Failed to open icon: " + url.toString());
            }
        }
        return images;
    }

    /**
     * Java5
     * @return a single icon for the WM
     * @throws IOException
     */
    private Image getIcon() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("icons/32.png");
        Image img = ImageIO.read(is);
        return img;
    }

    /**
     * Set the swing look&feel
     */
    private static void setLF() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                // GTK lf looks like crap on 1.5
                if (System.getProperty("java.version").startsWith("1.6") ||
                    System.getProperty("java.version").startsWith("1.7")) {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                }
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("addServer")) {
            this.serverDialog = new ServerDialog(this.frame, this);
            this.serverDialog.show();
        } else if (cmd.equals("serverDialog.cancel")) {
            if (this.serverDialog != null) {
                this.serverDialog.close();
                this.serverDialog = null;
            }
        } else if (cmd.equals("serverDialog.ok")) {
            if (this.serverDialog != null) {
                String name = serverDialog.getStartedServerName();
                Server srv = DataServer.getInstance().getServer(name);
                model.addServer(srv);
                addHistory(rootHistoryFile, srv.getRootDir(), false);
                addHistory(nameHistoryFile, srv.getName(), false);

                table.revalidate();
                this.serverDialog.close();
                this.serverDialog = null;
            }
        } else if (cmd.equals("exit")) {
            this.exit();
        } else if (cmd.equals("removeServer")) {
            int row = table.getSelectedRow();
            if (row == -1)
                return;
            Server srv = model.getValueAt(row);
            try {
                DataServer.getInstance().removeServer(srv.getName());
            } catch (DataSpacesException e1) {
                error("Failed to remove server: " + srv.getName(), e1);
            }
            model.getServers().remove(srv);
            removeServerBut.setEnabled(false);
            removeServerIt.setEnabled(false);

            table.revalidate();
        } else if (cmd.equals("startServer")) {
            int row = table.getSelectedRow();
            if (row == -1)
                return;
            final Server srv = model.getValueAt(row);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        srv.start(true);
                    } catch (DataSpacesException e1) {
                        error("Failed to start server " + srv.getName(), e1);
                        return;
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ServerBrowser.this.table.repaint();
                            ServerBrowser.this.startServerBut.setEnabled(false);
                            ServerBrowser.this.startServerIt.setEnabled(false);
                            ServerBrowser.this.stopServerBut.setEnabled(true);
                            ServerBrowser.this.stopServerIt.setEnabled(true);
                            ServerBrowser.this.copyServerBut.setEnabled(true);
                            ServerBrowser.this.copyServerIt.setEnabled(true);

                        }
                    });
                }
            }).start();

        } else if (cmd.equals("stopServer")) {
            int row = table.getSelectedRow();
            if (row == -1)
                return;
            final Server srv = model.getValueAt(row);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        srv.stop();
                    } catch (DataSpacesException e1) {
                        error("Failed to stop server " + srv.getName(), e1);
                        return;
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ServerBrowser.this.table.repaint();
                            ServerBrowser.this.startServerBut.setEnabled(true);
                            ServerBrowser.this.startServerIt.setEnabled(true);
                            ServerBrowser.this.stopServerBut.setEnabled(false);
                            ServerBrowser.this.stopServerIt.setEnabled(false);
                            ServerBrowser.this.copyServerBut.setEnabled(false);
                            ServerBrowser.this.copyServerIt.setEnabled(false);
                        }
                    });
                }
            }).start();
        } else if (cmd.equals("copyServer")) {
            copyServerUrl();
        }
    }

    private void copyServerUrl() {
        int row = table.getSelectedRow();
        if (row == -1)
            return;
        Server srv = model.getValueAt(row);
        if (!srv.isStarted())
            return;
        List<String> urls = srv.getUrls();
        String urlsstring = urls.toString();
        StringSelection str = new StringSelection(urlsstring.substring(1, urlsstring.length() - 1));
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(str, this);
    }

    private void exit() {
        int running = 0;
        for (Server srv : DataServer.getInstance().getServers().values()) {
            if (srv.isStarted())
                running++;
        }
        if (running > 0) {
            String msg = "<html>" + running + " server is still running.<br>Exit anyway?</html>";
            if (running > 1) {
                msg = "<html>" + running + " servers are still running.<br>Exit anyway?</html>";
            }

            int ret = JOptionPane.showConfirmDialog(this.frame, msg, "Exit", JOptionPane.YES_NO_OPTION);

            if (ret == JOptionPane.NO_OPTION) {
                return;
            }
        }

        DataServer.cleanup();
        System.exit(0);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        exit();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
            if (e.getKeyCode() == KeyEvent.VK_Q) {
                exit();
            } else if (e.getKeyCode() == KeyEvent.VK_C) {
                copyServerUrl();
            }
        }
        return false;
    }

    /**
     * Display an error message
     * no side effect
     * @param msg
     * @param cause
     */
    static void error(String msg, final Throwable cause) {
        if (cause != null && cause.getMessage() != null && cause.getMessage().trim().length() > 0) {
            msg += "<br>" + cause.getMessage();
        }
        final String err = "<html>" + msg + "<html>";
        cause.printStackTrace();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, err, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    static void addHistory(File file, String line, boolean keepDupesNoLimit) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                error("Could not open history file " + file.getAbsolutePath(), e);
            }
        }
        String[] content = getHistory(file);
        int lim = (keepDupesNoLimit ? 102 : 21);
        int len = Math.min(lim, content.length);

        ArrayList<String> ar = new ArrayList<String>(len);
        for (int i = 0; i < len; i++) {
            if (!content[i].equals(line) || keepDupesNoLimit) {
                ar.add(content[i]);
            }
        }

        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            pw.println(line);
            for (String str : ar) {
                pw.println(str);
            }
            pw.close();
        } catch (FileNotFoundException e) {
            error("Could not open history file " + file.getAbsolutePath(), e);
        }
    }

    static String[] getHistory(File f) {
        if (!f.exists()) {
            return new String[] {};
        } else {
            ArrayList<String> ar = new ArrayList<String>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                String line = null;

                while ((line = br.readLine()) != null) {
                    ar.add(line);
                }

                br.close();
            } catch (FileNotFoundException e) {
                error("Could not open history file " + f.getAbsolutePath(), e);
                return null;
            } catch (IOException e) {
                error("Could not read history file " + f.getAbsolutePath(), e);
                return null;
            }

            String[] ret = new String[ar.size()];
            int i = 0;
            for (String str : ar) {
                ret[i++] = str;
            }
            return ret;
        }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
