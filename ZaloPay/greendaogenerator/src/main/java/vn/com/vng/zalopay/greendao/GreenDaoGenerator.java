package vn.com.vng.zalopay.greendao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Index;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class GreenDaoGenerator {
    private static final int APP_DB_VERSION = 20;

    public static void main(String[] args) throws Exception {
        Schema appSchema = new Schema(APP_DB_VERSION, "vn.com.vng.zalopay.data.cache.model");

        //ADD TABLE
        addApplicationInfo(appSchema);
        addPaymentTransactionType(appSchema);
        addTransactionLog(appSchema);
        addDataManifest(appSchema);
        addCardList(appSchema);
        addZaloContact(appSchema);
        addTransferRecent(appSchema);
        addNotification(appSchema);
        addSentBundle(appSchema);
        addReceiveBundle(appSchema);

        new DaoGenerator().generateAll(appSchema, "./data/src/main/java");
    }

    private static void addSentBundle(Schema appSchema) {
        Entity packageEntity = appSchema.addEntity("SentPackageGD");
        Property propertyBundleId = packageEntity.addIdProperty().unique().notNull().getProperty();//bundleID
        packageEntity.addStringProperty("revZaloPayID");
        packageEntity.addLongProperty("revZaloID");
        packageEntity.addStringProperty("revFullName");
        packageEntity.addStringProperty("revAvatarURL");
        Property openTime = packageEntity.addLongProperty("openTime").getProperty();
        packageEntity.addLongProperty("amount");
        packageEntity.addStringProperty("sendMessage");
        packageEntity.addIntProperty("isLuckiest");

        Entity sentBundleEntity = appSchema.addEntity("SentBundleGD");
        sentBundleEntity.addIdProperty().unique().notNull();//bundleId
        sentBundleEntity.addIntProperty("type");
        sentBundleEntity.addLongProperty("createTime");
        sentBundleEntity.addLongProperty("lastOpenTime");
        sentBundleEntity.addIntProperty("totalLuck");
        sentBundleEntity.addIntProperty("numOfOpenedPakages");
        sentBundleEntity.addIntProperty("numOfPackages");
        ToMany sentBundleToPackage = sentBundleEntity.addToMany(packageEntity, propertyBundleId);
        sentBundleToPackage.setName("sentPackages");
        sentBundleToPackage.orderDesc(openTime);
    }

    private static void addReceiveBundle(Schema appSchema) {
        Entity revPackageEntity = appSchema.addEntity("ReceivePackageGD");
        Property revBundleId = revPackageEntity.addIdProperty().unique().notNull().getProperty();//BundleId
        revPackageEntity.addLongProperty("packageId");
        revPackageEntity.addStringProperty("sendZaloPayID");
        revPackageEntity.addStringProperty("sendFullName");
        revPackageEntity.addLongProperty("amount");
        Property openTime = revPackageEntity.addLongProperty("openedTime").getProperty();

        Entity receiveBundleEntity = appSchema.addEntity("ReceiveBundleGD");
        receiveBundleEntity.addIdProperty().unique().notNull();//Bundle
        receiveBundleEntity.addIntProperty("type");
        receiveBundleEntity.addLongProperty("createTime");
        receiveBundleEntity.addIntProperty("totalLuck");
        receiveBundleEntity.addIntProperty("numOfOpenedPakages");
        receiveBundleEntity.addIntProperty("numOfPackages");
        ToMany revBundleToPackage = receiveBundleEntity.addToMany(revPackageEntity, revBundleId);
        revBundleToPackage.setName("receivePackages");
        revBundleToPackage.orderDesc(openTime);
    }

    private static void addZaloContact(Schema appSchema) {
        Entity appInfoEntity = appSchema.addEntity("ZaloFriendGD");
        appInfoEntity.implementsInterface("vn.com.vng.zalopay.domain.model.IPersistentObject");
        appInfoEntity.addIdProperty();//zaloId
//        appInfoEntity.addLongProperty("userId").notNull().unique();
        appInfoEntity.addStringProperty("userName");
        appInfoEntity.addStringProperty("displayName");
        appInfoEntity.addStringProperty("avatar");
        appInfoEntity.addIntProperty("userGender");
        appInfoEntity.addStringProperty("birthday");
        appInfoEntity.addBooleanProperty("usingApp");
        appInfoEntity.addStringProperty("fulltextsearch");
    }

    private static void addTransferRecent(Schema appSchema) {
        Entity appInfoEntity = appSchema.addEntity("TransferRecent");
        appInfoEntity.addIdProperty();//zaloId
//        appInfoEntity.addLongProperty("userId").notNull().unique();
        appInfoEntity.addStringProperty("zaloPayId");
        appInfoEntity.addStringProperty("userName");
        appInfoEntity.addStringProperty("displayName");
        appInfoEntity.addStringProperty("avatar");
        appInfoEntity.addIntProperty("userGender");
        appInfoEntity.addStringProperty("birthday");
        appInfoEntity.addBooleanProperty("usingApp");
        appInfoEntity.addStringProperty("phoneNumber");
        appInfoEntity.addIntProperty("transferType");
        appInfoEntity.addLongProperty("amount");
        appInfoEntity.addStringProperty("message");
    }

    private static void addApplicationInfo(Schema schema) {
        Entity appInfoEntity = schema.addEntity("AppResourceGD");

        appInfoEntity.addIntProperty("appid").notNull().unique();
        appInfoEntity.addStringProperty("appname");
        appInfoEntity.addIntProperty("needdownloadrs");
        appInfoEntity.addStringProperty("imageurl");
        appInfoEntity.addStringProperty("jsurl");
        appInfoEntity.addIntProperty("status");
        appInfoEntity.addStringProperty("checksum");

        appInfoEntity.addIntProperty("stateDownload");
        appInfoEntity.addLongProperty("timeDownload");
        appInfoEntity.addIntProperty("numRetry");

    }

    private static void addPaymentTransactionType(Schema schema) {
        Entity appInfoEntity = schema.addEntity("PaymentTransTypeGD");

        Property transtype = appInfoEntity.addLongProperty("transtype").notNull().primaryKey().getProperty();
        Property pmcid = appInfoEntity.addLongProperty("pmcid").notNull().getProperty();

        appInfoEntity.addStringProperty("pmcname");
        appInfoEntity.addIntProperty("status");
        appInfoEntity.addLongProperty("minvalue");
        appInfoEntity.addLongProperty("maxvalue");
        appInfoEntity.addFloatProperty("feerate");

        appInfoEntity.addLongProperty("minfee");
        appInfoEntity.addStringProperty("feecaltype");

        Index uniqIndex = new Index();
        uniqIndex.addProperty(transtype);
        uniqIndex.addProperty(pmcid);
        uniqIndex.makeUnique();
        appInfoEntity.addIndex(uniqIndex);
    }


    private static void addTransactionLog(Schema schema) {
        Entity transHistoryData = schema.addEntity("TransactionLog");
        transHistoryData.addLongProperty("transid").notNull().unique().primaryKey();
        transHistoryData.addLongProperty("appid")
                .notNull()
        //.unique() //Todo: xem có cần thiết không
        ;

        transHistoryData.addStringProperty("userid");
        transHistoryData.addStringProperty("appuser");

        transHistoryData.addStringProperty("platform");
        transHistoryData.addStringProperty("description");
        transHistoryData.addIntProperty("pmcid");
        transHistoryData.addLongProperty("reqdate");
        transHistoryData.addIntProperty("userchargeamt");
        transHistoryData.addIntProperty("userfeeamt");
        transHistoryData.addIntProperty("amount");
        transHistoryData.addIntProperty("type");
        transHistoryData.addIntProperty("sign");
        transHistoryData.addStringProperty("username");
        transHistoryData.addStringProperty("appusername");
        transHistoryData.addIntProperty("statustype");
    }

    private static void addDataManifest(Schema schema) {
        Entity dataManifest = schema.addEntity("DataManifest");
        dataManifest.addStringProperty("key").notNull().unique().primaryKey();
        dataManifest.addStringProperty("value");
    }

    private static void addCardList(Schema schema) {
        Entity bankCard = schema.addEntity("BankCardGD");
        bankCard.addStringProperty("cardhash").primaryKey().notNull();
        bankCard.addStringProperty("cardname").notNull();
        bankCard.addStringProperty("first6cardno").notNull();
        bankCard.addStringProperty("last4cardno").notNull();
        bankCard.addStringProperty("bankcode").notNull();
    }


    private static void addNotification(Schema schema) {
        Entity notificationGD = schema.addEntity("NotificationGD");
        notificationGD.addIdProperty().primaryKey().autoincrement();
        notificationGD.addLongProperty("transid");
        notificationGD.addIntProperty("appid");
        notificationGD.addLongProperty("timestamp");
        notificationGD.addStringProperty("message");
        notificationGD.addStringProperty("userid");
        notificationGD.addStringProperty("destuserid");
        notificationGD.addBooleanProperty("read");
        
        notificationGD.addStringProperty("embeddata");
    }

}
