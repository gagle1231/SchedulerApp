package ddwu.mobile.finalproject.ma02_20200943;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertPlans(Plan plans);

    @Delete
    Completable deletePlan(Plan plan);

    @Update
    Completable updatePlan(Plan plan);

    @Query("SELECT * FROM plans_table order by year, month, day")
    Flowable<List<Plan>> getAllPlans();

    @Query("SELECT * FROM plans_table WHERE id = :id")
    Single<Plan> getPlan(int id);

    @Query("SELECT * FROM plans_table WHERE  year = :year and month = :month and day = :day order by month, minute")
    Flowable<List<Plan>> getTodayPlans(int year, int month, int day );

    @Query("DELETE FROM plans_table WHERE id = :id")
    Completable deletePlanByID(int id);


}