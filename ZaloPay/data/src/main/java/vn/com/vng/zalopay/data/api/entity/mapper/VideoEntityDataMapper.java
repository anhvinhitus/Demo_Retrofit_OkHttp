/*
package vn.com.vng.zalopay.data.api.entity.mapper;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.VideoEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.Video;

import static java.util.Collections.emptyList;

@Singleton
public class VideoEntityDataMapper {

    @Inject
    public VideoEntityDataMapper() {
        // use for Injection
    }

    public Video transform(VideoEntity videoEntity) {
        return transform(videoEntity, null);
    }

    public Video transform(VideoEntity videoEntity, String endpoint) {
        Video video = null;
        if (videoEntity != null) {
            video = new Video();

            if (!TextUtils.isEmpty(endpoint)) {
                video.endpoint = endpoint;
            }

            video.author = videoEntity.msc;
            video.singerName = videoEntity.sin;
            video.singerId = videoEntity.siid;
            video.download = videoEntity.d;
            video.fullName = videoEntity.fn;
            video.md5 = videoEntity.md5;
            video.lyric = videoEntity.ly;
            video.id = videoEntity.id;
            video.path = videoEntity.p;
            video.type = videoEntity.duet;

        }
        return video;
    }

    public List<Video> transform(Collection<VideoEntity> videoEntityCollection) {
        if (Lists.isEmptyOrNull(videoEntityCollection))
            return emptyList();

        List<Video> userList = new ArrayList<>(videoEntityCollection.size());
        for (VideoEntity userEntity : videoEntityCollection) {
            Video video = transform(userEntity);
            if (video != null) {
                userList.add(video);
            }
        }
        return userList;
    }
}
*/
