package qrcoba.w3engineers.com.qrcoba.ui.scanresult;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import qrcoba.w3engineers.com.qrcoba.LoginActivity;
import qrcoba.w3engineers.com.qrcoba.Parametre;
import qrcoba.w3engineers.com.qrcoba.R;
import qrcoba.w3engineers.com.qrcoba.databinding.ActivityScanResultBinding;
import qrcoba.w3engineers.com.qrcoba.helpers.constant.IntentKey;
import qrcoba.w3engineers.com.qrcoba.helpers.constant.PreferenceKey;
import qrcoba.w3engineers.com.qrcoba.helpers.model.Code;
import qrcoba.w3engineers.com.qrcoba.helpers.util.SharedPrefUtil;
import qrcoba.w3engineers.com.qrcoba.helpers.util.TimeUtil;
import qrcoba.w3engineers.com.qrcoba.helpers.util.database.DatabaseUtil;
import qrcoba.w3engineers.com.qrcoba.ui.settings.SettingsActivity;

public class ScanResultActivity extends AppCompatActivity implements View.OnClickListener {

    private CompositeDisposable mCompositeDisposable;
    private ActivityScanResultBinding mBinding;
    private Menu mToolbarMenu;
    private Code mCurrentCode;
    private boolean mIsHistory, mIsPickedFromGallery;
    SharedPreferences preference;

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public void setCompositeDisposable(CompositeDisposable compositeDisposable) {
        mCompositeDisposable = compositeDisposable;
    }

    public Code getCurrentCode() {
        return mCurrentCode;
    }

    public void setCurrentCode(Code currentCode) {
        mCurrentCode = currentCode;
    }

    public Menu getToolbarMenu() {
        return mToolbarMenu;
    }

    public void setToolbarMenu(Menu toolbarMenu) {
        mToolbarMenu = toolbarMenu;
    }

    EditText edCote;
    TextView edIdentifiant, valider, tToken;
    String getIdentifiant, getCote, getToken ;
    String dataUrl, spToken;

