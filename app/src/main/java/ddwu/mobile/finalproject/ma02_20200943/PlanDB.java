package ddwu.mobile.finalproject.ma02_20200943;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Plan.class}, version = 3)
public abstract class PlanDB extends RoomDatabase {
    public abstract PlanDao planDao();

    private static volatile PlanDB INSTANCE;
    //Singleton 패턴 적용
    static PlanDB getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (PlanDB.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PlanDB.class, "plan_db.db").build();
                }
            }
        }
        return INSTANCE;
    }
}
