package org.cytoscape.prorank.internal.task;

import java.awt.Component;
import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.prorank.internal.view.ProRankUI;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreateUiTask extends AbstractTask{
    
    private final CyServiceRegistrar cyRegistrar;
    public CreateUiTask(CyServiceRegistrar cyRegistrar) {
        this.cyRegistrar = cyRegistrar;
    }

    @Override
    public void run(TaskMonitor tm) throws Exception {
        CySwingApplication swingApplication = cyRegistrar.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.WEST);
        ProRankUI ui;
        
        if(getMainPanel(swingApplication, cytoPanel) != null) {
            ui = getMainPanel(swingApplication, cytoPanel);
            ui.resetParams();
        } else{
            ui = new ProRankUI(cyRegistrar);
            cyRegistrar.registerService(ui, CytoPanelComponent.class, new Properties());
            ui.activate();
            ui.resetParams();
        }
        
        int index = cytoPanel.indexOfComponent(ui);
        cytoPanel.setSelectedIndex(index);
    }
    
    public ProRankUI getMainPanel(CySwingApplication swingApplication, CytoPanel cytoPanel){
        int count = cytoPanel.getCytoPanelComponentCount();
        for (int i = 0; i < count; i++) {
            final Component comp = cytoPanel.getComponentAt(i);
            if (comp instanceof ProRankUI)
                return (ProRankUI) comp;
            }
        return null;
    }
    
    
}
