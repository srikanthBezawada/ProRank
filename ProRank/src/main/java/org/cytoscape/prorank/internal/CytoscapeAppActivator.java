package org.cytoscape.prorank.internal;

import java.util.Properties;
import org.cytoscape.prorank.internal.task.CreateUiTaskFactory;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

public class CytoscapeAppActivator extends AbstractCyActivator{
    public CytoscapeAppActivator() {
        super();
    }
    
    public void start(BundleContext bc) {
        final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
        final CreateUiTaskFactory createUiTaskFactory = new CreateUiTaskFactory(serviceRegistrar);
        
        Properties prorankProps = new Properties();
        prorankProps.setProperty(PREFERRED_MENU, "Apps");
        prorankProps.setProperty(TITLE, "ProRank");
        registerService(bc, createUiTaskFactory, TaskFactory.class, prorankProps);
    }


}