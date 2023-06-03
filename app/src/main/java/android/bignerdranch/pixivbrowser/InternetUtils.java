package android.bignerdranch.pixivbrowser;

import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InternetUtils {

    //获取每日排行榜top50
    static ArrayList<ImageItem> getRankList(String html) {
        ArrayList<ImageItem> list = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("section[class = ranking-item]");

        for (Element element : elements) {
            String title = element.attr("data-title");
            String rank = "#" + element.attr("data-rank");
            String author = element.attr("data-user-name");
            String id = element.attr("data-id");
            String imgUrl = element.select("div[class = ranking-image-item]")
                    .select("a")
                    .select("div[class = _layout-thumbnail]")
                    .select("img")
                    .attr("data-src");
            String time = element.attr("data-date");
            ImageItem item = new ImageItem(title, rank, author, id, imgUrl, time);
            list.add(item);
        }
        return list;
    }

    /*
    获取图片详情（其实是获取原画的url）
    但事实上由于网页的js加载机制导致了无法加载”查看全部“里的内容
     */
    static List<GlideUrl> getDetail(String url) {
        String html = FetchHtml(url,"https://www.pixiv.net");
        List<GlideUrl> Urls = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("main")
                .select("section")
                .select("figure")
                .select("div[role = presentation]");
        for (Element element : elements) {
            String imgUrl = element.select("div[role = presentation]")
                    .select("a")
                    .attr("href");
            Urls.add(buildGlideUrl(imgUrl));
        }
        return Urls;
    }

    //获取当日日期
    static String getDate(String html) {
        Document document = Jsoup.parse(html);
        return document.select("ul[class = sibling-items]")
                .select("a[class = current]")
                .text()
                .substring(0, 10);
    }

    //获取排行榜网页的url
    static String FetchHtml(String url,String referer) {
        final String TAG = "OkHttp";
        String html = "";
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.1.4031 SLBChan/105")
                .addHeader("referer", referer)
                .addHeader("cookie", "first_visit_datetime_pc=2023-05-20+13%3A57%3A36; p_ab_id=5; p_ab_id_2=1; p_ab_d_id=937876618; yuid_b=I4Vwlwg; device_token=464c469f8af043b1129a09377b03cba2; privacy_policy_agreement=5; c_type=18; privacy_policy_notification=0; a_type=0; b_type=1; QSI_S_ZN_5hF4My7Ad6VNNAi=v:0:0; login_ever=yes; PHPSESSID=94391824_5dKbkKiESh1HYVmU1yPJqXZElJti9FrO; __cf_bm=GwsAyfpDr8BKBnM3O82GyMelZuYZ6gKg1vG9zTE1LuM-1684930131-0-Afj1kMGvjA+T9HDSvjyBg+E1DrvmtxCRQjTifSb2S0nrQyXO8rmZ19hEfBezRKB2HcWh85rkoGoE3hfCt4lS8JfS3oApFLiJErhPN55YJudyUTUCQ1YL8tgpyBdnLdcY5r2aZ7BKPpq0N0uh+ZGON9YLbBOKRvJtOjeOvd0YA+bw; tag_view_ranking=RTJMXD26Ak~_hSAdpN9rx~v-2ugL23bq~UEi1k2IOb1~JOjpTnwiQO~_pwIgrV8TB~jH0uD88V6F~nP0KNyaoTX~rb2zDqZhZE~HY55MqmzzQ~zyKU3Q5L4C~GmCzj7c06U~gpglyfLkWs~8buMDtT-ku~4gzX-RNalt~dJgU3VsQmO~HuzL7QqGKa~1NUKKinHVe~BtXd1-LPRH~bXMh6mBhl8~4kvsf-4PzW~aKhT3n4RHZ~TOd0tpUry5~KN7uxuR89w~RqSSaz6DfD~1U7FHFq2tf~KTl_8e4XX0~bkSTvfrPKL~0xsDLqCEW6~xha5FQn_XC~DXPphy6Z-6~TpDPYlVD87~2acjSVohem~4kEIVWXVvI~yjXuOIRb29~6GY4tS8gKL~pEMGOL4wN7~y3RUmuZ1U0~9aCtrIRNdF~p-x0Yv00r2~ZvexGYezEm~hRJnHmrQu0~Q7cVAy8vED~iOrY1beCdG")
                .url(url)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            html = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.d(TAG, "FetchHtml: " + "error");
        }

        return html;

    }

    //给glide加载图片时附上header
    static GlideUrl buildGlideUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            return new GlideUrl(url, new LazyHeaders.Builder().addHeader("Referer", "https://www.pixiv.net/ranking.php?mode=daily").build());
        }
    }

    //获取当前日期的前一天日期
    static String getPrevDay(String html) {
        Document document = Jsoup.parse(html);
        return document.select("li[class = after]")
                .select("a")
                .attr("href");
    }

    //获取当前日期的后一天日期
    static String getNextDay(String html) {
        Document document = Jsoup.parse(html);
        if (!document.select("li[class = before]").isEmpty()) {
            return document.select("li[class = before]")
                    .select("a")
                    .attr("href");
        } else {
            return "";
        }
    }
}
