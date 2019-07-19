package qrcoba.w3engineers.com.qrcoba;

import androidx.appcompat.app.AppCompatActivity;
import qrcoba.w3engineers.com.qrcoba.ui.home.HomeActivity;
import qrcoba.w3engineers.com.qrcoba.ui.scanresult.ScanResultActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private EditText username, psw, token ;
    private Button btnReset, btnLogin,btnParametre;
    SharedPreferences sharedpreferences;
    //String DataParseUrl = "http://192.168.43.124/qrsolution/test.php";
    String DataParseUrl, UrlServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username =  findViewById(R.id.username);
        psw =  findViewById(R.id.psw);
        token = findViewById(R.id.token);
        btnLogin =  findViewById(R.id.btnLogin);
        btnReset =  findViewById(R.id.btnReset);
        btnParametre =  findViewById(R.id.btnParametre);

        sharedpreferences = getSharedPreferences("myPref",Context.MODE_PRIVATE);
        if (sharedpreferences.contains("IP")) {
            DataParseUrl =  sharedpreferences.getString("IP", "");
        }
        try{
            if (DataParseUrl.isEmpty()) {
                Intent intent= new Intent(LoginActivity.this, Parametre.class);
                startActivity(intent);
            } else { UrlServer = "http://" + DataParseUrl + "/qrsolution/test.php"; }
        } catch (Exception e){
            Intent intent= new Intent(LoginActivity.this, Parametre.class);
            startActivity(intent);
        }


        btnParametre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(LoginActivity.this, Parametre.class);
                startActivity(intent);
            }
        });

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
                String params[] = {username.getText().toString(), psw.getText().toString(), token.getText().toString().trim()};
                LoginAsyncTask loginAsyncTask = new LoginAsyncTask();
                loginAsyncTask.execute(params);
            }
        });
    }



    class LoginAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String _username = params[0] ;
            String _password = params[1] ;
            String _token = params[2] ;
            String checkResponse = "";
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();



            nameValuePairs.add(new BasicNameValuePair("username", _username));
            nameValuePairs.add(new BasicNameValuePair("psw", _password));
            nameValuePairs.add(new BasicNameValuePair("token", _token));

            try {

                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(UrlServer);
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

            if (isInteger(checkResponse)){

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("num", checkResponse);
                editor.commit();

                username.setText("");
                psw.setText("");
                token.setText("");

                Intent home = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(home);
            }else {
                if (checkResponse.contains("recommencer")) { psw.setText(""); }
                Toast.makeText(LoginActivity.this, checkResponse, Toast.LENGTH_LONG).show();
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
                Log.e("isInteger", e.getMessage());
                return false;
            }
        }

    }




}
