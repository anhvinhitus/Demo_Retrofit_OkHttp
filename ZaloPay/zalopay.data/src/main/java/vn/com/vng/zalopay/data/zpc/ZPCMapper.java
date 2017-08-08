package vn.com.vng.zalopay.data.zpc;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.FavoriteEntity;
import vn.com.vng.zalopay.data.api.entity.RedPacketUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;
import vn.com.vng.zalopay.data.api.entity.ZaloUserEntity;
import vn.com.vng.zalopay.data.cache.model.FavoriteZPC;
import vn.com.vng.zalopay.data.cache.model.UCB;
import vn.com.vng.zalopay.data.cache.model.ZFL;
import vn.com.vng.zalopay.data.cache.model.ZPC;
import vn.com.vng.zalopay.data.util.ConvertHelper;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zpc.contactloader.Contact;
import vn.com.vng.zalopay.data.zpc.contactloader.ContactPhone;

/**
 * Created by hieuvm on 7/20/17.
 * *
 */

final class ZPCMapper {
    ZPCMapper() {
    }

    List<UCB> transform(List<Contact> contacts) {
        if (Lists.isEmptyOrNull(contacts)) {
            return Collections.emptyList();
        }

        List<UCB> ret = new ArrayList<>();

        for (Contact contact : contacts) {

            if (contact == null || TextUtils.isEmpty(contact.name)) {
                continue;
            }

            List<UCB> item = transform(contact);
            if (Lists.isEmptyOrNull(item)) {
                continue;
            }

            ret.addAll(item);
        }

        return ret;
    }

    private List<UCB> transform(Contact contact) {
        if (Lists.isEmptyOrNull(contact.numbers)) {
            return Collections.emptyList();
        }

        List<UCB> ret = new ArrayList<>();

        for (ContactPhone number : contact.numbers) {
            String phoneNumber = PhoneUtil.formatPhoneNumber(number.number);
            if (TextUtils.isEmpty(phoneNumber)) {
                continue;
            }

            UCB ucb = new UCB();
            ucb.displayName = Strings.trim(contact.name);
            ucb.normalizeDisplayName = Strings.stripAccents(ucb.displayName);
            ucb.phoneNumber = phoneNumber;
            ucb.photoUri = contact.photoUri;
            ucb.firstName = contact.firstName;
            ucb.lastName = contact.lastName;
            ret.add(ucb);
        }

        return ret;
    }

    @Nullable
    ZPC transform(ZaloPayUserEntity entity) {
        if (entity == null) {
            return null;
        }

        long zaloId = ConvertHelper.parseLong(entity.zaloid, 0);
        long zalopayId = ConvertHelper.parseLong(entity.userid, 0);
        if (zaloId <= 0 || zalopayId <= 0) {
            Timber.d("Transform user error [zaloid:%s - zalopayid:%s]", entity.zaloid, entity.userid);
            return null;
        }

        if (TextUtils.isEmpty(entity.displayName)) {
            return null;
        }

        ZPC item = new ZPC();
        item.zaloId = zaloId;
        item.zalopayId = zalopayId;
        item.status = entity.status;

        long phone = 0;
        try {
            phone = Long.valueOf(entity.phonenumber);
        } catch (NumberFormatException ignore) {
        }

        String phoneNumber = PhoneUtil.formatPhoneNumber(phone);
        if (!TextUtils.isEmpty(phoneNumber)) {
            item.phoneNumber = phoneNumber;
        }

        if (!TextUtils.isEmpty(entity.zalopayname)) {
            item.zalopayName = entity.zalopayname;
        }

        item.displayName = entity.displayName;
        item.avatar = entity.avatar;
        item.normalizeDisplayName = Strings.stripAccents(item.displayName);
        return item;
    }

