package qrcoba.w3engineers.com.qrcoba.helpers.model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Flowable;
import qrcoba.w3engineers.com.qrcoba.helpers.constant.TableNames;
import qrcoba.w3engineers.com.qrcoba.helpers.util.database.BaseDao;

@Dao

public interface CodeDao extends BaseDao<Code> {
    @Query("SELECT * FROM " + TableNames.CODES)
    Flowable<List<Code>> getAllFlowableCodes();

}
