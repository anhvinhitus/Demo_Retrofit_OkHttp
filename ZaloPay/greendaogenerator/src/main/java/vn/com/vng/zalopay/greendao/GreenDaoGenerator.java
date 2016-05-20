package vn.com.vng.zalopay.greendao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Index;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoGenerator {
    private static final int APP_DB_VERSION = 6;


    public static void main(String[] args) throws Exception {
        Schema appSchema = new Schema(APP_DB_VERSION, "vn.com.vng.zalopay.data.cache.model");

        //ADD TABLE
        addApplicationInfo(appSchema);
        addPaymentTransactionType(appSchema);
        addTransactionLog(appSchema);
        addDataManifest(appSchema);
        addCardList(appSchema);

        new DaoGenerator().generateAll(appSchema, "./data/src/main/java");
    }

    private static void addApplicationInfo(Schema schema) {
        Entity appInfoEntity = schema.addEntity("AppResourceGD");

        appInfoEntity.addLongProperty("appid").notNull().unique().primaryKey();
        appInfoEntity.addStringProperty("appname");
        appInfoEntity.addIntProperty("needdownloadrs");
        appInfoEntity.addStringProperty("imageurl");
        appInfoEntity.addStringProperty("jsurl");
        appInfoEntity.addIntProperty("status");
        appInfoEntity.addStringProperty("checksum");

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


}
