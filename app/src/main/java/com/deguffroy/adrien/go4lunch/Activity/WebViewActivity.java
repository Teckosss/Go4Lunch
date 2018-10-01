package com.deguffroy.adrien.go4lunch.Activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends AppCompatActivity {

    @BindView(R.id.simple_toolbar) Toolbar mToolbar;
    @BindView(R.id.webview_swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.webview) WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        this.configureToolbar();
        this.displayWebView();
        this.configureSwipeRefreshLayout();
    }

    // -------------
    // CONFIGURATION
    // -------------

    private void configureToolbar(){
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();

    }

    private void configureSwipeRefreshLayout(){
        mSwipeRefreshLayout.setOnRefreshListener(this::displayWebView);
    }

    // -------------
    // ACTION
    // -------------

    private void displayWebView(){
        String url = getIntent().getStringExtra("Website");
        if (url != null){
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setLoadsImagesAutomatically(true);
            mWebView.loadUrl(url);
            mWebView.setWebViewClient(new WebViewClient());
            mSwipeRefreshLayout.setRefreshing(false);
        }else{
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }
}
