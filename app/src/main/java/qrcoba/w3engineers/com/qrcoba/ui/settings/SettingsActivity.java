package qrcoba.w3engineers.com.qrcoba.ui.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import qrcoba.w3engineers.com.qrcoba.R;
import qrcoba.w3engineers.com.qrcoba.databinding.ActivitySettingsBinding;
import qrcoba.w3engineers.com.qrcoba.helpers.constant.PreferenceKey;
import qrcoba.w3engineers.com.qrcoba.helpers.util.SharedPrefUtil;
import qrcoba.w3engineers.com.qrcoba.ui.about_us.AboutUsActivity;
import qrcoba.w3engineers.com.qrcoba.ui.privacy_policy.PrivayPolicyActivity;


public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private ActivitySettingsBinding mBinding;
    TextView url, token;
    String dataUrl,DataParseToken;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        preferences = getSharedPreferences(PreferenceKey.MY_PREFERENCE,
                Context.MODE_PRIVATE);

        url = findViewById(R.id.id_url);
        token = findViewById(R.id.token);

        if (preferences.contains(PreferenceKey.URL_IP)) {
            url.setText(preferences.getString(PreferenceKey.URL_IP, ""));
            dataUrl = preferences.getString(PreferenceKey.URL_IP, "");
            DataParseToken = "http://"+dataUrl+"/qrsolution/check.php";

            if (preferences.contains(PreferenceKey.QR_TOKEN)) {
                token.setText(preferences.getString(PreferenceKey.QR_TOKEN,  ""));
            }

        }else {
            url.setText("Aucun IP configuré");
            token.setText("Configuere d'abord l'adresse IP");
        }
        initializeToolbar();
        loadSettings();
        setListeners();

        Toast.makeText(this, DataParseToken, Toast.LENGTH_LONG).show();
    }
    private void loadSettings() {
        mBinding.switchCompatPlaySound.setChecked(SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.PLAY_SOUND));
        mBinding.switchCompatVibrate.setChecked(SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.VIBRATE));
        mBinding.switchCompatSaveHistory.setChecked(SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.SAVE_HISTORY));
        mBinding.switchCompatCopyToClipboard.setChecked(SharedPrefUtil.readBooleanDefaultTrue(PreferenceKey.COPY_TO_CLIPBOARD));
    }
    private void setListeners() {
        mBinding.switchCompatPlaySound.setOnCheckedChangeListener(this);
        mBinding.switchCompatVibrate.setOnCheckedChangeListener(this);
        mBinding.switchCompatSaveHistory.setOnCheckedChangeListener(this);
        mBinding.switchCompatCopyToClipboard.setOnCheckedChangeListener(this);

        mBinding.textViewPlaySound.setOnClickListener(this);
        mBinding.textViewVibrate.setOnClickListener(this);
        mBinding.textViewSaveHistory.setOnClickListener(this);
        mBinding.textViewCopyToClipboard.setOnClickListener(this);
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_compat_play_sound:
                SharedPrefUtil.write(PreferenceKey.PLAY_SOUND, isChecked);
                break;

            case R.id.switch_compat_vibrate:
                SharedPrefUtil.write(PreferenceKey.VIBRATE, isChecked);
                break;

            case R.id.switch_compat_save_history:
                SharedPrefUtil.write(PreferenceKey.SAVE_HISTORY, isChecked);
                break;

            case R.id.switch_compat_copy_to_clipboard:
                SharedPrefUtil.write(PreferenceKey.COPY_TO_CLIPBOARD, isChecked);
                break;

            default:
                break;
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_view_play_sound:
                mBinding.switchCompatPlaySound.setChecked(!mBinding.switchCompatPlaySound.isChecked());
                break;

            case R.id.text_view_vibrate:
                mBinding.switchCompatVibrate.setChecked(!mBinding.switchCompatVibrate.isChecked());
                break;

            case R.id.text_view_save_history:
                mBinding.switchCompatSaveHistory.setChecked(!mBinding.switchCompatSaveHistory.isChecked());
                break;

            case R.id.text_view_copy_to_clipboard:
                mBinding.switchCompatCopyToClipboard.setChecked(!mBinding.switchCompatCopyToClipboard.isChecked());
                break;

            default:
                break;
        }
    }
    public void startAboutUsActivity(View view) {

        startActivity(new Intent(this,AboutUsActivity.class));
    }
    public void startPrivacyPolicyActivity(View view) {
        startActivity(new Intent(this,PrivayPolicyActivity.class));
    }
    public void ip(View view) {
        addUrl();
    }public void tk (View view) {
        addToken();
    }
    public  void  addUrl(){
        View promptsView = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialogue_edittext, null);
        final CardView cardView = promptsView.findViewById(R.id.cv);
        cardView.setCardBackgroundColor(SettingsActivity.this.getResources().getColor(R.color.colorPrimary));
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("Entret l'Adresse");
        final EditText codeURL = promptsView.findViewById(R.id.editText);
        codeURL.setMaxLines(1);
        codeURL.setHint(getString(R.string.hint_url));
        codeURL.setHintTextColor(getResources().getColor(R.color.text_hint));
        codeURL.setBackgroundColor(SettingsActivity.this.getResources().getColor(R.color.colorPrimary));
        codeURL.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
        alertDialogBuilder.setCancelable(false).setPositiveButton(SettingsActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String n = codeURL.getText().toString().trim();

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PreferenceKey.URL_IP, n);
                    editor.commit();

                if (preferences.contains(PreferenceKey.URL_IP)) {
                    url.setText(preferences.getString(PreferenceKey.URL_IP, ""));
                    dataUrl = preferences.getString(PreferenceKey.URL_IP, "");
                    DataParseToken = "http://"+dataUrl+"/qrsolution/check.php";

                    if (preferences.contains(PreferenceKey.QR_TOKEN)) {
                        token.setText(SharedPrefUtil.readString(PreferenceKey.QR_TOKEN + ""));
                    }else {
                        token.setText("Aucun Token configuré");
                    }

                }else {
                    url.setText("Aucun IP configuré");
                    token.setText("Configuere d'abord l'adresse IP");
                }
            }
        }).setNegativeButton(SettingsActivity.this.getString(R.string.annuler), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }

    public  void  addToken(){
        View promptsView = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialogue_edittext, null);
        final CardView cardView = promptsView.findViewById(R.id.cv);
        cardView.setCardBackgroundColor(SettingsActivity.this.getResources().getColor(R.color.colorPrimary));
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("Entrer votre Token");
        final EditText codeToken = promptsView.findViewById(R.id.editText);
        codeToken.setMaxLines(1);
        codeToken.setHint(getString(R.string.hint_token));
        codeToken.setHintTextColor(getResources().getColor(R.color.text_hint));
        codeToken.setBackgroundColor(SettingsActivity.this.getResources().getColor(R.color.colorPrimary));
        codeToken.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});
        alertDialogBuilder.setCancelable(false).setPositiveButton(SettingsActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String t = codeToken.getText().toString().trim();

                    if (t.isEmpty()){
                        Toast.makeText(SettingsActivity.this, "Veuillez completer votre token", Toast.LENGTH_SHORT).show();

                    }else {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(PreferenceKey.QR_TOKEN, t);
                        editor.commit();

                        if (preferences.contains(PreferenceKey.QR_TOKEN)) {
                            token.setText(preferences.getString(PreferenceKey.QR_TOKEN, ""));
                            Log.e("Test", t);
                            try {
                                SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
                                sendPostReqAsyncTask.execute(t);
                            } catch (Exception e) {
                                Log.e("TEST", e.getMessage());
                            }

                        }else {
                            token.setText("Aucun IP configuré");
                        }
                    }
            }
        }).setNegativeButton(SettingsActivity.this.getString(R.string.annuler), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }
   // public void envoiDuToken (final String token){
        class SendPostReqAsyncTask extends AsyncTask<String, Void, Integer> {
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           // ps.setVisibility(View.VISIBLE);
       }
       @Override
       protected Integer doInBackground(String... params) {
           int checkResponse=-10;
           String StringToken = params[0];
           List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
           nameValuePairs.add(new BasicNameValuePair("token", StringToken));

                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(DataParseToken);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    checkResponse=Integer.parseInt(EntityUtils.toString(response.getEntity()));
                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }

           return checkResponse;
       }
       @Override
       protected void onPostExecute(Integer checkResponse) {
           super.onPostExecute(checkResponse);
           Log.e("On post", checkResponse.toString());
           //Toast.makeText(getApplicationContext(), checkResponse, Toast.LENGTH_LONG).show();

             if (checkResponse == 1) {
             } else if(checkResponse == -1){
                 //Token expiré, deconnexion
                 Toast.makeText(SettingsActivity.this, "Token expiré, deconnexion", Toast.LENGTH_SHORT).show();
             } else if (checkResponse == 0) {
                 Toast.makeText(SettingsActivity.this, "Token inexistant", Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(SettingsActivity.this, "Vérification échouée", Toast.LENGTH_SHORT).show();
             }
           //Toast.makeText(SplashActivity.this, "Token envoyé !", Toast.LENGTH_LONG).show();
       }


   }


}
