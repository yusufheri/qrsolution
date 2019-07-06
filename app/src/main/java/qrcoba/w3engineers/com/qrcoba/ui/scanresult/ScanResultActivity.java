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
    String DataParseUrl, dataUrl, spToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scan_result);
        setCompositeDisposable(new CompositeDisposable());
    //    playAd();
        getWindow().setBackgroundDrawable(null);
        initializeToolbar();
        loadQRCode();
        setListeners();
        //checkInternetConnection();
        TextView textViewsend = findViewById(R.id.text_view_send);
        edIdentifiant = findViewById(R.id.text_view_content);
        edCote = findViewById(R.id.edittext_cote);
        Toast.makeText(this, SharedPrefUtil.readString(PreferenceKey.QR_TOKEN + ""), Toast.LENGTH_SHORT).show();

        textViewsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                preference = getSharedPreferences(PreferenceKey.MY_PREFERENCE,
                        Context.MODE_PRIVATE);

                if (preference.contains(PreferenceKey.URL_IP)) {
                    dataUrl = preference.getString(PreferenceKey.URL_IP, "");
                    DataParseUrl = "http://"+dataUrl+"/qrsolution/index.php";

                    if (preference.contains(PreferenceKey.QR_TOKEN)){
                        GetDataFromEditText();

                        String params[] = {getIdentifiant, getCote, getToken};
                        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
                        sendPostReqAsyncTask.execute(params);
                    }else {
                        Toast.makeText(ScanResultActivity.this, "Configurer Token", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(ScanResultActivity.this, "Veuillez d'abord inserer une adrese valide", Toast.LENGTH_SHORT).show();
                } finish();
            }
        });
    }
 //  private void playAd() {
 //      AdRequest adRequest = new AdRequest.Builder().build();
 //      mBinding.adView.loadAd(adRequest);
 //      mBinding.adView.setAdListener(new AdListener() {
 //          @Override
 //          public void onAdLoaded() {
 //          }

 //          @Override
 //          public void onAdFailedToLoad(int errorCode) {
 //              mBinding.adView.setVisibility(View.GONE);
 //          }

 //          @Override
 //          public void onAdOpened() {
 //          }

 //          @Override
 //          public void onAdLeftApplication() {
 //          }

 //          @Override
 //          public void onAdClosed() {
 //          }
 //      });
 //  }

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
        getToken = preference.getString(PreferenceKey.QR_TOKEN,"");
        Toast.makeText(this, SharedPrefUtil.readString(PreferenceKey.QR_TOKEN), Toast.LENGTH_SHORT).show();
    }
    //Envoie de donées



        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                String _id = params[0] ;
                String _cote = params[1] ;
                String _token = params[2];
                String checkResponse = "";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();



                nameValuePairs.add(new BasicNameValuePair("id", _id));
                nameValuePairs.add(new BasicNameValuePair("cote", _cote));
                nameValuePairs.add(new BasicNameValuePair("token", _token));

                try {

                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(DataParseUrl);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    checkResponse = EntityUtils.toString(response.getEntity());


                }
                //catch (ClientProtocolException e) {

                //}
                catch (IOException e) {
                    Log.e("IOException", e.getMessage());
                }
                return checkResponse;
            }
            @Override
            protected void onPostExecute(String checkResponse) {
                super.onPostExecute(checkResponse);
                Log.e("TEST", checkResponse);

                if (checkResponse.equals("1")){
                    Toast.makeText(ScanResultActivity.this, "Bien enregistré", Toast.LENGTH_SHORT).show();
                }else {
                    //Non enregistré
                    Toast.makeText(ScanResultActivity.this, checkResponse, Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(ScanResultActivity.this, "Doneés envoyée avec succée !", Toast.LENGTH_LONG).show();

            }
        }



}
