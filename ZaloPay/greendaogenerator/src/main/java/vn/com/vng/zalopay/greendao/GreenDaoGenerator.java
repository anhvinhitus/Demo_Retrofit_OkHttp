package vn.com.vng.zalopay.greendao;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Index;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;


public class GreenDaoGenerator {
    private static final int APP_DB_VERSION = 59;
    private static final int GLOBAL_DB_VERSION = 2;

    /**
     * ./gradlew :greendaogenerator:run
     */

    public static void main(String[] args) throws Exception {
        Schema appSchema = new Schema(APP_DB_VERSION, "vn.com.vng.zalopay.data.cache.model");
        Schema globalSchema = new Schema(GLOBAL_DB_VERSION, "vn.com.vng.zalopay.data.cache.global");

        //ADD TABLE
        addApplicationInfo(appSchema);
        addTransactionLog(appSchema);
        addDataManifest(appSchema);
        addZaloContact(appSchema);
        addTransferRecent(appSchema);
        addNotification(appSchema);
        addRedPacket(appSchema);
        addMerchantUser(appSchema);

        //ADD TABLE GLOBAL
        addGlobalKeyValue(globalSchema);
        addApptransidLog(globalSchema);
        addApptransidLogTiming(globalSchema);

        DaoGenerator daoGenerator = new DaoGenerator("./daogenerator/src-template/");
        daoGenerator.generateAll(appSchema, "../zalopay.data/src/main/java");
        daoGenerator.generateAll(globalSchema, "../zalopay.data/src/main/java");
    }

    private static void addRedPacket(Schema appSchema) {
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
    }


    private static Entity addZaloProfile(Schema appSchema) {
        Entity zaloEntity = appSchema.addEntity("ZaloProfileGD");
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
        zaloPayEntity.addStringProperty("avatar");
        zaloPayEntity.addStringProperty("displayName");
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

        // zaloPayEntity.addToOne(contactEntity, phoneNumberForZalopay, "contact");
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

        entity.addLongProperty("downloadState");
        entity.addLongProperty("downloadTime");
        entity.addLongProperty("retryNumber");
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

    private static void addApptransidLog(Schema schema) {
        Entity entity = schema.addEntity("ApptransidLogGD");
        entity.setConstructors(false);
        entity.addStringProperty("apptransid").notNull().unique().primaryKey();
        entity.addLongProperty("appid");
        entity.addIntProperty("step");
        entity.addIntProperty("step_result");
        entity.addIntProperty("pcmid");
        entity.addIntProperty("transtype");
        entity.addLongProperty("transid");
        entity.addIntProperty("sdk_result");
        entity.addIntProperty("server_result");
        entity.addIntProperty("source");
        entity.addLongProperty("start_time");
        entity.addLongProperty("finish_time");
        entity.addStringProperty("bank_code");
        entity.addIntProperty("status");
    }

    private static void addApptransidLogTiming(Schema schema) {
        Entity entity = schema.addEntity("ApptransidLogTimingGD");
        entity.setConstructors(false);
        entity.addStringProperty("apptransid");
        entity.addIntProperty("step");
        entity.addLongProperty("timestamp").notNull().unique().primaryKey();
    }

    private static void addGlobalKeyValue(Schema schema) {
        Entity entity = schema.addEntity("KeyValueGD");
        entity.setConstructors(false);
        entity.addStringProperty("key").notNull().unique().primaryKey();
        entity.addStringProperty("value");
    }
}
