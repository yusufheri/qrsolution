package qrcoba.w3engineers.com.qrcoba;

import androidx.appcompat.app.AppCompatActivity;
import qrcoba.w3engineers.com.qrcoba.ui.scanresult.ScanResultActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private EditText username, psw ;
    private Button btnReset, btnLogin;
    String DataParseUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        psw = (EditText) findViewById(R.id.psw);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnReset = (Button) findViewById(R.id.btnReset);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username.setText("");
                psw.setText("");
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }



    class LoginAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String _username = params[0] ;
            String _password = params[1] ;
            String checkResponse = "";
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();



            nameValuePairs.add(new BasicNameValuePair("username", _username));
            nameValuePairs.add(new BasicNameValuePair("psw", _password));

            try {

                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(DataParseUrl);
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httpPost);
                checkResponse = EntityUtils.toString(response.getEntity());


            }catch (IOException e) {
                Log.e("IOException", e.getMessage());
            }
            return checkResponse;
        }
        @Override
        protected void onPostExecute(String checkResponse) {
            super.onPostExecute(checkResponse);
            Log.e("TEST", checkResponse);

            if (checkResponse.equals("1")){
                Toast.makeText(LoginActivity.this, "Bien enregistré", Toast.LENGTH_SHORT).show();
            }else {
                //Non enregistré
                Toast.makeText(LoginActivity.this, checkResponse, Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(ScanResultActivity.this, "Doneés envoyée avec succée !", Toast.LENGTH_LONG).show();

        }
    }




}
