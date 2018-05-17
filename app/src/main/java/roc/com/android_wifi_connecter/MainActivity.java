package roc.com.android_wifi_connecter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;

public class MainActivity extends Activity {

    WifiAdapter wifiAdapter;
    WifiManager wifiManager;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // ここでWifiを片っ端から検索する
            unregisterReceiver(this);
            wifiAdapter.scanResults.clear();
            wifiAdapter.scanResults.addAll(wifiManager.getScanResults());
            wifiAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiAdapter = new WifiAdapter(getApplicationContext());

        ListView wifiList = findViewById(R.id.wifi_list);
        wifiList.setAdapter(wifiAdapter);
        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                // Wifi接続用のダイアログを作成する
                WifiLoginDialogFragment dialogFragment = new WifiLoginDialogFragment();
                dialogFragment.setItemPosition(position);
                dialogFragment.show(getFragmentManager(), "wifi_dialog");
            }
        });

        ButtonRectangle wifeGrep = findViewById(R.id.wifi_grep);
        wifeGrep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // versionが23以上の時にパーミッションチェック
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},0);
                    }
                }

                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }

                // 検索ボタンを押した時にWifi検索を行う
                registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ButtonRectangle wifeGrep = findViewById(R.id.wifi_grep);
            wifeGrep.callOnClick();
        }
    }

    public void executeWifiLogin(int position, String inputPassword) {
        // 対象のwifi情報を取得する
        ScanResult scanResult = wifiAdapter.scanResults.get(position);

        WifiConfiguration configuration = new WifiConfiguration();
        String ssid = scanResult.SSID;
        String password = inputPassword;
        configuration.SSID = String.format("\"%s\"", ssid);

        // wifi情報から暗号化方式を取り出す
        String cipherType = scanResult.capabilities;
        // パターン別にwifiに接続する処理を記述する
        if (cipherType.contains("WPA")) {
            Toast.makeText(getApplicationContext(), "WPAで接続を試みます", Toast.LENGTH_SHORT).show();
            configuration.preSharedKey = String.format("\"%s\"", password);
            if ((null == password) || password.isEmpty()) {
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }
        } else if (cipherType.contains("WEP")) {
            Toast.makeText(getApplicationContext(), "WEPで接続を試みます", Toast.LENGTH_SHORT).show();
            configuration.wepKeys[0] = String.format("\"%s\"", password);
            configuration.wepTxKeyIndex = 0;
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else {
            Toast.makeText(getApplicationContext(), "暗号化タイプが読み込めませんでした", Toast.LENGTH_SHORT).show();
        }
        int netId = wifiManager.addNetwork(configuration);
        wifiManager.disconnect();
        if (netId == -1) {
            Log.d("wifi接続結果","wifi接続に失敗しました。");
        } else {
            wifiManager.enableNetwork(netId, true);
        }
        wifiManager.reconnect();

    }

    public static class WifiLoginDialogFragment extends DialogFragment {

        int position = 0;

        public void setItemPosition(int position) {
            this.position = position;
        }

        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View wifiDialogLayout = inflater.inflate(R.layout.wifi_login_dialog, (ViewGroup)getActivity().findViewById(R.id.wifi_login_dialog));

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Wifiログイン情報の入力");
            builder.setView(wifiDialogLayout);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText editText = wifiDialogLayout.findViewById(R.id.password);
                    String password = editText.getText().toString();
                    MainActivity activity = (MainActivity) getActivity();
                    activity.executeWifiLogin(position, password);
                    dismiss();
                }
            });
            builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // キャンセルなので、何もしない
                }
            });
            return builder.create();
        }
    }
}