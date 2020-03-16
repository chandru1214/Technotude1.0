package tech.info.sasurie.sociall;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {
    private WebView mywebview;
    private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
    // private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mywebview=findViewById(R.id.webview);
        WebSettings mywebsettings=mywebview.getSettings();
        mywebsettings.setUserAgentString(DESKTOP_USER_AGENT);
        mywebview.loadUrl("http://www.sasurieengg.com");
        mywebsettings.setJavaScriptEnabled(true);
        //WebSettings settings = mywebview.getSettings();

    }

    public class   mywebclient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            super.onPageStarted(view,url,favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onBackPressed (){
        if(mywebview.canGoBack()) {
            mywebview.goBack();
        }
        else{
            super.onBackPressed();
        }

    }
}
