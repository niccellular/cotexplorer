
package com.atakmap.android.cotexplorer.plugin;

import com.atak.plugins.impl.AbstractPlugin;
import com.atak.plugins.impl.PluginContextProvider;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import com.atakmap.android.cotexplorer.cotexplorerMapComponent;
import com.atakmap.coremap.log.Log;

public class cotexplorerLifecycle extends AbstractPlugin implements IPlugin {

    public cotexplorerLifecycle(IServiceController serviceController) {
        super(serviceController, new cotexplorerTool(serviceController.getService(PluginContextProvider.class).getPluginContext()), new cotexplorerMapComponent());
    }
}

