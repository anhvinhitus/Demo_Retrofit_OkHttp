/*
package vn.com.vng.zalopay.data.cache;

import java.util.List;
import java.util.Map;

import vn.com.vng.zalopay.data.api.entity.VideoEntity;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;

*/
/**
 * Created by AnhHieu on 3/24/16.
 *//*

public class SqliteVideoScopeImpl implements SqliteVideoScope {

    private final ThreadExecutor threadExecutor;


    public SqliteVideoScopeImpl(ThreadExecutor executor) {
        this.threadExecutor = executor;
    }

    @Override
    public void write(List<VideoEntity> values) {
        //  execute(new CacheWriter(values, //grend));
    }

    @Override
    public void write(Map<Long, VideoEntity> values) {

    }

    @Override
    public void write(VideoEntity val) {

    }

    private void execute(Runnable runnable) {
        this.threadExecutor.execute(runnable);
    }


    private static class CacheWriter implements Runnable {
        private final List<VideoEntity> val;

        CacheWriter(List<VideoEntity> val) {
            this.val = val;
        }

        @Override
        public void run() {
            // write here


        }
    }

}
*/
