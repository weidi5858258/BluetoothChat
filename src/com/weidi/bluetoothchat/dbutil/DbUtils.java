package com.weidi.bluetoothchat.dbutil;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.weidi.bluetoothchat.Constant;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * 在java bean中必须要有一个_id的整形
 * <p>
 * Created by root on 16-7-30.
 * <p>
 * 如何使用:
 * <p>
 * 一般建议把"_id"设置成主键
 *
 * @DbVersion(version = 0)
 * public class Man {}
 * @Primary private int _id;
 * <p>
 * // 修改成功后在下个版本中删除掉
 * // "name"是之前的属性名称,现在改成"sakura"
 * @OriginalField(value = "name")
 * private String sakura;
 * <p>
 * 开启线程操作
 * DbUtils.getInstance()
 * .createOrUpdateDBWithVersion(getApplicationContext(), new Class[]{Man.class});
 */

public class DbUtils {

    private static final String VERSION = "Version";
    private volatile static DbUtils mDbUtils;
    private MySQLiteOpenHelper helper;
    private IOperDBOver mIOperDBOver;

    private static final int SINGLECREATESUCCESS = 0;
    private static final int SINGLECREATEFAIL = 2;
    private static final int SINGLEUPDATESUCCESS = 4;
    private static final int SINGLEUPDATESFAIL = 8;
    private static final int ALLCREATESUCCESS = 16;
    private static final int ALLCREATEFAIL = 32;
    private static final int ALLUPDATESUCCESS = 64;
    private static final int ALLUPDATESFAIL = 128;

    public interface IOperDBOver {
        void onResult(int result);
    }

    private DbUtils() {
    }

    public static DbUtils getInstance() {
        if (mDbUtils == null) {
            synchronized (DbUtils.class) {
                if (mDbUtils == null) {
                    mDbUtils = new DbUtils();
                }
            }
        }
        return mDbUtils;
    }

    public MySQLiteOpenHelper getHelper(Context context) {
        if (helper == null) {
            helper = new MySQLiteOpenHelper(context);
        }
        return helper;
    }

    public void setIOperDBOver(IOperDBOver oper) {
        mIOperDBOver = oper;
    }

