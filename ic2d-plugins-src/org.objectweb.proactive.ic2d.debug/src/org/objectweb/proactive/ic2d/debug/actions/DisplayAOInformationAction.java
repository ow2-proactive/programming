package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.jface.action.Action;
import org.objectweb.proactive.ic2d.debug.views.AOInformationView;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;


public class DisplayAOInformationAction extends Action implements IActionExtPoint {

    public static final String DISAPLAYAOINFORMATIONS = "STEPBYSTEP03_Display AO Informations";

    public DisplayAOInformationAction() {
        super.setId(DISAPLAYAOINFORMATIONS);
        super.setEnabled(false);
    }

    public void setAbstractDataObject(AbstractData<?, ?> object) {
    }

    public void setActiveSelect(AbstractData<?, ?> ref) {
        if (ref instanceof ActiveObject) {
            AOInformationView aoinfInformationView = AOInformationView.getInstance();
            aoinfInformationView.selectItem((ActiveObject) ref);
        }
    }

}
