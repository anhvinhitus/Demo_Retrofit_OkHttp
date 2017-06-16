package vn.com.zalopay.wallet.business.objectmanager;

import com.zalopay.ui.widget.dialog.DialogManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.api.DataRepository;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * This class is used to manage all static instance
 */
public class SingletonLifeCircleManager {
    private static Set<Class<?>> mClassHasStaticAttrList = null;

    /**
     * Register an instance implementing {@link SingletonBase} class in order to
     * manage and release all singleton instances when finished.
     */
    public static synchronized void register(Class<?> pClassHasStaticAttr) {
        if (mClassHasStaticAttrList == null) {
            mClassHasStaticAttrList = new HashSet<Class<?>>();
        }
        mClassHasStaticAttrList.add(pClassHasStaticAttr);
    }

    /**
     * Release all singleton instances immediately.
     */
    public static synchronized void disposeAll() {
        dispose(GlobalData.class);
        dispose(DialogManager.class);
        if (mClassHasStaticAttrList != null) {
            for (Class<?> clazz : mClassHasStaticAttrList) {
                dispose(clazz);
            }
            mClassHasStaticAttrList = null;
        }
    }

    public static synchronized void disposeMerchant() {
        dispose(CShareData.class);
    }

    public static synchronized void disposeDataRepository() {
        dispose(DataRepository.class);
    }

    /**
     * Release all available static instance in this class by assigning them to
     * {@code null}.
     * <p>
     * This step will help Java garbage collector to detect and destroy cycles
     * of objects that refer to each other, but are not referenced by any other
     * active objects.
     */
    private static void dispose(Class<?> clazz) {
        Log.i("RELEASE_STATIC_OBJ", "======== Considering to :" + clazz.getName());
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())
                    && !field.getType().isPrimitive()) {
                try {
                    if (Modifier.isPrivate(field.getModifiers())) {
                        field.setAccessible(true);
                    }
                    field.set(null, null);
                    Log.i("RELEASE_STATIC_OBJ", "**** Release " + field.getName());
                } catch (Exception e) {
                    Log.d("RELEASE_STATIC_OBJ", e);
                }
            }
        }
    }

    private static void disposeNoStaticObject(Class<?> clazz) {
        Log.i("RELEASE_OBJ", "======== Considering to :" + clazz.getName());
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isFinal(field.getModifiers()) && !field.getType().isPrimitive()) {
                try {
                    if (Modifier.isPrivate(field.getModifiers())) {
                        field.setAccessible(true);
                    }
                    field.set(null, null);
                    Log.i("RELEASE_OBJ", "**** Release " + field.getName());
                } catch (Exception e) {
                    Log.d("RELEASE_OBJ", e);
                }
            }
        }
    }
}
