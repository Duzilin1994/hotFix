package com.du.hotfix;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 热修复工具类
 */
public class FixUtils {
    private static final String DEX_SUFFIX = ".dex";
    public static void fix(Context context){
//        用来存放补丁dex
        File dexDir = new File(Environment.getExternalStorageDirectory() , "hotFix");
        if(!dexDir.exists()){
            dexDir.mkdirs();
        }

//          获取到pathClassLoader
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
//        遍历hotFix文件夹
        for (File dexFile : dexDir.listFiles()){
//            如果不是*.dex文件直接跳过
            if(!dexFile.getName().endsWith(DEX_SUFFIX)){
               continue;
            }
//            创建DexClassLoader
            DexClassLoader dexClassLoader = new DexClassLoader(
                    dexFile.getAbsolutePath(),               //dex文件路径
                    context.getFilesDir().getAbsolutePath(), //存放dex的解压目录（用于jar、zip、apk格式的补丁）
                    null,                  // 加载dex时需要的库
                    pathClassLoader.getParent());            //父类加载器

            try {
//                反射获取到pathClassLoader的pathList即pathClassLoader的父类BaseDexClassLoader中的private final DexPathList pathList;
                Object pathClassLoader_DexPathList = getField(pathClassLoader , Class.forName("dalvik.system.BaseDexClassLoader") , "pathList");
//               反射获取到dexClassLoader的pathList即pathClassLoader的父类BaseDexClassLoader中的private final DexPathList pathList;
                Object dexClassLoader_DexPathList = getField(dexClassLoader , Class.forName("dalvik.system.BaseDexClassLoader") , "pathList");
//                反射获取到pathClassLoader中的DexPathList的dexElements
                Object pathClassLoader_DexPathList_DexElements = getField(pathClassLoader_DexPathList , pathClassLoader_DexPathList.getClass() , "dexElements");
//                反射获取到dexClassLoader中的DexPathList的dexElements
                Object dexClassLoader_DexPathList_DexElements = getField(dexClassLoader_DexPathList , dexClassLoader_DexPathList.getClass() , "dexElements");
//                合并两个dexElements
                Object newElements = mixElements(pathClassLoader_DexPathList_DexElements , dexClassLoader_DexPathList_DexElements);
//                重新获取pathClassLoader的pathList，重复使用之前的可能会报错
                Object pathClassLoader_DexPathList1 = getField(pathClassLoader , Class.forName("dalvik.system.BaseDexClassLoader") , "pathList");
//                将上面合并的dexDlements重新赋值给pathClassLoader的pathList的dexElements
                setField(pathClassLoader_DexPathList1 , pathClassLoader_DexPathList1.getClass() , "dexElements" , newElements);

                Toast.makeText(context, "修复完成", Toast.LENGTH_SHORT).show();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 反射获取属性值
     * @param object 需要获取值的对象
     * @param clazz  需要获取值所在的类
     * @param fieldName 属性名
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getField(Object object , Class clazz , String fieldName) throws NoSuchFieldException, IllegalAccessException {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
    }

    /**
     * 反射为对象的属性赋值
     * @param object 需要赋值的对象
     * @param clazz  需要赋值的属性所在的类
     * @param fieldName 属性名
     * @param value 属性值
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private static void setField(Object object , Class clazz ,String fieldName ,Object value) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object , value);
    }

    /**
     * 反射合并两个Element数组
     * @param element1
     * @param element2
     * @return
     */
    private static Object mixElements(Object element1 , Object element2){
        int len1 = Array.getLength(element1);
        int len2 = Array.getLength(element2);
        int newSize = len1 + len2;

        Class clazz = element1.getClass().getComponentType();
        Object result = Array.newInstance(clazz , newSize);
//        先添加补丁
        System.arraycopy(element2 , 0 , result , 0  , len2);
//        后添加原dex
        System.arraycopy(element1 , 0 , result , len2  , len1);
        return result;

    }


}
