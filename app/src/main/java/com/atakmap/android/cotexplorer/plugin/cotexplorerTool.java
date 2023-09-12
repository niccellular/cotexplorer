
package com.atakmap.android.cotexplorer.plugin;

import android.content.Context;

import com.atak.plugins.impl.AbstractPluginTool;
import gov.tak.api.util.Disposable;

public class cotexplorerTool extends AbstractPluginTool implements Disposable {

    private final static String TAG = "cotexplorerTool";

    public cotexplorerTool(Context context) {
        super(context,
                context.getString(R.string.app_name),
                context.getString(R.string.app_name),
                context.getResources().getDrawable(R.drawable.ic_launcher),
                "com.atakmap.android.cotexplorer.SHOW_PLUGIN");
        PluginNativeLoader.init(context);
    }

    @Override
    public void dispose() {
    }

}