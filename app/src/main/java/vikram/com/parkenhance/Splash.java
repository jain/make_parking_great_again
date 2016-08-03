package vikram.com.parkenhance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Splash extends Activity {
    InterstitialAd mInterstitialAd;
    Timer waitTimer;
    boolean interstitialCanceled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        ImageView image = (ImageView) findViewById(R.id.banner);
        Random rand = new Random();
        int i = rand.nextInt(4);
        switch (i){
            case 0:
                image.setImageResource(R.drawable.ad1);
                break;
            case 1:
                image.setImageResource(R.drawable.ad2);
                break;
            case 2:
                image.setImageResource(R.drawable.ad3);
                break;
            case 3:
                image.setImageResource(R.drawable.ad4);
                break;
        }
        /*ImageView image = new ImageView(this);
        // Assumes you have a resource with the name kitten.
        image.setImageResource(R.drawable.logo);
        setContentView(image);
      /*  mInterstitialAd = new InterstitialAd(this);

        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                startMainActivity();
            }
            @Override
            public void onAdLoaded() {
                if (!interstitialCanceled) {
                    waitTimer.cancel();
                    mInterstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                startMainActivity();
            }
        });*/
       /* MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
        waitTimer = new Timer();
        waitTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                interstitialCanceled = true;
                Splash.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startMainActivity();
                    }
                });
            }
        }, 1000);
       // requestNewInterstitial();
    }
    /*private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }*/
    private void startMainActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    /*@Override
    public void onPause() {
        waitTimer.cancel();
        interstitialCanceled = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else if (interstitialCanceled) {
            startMainActivity();
        }
    }*/
}
