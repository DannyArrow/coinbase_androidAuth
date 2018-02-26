package com.example.danilo.coinbase;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coinbase.android.sdk.OAuth;
import com.coinbase.api.Coinbase;
import com.coinbase.api.CoinbaseBuilder;
import com.coinbase.api.entity.OAuthTokensResponse;
import com.coinbase.api.exception.CoinbaseException;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

public class MainActivity extends RoboActivity {


        String REDIRECT_URI = "awallet://coinbase-oauth";
        String CLIENT_ID = "5eac8066590b47ad85929b3c1be042a69aa8803567b428a8b9a4d570e1d37442";
        String CLIENT_SECRET = "0edcd0f3a3a2b0fc8061948539c0a43b2e14438cd8cd8fa1b3f9119632d2fe86";
       //String authorize = "https://www.coinbase.com/oauth/authorize?client_id=bd1316d40b884b65c5d98afc51689364b4d";
       @InjectView(R.id.email)
       private TextView mTextView;

    public class DisplayEmailTask extends RoboAsyncTask<String> {
        private OAuthTokensResponse mTokens;

        public DisplayEmailTask(OAuthTokensResponse tokens) {
            super(MainActivity.this);
            mTokens = tokens;
        }

        public String call() throws Exception {
            Coinbase coinbase = new CoinbaseBuilder().withAccessToken(mTokens.getAccessToken()).build();
            return coinbase.getUser().getEmail();
        }

        @Override
        public void onException(Exception ex) {
            mTextView.setText("There was an error fetching the user's email address: " + ex.getMessage());
        }

        @Override
        public void onSuccess(String email) {
            mTextView.setText("Success! The user's email address is: " + email);
        }
    }

    public class CompleteAuthorizationTask extends RoboAsyncTask<OAuthTokensResponse> {
        private Intent mIntent;

        public CompleteAuthorizationTask(Intent intent) {
            super(MainActivity.this);
            mIntent = intent;
        }

        @Override
        public OAuthTokensResponse call() throws Exception {
            return OAuth.completeAuthorization(MainActivity.this, CLIENT_ID, CLIENT_SECRET, mIntent.getData());
        }

        @Override
        public void onSuccess(OAuthTokensResponse tokens) {
            new DisplayEmailTask(tokens).execute();

        }

        @Override
        public void onException(Exception ex) {
            mTextView.setText("There was an error fetching access tokens using the auth code: " + ex.getMessage());
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            new CompleteAuthorizationTask(intent).execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Button button;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
      button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startAuthorization();
              button.setVisibility(View.INVISIBLE);
          }
      });

    }

    private void startAuthorization(){

        try {
            OAuth.beginAuthorization(this, CLIENT_ID, "user", REDIRECT_URI, null);
        } catch (CoinbaseException ex) {
            mTextView.setText("There was an error redirecting to Coinbase: " + ex.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
