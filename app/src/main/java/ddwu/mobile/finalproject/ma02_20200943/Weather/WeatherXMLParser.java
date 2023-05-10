package ddwu.mobile.finalproject.ma02_20200943.Weather;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class WeatherXMLParser{
    private enum TagType { NONE, CATAGORY, FCSTDATE, FCSTTIME, FCSTVALUE };     // 해당없음, rank, movieNm, openDt, movieCd

    //    parsing 대상인 tag를 상수로 선언
    private final static String ITEM_TAG = "item";
    private final static String CATAGORY_TAG = "category";
    private final static String DATE_TAG = "fcstDate";
    private final static String TIME_TAG = "fcstTime";
    private final static String VALUE_TAG = "fcstValue";

    private XmlPullParser parser;

    public WeatherXMLParser() {
        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }


    public List<WeatherInfo> parse(String xml) {
        List<WeatherInfo> resultList = new ArrayList();
        WeatherInfo dbo = null;
        TagType tagType = TagType.NONE;     //  태그를 구분하기 위한 enum 변수 초기화

        try {
            parser.setInput(new StringReader(xml));// 파싱 대상 지정
            int eventType = parser.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String tag  = parser.getName();
                        if(tag.equals(ITEM_TAG))
                            dbo = new WeatherInfo();
                        else if(tag.equals(CATAGORY_TAG))
                            tagType = TagType.CATAGORY;
                        else if(tag.equals(DATE_TAG))
                            tagType = TagType.FCSTDATE;
                        else if(tag.equals(TIME_TAG))
                            tagType = TagType.FCSTTIME;
                        else if(tag.equals(VALUE_TAG))
                            tagType = TagType.FCSTVALUE;
                        else
                            tagType = TagType.NONE;
                        break;
                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals(ITEM_TAG)){
                            resultList.add(dbo);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        switch(tagType){
                            case CATAGORY:
                                dbo.setCategory(parser.getText());
                                break;
                            case FCSTDATE:
                                dbo.setFcstDate(parser.getText());
                                break;
                            case FCSTTIME:
                                dbo.setFcstTime(parser.getText());
                                break;
                            case FCSTVALUE:
                                dbo.setFcstValue(parser.getText());
                                break;
                        }
                        tagType = TagType.NONE;
                        break;

                }
                eventType = parser.next();
            }
        } catch (Exception e) {


        }

        return resultList;
    }
}

