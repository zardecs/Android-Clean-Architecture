package me.lolevsky.nasaplanetary.view;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.lolevsky.nasaplanetary.MainApplication;
import me.lolevsky.nasaplanetary.R;
import me.lolevsky.nasaplanetary.adapters.MainViewAdapter;
import me.lolevsky.nasaplanetary.adapters.OnItemClicked;
import me.lolevsky.nasaplanetary.domain.imageloader.IImageLoader;
import me.lolevsky.nasaplanetary.model.MainScreenModule;
import me.lolevsky.nasaplanetary.presenter.MainPresenter;
import me.lolevsky.nasaplanetary.presenter.Presenter;

public class MainActivity extends BaseActivity<MainScreenModule> implements OnItemClicked {
    @Inject MainApplication application;
    @Inject MainPresenter mainPresenter;
    @Inject IImageLoader imageLoader;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler) RecyclerView recyclerView;
    @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.image) ImageView imageBackground;

    MainViewAdapter mainViewAdapter;
    Dialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApplication) getApplication()).getApplicationComponent().inject(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mainViewAdapter = new MainViewAdapter(imageLoader, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(application, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mainViewAdapter);

        imageLoader.loadImage(R.drawable.back_main, imageBackground);

        if (savedInstanceState == null) {
            mainPresenter.loadData();
        }

        checkPlayServices();
    }

    private boolean checkPlayServices() {
        final int playServicesStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (playServicesStatus != ConnectionResult.SUCCESS) {
            if (errorDialog == null || (errorDialog != null && !errorDialog.isShowing())) {
                //If google play services in not available show an error dialog and return
                errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, playServicesStatus, 0, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
                errorDialog.show();
            }
            return false;
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        final int playServicesStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (playServicesStatus == ConnectionResult.SUCCESS) {
            application.setPersistence();
        }
    }

    @Override Presenter getPresenter() {
        return mainPresenter;
    }

    @Override public void onLoading() {

    }

    @Override public void onLoadingMore() {

    }

    @Override public void onComplete(MainScreenModule model) {
        mainViewAdapter.setList(model.getEntityList());
    }

    @Override public void onError(String error) {
        Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_SHORT).show();
        coordinatorLayout.setVisibility(View.VISIBLE);
    }

    public void onItemClicked(int position) {
        if (checkPlayServices()) {
            Intent intent = null;

            switch (position) {
                case 0:
                    intent = new Intent(this, PlanetaryApodActivity.class);
                    break;
                case 1:
                    intent = new Intent(this, MarsPhotosActivity.class);
                    break;
                default:
                    return;
            }

            startActivity(intent);
        }
    }
}