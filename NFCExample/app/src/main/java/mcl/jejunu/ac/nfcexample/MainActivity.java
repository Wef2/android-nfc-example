package mcl.jejunu.ac.nfcexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private String nfcTagId;
    private TextView nfcIdText, nfcConnectionText, nfcTimerText;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IsoDep isoDep;
    private Handler handler;
    private MyThread thread;
    private boolean threadStatus;
    private int second = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcIdText = (TextView) findViewById(R.id.nfcIdText);
        nfcConnectionText = (TextView) findViewById(R.id.nfcConnectionText);
        nfcTimerText = (TextView) findViewById(R.id.nfcTimerText);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (nfcAdapter == null) {
            Log.v("NFC", "nfcAdapter == null");
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Log.v("NFC", "nfcAdapter != null");
        }
        handler = new Handler();
        thread = new MyThread();
        threadStatus = true;
        thread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            Log.v("NFC", "Resume");
        } else {
            Log.v("NFC", "Resume else");
        }

        threadStatus = true;
    }

    @Override
    protected void onPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
            Log.v("NFC", "Pause");
        } else {
            Log.v("NFC", "Pause else");
        }
        super.onPause();

        threadStatus = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfcTagId = toMacAddress(tag.getId());
            isoDep = IsoDep.get(tag);
            try {
                if (!isoDep.isConnected()) {
                    isoDep.connect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("NFC", nfcTagId);
            nfcIdText.setText(nfcTagId);
            Toast.makeText(this, "NFC Read Success, ID : " + nfcTagId, Toast.LENGTH_SHORT).show();
        }
    }

    public String toMacAddress(byte[] byteArray) {
        StringBuilder sb = new StringBuilder(18);
        for (byte b : byteArray) {
            if (sb.length() > 0)
                sb.append(':');
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    class MyThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (threadStatus) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isoDep != null) {
                            if (isoDep.isConnected()) {
                                second = 0;
                                nfcConnectionText.setText("connect");
                            } else {
                                second = second + 1;
                                nfcConnectionText.setText("disconnect");
                            }
                        } else {
                            second = second + 1;
                            nfcConnectionText.setText("disconnect");
                        }
                        nfcTimerText.setText("" + second);
                        if (second > 30) {
                            Toast.makeText(MainActivity.this, "Time Over", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