    /**
     * 得到java bean类，然后得到注解的值
     *
     * @param context
     */
    public void createOrUpdateDBWithVersion(Context context, Class<?>[] cls) {
        try {
            String path = "/data/data/" + context.getPackageName() + "/databases";
            File file = new File(path, Constant.DB_NAME);
            if (file == null) {
                //
                return;
            }

            if (cls == null || cls.length == 0) {

                return;
            }

            int clsLength = cls.length;
            SharedPreferences sp = context.getSharedPreferences(
                    Constant.SHAREDPREFERENCES,
                    Context.MODE_PRIVATE);
            if (file.exists()) {
                for (int i = 0; i < clsLength; i++) {
                    Class clazz = cls[i];
                    if (clazz == null) {
                        continue;
                    }
                    DbVersion annotation = (DbVersion) clazz.getAnnotation(DbVersion.class);
                    if (annotation == null) {
                        //
                        return;
                    }
                    String object = clazz.getSimpleName();
                    String sql = "SELECT COUNT(*) FROM sqlite_master " +
                            "WHERE type='table' and name ='" + object.trim() + "' ";
                    Cursor cursor = getHelper(context).getReadableDb().rawQuery(sql, null);
                    boolean exists = false;
                    if (cursor != null && cursor.moveToNext()) {
                        int count = cursor.getInt(0);
                        if (count > 0) {
                            exists = true;
                        }
                    }
                    cursor.close();
                    cursor = null;
                    int version_new = annotation.version();
                    int version_old = sp.getInt(object + VERSION, 0);
                    if (exists) {
                        //
                        if (version_new != version_old) {
                            updateDB(context, clazz, sp);
                            saveJavaBeanVersion(sp, object, version_new);
                        }
                    } else {
                        //
                        createDB(context, clazz, sp);
                        saveJavaBeanVersion(sp, object, version_new);
                    }
                }
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(ALLCREATESUCCESS);
                    mIOperDBOver.onResult(ALLUPDATESUCCESS);
                }
                return;
            }

            System.out.println("new MySQLiteOpenHelper(context)");
            helper = new MySQLiteOpenHelper(context);
            helper.getWritableDb();

            //
            for (int i = 0; i < clsLength; i++) {
                Class clazz = cls[i];
                if (clazz == null) {
                    continue;
                }
                DbVersion annotation = (DbVersion) clazz.getAnnotation(DbVersion.class);
                if (annotation == null) {
                    //
                    return;
                }
                String object = clazz.getSimpleName();
                int version_new = annotation.version();
                createDB(context, clazz, sp);
                saveJavaBeanVersion(sp, object, version_new);
            }
            if (mIOperDBOver != null) {
                mIOperDBOver.onResult(ALLCREATESUCCESS);
            }
        } catch (Exception e) {
            if (mIOperDBOver != null) {
                mIOperDBOver.onResult(ALLCREATEFAIL);
                mIOperDBOver.onResult(ALLUPDATESFAIL);
            }
            if (e.getClass() == RuntimeException.class) {
                throw new RuntimeException(e.getMessage());
            } else if (e.getClass() == NullPointerException.class) {
                throw new NullPointerException(e.getMessage());
            }
        } finally {
            //            getHelper(context).getWritableDb().endTransaction();
            getHelper(context).closeDb();
        }
    }

    /**
     * 创建20张表,每张表里50个字段,测试了10次,每次时间在800ms左右.
     * <p>
     * 一个java bean中只能有一个主键
     * 当一个java bean中的属性没有设置@Primary的时候,
     * 那么默认id为主键,否则就设置那个属性为主键
     *
     * @param context
     * @param clazz
     */
    private void createDB(Context context, Class<?> clazz, SharedPreferences sp) {
        String table_name = clazz.getSimpleName();
        SQLiteDatabase db = getHelper(context).getWritableDb();
        try {
            //        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
            //        System.out.println("createDBWithVersion:" + helper.getDatabaseName());

            System.out.println("db.beginTransaction():clazz = " + clazz);
            db.beginTransaction();

            Field fields[] = clazz.getDeclaredFields();
            if (fields == null) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLECREATEFAIL);
                }
                return;
            }
            int fields_count = fields.length;
            int primaryCount = 0;
            Field field = null;
            boolean has_id = false;
            for (int i = 0; i < fields_count; i++) {
                Field fieldTemp = fields[i];
                if (fieldTemp == null) {
                    continue;
                }
                Class field_id_class = fieldTemp.getType();
                String field_id = fieldTemp.getName();
                if (field_id_class == int.class
                        && "_id".equals(field_id)) {
                    has_id = true;
                }
                Primary primary = fieldTemp.getAnnotation(Primary.class);
                if (primary != null) {
                    ++primaryCount;
                    field = fieldTemp;
                }
            }

            if (!has_id) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLECREATEFAIL);
                }
                throw new RuntimeException(table_name
                        + " 类中没有设置一个int类型的\"_id\"属性,这是不允许的");
            }

            if (primaryCount > 1) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLECREATEFAIL);
                }
                throw new RuntimeException(table_name
                        + " 类中设置了多个主键,这是不允许的");
            } else if (primaryCount == 0) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLECREATEFAIL);
                }
                throw new RuntimeException(table_name
                        + " 类中没有设置主键,这是不允许的,可以把\"_id\"属性设置为主键");
            }

            SharedPreferences.Editor edit = sp.edit();

            StringBuilder sb = new StringBuilder();
            String primaryKey = null;
            sb.append("CREATE TABLE IF NOT EXISTS ");
            sb.append(table_name);
            sb.append(" (");
            if (field == null) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLECREATEFAIL);
                }
                throw new NullPointerException("主键为null,表创建失败");
            }
            primaryKey = field.getName();

            for (int i = 0; i < fields_count; i++) {
                //            System.out.println("--------------->" + fields[i].getName() +
                //                    " " + fields[i].getType().getSimpleName());
                String fieldName = fields[i].getName();
                String fieldTypeName = fields[i].getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (fieldTypeName.equals(String.class.getSimpleName()) ||
                        fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {

                    sb.append(" ");
                    if ("_id".equals(fieldName) && fieldName.equals(primaryKey)) {
                        sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
                        edit.putString(table_name, "_id");
                        edit.commit();
                        continue;
                    }
                    sb.append(fieldName);

                    sb.append(" TYPELESSNESS");// NVARCHAR

                    if (fieldName.equals(primaryKey)) {
                        sb.append(" PRIMARY KEY");
                        edit.putString(table_name, fieldName);
                        edit.commit();
                    }
                    sb.append(",");
                }
            }
            if (sb.toString().endsWith(",")) {
                sb.deleteCharAt(sb.lastIndexOf(","));
            }
            sb.append(" );");
            //        System.out.println("----------------->" + sb.toString());
            // 新建数据库
            db.execSQL(sb.toString());

            db.setTransactionSuccessful();
            System.out.println("db.setTransactionSuccessful()");
            if (mIOperDBOver != null) {
                mIOperDBOver.onResult(SINGLECREATESUCCESS);
            }
        } catch (Exception e) {
            if (mIOperDBOver != null) {
                mIOperDBOver.onResult(SINGLECREATEFAIL);
            }
            e.printStackTrace();
        } finally {
            db.endTransaction();
            System.out.println("db.endTransaction()");
        }

    }

    /**
     * 不管id是不是主键,都应该要有.
     * 是主键时,增加数据时会自动增1,把这个id值返回;
     * 不是主键时,当增加数据时人为增1,然后把这个id值返回.
     * <p>
     * 属性类型不管怎么改,只要是String类型和8种基本数据类型就行.因此这种情况不需要考虑了.
     * 1.改属性名
     * 有两种操作方式:
     * 1.加注解,得到原来的属性名,然后把数据库里的那个字段改成现在的属性名(不能直接用sql命令实现)
     * 2.不加注解,那么在数据库里的字段跟属性进行比较,这时又有两种情况:
     * 1).类的属性有但是在数据库里没有相应的字段,那么可以把这些字段删了,同时数据也删了(不能直接用sql命令实现)
     * 2).类的属性有但是在数据库里没有相应的字段,不删除这些字段,
     * 只是增加在数据库里没有的类属性对应的字段(可以直接增加字段).原来字段留着,数据也留着,
     * 这里也采用这种方式,原因如下
     * <p>
     * 2.设置另外一个属性为主键
     * 原来属性A是主键,现在要把属性B设置成主键.这里的前提是原来就有一个属性是主键.
     * <p>
     * sqlite:
     * 可以进行的操作
     * 修改表名
     * 增加字段
     * 不可以进行的操作
     * 修改主键
     * 修改字段类型
     * 修改字段名称
     * 删除字段
     *
     * @param context
     * @param clazz
     */
    private void updateDB(Context context, Class<?> clazz, SharedPreferences sp) {
        SQLiteDatabase db = getHelper(context).getWritableDb();
        try {
            // ALTER TABLE table_name RENAME TO new_table_name;
            //        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
            //        System.out.println("------------->" + helper.getDatabaseName());
            String table_name = clazz.getSimpleName();
            String table_name_temp = table_name + "_Temp";


            Field fields[] = clazz.getDeclaredFields();
            if (fields == null) {
                getHelper(context).closeDb();
                return;
            }
            int fields_count = fields.length;
            int primaryCount = 0;
            Field field = null;
            boolean has_id = false;

            db.beginTransaction();

            /**
             sqlite> pragma table_info (Man);
             0|name|TYPELESSNESS|0||1
             1|_id|TYPELESSNESS|0||0
             sqlite> .mode column
             sqlite> .header on
             sqlite> pragma table_info (Man);
             cid         name        type          notnull     dflt_value  pk
             ----------  ----------  ------------  ----------  ----------  ----------
             0           name        TYPELESSNESS  0                       1
             1           _id         TYPELESSNESS  0                       0
             */
            Cursor cursor = getHelper(context).getReadableDb()
                    .query(table_name, null, null, null, null, null, null);
            if (cursor == null) {
                // 这种情况要不要把事务处理设置为成功?
                db.setTransactionSuccessful();
                db.endTransaction();
                getHelper(context).closeDb();
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLEUPDATESFAIL);
                }
                return;
            }

            int cursorColumnCount = cursor.getColumnCount();
            int matchCount = 0;
            ArrayList<String> motherList = new ArrayList<String>();
            ArrayList<String> sonList = new ArrayList<String>();
            ArrayList<HashMap<String, String>> originalFieldList =
                    new ArrayList<HashMap<String, String>>();
            // IdentityHashMap允许key相同
            HashMap<String, String> hashMap = new HashMap<String, String>();

            for (int i = 0; i < fields_count; i++) {
                Field fieldTemp = fields[i];
                if (fieldTemp == null) {
                    continue;
                }
                fieldTemp.setAccessible(true);
                Class field_id_class = fieldTemp.getType();
                String field_id = fieldTemp.getName();
                if (field_id.contains("$") || field_id.contains("serialVersionUID")) {
                    continue;
                }

                OriginalField originalField = fieldTemp.getAnnotation(OriginalField.class);
                if (originalField != null) {
                    String originalFieldName = originalField.value();
                    hashMap.put(field_id, originalFieldName);
                }

                motherList.add(field_id);
                if (field_id_class == int.class
                        && "_id".equals(field_id)) {
                    has_id = true;
                }
                Primary primary = fieldTemp.getAnnotation(Primary.class);
                if (primary != null) {
                    ++primaryCount;
                    field = fieldTemp;
                }

                for (int j = 0; j < cursorColumnCount; j++) {
                    String columnName = cursor.getColumnName(j);
                    if (!sonList.contains(columnName)) {
                        sonList.add(columnName);
                    }
                    if (field_id.equals(columnName)) {
                        ++matchCount;
                        break;
                    }
                }
            }
            originalFieldList.add(hashMap);

            cursor.close();
            cursor = null;

            if (!has_id) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLEUPDATESFAIL);
                }
                throw new RuntimeException(table_name
                        + " 类中没有设置一个int类型的\"_id\"属性,这是不允许的");
            }

            if (primaryCount > 1) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLEUPDATESFAIL);
                }
                throw new RuntimeException(table_name
                        + " 类中设置了多个主键,这是不允许的");
            } else if (primaryCount == 0) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLEUPDATESFAIL);
                }
                throw new RuntimeException(table_name
                        + " 类中没有设置主键,这是不允许的,可以把\"_id\"属性设置为主键");
            }

            String oldPrimary = sp.getString(table_name, table_name);
            if (field == null) {
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLEUPDATESFAIL);
                }
                throw new NullPointerException("主键为null,表创建失败");
            }
            String primaryKey = field.getName();

            int motherListSize = motherList.size();
            int sonListSize = sonList.size();
            HashMap<String, String> map = originalFieldList.get(0);
            int mapSize = map.size();

            //
            if (matchCount == sonListSize && sonListSize < motherListSize) {
                if (primaryKey.equals(oldPrimary)) {
                    for (int i = 0; i < sonListSize; i++) {
                        String columnName = sonList.get(i);
                        if (motherList.contains(columnName)) {
                            motherList.remove(columnName);
                        }
                    }
                    motherListSize = motherList.size();
                    if (motherListSize > 0) {
                        for (int i = 0; i < motherListSize; i++) {
                            String addName = motherList.get(i);
                            StringBuilder sqlSB = new StringBuilder();
                            sqlSB.append("ALTER TABLE ");
                            sqlSB.append(table_name);
                            sqlSB.append(" ADD ");
                            sqlSB.append(addName);
                            sqlSB.append(" TYPELESSNESS;");

                            // sql = "ALTER TABLE " + table_name
                            // + " ADD " + addName + " TYPELESSNESS;";
                            db.execSQL(sqlSB.toString());
                        }
                    }

                    db.setTransactionSuccessful();
                    db.endTransaction();
                    if (mIOperDBOver != null) {
                        mIOperDBOver.onResult(SINGLEUPDATESUCCESS);
                    }
                    return;
                }
            } else if (matchCount == sonListSize && sonListSize == motherListSize) {
                if (primaryKey.equals(oldPrimary)) {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    getHelper(context).closeDb();
                    if (mIOperDBOver != null) {
                        mIOperDBOver.onResult(SINGLEUPDATESUCCESS);
                    }
                    return;
                }
            } else {

            }

            /******************************创建缓存数据库******************************/

            //        String drop_table_sql = "DROP TABLE "+table_name+"2;";
            //        System.out.println(drop_table_sql);
            //        db.execSQL(drop_table_sql);
            //        drop_table_sql = "DROP TABLE "+table_name+"3;";
            //        System.out.println(drop_table_sql);
            //        db.execSQL(drop_table_sql);

            /******************************备份数据库******************************/

            String sql = "ALTER TABLE " + table_name + " RENAME TO " + table_name_temp + ";";
            db.execSQL(sql);

            /******************************创建新数据库******************************/

            SharedPreferences.Editor edit = sp.edit();
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS ");
            sb.append(table_name);
            sb.append(" (");

            for (int i = 0; i < motherListSize; i++) {
                String fieldName = motherList.get(i);

                sb.append(" ");
                if ("_id".equals(fieldName) && fieldName.equals(primaryKey)) {
                    sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
                    edit.putString(table_name, fieldName);
                    edit.commit();
                    continue;
                }

                sb.append(fieldName);

                sb.append(" TYPELESSNESS");// NVARCHAR

                if (fieldName.equals(primaryKey)) {
                    sb.append(" PRIMARY KEY");
                    edit.putString(table_name, fieldName);
                    edit.commit();
                }

                sb.append(",");
            }

            /******************************属性名改了咋办******************************/

            if (sb.toString().endsWith(",")) {
                sb.deleteCharAt(sb.lastIndexOf(","));
            }
            sb.append(" );");
            System.out.println("----------------->" + sb.toString());
            // 新建数据库
            db.execSQL(sb.toString());

            /******************************复制数据******************************/

            sql = "SELECT * FROM " + table_name_temp + ";";
            cursor = getHelper(context).getReadableDb().rawQuery(sql, null);
            if (cursor == null || cursor.getCount() <= 0) {
                sql = "DROP TABLE " + table_name_temp + ";";
                //        System.out.println(sql);
                db.execSQL(sql);
                db.setTransactionSuccessful();
                db.endTransaction();
                getHelper(context).closeDb();
                if (mIOperDBOver != null) {
                    mIOperDBOver.onResult(SINGLEUPDATESUCCESS);
                }
                return;
            }

            int columnCount = cursor.getColumnCount();

            String columnName = null;
            boolean addOnce = false;
            while (cursor.moveToNext()) {
                String needAddColumnValue = null;
                ContentValues values = new ContentValues();
                for (int i = 0; i < columnCount; i++) {
                    // 得到列名
                    columnName = cursor.getColumnName(i);
                    // 得到某列的值
                    String temp = cursor.getString(i);
                    if (primaryKey.equals(columnName)) {
                        needAddColumnValue = temp;
                    }
                    if (motherList.contains(columnName)) {
                        values.put(columnName, temp);
                    } else if (mapSize > 0) {
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            //                        System.out.println(entry.getKey() + "\t:\t" +
                            // entry.getValue());
                            if (entry.getValue().equals(columnName)) {
                                values.put(entry.getKey(), temp);
                                break;
                            }
                        }
                    }
                    //                System.out.println("------------>" + columnName + " " + temp);
                }

                if (primaryKey.equals(oldPrimary)) {
                    db.insert(table_name, primaryKey, values);
                } else {
                    if (!addOnce) {
                        addOnce = true;
                        db.insert(table_name, primaryKey, values);
                    } else {
                        if (needAddColumnValue != null) {
                            db.update(table_name, values,
                                    primaryKey + "=?", new String[]{needAddColumnValue});
                        }
                    }
                }
            }
            cursor.close();
            cursor = null;

            // DROP TABLE database_name.table_name;
            sql = "DROP TABLE " + table_name_temp + ";";
            db.execSQL(sql);

            db.setTransactionSuccessful();
            if (mIOperDBOver != null) {
                mIOperDBOver.onResult(SINGLEUPDATESUCCESS);
            }
        } catch (Exception e) {
            if (mIOperDBOver != null) {
                mIOperDBOver.onResult(SINGLEUPDATESFAIL);
            }
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    private static void saveJavaBeanVersion(SharedPreferences sp, String clazz, int version) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(clazz + VERSION, version);
        edit.commit();
    }


    /************************************备份**********************************/

    /**
     * 一个java bean中只能有一个主键
     * 当一个java bean中的属性没有设置@Primary的时候,
     * 那么默认id为主键,否则就设置那个属性为主键
     *
     * @param context
     * @param clazz
     */
    private void createDB2(Context context, Class<?> clazz) {
        //        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        //        System.out.println("createDBWithVersion:" + helper.getDatabaseName());
        String table_name = clazz.getSimpleName();
        SQLiteDatabase db = getHelper(context).getWritableDb();

        db.beginTransaction();

        Field fields[] = clazz.getDeclaredFields();
        if (fields == null) {
            return;
        }
        int fields_count = fields.length;
        int primaryCount = 0;
        Field field = null;
        for (int i = 0; i < fields_count; i++) {
            Field fieldTemp = fields[i];
            Primary primary = fieldTemp.getAnnotation(Primary.class);
            if (primary != null) {
                ++primaryCount;
                field = fieldTemp;
            }
        }

        if (primaryCount > 1) {
            throw new RuntimeException(table_name + "中设置了多个主键,这是不允许的");
        }

        StringBuilder sb = new StringBuilder();
        String primaryKey = null;
        sb.append("create table if not exists ");
        sb.append(table_name);
        sb.append(" (");
        if (primaryCount == 0) {
            sb.append(" _id integer primary key");
        } else {
            if (field == null) {
                throw new NullPointerException("主键为null,表创建失败");
            }
            primaryKey = field.getName();
            sb.append(" _id integer");
        }

        int i = 0;
        for (i = 0; i < fields_count; i++) {
            //            System.out.println("--------------->" + fields[i].getName() +
            //                    " " + fields[i].getType().getSimpleName());
            String fieldName = fields[i].getName();
            String fieldTypeName = fields[i].getType().getSimpleName();
            if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                continue;
            }

            if (fieldTypeName.equals(String.class.getSimpleName()) ||
                    fieldTypeName.equals(long.class.getSimpleName()) ||
                    fieldTypeName.equals(short.class.getSimpleName()) ||
                    fieldTypeName.equals(int.class.getSimpleName()) ||
                    fieldTypeName.equals(double.class.getSimpleName()) ||
                    fieldTypeName.equals(float.class.getSimpleName()) ||
                    fieldTypeName.equals(boolean.class.getSimpleName()) ||
                    fieldTypeName.equals(char.class.getSimpleName()) ||
                    fieldTypeName.equals(byte.class.getSimpleName()) ||
                    fieldTypeName.equals(Long.class.getSimpleName()) ||
                    fieldTypeName.equals(Short.class.getSimpleName()) ||
                    fieldTypeName.equals(Integer.class.getSimpleName()) ||
                    fieldTypeName.equals(Double.class.getSimpleName()) ||
                    fieldTypeName.equals(Float.class.getSimpleName()) ||
                    fieldTypeName.equals(Boolean.class.getSimpleName()) ||
                    fieldTypeName.equals(Character.class.getSimpleName()) ||
                    fieldTypeName.equals(Byte.class.getSimpleName())) {

                sb.append(",");
                sb.append(" ");
                sb.append(fieldName);
                sb.append(" ");
                if (i < fields_count) {
                    sb.append("varchar");
                }
                if (fieldName.equals(primaryKey)) {
                    sb.append(" primary key");
                }
            }
        }
        sb.append(" );");
        //                System.out.println("----------------->" + sb.toString());
        // 新建数据库
        db.execSQL(sb.toString());

        db.setTransactionSuccessful();
        db.endTransaction();
        getHelper(context).closeDb();
    }

    /**
     * db.execSQL("create table if not exists hero_info("
     * + "id integer primary key,"
     * + "name varchar,"
     * + "level integer)");
     * <p>
     * create table if not exists BlacklistPhone
     * ( id integer primary key, address varchar,
     * time varchar, number varchar,
     * flag varchar, date varchar,
     * duration varchar, news varchar,
     * type varchar, id varchar );
     *
     * @param clazz
     */
    private void updateDB(Context context, Class<?> clazz) {
        // ALTER TABLE table_name RENAME TO new_table_name;
        //        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        //        System.out.println("------------->" + helper.getDatabaseName());
        String table_name = clazz.getSimpleName();
        String table_name_temp = table_name + "_temp";
        SQLiteDatabase db = getHelper(context).getWritableDb();
        db.beginTransaction();

        //        String drop_table_sql = "DROP TABLE "+table_name+"2;";
        //        System.out.println(drop_table_sql);
        //        db.execSQL(drop_table_sql);
        //        drop_table_sql = "DROP TABLE "+table_name+"3;";
        //        System.out.println(drop_table_sql);
        //        db.execSQL(drop_table_sql);

        String sql = "ALTER TABLE " + table_name + " RENAME TO " + table_name_temp + ";";
        db.execSQL(sql);

        ArrayList<String> fieldList = new ArrayList<String>();
        sql = "select * from " + table_name_temp + ";";
        Cursor cursor = getHelper(context).getReadableDb().rawQuery(sql, null);
        int columnCount = cursor.getColumnCount();
        int i = 0;

        Field fields[] = clazz.getDeclaredFields();
        if (fields == null) {
            return;
        }
        int fields_count = fields.length;
        StringBuilder sb = new StringBuilder();
        sb.append("create table if not exists ");
        sb.append(table_name);
        sb.append(" (");
        sb.append(" id integer primary key");
        fieldList.add("id");
        for (i = 0; i < fields_count; i++) {
            //            System.out.println("--------------->"+fields[i].getName() +
            //                    " "+fields[i].getType().getSimpleName());
            Field field = fields[i];
            field.setAccessible(true);
            if (field.getName().contains("$") || field.getName().contains("serialVersionUID")) {
                continue;
            }
            sb.append(",");
            sb.append(" ");
            sb.append(field.getName());
            fieldList.add(field.getName());
            sb.append(" ");
            if (i < fields_count) {
                sb.append("varchar");
            }
        }
        for (i = 0; i < columnCount; i++) {
            String columnName = cursor.getColumnName(i);
            if (!fieldList.contains(columnName)) {
                sb.append(",");
                sb.append(" ");
                sb.append(columnName);
                sb.append(" ");
                sb.append("varchar");
                fieldList.add(columnName);
            }
        }
        sb.append(" );");
        //        System.out.println("----------------->"+sb.toString());
        // 新建数据库
        db.execSQL(sb.toString());

        String temp = null;
        String columnName = null;
        while (cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            for (i = 0; i < columnCount; i++) {
                temp = cursor.getString(i);
                columnName = cursor.getColumnName(i);
                //                System.out.println("------------>" + columnName + " " + temp);
                values.put(columnName, temp);
            }
            db.insert(table_name, columnName, values);
        }
        cursor.close();

        //        System.out.println("------------------------------start");
        //        sql = "ALTER TABLE " + table_name + " RENAME TO " + table_name_temp + ";";
        //        db.execSQL(sql);
        //        System.out.println("------------------------------end");

        // DROP TABLE database_name.table_name;
        sql = "DROP TABLE " + table_name_temp + ";";
        //        System.out.println(sql);
        db.execSQL(sql);

        db.setTransactionSuccessful();
        db.endTransaction();

        getHelper(context).closeDb();
    }

}
