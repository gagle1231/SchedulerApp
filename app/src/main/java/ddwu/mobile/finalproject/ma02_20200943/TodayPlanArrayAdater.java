package ddwu.mobile.finalproject.ma02_20200943;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TodayPlanArrayAdater extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<Plan> list;
    LayoutInflater inf;

    public TodayPlanArrayAdater(){

    }
    public TodayPlanArrayAdater(Context context, int layout, ArrayList<Plan> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
        inf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inf.inflate(layout, null);
        } // Layout.xml 자원을 대상으로 View 객체 생성

        // LayoutInflater 객체를 이용하여 Layout.xml 파일에서 id값을 가져오기
        TextView tvTitle = (TextView)convertView.findViewById(R.id.tvPlanTitle);
        TextView tvDate = (TextView)convertView.findViewById(R.id.tvplanDate);

        // 리스트에서 저장된 데이터를 순서대로 가져와서 DTO 클래스 타입 변수에 저장
        Plan dto = list.get(position); // position에 해당하는 레코드 가져오기
        tvTitle.setText(dto.getTitle());

        tvDate.setText(String.format("%02d:%02d", dto.getHourOfDay(), dto.getMinute()));

        return convertView;
    }
}