package com.weidi.dbutil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.weidi.log.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 16-7-31.
 */

public class BaseDaoImpl extends ABaseDao {

    private static final String TAG = "BaseDaoImpl";
    private MySQLiteOpenHelper helper;
    private SQLiteDatabase db_write;
    private SQLiteDatabase db_read;
    private Context mContext;
    //    private SharedPreferences mSharedPreferences;
    private String tableName;

    public interface IOperDBResult {
        void onAddResult(boolean isSuccess);

        void onDeleteResult(boolean isSuccess);

        void onUpdateResult(boolean isSuccess);

        void onQueryResult(boolean isSuccess);
    }

    public BaseDaoImpl(Context context) {
        if (context == null) {
            throw new NullPointerException("BaseDaoImpl中的context不能为null");
        }
        if (helper == null) {
            helper = new MySQLiteOpenHelper(context);
        }
        mContext = context;
    }

    /**
     *
     */
    public void close() {
        if (db_read != null && db_read.isOpen()) {
            db_read.close();
            db_read = null;
        }
        if (db_write != null && db_write.isOpen()) {
            db_write.close();
            db_write = null;
        }
        if (helper != null) {
            helper.closeDb();
        }
    }

    /*************************************主键是_id时的操作*************************************/

    /**
     * OK
     *
     * @param clazz
     * @param object
     * @return 新添加的_id号
     */
    public long add(Class<?> clazz, Object object) {
        long index = -1;
        try {
            if (clazz == null) {
                return index;
            }
            tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return index;
            }

            int length = fields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }
                if (fieldTypeName.equals(String.class.getSimpleName())) {

                    values.put(fieldName, (String) field.get(object));
                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getLong(object)));
                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getShort(object)));
                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {

                    if ("_id".equals(fieldName)) {
                        continue;
                    }
                    values.put(fieldName, String.valueOf(field.getInt(object)));
                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getDouble(object)));
                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getFloat(object)));
                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getBoolean(object)));
                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getChar(object)));
                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getByte(object)));
                }
            }

            index = getWritableDb().insert(tableName, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param values
     * @return 新添加的_id号(_id就是主键)
     */
    @Override
    public long add(Class<?> clazz, ContentValues values) {
        long index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            tableName = clazz.getSimpleName();
            /**
             * INSERT INTO table_name( column1, column2....columnN)
             * VALUES ( value1, value2....valueN);
             */
            //            StringBuilder sb = new StringBuilder();
            //            sb.append("INSERT INTO ");
            //            sb.append(tableName);
            //            sb.append(" ( ");
            //            Field fields[] = clazz.getDeclaredFields();
            //            if (fields == null) {
            //                return index;
            //            }
            //            int fields_count = fields.length;
            //            String fieldName = null;
            //            LinkedList<String> list = new LinkedList<>();
            //            for (int i = 0; i < fields_count; ++i) {
            //                //            System.out.println("--------------->"+fields[i]
            // .getName() +
            //                //                    " "+fields[i].getType().getSimpleName());
            //                fieldName = fields[i].getName();
            //                if (fieldName.contains("$")) {
            //                    continue;
            //                }
            //                sb.append(fieldName);
            //                list.add(fieldName);
            //                if(i<fields_count - 1){
            //                    sb.append(", ");
            //                }else{
            //                    sb.append(" )");
            //                }
            //            }
            //            sb.append("VALUES ( ");
            //            int count = list.size();
            //            String key = null;
            //            String value = null;
            //            for(int i=0;i<count;++i){
            //                key = list.get(i);
            //                if(!map.containsKey(key)){
            //                    continue;
            //                }
            //                value = map.get(key);
            //                sb.append("\'");
            //                sb.append(value);
            //                if(i<count-1){
            //                    sb.append("\', ");
            //                }else{
            //                    sb.append("\' );");
            //                }
            //            }
            //            System.out.println(sb.toString());
            //            getWritableDb().execSQL(sb.toString());

            index = getWritableDb().insert(tableName, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /*************************************主键不是_id时的操作*************************************/

    /**
     * 当数据库里已经有这个主键的值时,不再添加(添加会出错),也不更新
     *
     * @param clazz
     * @param object
     * @return 新添加的_id号
     */
    public long add2(Class<?> clazz, Object object, String primaryKey, String primaryValue) {
        long index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            if (isExists(clazz, primaryKey, primaryValue)) {
                return index;
            }

            tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return index;
            }
            long _id = getLastId(clazz);
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            int length = fields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }
                if (fieldTypeName.equals(String.class.getSimpleName())) {

                    values.put(fieldName, (String) field.get(object));
                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getLong(object)));
                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getShort(object)));
                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {

                    if ("_id".equals(fieldName)) {
                        values.put(fieldName, String.valueOf(_id));
                        continue;
                    }
                    values.put(fieldName, String.valueOf(field.getInt(object)));
                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getDouble(object)));
                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getFloat(object)));
                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getBoolean(object)));
                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getChar(object)));
                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getByte(object)));
                }
            }

            index = getWritableDb().insert(tableName, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * 当数据库里已经有这个主键的值时,不再添加(添加会出错),而是更新
     *
     * @param clazz
     * @param object
     * @return 新添加的_id号
     */
    public long add2OrUpdate(
            Class<?> clazz,
            Object object,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            if (clazz == null) {
                return index;
            }
            tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return index;
            }
            long _id = getLastId(clazz);
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            int length = fields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }
                if (fieldTypeName.equals(String.class.getSimpleName())) {

                    values.put(fieldName, (String) field.get(object));
                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getLong(object)));
                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getShort(object)));
                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {

                    if ("_id".equals(fieldName)) {
                        values.put(fieldName, String.valueOf(_id));
                        continue;
                    }
                    values.put(fieldName, String.valueOf(field.getInt(object)));
                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getDouble(object)));
                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getFloat(object)));
                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getBoolean(object)));
                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getChar(object)));
                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getByte(object)));
                }
            }

            if (!isExists(clazz, primaryKey, primaryValue)) {
                index = getWritableDb().insert(tableName, null, values);
            } else {
                // String table, ContentValues values, String whereClause, String[] whereArgs
                index = getWritableDb().update(
                        tableName,
                        values,
                        primaryKey + "=?",
                        new String[]{primaryValue});
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * 当数据库里已经有这个主键的值时,不再添加(添加会出错),也不更新
     *
     * @param clazz
     * @param values
     * @return 新添加的_id号
     */
    public long add2(Class<?> clazz, ContentValues values, String primaryKey, String primaryValue) {
        long index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            if (isExists(clazz, primaryKey, primaryValue)) {
                return index;
            }

            tableName = clazz.getSimpleName();
            long _id = getLastId(clazz);
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;
            values.put("_id", String.valueOf(_id));

            index = getWritableDb().insert(tableName, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * @param clazz
     * @param values
     * @return 新添加的_id号
     */
    public long add2OrUpdate(
            Class<?> clazz,
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            tableName = clazz.getSimpleName();
            long _id = getLastId(clazz);
            Log.d(TAG, "_id = " + String.valueOf(_id));
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            if (!isExists(clazz, primaryKey, primaryValue)) {
                values.put("_id", String.valueOf(_id));
                index = getWritableDb().insert(tableName, primaryKey, values);
                Log.d(TAG, "insert():index = " + index);
            } else {
                index = getWritableDb().update(
                        tableName,
                        values,
                        primaryKey + "=?",
                        new String[]{primaryValue});
                Log.d(TAG, "update():index = " + index);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /******************************************************************************************/

    /**
     * OK
     *
     * @param clazz
     * @param _id   当_id设置为主键时,可以用这个删,如果是其他主键,不建议
     * @return 删除的条数
     */
    @Override
    public int delete(Class<?> clazz, int _id) {
        int index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            tableName = clazz.getSimpleName();

            index = getWritableDb().delete(tableName, "_id=?", new String[]{String.valueOf(_id)});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param primaryKey
     * @param primaryValue
     * @return 删除的条数
     */
    public int delete(
            Class<?> clazz,
            String primaryKey,
            String primaryValue) {
        int index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            tableName = clazz.getSimpleName();
            // getWritableDb().delete(tableName, "number=? and flag=?", new String[]{"12345", "5"})
            // String table, String whereClause, String[] whereArgs

            index = getWritableDb().delete(
                    tableName,
                    primaryKey + "=?",
                    new String[]{primaryValue});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            //            getWritableDb().close();
        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param map   多个条件
     * @return 删除的条数
     */
    @Override
    public int delete(Class<?> clazz, Map<String, String> map) {
        int index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            tableName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String values[] = new String[count];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                j = i++;
                if (j < count - 1) {
                    sb.append(entry.getKey());
                    sb.append("=?");
                    sb.append(" and ");
                } else {
                    sb.append(entry.getKey());
                    sb.append("=?");
                }
                values[j] = entry.getValue();
            }
            //            Iterator iterator=map.entrySet().iterator();
            //            while(iterator.hasNext()){
            //                Map.Entry<String, String> entry= (Entry<String, String>) iterator
            // .next();
            //                System.out.println("key:"+entry.getKey()+" value"+entry.getValue());
            //            }

            // getWritableDb().delete(tableName, "number=? and flag=?", new String[]{"12345", "5"})

            index = getWritableDb().delete(tableName, sb.toString(), values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     *
     * @param clazz
     * @param values
     * @param _id 一般是主键
     * @return
     */
    public int update(
            Class<?> clazz,
            ContentValues values,
            int _id) {
        int index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            tableName = clazz.getSimpleName();
            // update(String table, ContentValues values, String whereClause, String[] whereArgs)

            index = getWritableDb().update(
                    tableName,
                    values,
                    "_id=?",
                    new String[]{String.valueOf(_id)});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param values       需要更新的的内容
     * @param primaryKey   更新哪一条的条件 一般是主键
     * @param primaryValue 更新哪一条的条件
     * @return 更新的条数
     */
    public int update(
            Class<?> clazz,
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        int index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            tableName = clazz.getSimpleName();
            // update(String table, ContentValues values, String whereClause, String[] whereArgs)

            index = getWritableDb().update(
                    tableName,
                    values,
                    primaryKey + "=?",
                    new String[]{primaryValue});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param values 需要更新的的内容
     * @param map    更新哪一条的条件,多个条件
     * @return 更新的条数
     */
    @Override
    public int update(Class<?> clazz, ContentValues values, Map<String, String> map) {
        int index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            tableName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String whereArgs[] = new String[count];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                j = i++;
                if (j < count - 1) {
                    sb.append(entry.getKey());
                    sb.append("=?");
                    sb.append(" and ");
                } else {
                    sb.append(entry.getKey());
                    sb.append("=?");
                }
                whereArgs[j] = entry.getValue();
            }
            // update(String table, ContentValues values, String whereClause, String[] whereArgs)

            index = getWritableDb().update(tableName, values, sb.toString(), whereArgs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param _id   当_id设置为主键时,可以用这个查,如果是其他主键,不建议
     * @return 单个对象
     */
    @Override
    public Object querySingle(Class<?> clazz, int _id) {
        Object object = null;
        try {
            if (clazz == null) {
                return object;
            }

            tableName = clazz.getSimpleName();
            Cursor cursor = getReadableDb().query(
                    tableName, null,
                    "_id=?",
                    new String[]{String.valueOf(_id)},
                    null, null, null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = clazz.newInstance();
                object = internalQuerySingle(clazz, object_, cursor);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return object;
    }

    /**
     * OK
     *
     * @param clazz
     * @param primaryKey 一般是主键
     * @param primaryValue
     * @return 单个对象
     */
    public Object querySingle(Class<?> clazz, String primaryKey, String primaryValue) {
        Object object = null;
        try {
            if (clazz == null) {
                return object;
            }
            tableName = clazz.getSimpleName();
            /**
             * query(String table, String[] columns,
             * String selection, String[] selectionArgs,
             * String groupBy, String having, String orderBy)
             */
            Cursor cursor = getReadableDb().query(tableName, null,
                    primaryKey + "=?", new String[]{primaryValue}, null, null, null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = clazz.newInstance();
                object = internalQuerySingle(clazz, object_, cursor);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return object;
    }

    /**
     * OK
     *
     * @param clazz
     * @param object
     * @param isIdPrimary
     * @return 单个对象
     */
    public Object querySingle(Class<?> clazz, Object object, boolean isIdPrimary) {
        Object obj = null;
        try {

            if (clazz == null) {
                return obj;
            }
            tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return obj;
            }
            int length = fields.length;
            StringBuilder sb = new StringBuilder();
            String values[] = new String[length];
            int index = 0;
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }
                if (fieldTypeName.equals(String.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = (String) field.get(object);
                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getLong(object));
                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getShort(object));
                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {

                    if ("_id".equals(fieldName)) {
                        if (!isIdPrimary) {
                            sb.append(fieldName);
                            sb.append("=?");
                            sb.append(" and ");
                            values[index++] = String.valueOf(field.getInt(object));
                        }
                        continue;
                    }
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getInt(object));
                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getDouble(object));
                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getFloat(object));
                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getBoolean(object));
                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getChar(object));
                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getByte(object));
                }
            }
            if (sb.toString().endsWith("and ")) {
                sb.delete(sb.lastIndexOf("a"), sb.lastIndexOf(" "));
            }
            String values_[] = new String[index];
            int values_count = values_.length;
            for (int i = 0; i < values_count; i++) {
                values_[i] = values[i];
            }

            Cursor cursor = getReadableDb().query(tableName, null, sb.toString(), values_, null,
                    null, null);
            if (cursor == null) {
                return obj;
            }

            if (cursor.getCount() > 0) {
                Object object_ = clazz.newInstance();
                obj = internalQuerySingle(clazz, object_, cursor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return obj;
    }

    /**
     * OK
     *
     * @param clazz
     * @param map   多个条件
     * @return 单个对象
     */
    @Override
    public Object querySingle(Class<?> clazz, Map<String, String> map) {
        Object object = null;
        try {
            if (clazz == null) {
                return object;
            }
            tableName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String values[] = new String[count];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                j = i++;
                if (j < count - 1) {
                    sb.append(entry.getKey());
                    sb.append("=?");
                    sb.append(" and ");
                } else {
                    sb.append(entry.getKey());
                    sb.append("=?");
                }
                values[j] = entry.getValue();
            }
            /**
             * query(String table, String[] columns,
             * String selection, String[] selectionArgs,
             * String groupBy, String having, String orderBy)
             */
            Cursor cursor = getReadableDb().query(tableName, null, sb.toString(), values, null,
                    null, null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = clazz.newInstance();
                object = internalQuerySingle(clazz, object_, cursor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return object;
    }

    /**
     * 主键是_id时
     *
     * @param clazz
     * @param primaryKey   查找符合某种键值对的记录(如性别是男的记录)
     * @param primaryValue
     * @return
     */
    public ArrayList queryMore(Class<?> clazz, String primaryKey, String primaryValue) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return mList;
            }
            tableName = clazz.getSimpleName();
            cursor = getReadableDb().query(tableName, null,
                    primaryKey + "=?", new String[]{primaryValue}, null, null, null);
            if (cursor == null) {
                return mList;
            }
            if (cursor.getCount() <= 0) {
                return mList;
            }
            mList = internalQueryMore(clazz, cursor);
            if (mList == null) {
                return mList;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return mList;
        } finally {

        }
        return mList;
    }

    /**
     * OK
     *
     * @param clazz
     * @param map   多个条件
     * @return
     */
    @Override
    public ArrayList queryMore(Class<?> clazz, Map<String, String> map) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return mList;
            }
            tableName = clazz.getSimpleName();
            if (map != null && !map.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int count = map.size();
                int i = 0;
                int j = i;
                String values[] = new String[count];
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    j = i++;
                    if (j < count - 1) {
                        sb.append(entry.getKey());
                        sb.append("=?");
                        sb.append(" and ");
                    } else {
                        sb.append(entry.getKey());
                        sb.append("=?");
                    }
                    values[j] = entry.getValue();
                }
                cursor = getReadableDb().query(tableName, null, sb.toString(), values, null,
                        null, null);
            } else {
                cursor = getReadableDb().query(tableName, null, null, null, null, null, null);
            }

            mList = internalQueryMore(clazz, cursor);
            if (mList == null) {
                return mList;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return mList;
        } finally {

        }
        return mList;
    }

    public ArrayList queryAll(Class<?> clazz) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return mList;
            }
            tableName = clazz.getSimpleName();
            String sql = "SELECT * FROM " + tableName;
            cursor = getReadableDb().rawQuery(sql, null, null);
            if (cursor == null) {
                return mList;
            }
            if (cursor.getCount() <= 0) {
                cursor.close();
                cursor = null;
                return mList;
            }

            mList = internalQueryMore(clazz, cursor);
            if (mList == null) {
                return mList;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return mList;
        } finally {

        }
        return mList;
    }

    public boolean isExists(Class<?> clazz, int _id) {
        boolean isExists = false;
        try {
            if (clazz == null) {

                return isExists;
            }
            tableName = clazz.getSimpleName();
            String sql = "SELECT * FROM " + tableName + " where "
                    + "_id = \'" + String.valueOf(_id) + "\';";
            Cursor cursor = getReadableDb().rawQuery(sql, null);
            if (cursor == null) {

                return isExists;
            }
            if (cursor.getCount() > 0) {
                isExists = true;
            }
            cursor.close();
            cursor = null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return isExists;
    }

    /**
     * OK
     * 当_id不设置为主键时用这个方法判断
     *
     * @param clazz
     * @param primaryKey   主键
     * @param primaryValue 主键的值
     * @return
     */
    public boolean isExists(Class<?> clazz, String primaryKey, String primaryValue) {
        boolean isExists = false;
        try {
            if (clazz == null) {
                return isExists;
            }
            tableName = clazz.getSimpleName();
            String sql = "SELECT * FROM " + tableName + " where "
                    + primaryKey + " = \'" + primaryValue + "\';";
            Cursor cursor = getReadableDb().rawQuery(sql, null);
            if (cursor == null) {

                return isExists;
            }
            if (cursor.getCount() > 0) {
                isExists = true;
            }
            cursor.close();
            cursor = null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return isExists;
    }

    public boolean isExists(Class<?> clazz, Object object, boolean isIdPrimary) {
        boolean isExists = false;
        try {
            if (clazz == null) {
                return isExists;
            }
            tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return isExists;
            }
            int length = fields.length;
            StringBuilder sb = new StringBuilder();
            String values[] = new String[length];
            int index = 0;
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }
                if (fieldTypeName.equals(String.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = (String) field.get(object);
                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getLong(object));
                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getShort(object));
                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {

                    if ("_id".equals(fieldName)) {
                        if (!isIdPrimary) {
                            sb.append(fieldName);
                            sb.append("=?");
                            sb.append(" and ");
                            values[index++] = String.valueOf(field.getInt(object));
                        }
                        continue;
                    }
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getInt(object));
                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getDouble(object));
                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getFloat(object));
                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getBoolean(object));
                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getChar(object));
                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {

                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getByte(object));
                }
            }
            if (sb.toString().endsWith("and ")) {
                sb.delete(sb.lastIndexOf("a"), sb.lastIndexOf(" "));
            }
            String values_[] = new String[index];
            int values_count = values_.length;
            for (int i = 0; i < values_count; i++) {
                values_[i] = values[i];
            }

            Cursor cursor = getReadableDb().query(tableName, null, sb.toString(), values_, null,
                    null, null);
            if (cursor == null) {
                return isExists;
            }
            if (cursor.getCount() > 0) {
                isExists = true;
            }
            cursor.close();
            cursor = null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return isExists;
    }

    /**
     * OK
     *
     * @param clazz
     * @param map   多个条件
     * @return
     */
    public boolean isExists(Class<?> clazz, Map<String, String> map) {
        boolean isExists = false;
        try {
            if (clazz == null) {
                return isExists;
            }
            tableName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String values[] = new String[count];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                j = i++;
                if (j < count - 1) {
                    sb.append(entry.getKey());
                    sb.append("=?");
                    sb.append(" and ");
                } else {
                    sb.append(entry.getKey());
                    sb.append("=?");
                }
                values[j] = entry.getValue();
            }
            Cursor cursor = getReadableDb().query(tableName, null, sb.toString(), values, null,
                    null, null);
            if (cursor == null) {
                return isExists;
            }
            if (cursor.getCount() > 0) {
                isExists = true;
            }
            cursor.close();
            cursor = null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return isExists;
    }

    /**
     * 当_id不是主键时,先取出最后的_id值,然后加1,再存入数据
     * 41条数据时用时大概2~4ms
     *
     * @param clazz
     * @return
     */
    public long getLastId(Class<?> clazz) {
        long lastId = -1;
        try {
            if (clazz == null) {
                return lastId;
            }
            tableName = clazz.getSimpleName();
            String sql = "SELECT * FROM " + tableName + " ORDER BY " + "_id DESC LIMIT 0,1;";
            Cursor cursor = getReadableDb().rawQuery(sql, null);
            if (cursor == null) {

                return lastId;
            }
            if (cursor.getCount() <= 0) {

                return lastId;
            }
            cursor.moveToFirst();
            lastId = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();
            cursor = null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return lastId;
    }

    //    private Context getContext() {
    //        return mContext;
    //    }
    //
    //    private SharedPreferences getSharedPreferences() {
    //        if (mSharedPreferences == null) {
    //            mSharedPreferences = getContext().getSharedPreferences(
    //                    Constant.SHAREDPREFERENCES,
    //                    Context.MODE_PRIVATE);
    //        }
    //        return mSharedPreferences;
    //    }
    private Object internalQuerySingle(Class<?> clazz, Object object, Cursor cursor) {
        try {
            int columnCount = cursor.getColumnCount();
            String temp = null;
            String columnName = null;
            int i = 0;
            Map<String, String> map = new HashMap<String, String>();
            if (cursor.moveToNext()) {
                for (i = 0; i < columnCount; i++) {
                    temp = cursor.getString(i);
                    columnName = cursor.getColumnName(i);
                    //                System.out.println("------------>" + columnName + " " + temp);
                    map.put(columnName, temp);
                }
            }
            cursor.close();
            cursor = null;

            Field fields[] = clazz.getDeclaredFields();
            if (fields == null) {
                return null;
            }
            int fields_count = fields.length;
            for (i = 0; i < fields_count; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();

                if (fieldName.contains("$")
                        || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (map.containsKey(fieldName)) {
                    String value = map.get(fieldName);
                    if (TextUtils.isEmpty(value) || "null".equals(value)) {
                        continue;
                    }
                    if (fieldTypeName.equals(String.class.getSimpleName())) {
                        field.set(object, value);
                    } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                            fieldTypeName.equals(Long.class.getSimpleName())) {
                        field.setLong(object, Long.parseLong(value));
                    } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                            fieldTypeName.equals(Short.class.getSimpleName())) {
                        field.setShort(object, Short.parseShort(value));
                    } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                            fieldTypeName.equals(Integer.class.getSimpleName())) {
                        field.setInt(object, Integer.parseInt(value));
                    } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                            fieldTypeName.equals(Double.class.getSimpleName())) {
                        field.setDouble(object, Double.parseDouble(value));
                    } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                            fieldTypeName.equals(Float.class.getSimpleName())) {
                        field.setFloat(object, Float.parseFloat(value));
                    } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                            fieldTypeName.equals(Boolean.class.getSimpleName())) {
                        field.setBoolean(object, Boolean.parseBoolean(value));
                    } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                            fieldTypeName.equals(Character.class.getSimpleName())) {
                        field.setChar(object, value.charAt(0));
                    } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                            fieldTypeName.equals(Byte.class.getSimpleName())) {
                        field.setByte(object, Byte.parseByte(value));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return object;
    }

    private ArrayList<Object> internalQueryMore(Class<?> clazz, Cursor cursor) {
        ArrayList<Object> mList = null;
        try {
            int columnCount = cursor.getColumnCount();
            String temp = null;
            String columnName = null;
            int i = 0;
            int j = 0;
            ArrayList<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
            Map<String, String> map = null;
            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                for (i = 0; i < columnCount; i++) {
                    temp = cursor.getString(i);
                    columnName = cursor.getColumnName(i);
                    map.put(columnName, temp);
                }
                //                System.out.println("------------->"+map);
                tempList.add(map);
            }
            cursor.close();
            cursor = null;

            Field fields[] = clazz.getDeclaredFields();
            if (fields == null) {
                return null;
            }
            int fields_count = fields.length;
            mList = new ArrayList<Object>();

            int tempListCount = tempList.size();
            for (i = 0; i < tempListCount; i++) {
                map = tempList.get(i);
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object = clazz.newInstance();
                for (j = 0; j < fields_count; j++) {
                    if (fields[j].getName().contains("$")) {
                        continue;
                    }
                    Field field = fields[j];
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    String fieldTypeName = field.getType().getSimpleName();
                    if (map.containsKey(fieldName)) {
                        String value = map.get(fieldName);
                        if (TextUtils.isEmpty(value) || "null".equals(value)) {
                            continue;
                        }
                        if (fieldTypeName.equals(String.class.getSimpleName())) {
                            field.set(object, value);
                        } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                                fieldTypeName.equals(Long.class.getSimpleName())) {
                            field.setLong(object, Long.parseLong(value));
                        } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                                fieldTypeName.equals(Short.class.getSimpleName())) {
                            field.setShort(object, Short.parseShort(value));
                        } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                                fieldTypeName.equals(Integer.class.getSimpleName())) {
                            field.setInt(object, Integer.parseInt(value));
                        } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                                fieldTypeName.equals(Double.class.getSimpleName())) {
                            field.setDouble(object, Double.parseDouble(value));
                        } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                                fieldTypeName.equals(Float.class.getSimpleName())) {
                            field.setFloat(object, Float.parseFloat(value));
                        } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                                fieldTypeName.equals(Boolean.class.getSimpleName())) {
                            field.setBoolean(object, Boolean.parseBoolean(value));
                        } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                                fieldTypeName.equals(Character.class.getSimpleName())) {
                            field.setChar(object, value.charAt(0));
                        } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                                fieldTypeName.equals(Byte.class.getSimpleName())) {
                            field.setByte(object, Byte.parseByte(value));
                        }
                    }
                }
                mList.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mList;
    }

    private MySQLiteOpenHelper getHelper() {
        if (helper == null) {
            helper = new MySQLiteOpenHelper(mContext);
        }
        return helper;
    }

    private SQLiteDatabase getWritableDb() {
        if (db_write == null) {
            db_write = getHelper().getWritableDb();
        }
        return db_write;
    }

    private SQLiteDatabase getReadableDb() {
        if (db_read == null) {
            db_read = getHelper().getReadableDb();
        }
        return db_read;
    }

}
