package android.bignerdranch.pixivbrowser;

import static android.bignerdranch.pixivbrowser.InternetUtils.FetchHtml;
import static android.bignerdranch.pixivbrowser.InternetUtils.buildGlideUrl;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Detail extends AppCompatActivity {
    ImageView mImageView;
    TextView title,author,rank,time;
    ImageButton like,share,next,prev;
    int position = 0;
    int num;
    String id;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_detail);

        title = findViewById(R.id.title);
        author = findViewById(R.id.author);
        rank = findViewById(R.id.rank);
        time = findViewById(R.id.time);
        like = findViewById(R.id.like);
        share = findViewById(R.id.share);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        mImageView = findViewById(R.id.image);

        id = getIntent().getStringExtra("id");
        String author_t = getIntent().getStringExtra("author");
        String rank_t = getIntent().getStringExtra("rank");
        String title_t = getIntent().getStringExtra("title");
        String time_t = getIntent().getStringExtra("time");


        title.setText(title_t);
        author.setText("作者：" + author_t);
        rank.setText(rank_t + ' ');
        time.setText(time_t);


        prev.setOnClickListener(v -> {
            if(position > 0){
                next.setVisibility(View.VISIBLE);
                position--;
                loadImg(buildGlideUrl(url.substring(0,url.length()-5) + position + ".jpg"));
                if(position == 0){
                    prev.setVisibility(View.INVISIBLE);
                }
            }
        });
        next.setOnClickListener(v -> {
            if(position < num){
                prev.setVisibility(View.VISIBLE);
                position++;
                loadImg(buildGlideUrl(url.substring(0,url.length()-5) + position + ".jpg"));
                if(position == num - 1){
                    next.setVisibility(View.INVISIBLE);
                }
            }
        });

        //处理thread回调
        Handler handler = new Handler(msg -> {
            if (msg.what == 0) {
                loadImg(buildGlideUrl(url));
                if(num > 1) next.setVisibility(View.VISIBLE);
                if (num == 1) {
                    next.setVisibility(View.INVISIBLE);
                    prev.setVisibility(View.INVISIBLE);
                }
            }
            return false;
        });

        new Thread(() -> {
            String html = FetchHtml("https://www.pixiv.net/ajax/illust/" + id,"https://www.pixiv.net/artworks/108343272");
            Document document = Jsoup.parse(html);
            String json = document.select("body").text();
            url = getSourceUrlByJsonFromAjax(json);
            num = getNumByJsonFromAjax(json);
            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);
        }).start();

        like.setOnClickListener(v -> {

        });
        share.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("URL","www.pixiv.net/artworks/" + id);
            clipboardManager.setPrimaryClip(data);
            Toast.makeText(this, "已复制链接到剪贴板！", Toast.LENGTH_SHORT).show();
        });


    }

    private void loadImg(GlideUrl url){
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(url)
                .into(mImageView);
    }

    public String getSourceUrlByJsonFromAjax(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        return (String)jsonObject.getJSONObject("body").getJSONObject("urls").get("original");
    }

    public int getNumByJsonFromAjax(String jsonStr){
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        return (int)jsonObject.getJSONObject("body").getJSONObject("userIllusts").getJSONObject(id).get("pageCount");
    }
}