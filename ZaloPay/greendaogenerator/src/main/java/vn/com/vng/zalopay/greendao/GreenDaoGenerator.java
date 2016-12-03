package vn.com.vng.zalopay.greendao;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Index;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToMany;


public class GreenDaoGenerator {
    private static final int APP_DB_VERSION = 49;

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
        addRedPacket(appSchema);
        addMerchantUser(appSchema);

        new DaoGenerator("./daogenerator/src-template/").generateAll(appSchema, "./data/src/main/java");
    }

    private static void addRedPacket(Schema appSchema) {
        Entity sentBundleSummary = appSchema.addEntity("SentBundleSummaryDB");
        sentBundleSummary.setConstructors(false);
        sentBundleSummary.addIdProperty();
        sentBundleSummary.addLongProperty("totalOfSentAmount");
        sentBundleSummary.addIntProperty("totalOfSentBundle");
        sentBundleSummary.addLongProperty("timeCreate");

        Entity receivePacketSummary = appSchema.addEntity("ReceivePacketSummaryDB");
        receivePacketSummary.setConstructors(false);
        receivePacketSummary.addIdProperty();
        receivePacketSummary.addLongProperty("totalOfRevamount");
        receivePacketSummary.addIntProperty("totalOfRevPackage");
        receivePacketSummary.addIntProperty("totalOfLuckiestDraw");
        receivePacketSummary.addLongProperty("timeCreate");

        //Cache all bundle (contain sent & received)
        Entity bundleEntity = appSchema.addEntity("BundleGD");
        bundleEntity.setConstructors(false);
        bundleEntity.addIdProperty().unique().notNull();//bundleId
        bundleEntity.addLongProperty("createTime"); //Time when bundle has been created
        bundleEntity.addLongProperty("lastTimeGetPackage"); //Last time that get package in bundle from server.

        Entity packageEntity = appSchema.addEntity("PackageInBundleGD");
        packageEntity.setConstructors(false);
        packageEntity.addIdProperty(); //packageId
        Property propertyBundleId = packageEntity.addLongProperty("bundleID").getProperty();
        packageEntity.addStringProperty("revZaloPayID");
        packageEntity.addLongProperty("revZaloID");
        packageEntity.addStringProperty("revFullName");
        packageEntity.addStringProperty("revAvatarURL");
        Property openTime = packageEntity.addLongProperty("openTime").getProperty();
        packageEntity.addLongProperty("amount");
        packageEntity.addStringProperty("sendMessage");
        packageEntity.addIntProperty("isLuckiest");

        Entity sentBundleEntity = appSchema.addEntity("SentBundleGD");
        sentBundleEntity.setConstructors(false);
        sentBundleEntity.addIdProperty().unique().notNull();//bundleId
        sentBundleEntity.addStringProperty("senderZaloPayID").notNull();//sendZaloPayID
        sentBundleEntity.addIntProperty("type");
        sentBundleEntity.addLongProperty("createTime");
        sentBundleEntity.addLongProperty("lastOpenTime");
        sentBundleEntity.addIntProperty("totalLuck");
        sentBundleEntity.addIntProperty("numOfOpenedPakages");
        sentBundleEntity.addIntProperty("numOfPackages");
        sentBundleEntity.addStringProperty("sendMessage");
        sentBundleEntity.addIntProperty("status");
        ToMany sentBundleToPackage = sentBundleEntity.addToMany(packageEntity, propertyBundleId);
        sentBundleToPackage.setName("sentPackages");
        sentBundleToPackage.orderDesc(openTime);

        Entity receivePackageGD = appSchema.addEntity("ReceivePackageGD");
        receivePackageGD.setConstructors(false);
        receivePackageGD.addIdProperty().unique().notNull();//packageID
        receivePackageGD.addLongProperty("bundleID");
        receivePackageGD.addStringProperty("receiverZaloPayID");
        receivePackageGD.addStringProperty("senderZaloPayID");
        receivePackageGD.addStringProperty("senderFullName");
        receivePackageGD.addStringProperty("senderAvatar");
        receivePackageGD.addLongProperty("amount");
        receivePackageGD.addLongProperty("openedTime");
        receivePackageGD.addIntProperty("status");
        receivePackageGD.addStringProperty("messageStatus");
        receivePackageGD.addStringProperty("message");
        receivePackageGD.addIntProperty("isLuckiest");
        receivePackageGD.addLongProperty("createTime");
        ToMany revBundleToPackage = receivePackageGD.addToMany(packageEntity, propertyBundleId);
        revBundleToPackage.setName("receivePackages");
        revBundleToPackage.orderDesc(openTime);

        Entity appInfoGD = appSchema.addEntity("RedPacketAppInfoGD");
        appInfoGD.setConstructors(false);
        appInfoGD.addIdProperty().autoincrement();
        appInfoGD.addStringProperty("checksum");
        appInfoGD.addLongProperty("expiredTime");
        appInfoGD.addLongProperty("minAmountEach");
        appInfoGD.addLongProperty("maxTotalAmountPerBundle");
        appInfoGD.addIntProperty("maxPackageQuantity");
        appInfoGD.addIntProperty("maxCountHist");
        appInfoGD.addIntProperty("maxMessageLength");
        appInfoGD.addLongProperty("bundleExpiredTime");
        appInfoGD.addLongProperty("minDivideAmount");
        appInfoGD.addLongProperty("maxAmountPerPackage");
    }

    private static void addZaloContact(Schema appSchema) {

        Entity entity = appSchema.addEntity("ZaloFriendGD");
        entity.setConstructors(false);

        entity.addLongProperty("zaloId").notNull()
                .dbName("_id").primaryKey();
        
        entity.addStringProperty("userName");
        entity.addStringProperty("displayName");
        entity.addStringProperty("avatar");
        entity.addIntProperty("userGender");
        entity.addStringProperty("birthday");
        entity.addBooleanProperty("usingApp");
        entity.addStringProperty("fulltextsearch");

        //merge from zalopay system
        entity.addStringProperty("zaloPayId");
        entity.addIntProperty("status");
        entity.addLongProperty("phoneNumber");
        entity.addStringProperty("zaloPayName");
    }

    private static void addTransferRecent(Schema appSchema) {
        Entity entity = appSchema.addEntity("TransferRecent");
        entity.setConstructors(false);
        entity.addStringProperty("zaloPayId").primaryKey();
        entity.addStringProperty("zaloPayName");
        entity.addStringProperty("displayName");
        entity.addStringProperty("avatar");
        entity.addStringProperty("phoneNumber");
        entity.addIntProperty("transferType");
        entity.addLongProperty("amount");
        entity.addStringProperty("message");
        entity.addLongProperty("timeCreate");
    }

    private static void addApplicationInfo(Schema schema) {
        Entity entity = schema.addEntity("AppResourceGD");
        entity.setConstructors(false);

        entity.addIntProperty("appid").notNull().unique();
        entity.addStringProperty("appname");
        entity.addIntProperty("needdownloadrs");
        entity.addStringProperty("imageurl");
        entity.addStringProperty("jsurl");
        entity.addIntProperty("status");
        entity.addStringProperty("checksum");
        entity.addIntProperty("apptype");
        entity.addStringProperty("weburl");
        entity.addStringProperty("iconurl");
        entity.addIntProperty("sortOrder");

        entity.addIntProperty("stateDownload");
        entity.addLongProperty("timeDownload");
        entity.addIntProperty("numRetry");

    }

    private static void addPaymentTransactionType(Schema schema) {
        Entity entity = schema.addEntity("PaymentTransTypeGD");
        entity.setConstructors(false);

        Property transtype = entity.addLongProperty("transtype").notNull().primaryKey().getProperty();
        Property pmcid = entity.addLongProperty("pmcid").notNull().getProperty();

        entity.addStringProperty("pmcname");
        entity.addIntProperty("status");
        entity.addLongProperty("minvalue");
        entity.addLongProperty("maxvalue");
        entity.addFloatProperty("feerate");

        entity.addLongProperty("minfee");
        entity.addStringProperty("feecaltype");

        Index index = new Index();
        index.addProperty(transtype);
        index.addProperty(pmcid);
        index.makeUnique();
        entity.addIndex(index);
    }


    private static void addTransactionLog(Schema schema) {
        Entity entity = schema.addEntity("TransactionLog");
        entity.setConstructors(false);
        entity.addLongProperty("transid").notNull().unique().primaryKey();
        entity.addLongProperty("appid").notNull();

        entity.addStringProperty("userid");
        entity.addStringProperty("appuser");

        entity.addStringProperty("platform");
        entity.addStringProperty("description");
        entity.addIntProperty("pmcid");
        entity.addLongProperty("reqdate");
        entity.addIntProperty("userchargeamt");
        entity.addIntProperty("userfeeamt");
        entity.addIntProperty("amount");
        entity.addIntProperty("type");
        entity.addIntProperty("sign");
        entity.addStringProperty("username");
        entity.addStringProperty("appusername");
        entity.addIntProperty("statustype");
    }

    private static void addDataManifest(Schema schema) {
        Entity entity = schema.addEntity("DataManifest");
        entity.setConstructors(false);
        entity.addStringProperty("key").notNull().unique().primaryKey();
        entity.addStringProperty("value");
    }

    private static void addCardList(Schema schema) {
        Entity bankCard = schema.addEntity("BankCardGD");
        bankCard.setConstructors(false);
        bankCard.addStringProperty("cardhash").primaryKey().notNull();
        bankCard.addStringProperty("cardname").notNull();
        bankCard.addStringProperty("first6cardno").notNull();
        bankCard.addStringProperty("last4cardno").notNull();
        bankCard.addStringProperty("bankcode").notNull();
    }


    private static void addNotification(Schema schema) {
        Entity entity = schema.addEntity("NotificationGD");
        entity.setConstructors(false);
        entity.addIdProperty().primaryKey().autoincrement();
        entity.addLongProperty("transid");
        entity.addIntProperty("appid");
        entity.addLongProperty("timestamp");
        entity.addStringProperty("message");
        entity.addStringProperty("userid");
        entity.addStringProperty("destuserid");
        entity.addIntProperty("area");
        entity.addIntProperty("notificationstate");
        entity.addIntProperty("notificationtype");
        Property mtaid = entity.addLongProperty("mtaid").getProperty();
        Property mtuid = entity.addLongProperty("mtuid").getProperty();
        entity.addStringProperty("embeddata");

        Index index = new Index();
        index.addProperty(mtaid);
        index.addProperty(mtuid);
        index.makeUnique();

        entity.addIndex(index);
    }

    private static void addMerchantUser(Schema schema) {
        Entity entity = schema.addEntity("MerchantUser");
        entity.setConstructors(false);
        entity.addLongProperty("appid").primaryKey().notNull();
        entity.addStringProperty("mUid");
        entity.addStringProperty("mAccessToken");
        entity.addStringProperty("displayName");
        entity.addStringProperty("avatar");
        entity.addStringProperty("birthday");
        entity.addIntProperty("gender");
    }

}
