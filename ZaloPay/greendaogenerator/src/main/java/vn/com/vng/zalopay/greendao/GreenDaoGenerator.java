package vn.com.vng.zalopay.greendao;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Index;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToMany;


public class GreenDaoGenerator {
    private static final int APP_DB_VERSION = 55;

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
        sentBundleSummary.addLongProperty("totalOfSentBundle");
        sentBundleSummary.addLongProperty("timeCreate");

        Entity receivePacketSummary = appSchema.addEntity("ReceivePacketSummaryDB");
        receivePacketSummary.setConstructors(false);
        receivePacketSummary.addIdProperty();
        receivePacketSummary.addLongProperty("totalOfRevamount");
        receivePacketSummary.addLongProperty("totalOfRevPackage");
        receivePacketSummary.addLongProperty("totalOfLuckiestDraw");
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
        packageEntity.addLongProperty("isLuckiest");

        Entity sentBundleEntity = appSchema.addEntity("SentBundleGD");
        sentBundleEntity.setConstructors(false);
        sentBundleEntity.addIdProperty().unique().notNull();//bundleId
        sentBundleEntity.addStringProperty("senderZaloPayID").notNull();//sendZaloPayID
        sentBundleEntity.addLongProperty("type");
        sentBundleEntity.addLongProperty("createTime");
        sentBundleEntity.addLongProperty("lastOpenTime");
        sentBundleEntity.addLongProperty("totalLuck");
        sentBundleEntity.addLongProperty("numOfOpenedPakages");
        sentBundleEntity.addLongProperty("numOfPackages");
        sentBundleEntity.addStringProperty("sendMessage");
        sentBundleEntity.addLongProperty("status");
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
        receivePackageGD.addLongProperty("status");
        receivePackageGD.addStringProperty("messageStatus");
        receivePackageGD.addStringProperty("message");
        receivePackageGD.addLongProperty("isLuckiest");
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
        appInfoGD.addLongProperty("maxPackageQuantity");
        appInfoGD.addLongProperty("maxCountHist");
        appInfoGD.addLongProperty("maxMessageLength");
        appInfoGD.addLongProperty("bundleExpiredTime");
        appInfoGD.addLongProperty("minDivideAmount");
        appInfoGD.addLongProperty("maxAmountPerPackage");
    }


    private static Entity addZaloProfile(Schema appSchema) {
        Entity zaloEntity = appSchema.addEntity("ZaloFriendGD");
        zaloEntity.setConstructors(false);
        zaloEntity.addStringProperty("userName");
        zaloEntity.addStringProperty("displayName");
        zaloEntity.addStringProperty("avatar");
        zaloEntity.addLongProperty("userGender");
        zaloEntity.addStringProperty("birthday");
        zaloEntity.addBooleanProperty("usingApp");
        zaloEntity.addStringProperty("fulltextsearch");
        return zaloEntity;
    }

    private static Entity addZaloPayProfile(Schema appSchema) {
        Entity zaloPayEntity = appSchema.addEntity("ZaloPayProfileGD");
        zaloPayEntity.setConstructors(false);
        zaloPayEntity.addStringProperty("zaloPayId").unique().notNull();
        zaloPayEntity.addLongProperty("status");
        zaloPayEntity.addStringProperty("zaloPayName");
        return zaloPayEntity;
    }

    private static Entity addContact(Schema appSchema) {
        Entity contactEntity = appSchema.addEntity("ContactGD");
        contactEntity.setConstructors(false);
        contactEntity.addStringProperty("fulltextsearch");
        contactEntity.addStringProperty("displayName");
        return contactEntity;
    }


    private static void addZaloContact(Schema appSchema) {
        Entity zaloEntity = addZaloProfile(appSchema);
        Entity zaloPayEntity = addZaloPayProfile(appSchema);
        Entity contactEntity = addContact(appSchema);

        Property zaloIdForZalopayProfile = zaloPayEntity.addLongProperty("zaloId").notNull()
                .primaryKey().getProperty();

        Property zaloIdForZaloProfile = zaloEntity.addLongProperty("zaloId").notNull()
                .dbName("_id").primaryKey().getProperty();

        zaloPayEntity.addToOne(zaloEntity, zaloIdForZalopayProfile, "zaloInfo");
        zaloEntity.addToOne(zaloPayEntity, zaloIdForZaloProfile, "zaloPayInfo");


        Property phoneNumberForZalopay = zaloPayEntity.addLongProperty("phoneNumber").getProperty();
        Property phoneNumberForContact = contactEntity.addLongProperty("phoneNumber")
                .notNull().primaryKey().getProperty();

        zaloPayEntity.addToOne(contactEntity, phoneNumberForZalopay, "contact");
        // contactEntity.addToOne(zaloPayEntity, phoneNumberForContact, "zaloPayInfo");

    }

    private static void addTransferRecent(Schema appSchema) {
        Entity entity = appSchema.addEntity("TransferRecent");
        entity.setConstructors(false);
        entity.addStringProperty("zaloPayId").primaryKey();
        entity.addStringProperty("zaloPayName");
        entity.addStringProperty("displayName");
        entity.addStringProperty("avatar");
        entity.addStringProperty("phoneNumber");
        entity.addLongProperty("transferType");
        entity.addLongProperty("amount");
        entity.addStringProperty("message");
        entity.addLongProperty("timeCreate");
    }

    private static void addApplicationInfo(Schema schema) {
        Entity entity = schema.addEntity("AppResourceGD");
        entity.setConstructors(false);

        entity.addLongProperty("appid").notNull().unique();
        entity.addStringProperty("appname");
        entity.addLongProperty("needdownloadrs");
        entity.addStringProperty("imageurl");
        entity.addStringProperty("jsurl");
        entity.addLongProperty("status");
        entity.addStringProperty("checksum");
        entity.addLongProperty("apptype");
        entity.addStringProperty("weburl");
        entity.addStringProperty("iconname");
        entity.addStringProperty("iconcolor");
        entity.addLongProperty("sortOrder");

        entity.addLongProperty("stateDownload");
        entity.addLongProperty("timeDownload");
        entity.addLongProperty("numRetry");

    }

    private static void addPaymentTransactionType(Schema schema) {
        Entity entity = schema.addEntity("PaymentTransTypeGD");
        entity.setConstructors(false);

        Property transtype = entity.addLongProperty("transtype").notNull().primaryKey().getProperty();
        Property pmcid = entity.addLongProperty("pmcid").notNull().getProperty();

        entity.addStringProperty("pmcname");
        entity.addLongProperty("status");
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
        addTransactionLog(schema, "TransactionLog");
        addTransactionLog(schema, "TransactionLogBackup");
    }

    private static void addTransactionLog(Schema schema, String entityName) {
        Entity entity = schema.addEntity(entityName);
        entity.setConstructors(false);
        entity.addLongProperty("transid").notNull().unique().primaryKey();
        entity.addLongProperty("appid").notNull();

        entity.addStringProperty("userid");
        entity.addStringProperty("appuser");

        entity.addStringProperty("platform");
        entity.addStringProperty("description");
        entity.addLongProperty("pmcid");
        entity.addLongProperty("reqdate");
        entity.addLongProperty("userchargeamt");
        entity.addLongProperty("userfeeamt");
        entity.addLongProperty("amount");
        entity.addLongProperty("type");
        entity.addLongProperty("sign");
        entity.addStringProperty("username");
        entity.addStringProperty("appusername");
        entity.addLongProperty("statustype");
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
        entity.addLongProperty("appid");
        entity.addLongProperty("timestamp");
        entity.addStringProperty("message");
        entity.addStringProperty("userid");
        entity.addStringProperty("destuserid");
        entity.addLongProperty("area");
        entity.addLongProperty("notificationstate");
        entity.addLongProperty("notificationtype");
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
        entity.addLongProperty("gender");
    }

}
