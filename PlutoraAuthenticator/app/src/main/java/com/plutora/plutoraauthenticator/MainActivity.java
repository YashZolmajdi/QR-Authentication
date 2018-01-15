package com.plutora.plutoraauthenticator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by ASUS on 11-Jan-18.
 */

public class MainActivity extends Activity implements ZXingScannerView.ResultHandler{
    public static final int CAMERA_REQUEST_CODE = 10;
    public static final String COMPANY = "company";
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        showQrScanner();
    }

    public void addNewUser(View v) {
        setContentView(R.layout.newuser);

        SharedPreferences settings = getApplicationContext().getSharedPreferences(COMPANY, 0);
        ((EditText)findViewById(R.id.domain)).setText(settings.getString("domain", ""));
        ((EditText)findViewById(R.id.username)).setText(settings.getString("username", ""));
        ((EditText)findViewById(R.id.password)).setText(settings.getString("password", ""));
    }

    public void saveUser(View v) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(COMPANY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("domain", ((EditText)findViewById(R.id.domain)).getText().toString());
        editor.putString("username", ((EditText)findViewById(R.id.username)).getText().toString());
        editor.putString("password", ((EditText)findViewById(R.id.password)).getText().toString());
        editor.apply();

        setContentView(R.layout.layout);
        showQrScanner();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScannerView != null && mScannerView.isActivated())
            mScannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.layout);
        showQrScanner();
    }

    @Override
    public void handleResult(final Result result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, CAMERA_REQUEST_CODE);
            } else {
                RequestQueue queue = Volley.newRequestQueue(this);

                JSONObject obj = new JSONObject(result.getText());
                Log.d("My App", obj.get("url").toString());

                SharedPreferences settings = getApplicationContext().getSharedPreferences(COMPANY, 0);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, obj.get("url").toString()+"/"+settings.getString("username", ""),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.d("My App", "Response is: "+ response);

                                builder.setTitle("Message");
                                builder.setMessage("You are now logged in!");
                                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        resumeCam();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();

                                resumeCam();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("My App", "That didn't work!");
                    }
                });
// Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + result.getText() + "\"");
        }
    }

    private void resumeCam() {
        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showQrScanner();
                }
                return;
            }
        }
    }

    private void showQrScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            mScannerView = new ZXingScannerView(this);
            mScannerView = findViewById(R.id.zxscan);
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }
    }
}
