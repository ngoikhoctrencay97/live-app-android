package com.hypertrack.live.debug;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hypertrack.live.R;
import com.hypertrack.sdk.HyperTrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DebugActivity extends Activity implements AdapterView.OnItemClickListener {
    public static final String TAG = DebugActivity.class.getSimpleName();

    public static final int CHANGE_DOMAIN_KEY = 0;
    public static final int CHANGE_API_DOMAIN_KEY = 1;
    public static final int SET_DEVICE_META_KEY = 2;
    public static final int RESET_KEY = 3;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        ListView listView = findViewById(R.id.debugListView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.debug_menu));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);


        sharedPreferences = DebugHelper.getSharedPreferences(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (position) {
            case CHANGE_DOMAIN_KEY:
                showDomainChooseDialog();
                break;
            case CHANGE_API_DOMAIN_KEY:
                showApiDomainChooseDialog();
                break;
            case SET_DEVICE_META_KEY:
                showSetDeviceMetaDialog();
                break;
            case RESET_KEY:
                sharedPreferences.edit()
                        .clear()
                        .putString(DebugHelper.DEV_DOMAIN_KEY, "live-api.htprod.hypertrack.com")
                        .apply();
                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit().clear().apply();
                restart();
                break;
        }
    }

    private void showDomainChooseDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.paste, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText debugDomainEditText = ((AlertDialog) dialogInterface).findViewById(R.id.debugDomainEditText);
                        EditText debugPKEditText = ((AlertDialog) dialogInterface).findViewById(R.id.debugPKEditText);
                        String domain = debugDomainEditText.getText().toString();
                        String debugPK = debugPKEditText.getText().toString();

                        sharedPreferences.edit().putString(DebugHelper.DEV_DOMAIN_KEY, domain).apply();
                        getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit()
                                .putString("pub_key", debugPK)
                                .putBoolean("is_tracking", false)
                                .apply();

                        restart();
                        dialogInterface.dismiss();
                    }
                })
                .setView(R.layout.dialog_debug_changedomain)
                .show();
        final EditText debugDomainEditText = ((AlertDialog) alertDialog).findViewById(R.id.debugDomainEditText);
        debugDomainEditText.setText(DebugHelper.getDomain(this));
        EditText debugPKEditText = ((AlertDialog) alertDialog).findViewById(R.id.debugPKEditText);
        debugPKEditText.setText(getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).getString("pub_key", ""));

        alertDialog.findViewById(R.id.debugLiveEnv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debugDomainEditText.setText("live-api.htprod.hypertrack.com");
            }
        });
        alertDialog.findViewById(R.id.debugDevEnv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debugDomainEditText.setText("devpoc-api.htdev.hypertrack.com");
            }
        });
        alertDialog.findViewById(R.id.debugOtherEnv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debugDomainEditText.setText("");
            }
        });
    }

    private void showApiDomainChooseDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.paste, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText debugApiDomainEditText = ((AlertDialog) dialogInterface).findViewById(R.id.debugApiDomainEditText);
                        EditText debugAccountIdEditText = ((AlertDialog) dialogInterface).findViewById(R.id.debugAccountIdEditText);
                        EditText debugSecretKeyEditText = ((AlertDialog) dialogInterface).findViewById(R.id.debugSecretKeyEditText);
                        String domain = debugApiDomainEditText.getText().toString();
                        String accountId = debugAccountIdEditText.getText().toString();
                        String secretKey = debugSecretKeyEditText.getText().toString();

                        sharedPreferences.edit()
                                .putString(DebugHelper.DEV_API_DOMAIN_KEY, domain)
                                .putString(DebugHelper.DEV_ACCOUNTID_KEY, accountId)
                                .putString(DebugHelper.DEV_SECRETKEY_KEY, secretKey)
                                .apply();

                        restart();
                        dialogInterface.dismiss();
                    }
                })
                .setView(R.layout.dialog_debug_changeapidomain)
                .show();
        final EditText debugApiDomainEditText = ((AlertDialog) alertDialog).findViewById(R.id.debugApiDomainEditText);
        debugApiDomainEditText.setText(DebugHelper.getApiDomain(this));
        EditText debugAccountIdEditText = ((AlertDialog) alertDialog).findViewById(R.id.debugAccountIdEditText);
        EditText debugSecretKeyEditText = ((AlertDialog) alertDialog).findViewById(R.id.debugSecretKeyEditText);
        debugAccountIdEditText.setText(sharedPreferences.getString(DebugHelper.DEV_ACCOUNTID_KEY, ""));
        debugSecretKeyEditText.setText(sharedPreferences.getString(DebugHelper.DEV_SECRETKEY_KEY, ""));

        alertDialog.findViewById(R.id.debugLiveEnv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debugApiDomainEditText.setText("v3.api.hypertrack.com");
            }
        });
        alertDialog.findViewById(R.id.debugDevEnv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debugApiDomainEditText.setText("devpoc-public-api.htdev.hypertrack.com");
            }
        });
        alertDialog.findViewById(R.id.debugOtherEnv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debugApiDomainEditText.setText("");
            }
        });
    }

    private void showSetDeviceMetaDialog() {
        new AlertDialog.Builder(this)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.paste, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog alertDialog = ((AlertDialog) dialogInterface);
                        EditText debugNameEditText = alertDialog.findViewById(R.id.debugNameEditText);
                        EditText debugDataEditText = alertDialog.findViewById(R.id.debugDataEditText);

                        String name = debugNameEditText.getText().toString();
                        String dataText = debugDataEditText.getText().toString();
                        final Map<String, Object> data = new HashMap<>();
                        try {
                            JSONObject jsonObject = new JSONObject(dataText);
                            Iterator<String> iterator = jsonObject.keys();
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                data.put(key, jsonObject.get(key));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        HyperTrack.setNameAndMetadataForDevice(name, data);
                        dialogInterface.dismiss();
                    }
                })
                .setView(R.layout.dialog_debug_setdevicemeta)
                .show();
    }

    private void restart() {
        LocalBroadcastManager.getInstance(DebugActivity.this).sendBroadcast(new Intent(DebugHelper.RESTART_ACTION));
        finish();
    }
}