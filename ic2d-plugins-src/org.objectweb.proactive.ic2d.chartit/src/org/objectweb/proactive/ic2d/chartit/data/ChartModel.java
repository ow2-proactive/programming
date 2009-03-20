/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.objectweb.proactive.ic2d.chartit.data;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


/**
 * This class represents a model of a chart that can be updated.
 * <p>
 * The chart is associated to some data providers that will be 
 * asked for values.
 * <p>
 * To avoid any concurrency problems at runtime any user interactions
 * should be avoided.
 * <p>
 * The user should explicitly stop the runtime.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ChartModel {

    /**
     * An empty array of string
     */
    public static final String[] EMPTY_RUNTIME_NAMES = new String[0];

    /**
     * An empty array of double
     */
    public static final double[] EMPTY_RUNTIME_VALUES = new double[0];

    /**
     * The default name of a chart
     */
    public static final String DEFAULT_CHART_NAME = "Chart#";

    /**
     * Default period for refreshing cached value (in milliseconds)
     */
    public static final long DEFAULT_REFRESH_PERIOD = 4 * 1000;

    /**
     * The name of this chart
     */
    protected String name;

    /**
     * The chart type
     */
    protected ChartType chartType;

    /**
     * The period for refreshing cached value (in milliseconds)
     */
    protected long refreshPeriodInMs;

    /**
     * The list of data providers used by this chart
     */
    protected final List<IDataProvider> providers;

    // /////////////////
    // RUNTIME ONLY //
    // /////////////////

    /**
     * Chart Model listener
     */
    protected IChartModelListener chartModelListener;

    /**
     * The updater of the runtime values
     * <p>
     * This field must accessed carefully! 
     */
    protected IRuntimeValuesUpdater runtimeValuesUpdater;

    /**
     * The runtime names
     */
    protected String[] runtimeNames;

    /**
     * The list of labels (can be defined by the user) to be used inside charts instead of runtime names.
     */
    protected List<String> labels;

    /**
     * The runtime values
     */
    protected double[] runtimeValues;

    public ChartModel() {
        this(ChartModel.DEFAULT_CHART_NAME, ChartType.PIE, ChartModel.DEFAULT_REFRESH_PERIOD);
    }

    public ChartModel(final String name) {
        this(name, ChartType.PIE, ChartModel.DEFAULT_REFRESH_PERIOD);
    }

    public ChartModel(final String name, final ChartType chartType, final long refreshPeriodInMs) {
        this.name = name;
        this.chartType = chartType;
        this.refreshPeriodInMs = refreshPeriodInMs;

        this.runtimeNames = ChartModel.EMPTY_RUNTIME_NAMES;
        this.labels = new ArrayList<String>();
        this.runtimeValues = ChartModel.EMPTY_RUNTIME_VALUES;

        this.providers = new ArrayList<IDataProvider>();
    }

    /**
     * Runs this model
     */
    public void run() {
        // Update runtime values
        this.runtimeValuesUpdater.updateValues(this.runtimeValues);

        if (this.chartModelListener != null)
            this.chartModelListener.modelChanged(IChartModelListener.CHANGED, null, null);
    }

    public boolean addProvider(final IDataProvider provider) {
        if (!this.providers.contains(provider)) {
            this.providers.add(provider);
            return true;
        }
        return false;
    }

    public void removeProvider(final IDataProvider provider) {
        if (this.providers.contains(provider)) {
            this.providers.remove(provider);
            this.labels.remove(provider.getName());
        }
    }

    public void removeProviderByName(final String name) {
        IDataProvider providerToRemove = null;
        for (final IDataProvider p : this.providers) {
            if (name.equals(p.getName())) {
                providerToRemove = p;
                break;
            }
        }
        this.providers.remove(providerToRemove);
        this.labels.remove(providerToRemove.getName());
    }

    public void removeProvidersByNames(final String[] names) {
        for (final String s : names) {
            this.removeProviderByName(s);
        }
    }

    public List<IDataProvider> getProviders() {
        return this.providers;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getRuntimeNames() {
        return this.runtimeNames;
    }

    public void setRuntimeNames(final String[] runtimeNames) {
        this.runtimeNames = runtimeNames;
    }

    public void addLabel(final String label) {
        this.labels.add(label);
    }

    public void setLabelAtIndex(final int index, final String label) {
        this.labels.set(index, label);
    }

    public void setLabels(final String[] labels) {
        this.labels = new ArrayList<String>(labels.length);
        for (final String label : labels) {
            this.labels.add(label);
        }
    }

    public String[] getLabels() {
        final String[] res = new String[this.labels.size()];
        for (int i = res.length; i-- > 0;) {
            res[i] = this.labels.get(i);
        }
        return res;
    }

    public double[] getRuntimeValues() {
        return runtimeValues;
    }

    public void setRuntimeValues(final double[] runtimeValues) {
        this.runtimeValues = runtimeValues;
    }

    public long getRefreshPeriodInMs() {
        return this.refreshPeriodInMs;
    }

    public void setRefreshPeriodInMs(final long refreshPeriodInMs) {
        this.refreshPeriodInMs = refreshPeriodInMs;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(final ChartType chartType) {
        this.chartType = chartType;
    }

    public void setChartModelListener(final IChartModelListener chartModelListener) {
        this.chartModelListener = chartModelListener;
    }

    public void unSetChartModelListener() {
        this.chartModelListener = null;
    }

    public boolean isChronological() {
        return this.chartType == ChartType.TIME_SERIES;
    }

    public String toString() {
        return "Name : " + this.name + " type : " + this.chartType + " rp : " + this.refreshPeriodInMs +
            " providers : " + this.providers.size();
    }

    public void fillRuntimeNames() {
        this.runtimeNames = new String[this.providers.size()];
        int i = 0;
        for (final IDataProvider provider : this.providers) {
            this.runtimeNames[i++] = provider.getName();
        }
    }
}
