package qrcoba.w3engineers.com.qrcoba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Parametre extends AppCompatActivity {
    private Button btnExit, btnSave;
    private EditText edURL;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametre);

        edURL = (EditText) findViewById(R.id.ipaddress);
        btnExit = (Button) findViewById(R.id.btnExit);
        btnSave = (Button) findViewById(R.id.btnSave);

        sharedpreferences = getSharedPreferences("myPref",
                Context.MODE_PRIVATE);
        if (sharedpreferences.contains("IP")) {
            edURL.setText(sharedpreferences.getString("IP", ""));
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edURL.setText("");
                Intent intent = new Intent(Parametre.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void Save() {
        String n = edURL.getText().toString();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("IP", n);
        editor.commit();
        Intent intent = new Intent(Parametre.this, LoginActivity.class);
        startActivity(intent);
    }
}
