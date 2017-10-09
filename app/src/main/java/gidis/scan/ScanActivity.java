package gidis.scan;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;



import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;


/**
 * Created by gidis on 9/14/17.
 */
public class ScanActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 42;
    private static final String TAG = ScanActivity.class.getSimpleName();
    protected Button mScanMore;
    protected DecoratedBarcodeView barcodeView;
    protected BeepManager beepManager;
    protected Context  context;
    protected String apiBaseURL="http://api.themoviedb.org/3/movie/550?api_key=";
    protected ProgressDialog pDialog;


    public void makeRequest(final String data){

        showDialog();


        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();



        asyncHttpClient.get(context, apiBaseURL+data, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                try {
                    JSONObject jObj = new JSONObject(String.valueOf(response));
                    String title = jObj.getString("title");
                    String overView = jObj.getString("overview");
                    String dialog = title + " \n" + title + " \n" +" \n" +overView;
                    genericDialog(dialog);


                } catch (JSONException e) {
                    e.printStackTrace();
                }




                Log.d(TAG, response.toString());
                hideDialog();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {



                genericDialog(errorResponse.toString());
                Log.d(TAG, "Error Response "+errorResponse.toString());
                hideDialog();
            }
        });

    }


    public BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                String extractedData=result.getText();
                makeRequest(extractedData);


            }

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Progress dialog
        pDialog = new ProgressDialog(ScanActivity.this);
        pDialog.setCancelable(false);


        mScanMore =findViewById(R.id.scan_more);
        barcodeView = findViewById(R.id.barcode_scanner);
        barcodeView.decodeSingle(callback);
        beepManager = new BeepManager(this);
        beepManager.setBeepEnabled(true);
        beepManager.setVibrateEnabled(true);
         mScanMore.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             barcodeView.decodeSingle(callback);

         }
     });
//
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }

    }
    @Override
    public void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();

        barcodeView.pause();
    }



    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.setMessage("Checking... ");
        pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            mScanMore.setVisibility(View.VISIBLE);
            pDialog.dismiss();

    }


    private void genericDialog(String content){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Message")
                .content(content)
                .positiveText("Ok")
                .show();
    }
}


