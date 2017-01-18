package vn.com.vng.zalopay.data.api.entity.mapper;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.cache.model.ContactGD;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.data.cache.model.ZaloPayProfileGD;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.contactloader.Contact;
import vn.com.vng.zalopay.data.zfriend.contactloader.ContactPhone;

/**
 * Created by hieuvm on 1/1/17.
 */

@Singleton
public class FriendEntityDataMapper {

    @Inject
    public FriendEntityDataMapper() {
    }

    public ZaloFriendGD transform(ZaloUserEntity entity) {
        if (entity == null || entity.userId <= 0) {
            return null;
        }

        ZaloFriendGD item = new ZaloFriendGD();
        item.zaloId = entity.userId;
        item.userName = (entity.userName);
        item.avatar = (entity.avatar);
        item.usingApp = (entity.usingApp);
        item.displayName = Strings.trim(entity.displayName);
        item.fulltextsearch = Strings.stripAccents(item.displayName);
        return item;
    }

    public ZaloUserEntity transform(ZaloFriendGD zaloFriendGD) {
        if (zaloFriendGD == null) {
            return null;
        }

        ZaloUserEntity entity = new ZaloUserEntity(zaloFriendGD.zaloId);
        entity.avatar = zaloFriendGD.avatar;
        entity.userName = zaloFriendGD.userName;
        entity.displayName = zaloFriendGD.displayName;
        entity.usingApp = zaloFriendGD.usingApp;
        entity.normalizeDisplayName = zaloFriendGD.fulltextsearch;
        return entity;
    }


    public ZaloPayProfileGD transform(ZaloPayUserEntity entity) {
        if (entity == null || TextUtils.isEmpty(entity.userid) || TextUtils.isEmpty(entity.zaloid)) {
            return null;
        }

        ZaloPayProfileGD item = new ZaloPayProfileGD();

        try {
            item.zaloId = Long.parseLong(entity.zaloid);
        } catch (Exception ex) {
            Timber.e(ex, "transform user fail");
            return null;
        }

        item.zaloPayId = entity.userid;
        item.status = entity.status;
        try {
            String phone = PhoneUtil.formatPhoneNumber(entity.phonenumber);
            if (!TextUtils.isEmpty(phone)) {
                item.phoneNumber = Long.valueOf(phone);
            }
        } catch (Exception e) {
            Timber.d(e, "transform");
        }

        item.zaloPayName = entity.zalopayname;
        return item;
    }

    public ZaloPayUserEntity transform(ZaloPayProfileGD gd) {
        if (gd == null) {
            return null;
        }

        ZaloPayUserEntity entity = new ZaloPayUserEntity();
        entity.userid = gd.zaloPayId;
        entity.zaloid = String.valueOf(gd.zaloId);
        entity.zalopayname = gd.zaloPayName;
        entity.phonenumber = gd.phoneNumber;
        entity.status = gd.status;
        return entity;
    }

    public RedPacketUserEntity transformRedPacketEntity(ZaloFriendGD zaloFriendGD) {
        if (zaloFriendGD == null) {
            return null;
        }
        ZaloPayProfileGD zaloPayInfo = zaloFriendGD.getZaloPayInfo();

        if (zaloPayInfo == null || TextUtils.isEmpty(zaloPayInfo.zaloPayId)) {
            Timber.d(new NullPointerException(), "Transform red packet null");
            return null;
        }

        RedPacketUserEntity entity = new RedPacketUserEntity();
        entity.avatar = zaloFriendGD.avatar;
        entity.zaloID = String.valueOf(zaloFriendGD.zaloId);
        entity.zaloName = zaloFriendGD.displayName;
        entity.zaloPayID = zaloPayInfo.zaloPayId;

        return entity;
    }

    public List<ContactGD> transform(Contact contact) {
        if (contact == null || TextUtils.isEmpty(contact.name) || Lists.isEmptyOrNull(contact.numbers)) {
            return Collections.emptyList();
        }
        List<ContactGD> ret = new ArrayList<>();

        for (ContactPhone number : contact.numbers) {
            long phoneNumber = convert(number.number);
            if (phoneNumber <= 0) {
                continue;
            }

            ContactGD _contact = new ContactGD();
            _contact.displayName = Strings.trim(contact.name);
            _contact.phoneNumber = phoneNumber;
            _contact.fulltextsearch = Strings.stripAccents(_contact.displayName);
            ret.add(_contact);
        }

        return ret;
    }

    private long convert(String number) {
        long phoneNumber = 0;

        try {
            phoneNumber = Long.parseLong(number);
        } catch (NumberFormatException ignore) {
        }

        return phoneNumber;
    }

    public List<ZaloUserEntity> transformZaloUserEntity(List<ZaloFriendGD> gd) {
        return Lists.transform(gd, this::transform);
    }

    public List<ZaloFriendGD> transformZaloUser(List<ZaloUserEntity> entities) {
        return Lists.transform(entities, this::transform);
    }

    public List<ZaloPayProfileGD> transformZaloPayUser(List<ZaloPayUserEntity> entities) {
        return Lists.transform(entities, this::transform);
    }

    public List<ZaloPayUserEntity> transformZaloPayUserEntity(List<ZaloPayProfileGD> gd) {
        return Lists.transform(gd, this::transform);
    }

    public List<RedPacketUserEntity> transformRedPacketEntity(List<ZaloFriendGD> gd) {
        return Lists.transform(gd, this::transformRedPacketEntity);
    }

    public List<ContactGD> transformContact(List<Contact> contacts) {
        if (Lists.isEmptyOrNull(contacts)) {
            return Collections.emptyList();
        }

        List<ContactGD> ret = new ArrayList<>();

        for (Contact contact : contacts) {
            List<ContactGD> item = transform(contact);
            if (Lists.isEmptyOrNull(item)) {
                continue;
            }

            ret.addAll(item);
        }

        return ret;
    }


}
