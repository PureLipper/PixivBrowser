package android.bignerdranch.pixivbrowser;

import static android.bignerdranch.pixivbrowser.InternetUtils.FetchHtml;
import static android.bignerdranch.pixivbrowser.InternetUtils.buildGlideUrl;
import static android.bignerdranch.pixivbrowser.InternetUtils.getDate;
import static android.bignerdranch.pixivbrowser.InternetUtils.getNextDay;
import static android.bignerdranch.pixivbrowser.InternetUtils.getPrevDay;
import static android.bignerdranch.pixivbrowser.InternetUtils.getRankList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView1;
    private RecyclerView mRecyclerView2;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView date;
    private ImageButton prev_day;
    private String prev_day_subUrl;
    private ImageButton next_day;
    private String next_day_subUrl;
    private String date_now;
    private String date_now_subUrl;
    private Adapter adapter1;
    private Adapter adapter2;
    private ParcelFileDescriptor vpn = null;
    private ArrayList<ImageItem> ImageItems = new ArrayList<>();
    private String url_now = "https://www.pixiv.net/ranking.php?mode=daily";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_main);

        date = findViewById(R.id.date);
        prev_day = findViewById(R.id.prev_day);
        next_day = findViewById(R.id.next_day);
        mSwipeRefreshLayout = findViewById(R.id.refresh);
        mRecyclerView1 = findViewById(R.id.recycler1);
        mRecyclerView1.setItemViewCacheSize(25);
        mRecyclerView1.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView2 = findViewById(R.id.recycler2);
        mRecyclerView2.setItemViewCacheSize(25);
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(this));

        Handler handler = new Handler(msg -> {
            if(msg.what == 0){
                adapter1 = new Adapter(ImageItems,0);
                adapter2 = new Adapter(ImageItems,1);
                mRecyclerView1.setAdapter(adapter1);
                mRecyclerView2.setAdapter(adapter2);
                date.setText(date_now);
                if(next_day_subUrl.equals("")) next_day.setVisibility(View.INVISIBLE);
                else next_day.setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
            }else if(msg.what == 1){
                Toast.makeText(this, "网络加载错误，请稍后重试", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
            return false;
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if(next_day_subUrl.equals("")){
                getDaily(handler);
            }else{
                getDailyByDate(handler,date_now_subUrl);
            }
        });
        mSwipeRefreshLayout.setDistanceToTriggerSync(500);
        mSwipeRefreshLayout.setRefreshing(true);
        getDaily(handler);

        //同步滑动
        mRecyclerView1.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (RecyclerView.SCROLL_STATE_IDLE != recyclerView.getScrollState()) {
                    mRecyclerView2.scrollBy(dx, dy);
                }
            }

        });
        mRecyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (RecyclerView.SCROLL_STATE_IDLE != recyclerView.getScrollState()) {
                    mRecyclerView1.scrollBy(dx, dy);
                }
            }
        });

        prev_day.setOnClickListener(v -> {
            mSwipeRefreshLayout.setRefreshing(true);
            date_now_subUrl = prev_day_subUrl;
            getDailyByDate(handler, prev_day_subUrl);
        });
        next_day.setOnClickListener(v -> {
            date_now_subUrl = next_day_subUrl;
            mSwipeRefreshLayout.setRefreshing(true);
            getDailyByDate(handler, next_day_subUrl);
        });

    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private List<ImageItem> mImageItems;
        int side;
        public Adapter(List<ImageItem> imageItems,int side) {
            mImageItems = imageItems;
            this.side = side;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            return new Holder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GlideUrl glideUrl = buildGlideUrl(mImageItems.get(position * 2 + side).getImgUrl());
            Glide.with(MainActivity.this)
                    .asBitmap()
                    .load(glideUrl)
                    .into(((Holder) holder).mImageView);
            ((Holder) holder).title.setText(mImageItems.get(position * 2 + side).getTitle());
            ((Holder) holder).rank.setText(mImageItems.get(position * 2 + side).getRank());
            ((Holder) holder).bind(mImageItems.get(position * 2 + side));
        }

        @Override
        public int getItemCount() {
            return mImageItems.size() / 2;
        }
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        private TextView title;
        private TextView rank;
        private String id;
        private String author;
        private String time;
        private String rank_t;
        private String title_t;
        public Holder(LayoutInflater inflater,ViewGroup parent){
            super(inflater.inflate(R.layout.image_item,parent,false));

            itemView.setOnClickListener(this);
            mImageView = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.textView);
            rank = itemView.findViewById(R.id.rank);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this,Detail.class);
            intent.putExtra("id",id);
            intent.putExtra("author",author);
            intent.putExtra("time",time);
            intent.putExtra("rank",rank_t);
            intent.putExtra("title",title_t);
            startActivity(intent);
        }

        public void bind(ImageItem imageItem){
            id = imageItem.getId();
            author = imageItem.getAuthor();
            time = imageItem.getTime();
            rank_t = imageItem.getRank();
            title_t = imageItem.getTitle();
        }

    }

    private void getDailyByDate(Handler handler,String date_subUrl){
        new Thread(() -> {
            Message message = new Message();
            message.what = 0;
            String html = FetchHtml("https://www.pixiv.net/ranking.php" + date_subUrl,"https://www.pixiv.net/");
            date_now = getDate(html);
            prev_day_subUrl = getPrevDay(html);
            next_day_subUrl = getNextDay(html);
            ImageItems = getRankList(html);
            handler.sendMessage(message);
        }).start();
    }

    private void getDaily(Handler handler){
        new Thread(() -> {
            Message message = new Message();
            message.what = 0;
            String html = FetchHtml(url_now,"https://www.pixiv.net/");
            if(html.equals("")) {
                Message error = new Message();
                error.what = 1;
                handler.sendMessage(error);
            }else{
                date_now = getDate(html);
                prev_day_subUrl = getPrevDay(html);
                next_day_subUrl = getNextDay(html);
                ImageItems = getRankList(html);
                handler.sendMessage(message);
            }

        }).start();
    }
}