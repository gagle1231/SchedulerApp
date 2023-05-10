package ddwu.mobile.finalproject.ma02_20200943.Weather;

public class WeatherInfo {

    private String category;
    private String fcstDate;
    private String fcstTime;
    private String fcstValue;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFcstDate() {
        return fcstDate;
    }

    public void setFcstDate(String fcstDate) {
        this.fcstDate = fcstDate;
    }

    public String getFcstTime() {
        return fcstTime;
    }

    public void setFcstTime(String fcstTime) {
        this.fcstTime = fcstTime;
    }

    public String getFcstValue() {
        return fcstValue;
    }

    public void setFcstValue(String fcstValue) {
        this.fcstValue = fcstValue;
    }

    @Override
    public String toString() {
        return "WeatherInfo{" +
                "category='" + category + '\'' +
                ", fcstDate='" + fcstDate + '\'' +
                ", fcstTime='" + fcstTime + '\'' +
                ", fcstValue='" + fcstValue + '\'' +
                '}';
    }
}
