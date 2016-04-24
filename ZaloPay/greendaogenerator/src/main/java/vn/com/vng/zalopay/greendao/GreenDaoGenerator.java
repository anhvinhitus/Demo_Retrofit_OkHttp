package vn.com.vng.zalopay.greendao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoGenerator {
    private static final int APP_DB_VERSION = 1023;

    private static Schema appSchema;

    public static void main(String[] args) throws Exception {
        appSchema = new Schema(APP_DB_VERSION, "vn.com.vng.zalopay.db.model");
        genApplicationTable();
    }

    private static void genApplicationTable() throws Exception{
//        addFriendTable(appSchema);

        String path = "./app/src/main/java";
        new DaoGenerator().generateAll(appSchema, path);
    }

//    private static void addSuggestTransferTable(Schema schema) {
//        Entity suggestTransferMoney = schema.addEntity("SuggestTransferMoney");
//        suggestTransferMoney.addIdProperty();
//        suggestTransferMoney.addStringProperty("text").notNull();
//        suggestTransferMoney.addStringProperty("alias");
//        suggestTransferMoney.addIntProperty("frequency");
//        //0: auto generator; 1: individual
//        suggestTransferMoney.addIntProperty("type");
//        suggestTransferMoney.addDateProperty("updateTime");
//    }
//
//    private static void addRecentContactTransferTable(Schema schema) {
//        Entity entity = schema.addEntity("MoneyTransferRecentContact");
////        entity.addIdProperty();
//        entity.addStringProperty("phoneNumber").notNull().unique().primaryKey();
//        entity.addStringProperty("contactId");
//        entity.addStringProperty("displayName");
//        entity.addStringProperty("thumbnail");
//        entity.addIntProperty("frequency");
//        entity.addDateProperty("updateTime");
//    }

//    private static void addTelCardTable(Schema schema) {
//        Entity telCardEntity = schema.addEntity("TelCard");
//        telCardEntity.addIdProperty();
//        Property propertyCardCode = telCardEntity.addStringProperty("CardCode").getProperty();
//        Property propertySerialNo = telCardEntity.addStringProperty("SerialNo").getProperty();
//        telCardEntity.addStringProperty("CardTypeId");
//        telCardEntity.addLongProperty("CardValue");
//        telCardEntity.addStringProperty("ExpireDateFormat");
//        telCardEntity.addDateProperty("updateTime");
//
//        Index indexUnique = new Index();
//        indexUnique.addProperty(propertyCardCode);
//        indexUnique.addProperty(propertySerialNo);
//        indexUnique.makeUnique();
//        telCardEntity.addIndex(indexUnique);
//    }

//    private static void addFriendTable(Schema schema) {
//        Entity entity = schema.addEntity("Friend");
//        entity.addIdProperty();
//        entity.addStringProperty("rowId");
//        entity.addStringProperty("accountId").notNull();
//        entity.addStringProperty("groupId");
//        entity.addStringProperty("status");
//        entity.addStringProperty("createdDate");
//        entity.addStringProperty("modifiedDate");
//        entity.addStringProperty("userPhoneNumber").unique().notNull();
//        entity.addStringProperty("fullName");
//        entity.addStringProperty("email");
//        entity.addStringProperty("search");
//        entity.addIntProperty("serverOrder");
//        entity.addContentProvider();
//    }

}