    @Nullable
    ZaloPayUserEntity transform(ZPC zpc) {

        if (zpc == null) {
            return null;
        }

        ZaloPayUserEntity entity = new ZaloPayUserEntity();
        entity.zaloid = String.valueOf(zpc.zaloId);
        entity.userid = String.valueOf(zpc.zalopayId);
        entity.zalopayname = zpc.zalopayName;
        entity.phonenumber = zpc.phoneNumber;
        entity.avatar = zpc.avatar;
        entity.displayName = zpc.displayName;
        entity.status = zpc.status;
        return entity;
    }

    @Nullable
    ZFL transform(ZaloUserEntity entity) {
        if (entity == null || entity.userId <= 0) {
            return null;
        }

        if (TextUtils.isEmpty(entity.displayName)) {
            return null;
        }

        ZFL ret = new ZFL();
        ret.zaloId = entity.userId;
        ret.avatar = entity.avatar;
        ret.displayName = entity.displayName;
        ret.usingApp = entity.usingApp;
        ret.userName = entity.userName;
        ret.normalizeDisplayName = Strings.stripAccents(ret.displayName);
        return ret;
    }

    @Nullable
    RedPacketUserEntity transformToRedPacket(ZPC zpc) {
        if (zpc == null) {
            return null;
        }

        RedPacketUserEntity entity = new RedPacketUserEntity();
        entity.zaloPayID = String.valueOf(zpc.zalopayId);
        entity.zaloID = String.valueOf(zpc.zaloId);
        entity.zaloName = zpc.displayName;
        entity.avatar = zpc.avatar;
        return entity;
    }

    void replaceNameZFLToNameZPC(List<ZPC> listZPC, List<ZFL> listZFL) {
        if (Lists.isEmptyOrNull(listZPC) || Lists.isEmptyOrNull(listZFL)) {
            return;
        }

        for (ZPC zpc : listZPC) {
            replaceNameZFLToNameZPC(zpc, listZFL);
        }
    }

    private void replaceNameZFLToNameZPC(ZPC zpc, List<ZFL> listZFL) {
        if (zpc == null) {
            return;
        }

        long zaloid = zpc.zaloId;

        for (ZFL zfl : listZFL) {
            if (zaloid != zfl.zaloId) {
                continue;
            }

            if (!TextUtils.isEmpty(zfl.displayName)) {
                zpc.displayName = zfl.displayName;
            }

            return;
        }
    }

    @Nullable
    Contact transform(UCB ucb) {
        if (ucb == null) {
            return null;
        }
        Contact entity = new Contact("", ucb.displayName, ucb.photoUri, ucb.firstName, ucb.lastName);
        entity.addNumber(ucb.phoneNumber, "");
        return entity;
    }

    @Nullable
    ZaloUserEntity transform(ZFL zfl) {
        if (zfl == null) {
            return null;
        }

        ZaloUserEntity entity = new ZaloUserEntity(zfl.zaloId);
        entity.displayName = zfl.displayName;
        entity.avatar = zfl.avatar;
        entity.usingApp = zfl.usingApp;
        entity.userName = zfl.userName;
        return entity;
    }

    @Nullable
    FavoriteZPC create(String phone, long zaloId) {
        if (TextUtils.isEmpty(phone) && zaloId <= 0) {
            return null;
        }

        FavoriteZPC favorite = new FavoriteZPC();
        favorite.phoneNumber = phone;
        favorite.createTime = System.currentTimeMillis();
        favorite.zaloId = zaloId;

        return favorite;
    }

    @Nullable
    FavoriteEntity transform(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        FavoriteEntity profile = new FavoriteEntity();
        profile.zaloId = cursor.getLong(cursor.getColumnIndex(ZPCAlias.ColumnAlias.ZALO_ID));
        profile.displayName = cursor.getString(cursor.getColumnIndex(ZPCAlias.ColumnAlias.DISPLAY_NAME));
        profile.avatar = cursor.getString(cursor.getColumnIndex(ZPCAlias.ColumnAlias.AVATAR));
        profile.phoneNumber = cursor.getString(cursor.getColumnIndex(ZPCAlias.ColumnAlias.PHONE_NUMBER));
        return profile;
    }

}
