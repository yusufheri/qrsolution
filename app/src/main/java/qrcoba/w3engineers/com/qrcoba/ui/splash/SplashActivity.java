package qrcoba.w3engineers.com.qrcoba.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import qrcoba.w3engineers.com.qrcoba.LoginActivity;
import qrcoba.w3engineers.com.qrcoba.R;
import qrcoba.w3engineers.com.qrcoba.ui.home.HomeActivity;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DELAY = 1500;
    private ImageView mImageViewLogo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setBackgroundDrawable(null);

        initializeViews();
        animateLogo();
        goToMainPage();
    }
    private void initializeViews() {
        mImageViewLogo = findViewById(R.id.image_view_logo);
    }
    private void goToMainPage() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DELAY);
    }
    private void animateLogo() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_without_duration);
        fadeInAnimation.setDuration(SPLASH_DELAY);

        mImageViewLogo.startAnimation(fadeInAnimation);
    }
}
