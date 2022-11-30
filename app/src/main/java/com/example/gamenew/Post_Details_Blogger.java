package com.example.gamenew;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Post_Details_Blogger extends AppCompatActivity {


    // Khai báo các loại sử dụng
    private TextView titleTV,publishInfoTV;
    private WebView webView;

    private String postID;
    private static final String TAG = "POST_DETAILS_TAG";
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details_blogger);

        titleTV = findViewById(R.id.titleTV_detail);
        publishInfoTV = findViewById(R.id.publishInfoTV);
        webView = findViewById(R.id.webView);

        /// Sửa thanh action bar;
        //actionBar = getSupportActionBar();
        //actionBar.setTitle("Chi tiết bài viết");
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);

        /// Lấy post ID từ intent truyền sang

        postID = getIntent().getStringExtra("postID");
        Log.d(TAG, "onCreate: postid " + postID);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSaveFormData(true);




        /// Load bài viêt
        String url;
        url ="https://www.googleapis.com/blogger/v3/blogs/"
                +Blogger_Setup.Blog_id
                +"/posts/"+postID
                +"?key="+Blogger_Setup.Api_key;
        Log.d(TAG, "load_chi_tiet: "+url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Title = jsonObject.getString("title");
                    String Published = jsonObject.getString("published");
                    String content = jsonObject.getString("content");
                    String url = jsonObject.getString("url");
                    String Displayname = jsonObject.getJSONObject("author").getString("displayName");


                    //// Chuyển đổi thời gian
                    /// Định dạng ngày đăng
                    String GTMdate = Published;
                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/mm/yyyy K:mm a");
                    String formatteddate ="";

                    try{
                        Date date = dateFormat1.parse(GTMdate);
                        formatteddate = dateFormat2.format(date);

                    }catch (Exception e){
                        formatteddate = Published;
                        e.printStackTrace();
                    }


                    /// Sửa thanh action bar;
                    actionBar = getSupportActionBar();
                    actionBar.setTitle("Quay lại trang chính");
                    actionBar.setDisplayShowHomeEnabled(true);
                    actionBar.setDisplayHomeAsUpEnabled(true);

                    /// Set data hiển thị
                    titleTV.setText(Title);
                    publishInfoTV.setText(Displayname + " " + formatteddate);
                    //// hiển thị định dạng web chỗ content thông qua webview
                    webView.loadDataWithBaseURL(null,
                            "<style>img,tbody,table {display: inline;height: auto;max-width: 100%;} iframe { display: block; max-width:100%; margin-top:10px; margin-bottom:10px; }</style>"+content,"text/html", "UTF-8" , null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /// Lỗi khi tải bài viết
                Toast.makeText(Post_Details_Blogger.this, "Đã có lỗi khi tải bài viết", Toast.LENGTH_SHORT).show();
            }
        });

        /// Thêm reque vào hàng đợi
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);



    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;}


}