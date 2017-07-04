package vn.com.vng.zalopay.data.redpacket;

import android.support.annotation.Nullable;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.RedPacketStatusEntity;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGDDao;
import vn.com.vng.zalopay.data.notification.RedPacketStatus;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by longlv on 13/07/2016.
 * Implementation of RedPacketStore.LocalStorage
 */
public class RedPacketLocalStorage extends SqlBaseScopeImpl implements RedPacketStore.LocalStorage {

    public RedPacketLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public ReceivePackageGD getPacketStatus(long packetId) {
        Timber.d("query status for packet: %s", packetId);
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            Timber.d("Packet not found");
            return null;
        }

        Timber.d("query status for packet: %s, status: %s", packetId, packageGD.status);
        return packageGD;
    }

    @Override
    public Void setPacketStatus(long packetId, long amount, int status, String messageStatus) {
        Timber.d("set open status for packet: %s", packetId);
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            Timber.d("Packet not found");
            return null;
        }

        packageGD.status = (long) (status);
        packageGD.messageStatus = (messageStatus);
        packageGD.amount = (amount);
        packageGD.openedTime = (System.currentTimeMillis());
        getDaoSession().getReceivePackageGDDao().insertOrReplace(packageGD);
        Timber.d("Packet is set to be opened");
        return null;
    }

    @Override
    public Void addReceivedRedPacket(long packetId, long bundleId, String senderName, String senderAvatar, String message) {
        Timber.d("Add received red packet: [packetId: %s, bundleId: %s, sender: %s, avatar: %s, message: %s",
                packetId, bundleId, senderName, senderAvatar, message);
        ReceivePackageGD packageGD = getReceivePackageGD(packetId);
        if (packageGD == null) {
            packageGD = new ReceivePackageGD();
            packageGD.id = (packetId);
            packageGD.status = (long) (RedPacketStatus.CanOpen.getValue());
        }
        packageGD.bundleID = (bundleId);
        packageGD.senderFullName = (senderName);
        packageGD.senderAvatar = (senderAvatar);
        packageGD.message = (message);

        getDaoSession().getReceivePackageGDDao().insertOrReplace(packageGD);
        return null;
    }

    @Override
    public void updateListPackageStatus(@Nullable List<RedPacketStatusEntity> entities) {
        if (Lists.isEmptyOrNull(entities)) {
            return;
        }

        Timber.d("updateListPackageStatus: %s", entities.size());

        for (RedPacketStatusEntity entity : entities) {
            ReceivePackageGD receivePackageGD = getPacketStatus(entity.packageID);
            if (receivePackageGD == null) {
                continue;
            }
            receivePackageGD.status = entity.status;
            receivePackageGD.amount = entity.amount;
            getDaoSession().insertOrReplace(receivePackageGD);
        }
    }

    private ReceivePackageGD getReceivePackageGD(long packetId) {
        List<ReceivePackageGD> receivePackages = getDaoSession()
                .getReceivePackageGDDao()
                .queryBuilder()
                .where(ReceivePackageGDDao.Properties.Id.eq(packetId))
                .limit(1)
                .list();
        if (Lists.isEmptyOrNull(receivePackages)) {
            return null;
        } else {
            return receivePackages.get(0);
        }
    }
}
