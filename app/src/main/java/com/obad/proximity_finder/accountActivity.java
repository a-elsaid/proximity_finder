package com.obad.proximity_finder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class accountActivity extends Activity implements  View.OnClickListener {
    Button SignIn;
    Button SignUp;
    EditText idTB;
    EditText PasswordTB;
    TextView Register;
    String id;
    String password;
    StringBuffer buffer;
    byte[] data;

    public static final String TAG = accountActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        SignIn=(Button) findViewById(R.id.buttonSignin);
        idTB= (EditText) findViewById(R.id.Email_addressTB);
        PasswordTB =(EditText) findViewById(R.id.PasswordTB);
        Register =(TextView) findViewById(R.id.Registorlink);

        SignIn.setOnClickListener(this);
        Register.setOnClickListener(this);






    }





    @Override
    public void onClick(View v) {
        StrictMode.enableDefaults(); //STRICT MODE ENABLED
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        id= idTB.getText().toString();
        password=PasswordTB.getText().toString();
        String data= id;
        Pattern emailPattern = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher emailMatcher = emailPattern.matcher(data);

        InputStream stream = null;
        // Setting the NameValuePair


        switch (v.getId()) {
            case R.id.buttonSignin:
                if( id.equals("") && password.equals("")){
                    Toast.makeText(getApplicationContext(), "Please Enter valid values ", Toast.LENGTH_LONG).show();
                }
//                else if(!emailMatcher.matches()){
//                    Toast.makeText(getApplicationContext(), "Please enter valid email ", Toast.LENGTH_LONG).show();
//                }
                else {
                    List<NameValuePair> Values = new ArrayList<NameValuePair>();
                    Values.add(new BasicNameValuePair("email", id));
                    Values.add(new BasicNameValuePair("pass", password));
//id.equals("6") &&
                    if (password.equals("1234567")){
                        Intent i = new Intent(accountActivity.this, servicesActivity.class);
                        i.putExtra("id",id);
                        startActivity(i);
                    }
                    // Setting the connection inside try catch block
//                    try {
//                        HttpClient HClient = new DefaultHttpClient();
//                        HttpPost hpost = new HttpPost("http://people.cs.und.edu/~aelsaid/checkdataDB.php");
////                        HttpPost hpost = new HttpPost("http://people.aero.und.edu/~elemma/checkdataDB.php");
//                        hpost.setEntity(new UrlEncodedFormEntity(Values));
//                        // excute Http post requires
//                        HttpResponse response = HClient.execute(hpost);
//                        HttpEntity hentity = response.getEntity();
//                        stream = hentity.getContent();
//                        if (hentity.getContentLength()==0) {
//
//
//                            Intent i = new Intent(accountActivity.this, MainActivity.class);
//                            i.putExtra("email",email);
//                            startActivity(i);
//                        }
//                        else {
//                            Toast.makeText(getApplicationContext(), "Could not find the Data from Database ", Toast.LENGTH_LONG).show();
//                        }
//
//                    }catch(ClientProtocolException e){
//
//                        Log.e("Failed ", "Log_TAG");
//                        e.printStackTrace();
//                        // StringBuffer buffer = new StringBuffer();
//                    }catch(IOException e){
//                        Log.e("Log_TAG", "IOException");
//                        e.printStackTrace();
//                    }

                }
                break;
            case R.id.Registorlink:
//                startActivity(new Intent(this, Registor.class));
                break;
        }
    }

}
