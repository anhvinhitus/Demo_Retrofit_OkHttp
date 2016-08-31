package vn.com.zalopay.game.businnesslogic.base;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

/**
 * This class is used to manage all static instance
 * 
 */
public class AppGameSingletonLifeCircle
{
	private static Set<Class<?>> mClassHasStaticAttrList = null;

	public static synchronized void register(Class<?> pClassHasStaticAttr)
	{
		if (mClassHasStaticAttrList == null)
		{
			mClassHasStaticAttrList = new HashSet<Class<?>>();
		}

		mClassHasStaticAttrList.add(pClassHasStaticAttr);
	}

	/**
	 * Release all singleton instances immediately.
	 */
	public static synchronized void disposeAll()
	{
		if (mClassHasStaticAttrList != null)
		{

			for (Class<?> clazz : mClassHasStaticAttrList)
			{
				dispose(clazz);
			}

			mClassHasStaticAttrList = null;
		}
	}

	/**
	 * Release all available static instance in this class by assigning them to
	 * {@code null}.
	 * 
	 * This step will help Java garbage collector to detect and destroy cycles
	 * of objects that refer to each other, but are not referenced by any other
	 * active objects.
	 */
	private static void dispose(Class<?> clazz)
	{
		Timber.d("RELEASE_STATIC_OBJ ======== Considering to :" + clazz.getName());
		
		Field[] fields = clazz.getDeclaredFields();

		for (int i = 0; i < fields.length; i++)
		{

			if (Modifier.isStatic(fields[i].getModifiers()) && !Modifier.isFinal(fields[i].getModifiers()) && !fields[i].getType().isPrimitive())
			{
				try
				{

					if (Modifier.isPrivate(fields[i].getModifiers()))
					{
						fields[i].setAccessible(true);
					}

					fields[i].set(null, null);

					Timber.d("RELEASE_STATIC_OBJ **** Release " + fields[i].getName());

				} catch (Exception e)
				{
					Timber.e("RELEASE_STATIC_OBJ %s", e != null ? e.getMessage(): "error");
				}
			}
		}
	}
}
