package android.bignerdranch.pixivbrowser;

public class ImageItem {
    private String title;
    private String rank;
    private String author;
    private String id;
    private String time;
    private String imgUrl;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getRank() {
        return rank;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public ImageItem(String title, String rank, String author, String id, String imgUrl,String time) {
        this.title = title;
        this.rank = rank;
        this.author = author;
        this.id = id;
        this.imgUrl = imgUrl;
        this.time = time;
    }
}
