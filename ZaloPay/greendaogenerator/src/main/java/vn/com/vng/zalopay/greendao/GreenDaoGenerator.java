package vn.com.vng.zalopay.greendao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoGenerator {
    private static final int APP_DB_VERSION = 1;


    public static void main(String[] args) throws Exception {
        Schema appSchema = new Schema(APP_DB_VERSION, "vn.com.vng.zalopay.data.cache.model");

        //ADD TABLE
        addApplicationInfo(appSchema);


        new DaoGenerator().generateAll(appSchema, "./data/src/main/java");
    }

    private static void addApplicationInfo(Schema schema) {
        Entity appInfoEntity = schema.addEntity("AppInfo");
        appInfoEntity.addStringProperty("app_id").notNull().unique().primaryKey();
        appInfoEntity.addStringProperty("app_name");
        appInfoEntity.addStringProperty("app_icon_url");
        appInfoEntity.addStringProperty("js_url");
        appInfoEntity.addStringProperty("resource_url");
        appInfoEntity.addStringProperty("base_url");
        appInfoEntity.addStringProperty("app_checksum");
        appInfoEntity.addIntProperty("status");
        appInfoEntity.addStringProperty("app_local_url");

    }

}
