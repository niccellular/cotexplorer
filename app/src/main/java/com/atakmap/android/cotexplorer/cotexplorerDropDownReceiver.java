
package com.atakmap.android.cotexplorer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;

import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.gui.AlertDialogHelper;
import com.atakmap.android.gui.EditText;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.cotexplorer.plugin.R;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;

import com.atakmap.comms.CommsLogger;
import com.atakmap.comms.CommsMapComponent;
import com.atakmap.coremap.cot.event.CotEvent;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;


public class cotexplorerDropDownReceiver extends DropDownReceiver implements
        OnStateListener, CommsLogger, View.OnClickListener {

    public static final String TAG = "cotexplorer";

    public static final String SHOW_PLUGIN = "com.atakmap.android.cotexplorer.SHOW_PLUGIN";
    private final Context pluginContext;
    private final Context appContext;
    private final MapView mapView;
    private final View mainView;

    private boolean paused = false;
    private TextView cotexplorerlog = null;
    private Button sendBtn, clearBtn, pauseBtn, filterBtn, saveBtn = null;
    private SharedPreferences _sharedPreference = null;
    private String cotFilter = "";

    /**************************** CONSTRUCTOR *****************************/

    public cotexplorerDropDownReceiver(final MapView mapView,
            final Context context) {
        super(mapView);
        this.pluginContext = context;
        this.appContext = mapView.getContext();
        this.mapView = mapView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mainView = inflater.inflate(R.layout.main_layout, null);
        cotexplorerlog = mainView.findViewById(R.id.cotexplorerlog);
        clearBtn = mainView.findViewById(R.id.clearBtn);
        pauseBtn = mainView.findViewById(R.id.pauseBtn);
        filterBtn = mainView.findViewById(R.id.filterBtn);
        saveBtn = mainView.findViewById(R.id.saveBtn);
        sendBtn = mainView.findViewById(R.id.sendBtn);

        _sharedPreference = PreferenceManager.getDefaultSharedPreferences(mapView.getContext().getApplicationContext());

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                EditText et = new EditText(mapView.getContext());

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mapView.getContext());
                alertDialog.setTitle("Enter CoT to send");
                alertDialog.setView(et);
                alertDialog.setNegativeButton("Cancel", null);
                alertDialog.setPositiveButton("Send", (dialogInterface, i) -> {
                    CotEvent cot = CotEvent.parse(et.getText().toString());
                    if (cot.isValid()) {
                        CotMapComponent.getExternalDispatcher().dispatch(cot);
                    } else {
                        Toast.makeText(mapView.getContext(), "Invalid CoT", Toast.LENGTH_LONG).show();
                    }
                });
                alertDialog.show();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File(Environment.getExternalStorageDirectory() + "/atak/cotexplorer.txt");
                try {
                    FileWriter fw = new FileWriter(f);
                    fw.write(cotexplorerlog.getText().toString());
                    fw.flush();
                    fw.close();
                    Toast.makeText(mapView.getContext(), "Log written to /sdcard/atak/cotexplorer.txt", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cotexplorerlog.setText("");
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (paused) {
                    pauseBtn.setText("   Pause   ");
                    paused = false;
                } else {
                    pauseBtn.setText("   Paused   ");
                    paused = true;
                }
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mapView.getContext());
                alertBuilder.setTitle("Set filter");
                final EditText input = new EditText(mapView.getContext());
                input.setText(cotFilter);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                alertBuilder.setView(input);

                alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            cotFilter = input.getText().toString();
                    }
                });
                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
                alertBuilder.setCancelable(true);
                alertBuilder.show();
            }
        });

        CommsMapComponent.getInstance().registerCommsLogger(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(SHOW_PLUGIN)) {
            showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT,
                    FULL_WIDTH, HALF_HEIGHT, false, this);
        }
    }

    private void writeLog(final String log, final String flag) {
        if (paused) return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                boolean send = _sharedPreference.getBoolean("plugin_cotexplorer_send", true);
                boolean recv = _sharedPreference.getBoolean("plugin_cotexplorer_recv", true);
                if (flag.equalsIgnoreCase("S") && send)
                    cotexplorerlog.setText(String.format("%s: %s\n----------\n", flag, log) + cotexplorerlog.getText());
                else if (flag.equalsIgnoreCase("R") && recv)
                    cotexplorerlog.setText(String.format("%s: %s\n----------\n", flag, log) + cotexplorerlog.getText());
            }
        });
    }

    @Override
    public void disposeImpl() {

    }

    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownVisible(boolean v) {
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
    }

    @Override
    public void onDropDownClose() {
    }

    @Override
    public void logSend(CotEvent cotEvent, String s) {
        Log.i(TAG, "Sending");
        String filter;
        if (cotFilter.isEmpty())
            filter = _sharedPreference.getString("plugin_cotexplorer_type", "");
        else
            filter = cotFilter;
        if (filter.isEmpty())
            writeLog(cotEvent.toString(), "S");
        else if (filter.startsWith(cotEvent.getType()))
            writeLog(cotEvent.toString(), "S");
    }

    @Override
    public void logSend(CotEvent cotEvent, String[] strings) {
        Log.i(TAG, "Sending2");
        String filter;
        if (cotFilter.isEmpty())
            filter = _sharedPreference.getString("plugin_cotexplorer_type", "");
        else
            filter = cotFilter;
        if (filter.isEmpty())
            writeLog(cotEvent.toString(), "S");
        else if (filter.startsWith(cotEvent.getType()))
            writeLog(cotEvent.toString(), "S");
    }

    @Override
    public void logReceive(CotEvent cotEvent, String s, String s1) {
        Log.i(TAG, "Receive");
        String filter;
        if (cotFilter.isEmpty())
             filter = _sharedPreference.getString("plugin_cotexplorer_type", "");
        else
            filter = cotFilter;
        if (filter.isEmpty())
            writeLog(cotEvent.toString(), "R");
        else if (filter.startsWith(cotEvent.getType()))
            writeLog(cotEvent.toString(), "R");
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");

    }
}