    SharedPreferences sharedpreferences;
    String DataParseUrl, UrlServer, num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scan_result);
        setCompositeDisposable(new CompositeDisposable());

        getWindow().setBackgroundDrawable(null);
        initializeToolbar();
        loadQRCode();
        setListeners();

        TextView textViewsend = findViewById(R.id.text_view_send);
        edIdentifiant = findViewById(R.id.text_view_content);
        edCote = findViewById(R.id.edittext_cote);

        sharedpreferences = getSharedPreferences("myPref",Context.MODE_PRIVATE);
        if (sharedpreferences.contains("IP")) {
            DataParseUrl =  sharedpreferences.getString("IP", "");
        }

        //Toast.makeText(this, "OnCreate : " + edIdentifiant.getText().toString(), Toast.LENGTH_LONG).show();

        textViewsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (sharedpreferences.contains("num")) {
                    num =  sharedpreferences.getString("num", "");
                }

                if (DataParseUrl.isEmpty()) {
                    Intent intent= new Intent(getApplicationContext(), Parametre.class);
                    startActivity(intent);
                } else {
                    if (num.isEmpty()) {
                        Intent intent= new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    } else {
                        UrlServer = "http://" + DataParseUrl + "/qrsolution/index.php";
                        GetDataFromEditText();

                        String params[] = {getIdentifiant, getCote, getToken, num};
                        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
                        sendPostReqAsyncTask.execute(params);
                    }


                }
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "Resume : " + edIdentifiant.getText().toString(), Toast.LENGTH_LONG).show();

        String params2[] = {edIdentifiant.getText().toString()};
        RecoverDataAsyncTask recoverDataAsyncTask = new RecoverDataAsyncTask();
        recoverDataAsyncTask.execute(params2);
    }

    private void setListeners() {
        mBinding.textViewOpenInBrowser.setOnClickListener(this);
        mBinding.imageViewShare.setOnClickListener(this);
    }
    private void loadQRCode() {
        Intent intent = getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle != null && bundle.containsKey(IntentKey.MODEL)) {
                setCurrentCode(bundle.getParcelable(IntentKey.MODEL));
            }

            if (bundle != null && bundle.containsKey(IntentKey.IS_HISTORY)) {
                mIsHistory = bundle.getBoolean(IntentKey.IS_HISTORY);
            }

            if (bundle != null && bundle.containsKey(IntentKey.IS_PICKED_FROM_GALLERY)) {
                mIsPickedFromGallery = bundle.getBoolean(IntentKey.IS_PICKED_FROM_GALLERY);
            }
        }

        if (getCurrentCode() != null) {
            mBinding.textViewContent.setText(String.format(Locale.ENGLISH,
                    getString(R.string.content), getCurrentCode().getContent()));

            mBinding.textViewType.setText(String.format(Locale.ENGLISH, getString(R.string.code_type),
                    getResources().getStringArray(R.array.code_types)[getCurrentCode().getType()]));

            mBinding.textViewTime.setText(String.format(Locale.ENGLISH, getString(R.string.created_time),
                    TimeUtil.getFormattedDateString(getCurrentCode().getTimeStamp())));

            mBinding.textViewOpenInBrowser.setEnabled(URLUtil.isValidUrl(getCurrentCode().getContent()));

            if (!TextUtils.isEmpty(getCurrentCode().getCodeImagePath())) {
                Glide.with(this)
                        .asBitmap()
                        .load(getCurrentCode().getCodeImagePath())
                        .into(mBinding.imageViewScannedCode);
            }

            if (SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.COPY_TO_CLIPBOARD)
                    && !mIsHistory) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText(
                            getString(R.string.scanned_qr_code_content),
                            getCurrentCode().getContent());
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(this, getString(R.string.copied_to_clipboard),
                            Toast.LENGTH_SHORT).show();
                }
            }

            if (SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.SAVE_HISTORY) && !mIsHistory) {
                getCompositeDisposable().add(DatabaseUtil.on().insertCode(getCurrentCode())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        }));
            }
        }
    }
    private void initializeToolbar() {
        setSupportActionBar(mBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar_menu, menu);
        setToolbarMenu(menu);
        return true;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_open_in_browser:
                if (getCurrentCode() != null
                        && URLUtil.isValidUrl(getCurrentCode().getContent())) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(getCurrentCode().getContent()));
                    startActivity(browserIntent);
                }
                break;

            case R.id.image_view_share:
                if (getCurrentCode() != null) {
                    shareCode(new File(getCurrentCode().getCodeImagePath()));
                }
                break;

            default:
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getCompositeDisposable().dispose();

        if (getCurrentCode() != null
                && !SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.SAVE_HISTORY)
                && !mIsHistory && !mIsPickedFromGallery) {
            new File(getCurrentCode().getCodeImagePath()).delete();
        }
    }
    private void shareCode(File codeImageFile) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                    getString(R.string.file_provider_authority), codeImageFile));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(codeImageFile));
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_code_using)));
    }


    public void GetDataFromEditText(){
        getCote = edCote.getText().toString();
        getIdentifiant = edIdentifiant.getText().toString();
    }
    //Envoie de donées

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                String _id = params[0] ;
                String _cote = params[1] ;
                String _token = params[2];
                String _num = params[3];
                String checkResponse = "";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("id", _id));
                nameValuePairs.add(new BasicNameValuePair("cote", _cote));
                nameValuePairs.add(new BasicNameValuePair("token", _token));
                nameValuePairs.add(new BasicNameValuePair("n", _num));

                try {
                    Log.e("URL", UrlServer);
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(UrlServer);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    checkResponse = EntityUtils.toString(response.getEntity());


                }
                catch (ClientProtocolException e) {

                }
                catch (IOException e) {
                    Log.e("IOException", e.getMessage());
                }
                return checkResponse;
            }
            @Override
            protected void onPostExecute(String checkResponse) {
                super.onPostExecute(checkResponse);
                Log.e("TEST", checkResponse);

                if (isInteger(checkResponse)){
                    Toast.makeText(ScanResultActivity.this, "Bien enregistré", Toast.LENGTH_SHORT).show();
                }else {
                    //Non enregistré
                    Toast.makeText(ScanResultActivity.this, checkResponse, Toast.LENGTH_SHORT).show();
                }

            }

            public boolean isInteger( String input )
            {
                try
                {
                    Integer.parseInt( input );
                    return true;
                }
                catch( Exception e)
                {
                    return false;
                }
            }
        }


        class RecoverDataAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String _id = params[0] ;
            String checkResponse = "";
            List<NameValuePair> nameValuePairs2 = new ArrayList<NameValuePair>();

            nameValuePairs2.add(new BasicNameValuePair("id", _id));
            Log.e("RECOVER", _id);

            try {

                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://" + DataParseUrl + "/qrsolution/recover.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs2));
                HttpResponse response = httpClient.execute(httpPost);
                checkResponse = EntityUtils.toString(response.getEntity());


            }
            catch (ClientProtocolException e) {

            }
            catch (IOException e) {
                Log.e("IOException", e.getMessage());
            }
            return checkResponse;
        }
        @Override
        protected void onPostExecute(String checkResponse) {
            super.onPostExecute(checkResponse);
            Log.e("TEST", checkResponse);

            if (isInteger(checkResponse)){
                int val = Integer.parseInt(checkResponse);
                if (val > -1) {
                    edCote.setText(String.valueOf(val));
                    edCote.setEnabled(false);
                } else { edCote.setEnabled(true);}

            }

        }

        public boolean isInteger( String input )
        {
            try
            {
                Integer.parseInt( input );
                return true;
            }
            catch( Exception e)
            {
                return false;
            }
        }
    }


}
