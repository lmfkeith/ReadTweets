package com.example.keithliu.readtweets;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;

import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import android.os.AsyncTask;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.text.NumberFormat;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity {

    private String htmlPageUrl = "http://www.twitter.com/search?q=エグゼイド&src=tren"; // ログインしていない状態ではツィートを表示しないため、適当に検索します
    private Document htmlDocument;
    private Elements htmlTweetList;

    private final int minRetweetCount = 5000; // リツィート数５０００以上のツィートは存在しないかも知れないけど、とりあえず５０００に設定する

    private String strHtml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView displayWebvirew = (WebView) findViewById(R.id.webview);

        WebSettings dSettings = displayWebvirew.getSettings();
        dSettings.setDefaultTextEncodingName("utf-8");

        displayWebvirew.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        WebView myWebView = (WebView) findViewById(R.id.webview2);

        WebSettings settings = myWebView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        myWebView.setWebViewClient(new MyWebViewClient());

        /* JavaScript must be enabled if you want it to work, obviously */
        myWebView.getSettings().setJavaScriptEnabled(true);

        /* Register a new JavaScript interface called HTMLOUT */
        myWebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        myWebView.loadUrl(htmlPageUrl);
    }

    /* An instance of this class will be registered as a JavaScript interface */
    private class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            // process the html as needed by the app
            strHtml = html;

            Log.v("ReadTweets", strHtml);

            JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
            jsoupAsyncTask.execute();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        private Boolean isLoadOnce = false;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!isLoadOnce) {
                view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            } else {
                isLoadOnce = false;
            }
        }
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            htmlDocument = Jsoup.parse(strHtml);

            htmlTweetList = htmlDocument.getElementsByClass("Tweet-body");

//            for (Element ele : htmlElementList) {
//                if (ele.className() == "new") {
//                    htmlLinksList.add(ele.getElementsByTag("a").first());
//                }
//            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (htmlTweetList != null){
                String outputData = "<html><head></head><body>";

                for (Element tweet : htmlTweetList) {
                    Element retweetElement = tweet.getElementsByClass("TweetAction-count").first();
                    if (retweetElement != null){
                        String retweetCount = retweetElement.text();
                        Log.v("JSoupParse", "RetweetCount: " + retweetCount);

                        try {
                            Number count = NumberFormat.getNumberInstance(java.util.Locale.US).parse(retweetCount);
                            if (count.intValue() > minRetweetCount){
                                Element linkURL = tweet.select("a[href]").first();
                                if (linkURL != null) {
                                    String url = linkURL.attr("abs:href");
                                    outputData += "<li><a href=\"" + url + "\">"  + url + "</a></li>";
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                }

                outputData += "</body></html>";

                WebView myWebView = (WebView) findViewById(R.id.webview);

                myWebView.loadData(outputData, "text/html; charset=utf-8", "utf-8");
            }


//            Log.v("JSoupParse", htmlBody.toString());
//            if (htmlLinksList != null){
//                String outputData = "<html><head></head><body>";
//                for (Element link : htmlLinksList) {
//                    Log.v("JSoupParse", link.toString());
//                    outputData += "<li>" + link.getElementsByTag("a").first().text() + "</li>";
//                }
//                outputData += "</body></html>";
//
//                WebView myWebView = (WebView) findViewById(R.id.webview);
//
//                myWebView.loadData(outputData, "text/html; charset=utf-8", "utf-8");
//
//                myWebView.setVisibility(View.VISIBLE);
//            }
        }
    }

    /*
    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            htmlDocument = Jsoup.parse(strHtml);
            Element htmlNews = htmlDocument.getElementById("newsTabList01");
            if (htmlNews != null) {
                htmlLinksList = htmlNews.getElementsByClass("new");
            } else {
                htmlLinksList = null;
            }
//            for (Element ele : htmlElementList) {
//                if (ele.className() == "new") {
//                    htmlLinksList.add(ele.getElementsByTag("a").first());
//                }
//            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (htmlLinksList != null){
                String outputData = "<html><head></head><body>";
                for (Element link : htmlLinksList) {
                    Log.v("JSoupParse", link.toString());
                    outputData += "<li>" + link.getElementsByTag("a").first().text() + "</li>";
                }
                outputData += "</body></html>";

                WebView myWebView = (WebView) findViewById(R.id.webview);

                myWebView.loadData(outputData, "text/html; charset=utf-8", "utf-8");

                myWebView.setVisibility(View.VISIBLE);
            }
        }
    }
    */
}
