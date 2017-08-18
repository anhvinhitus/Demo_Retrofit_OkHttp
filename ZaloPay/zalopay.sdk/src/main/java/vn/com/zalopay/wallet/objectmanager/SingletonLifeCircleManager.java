package vn.com.zalopay.wallet.objectmanager;

import com.zalopay.ui.widget.dialog.DialogManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;
import vn.com.zalopay.wallet.api.ServiceManager;
import vn.com.zalopay.wallet.GlobalData;
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
            mClassHasStaticAttrList = new HashSet<>();
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
        dispose(ServiceManager.class);
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
        Timber.d("RELEASE_STATIC_OBJ Considering to : %s", clazz.getName());
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())
                    && !field.getType().isPrimitive()) {
                try {
                    if (Modifier.isPrivate(field.getModifiers())) {
                        field.setAccessible(true);
                    }
                    field.set(null, null);
                    Timber.d("RELEASE_STATIC_OBJ Release %s", field.getName());
                } catch (Exception e) {
                    Timber.d(e, "RELEASE_STATIC_OBJ");
                }
            }
        }
    }
}
