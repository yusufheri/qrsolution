package qrcoba.w3engineers.com.qrcoba.ui.home;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import qrcoba.w3engineers.com.qrcoba.AproposFragment;
import qrcoba.w3engineers.com.qrcoba.R;
import qrcoba.w3engineers.com.qrcoba.databinding.ActivityHomeBinding;
import qrcoba.w3engineers.com.qrcoba.helpers.util.PermissionUtil;
import qrcoba.w3engineers.com.qrcoba.ui.generate.GenerateFragment;
import qrcoba.w3engineers.com.qrcoba.ui.history.HistoryFragment;
import qrcoba.w3engineers.com.qrcoba.ui.scan.ScanFragment;
import qrcoba.w3engineers.com.qrcoba.ui.settings.SettingsActivity;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityHomeBinding mBinding;
    private Menu mToolbarMenu;

    public Menu getToolbarMenu() {
        return mToolbarMenu;
    }

    public void setToolbarMenu(Menu toolbarMenu) {
        mToolbarMenu = toolbarMenu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        getWindow().setBackgroundDrawable(null);

        setListeners();
        initializeToolbar();
        initializeBottomBar();
        //checkInternetConnection();
        //playAd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
    }

    private void initializeToolbar() {
        setSupportActionBar(mBinding.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar_menu, menu);
        setToolbarMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        mBinding.textViewHistory.setOnClickListener(this);
        mBinding.textViewScan.setOnClickListener(this);
        mBinding.textViewApropos.setOnClickListener(this);

        mBinding.textViewHistory.setOnClickListener(this);
        mBinding.imageViewScan.setOnClickListener(this);
        mBinding.imageViewApropos.setOnClickListener(this);

        mBinding.constraintLayoutHistoryContainer.setOnClickListener(this);
        mBinding.constraintLayoutScanContainer.setOnClickListener(this);
        mBinding.constraintLayoutAproposContainer.setOnClickListener(this);
    }

    private void initializeBottomBar() {
        clickOnScan();
    }


    private void clickOnHistory() {
        mBinding.textViewHistory.setTextColor(
                ContextCompat.getColor(this, R.color.bottom_bar_selected));

        mBinding.textViewScan.setTextColor(
                ContextCompat.getColor(this, R.color.bottom_bar_normal));

        mBinding.textViewApropos.setTextColor(
                ContextCompat.getColor(this, R.color.bottom_bar_normal));

        mBinding.imageViewHistory.setVisibility(View.INVISIBLE);
        mBinding.imageViewHistoryActive.setVisibility(View.VISIBLE);

        mBinding.imageViewScan.setVisibility(View.VISIBLE);
        mBinding.imageViewScanActive.setVisibility(View.INVISIBLE);

        mBinding.imageViewApropos.setVisibility(View.VISIBLE);
        mBinding.imageViewAproposActive.setVisibility(View.INVISIBLE);

        setToolbarTitle(getString(R.string.toolbar_title_history));
        showFragment(HistoryFragment.newInstance());
    }
    private void clickOnScan() {
        if (PermissionUtil.on().requestPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {


            mBinding.textViewHistory.setTextColor(
                    ContextCompat.getColor(this, R.color.bottom_bar_normal));

            mBinding.textViewScan.setTextColor(
                    ContextCompat.getColor(this, R.color.bottom_bar_selected));

            mBinding.textViewApropos.setTextColor(
                    ContextCompat.getColor(this, R.color.bottom_bar_normal));

            mBinding.imageViewHistory.setVisibility(View.VISIBLE);
            mBinding.imageViewHistoryActive.setVisibility(View.INVISIBLE);

            mBinding.imageViewScan.setVisibility(View.INVISIBLE);
            mBinding.imageViewScanActive.setVisibility(View.VISIBLE);

            mBinding.imageViewApropos.setVisibility(View.VISIBLE);
            mBinding.imageViewAproposActive.setVisibility(View.INVISIBLE);

            setToolbarTitle(getString(R.string.toolbar_title_scan));
            showFragment(ScanFragment.newInstance());
        }
    }
    private void clickOnApropos() {
        mBinding.textViewHistory.setTextColor(
                ContextCompat.getColor(this, R.color.bottom_bar_normal));

        mBinding.textViewScan.setTextColor(
                ContextCompat.getColor(this, R.color.bottom_bar_normal));

        mBinding.textViewApropos.setTextColor(
                ContextCompat.getColor(this, R.color.bottom_bar_selected));

        mBinding.imageViewHistory.setVisibility(View.VISIBLE);
        mBinding.imageViewHistoryActive.setVisibility(View.INVISIBLE);

        mBinding.imageViewScan.setVisibility(View.VISIBLE);
        mBinding.imageViewScanActive.setVisibility(View.INVISIBLE);

        mBinding.imageViewApropos.setVisibility(View.INVISIBLE);
        mBinding.imageViewAproposActive.setVisibility(View.VISIBLE);

        setToolbarTitle(getString(R.string.toolbar_title_apropos));
        showFragment(AproposFragment.newInstance());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_view_history:
            case R.id.text_view_generate:
            case R.id.constraint_layout_history_container:
                clickOnHistory();
                break;

            case R.id.image_view_scan:
            case R.id.text_view_scan:
            case R.id.constraint_layout_scan_container:
                clickOnScan();
                break;

            case R.id.image_view_apropos:
            case R.id.text_view_apropos:
            case R.id.constraint_layout_apropos_container:
                clickOnApropos();
                break;
        }
    }

    private void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.coordinator_layout_fragment_container, fragment,
                fragment.getClass().getSimpleName());
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.REQUEST_CODE_PERMISSION_DEFAULT) {
            boolean isAllowed = true;

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isAllowed = false;
                }
            }

            if (isAllowed) {
                clickOnScan();
            }
        }
    }

  /*  public void hideAdMob()
    {
        if (mBinding.adView.isShown())
            mBinding.adView.setVisibility(View.GONE);
    }

    public void showAdmob()
    {
        if (!mBinding.adView.isShown())
            mBinding.adView.setVisibility(View.VISIBLE);
    }*/
}
