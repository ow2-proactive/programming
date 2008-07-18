/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.p2p.Monitoring.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.extra.p2p.monitoring.ProgressObserver;


public class PeersimLogViewObserver implements ProgressObserver {

    protected final int max = 500;
    protected Shell shell;
    protected ProgressBar bar;

    public PeersimLogViewObserver() {
        shell = new Shell(PlatformUI.getWorkbench().getDisplay(), SWT.NONE);

        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        bar = new ProgressBar(shell, SWT.SMOOTH);
        Rectangle displayRect = PlatformUI.getWorkbench().getDisplay().getBounds();
        bar.setBounds(0, 0, 200, 20);
        shell.setLocation((displayRect.width - 200) / 2, (displayRect.height - 20) / 2);
        bar.setMaximum(max);
        // bar.setSelection(50);
    }

    public void percentage(final double p) {
        // System.out.println("PeersimLogViewObserver.percentage() " + p);

        if (shell.getDisplay().isDisposed())
            return;
        shell.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (bar.isDisposed())
                    return;
                bar.setSelection((int) (p * max / 100));

            }
        });

    }

    public void open() {
        // shell.pack();
        // shell.close();
        shell.pack();
        shell.open();
    }

    public void dispose() {
        // shell.dispose();
        shell.getDisplay().asyncExec(new Runnable() {
            public void run() {
                shell.dispose();
            }
        });
    }

}
