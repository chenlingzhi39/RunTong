package com.callba.phone.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import com.callba.phone.bean.ContactData;
import com.umeng.socialize.utils.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PC-20160514 on 2016/6/2.
 */
public class ContactsAccessPublic {

        final static String TAG = "ContactsAccess";
        final static String PhoneAccountName = "Phone";
        final static String SIMAccountName = "SIM";

        // 读取联系人信息
        public static List<ContactData> getPhoneContacts(Context context, List<ContactData> list, boolean bSort)
        {
            if(list == null)
                list = new ArrayList<ContactData>();

            Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI,
                    null,
                    RawContacts.ACCOUNT_NAME + "=?",
                    new String[]{"Phone"}, null);
            while (cursor.moveToNext())
            {
                int indexId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                String contactId = cursor.getString(indexId);
                int indexDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String name = cursor.getString(indexDisplayName);

                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                while (phones.moveToNext())
                {
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    ContactData data = new ContactData();
                    data.id = contactId;
                    data.name = name;
                    data.number = phoneNumber;
                    list.add(data);
                }
                phones.close();
            }
            cursor.close();
            return list;
        }

        public static List<ContactData> getSIMContacts(Context context, List<ContactData> list, boolean bSort) {
        if(list == null)
            list = new ArrayList<ContactData>();

        ContentResolver resolver = context.getContentResolver();
        // 获取Sims卡联系人
        Uri uri = Uri.parse("content://icc/adn");
        Cursor phoneCursor = resolver.query(uri, null, null, null,null);
        //"sort_key asc");

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                for(int i=0; i<phoneCursor.getColumnCount(); i++){
                    String columnName = phoneCursor.getColumnName(i);
                    String value = phoneCursor.getString(i);
                    Log.e("","i: "+columnName+" value: "+value);
                }
                // 得到手机号码
//     String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                //Phone._ID,
//       Phone.DISPLAY_NAME, Phone.NUMBER, "number"
                //FIXME 2.2 与 4.0 不一样。
                int numberIndex = phoneCursor.getColumnIndex(Phone.NUMBER);
                if(numberIndex == -1){
                    numberIndex = phoneCursor.getColumnIndex("number"); // Android2.2
                }
                String phoneNumber = phoneCursor.getString(numberIndex);
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber)) {
                    continue;
                }
                // 得到联系人名称
                int nameIndex = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME);
                if(nameIndex == -1)
                    nameIndex = phoneCursor.getColumnIndex("name");// Android2.2
                String contactName = phoneCursor.getString(nameIndex);//phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);

                //Sim卡中没有联系人头像
                ContactData data = new ContactData();
                data.setId(phoneCursor.getString(phoneCursor.getColumnIndex(Phone._ID))); //(phoneCursor.getString(PHONES_ID_INDEX));
                data.setContactName(contactName);
                data.setNumber(phoneNumber);

                list.add(data);
            }

            phoneCursor.close();
        }

        return list;
    }

    public static long insertSIMContact(Context context, ContactData contact)
    {
        ContentValues values = new ContentValues();
        Uri uri = Uri.parse("content://icc/adn");
        values.clear();
        values.put("tag", contact.getContactName());
        values.put("number", contact.getNumber());
        ContentResolver resolver = context.getContentResolver();
//        Uri newSimContactUri = resolver.insert(uri, values); //Android4.0
        ///////////////////////////////////////////////////////
        //for android 2.2
        Uri newSimContactUri = null;
        try{
            newSimContactUri = resolver.insert(uri, values);
        }
        catch(Exception e){
            e.printStackTrace();

//            values.clear();
////            values.put("name", contact.getContactName());
//            values.put("number", contact.getNumber());
//            newSimContactUri = resolver.insert(uri, values);
        }


        if(newSimContactUri != null){
            long id = ContentUris.parseId(newSimContactUri);
            contact.setId(id+"");
            return id;
        }
        else
            return -1;
    }

    public static boolean updateSIMContact(Context context,ContactData oldContact, ContactData newContact)
    {
        Uri uri = Uri.parse("content://icc/adn");
        ContentValues values = new ContentValues();
        values.put("tag", oldContact.getContactName());
        values.put("number", oldContact.getNumber());
        values.put("newTag", newContact.getContactName());
        values.put("newNumber", newContact.getNumber());
        int rc = context.getContentResolver().update(uri, values, null, null);
        if (rc > 0){
            return true;
        }
        else{
            return false;
        }
    }

    public static String hasName(Context context, String name) {
        String[] projection = { Data.RAW_CONTACT_ID};
        // 将自己添加到 msPeers 中
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                projection, // Which columns to return.
                Data.DISPLAY_NAME + " =?", // WHERE clause.
                new String[]{name}, // WHERE clause value substitution
                null); // Sort order.
        if(cursor!=null)
        { if(cursor.getCount()==0)
        {return "0";}
        else {
            cursor.moveToFirst();
            Logger.i("raw_contact_id",cursor.getString(0));
            return cursor.getCount()-1+"";}}else return "0";

    }
    public static String getId(Context context, String name){
        String[] projection = { Data.RAW_CONTACT_ID};
        // 将自己添加到 msPeers 中
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                projection, // Which columns to return.
                Data.DISPLAY_NAME + " =?", // WHERE clause.
                new String[]{name}, // WHERE clause value substitution
                null); // Sort order.
        cursor.moveToFirst();
        return  cursor.getString(0);
    }
public static void updatePhoneContact(Context context,String name,ArrayList<String> numbers){
    String[] projection = { Data.RAW_CONTACT_ID};
    // 将自己添加到 msPeers 中
    Cursor cursor = context.getContentResolver().query(
            ContactsContract.Data.CONTENT_URI,
            projection, // Which columns to return.
            Data.DISPLAY_NAME + " =?", // WHERE clause.
            new String[]{name}, // WHERE clause value substitution
            null); // Sort order.
      if(cursor.moveToFirst()){
          ContentValues values = new ContentValues();
          for(int i=1;i<=numbers.size();i++)
          { values.clear();
          values.put(Data.RAW_CONTACT_ID,cursor.getString(0));
          values.put(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE);
          values.put(Phone.TYPE,Phone.TYPE_MOBILE);
          values.put(Phone.NUMBER,numbers.get(i-1));
         context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,values);}
      }else return;
}

    public static boolean insertPhoneContact(Context context, ContactData contact,ArrayList<String> numbers){
        /**
         * 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
         * 这时后面插入data表的依据，只有执行空值插入，才能使插入的联系人在通讯录里面可见
         */
        Uri rcUri;
        ContentValues values = new ContentValues();

//            ContentResolver resolver = context.getContentResolver();
        //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
       /* values.put(RawContacts.ACCOUNT_NAME, "null");
        values.put(RawContacts.ACCOUNT_TYPE, "null");*/
        Uri rawContactUri =context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        contact.setId(rawContactId+"");

        //往data表入姓名数据
        values.clear();
        values.put(Data.RAW_CONTACT_ID,rawContactId);
        values.put(Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);//内容类型
       // values.put(StructuredName.GIVEN_NAME,contact.getContactName());
        values.put(StructuredName.DISPLAY_NAME,contact.getContactName());
        rcUri = context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,values);
        for(int i=1;i<=numbers.size();i++)
        if(rcUri != null){
            //往data表入电话数据
            values.clear();
            values.put(Data.RAW_CONTACT_ID,rawContactId);
            values.put(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE);
            values.put(Phone.TYPE,Phone.TYPE_MOBILE);
            values.put(Phone.NUMBER,numbers.get(i-1));
            rcUri = context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,values);

        }

        return (rcUri != null);
    }

    public static boolean updatePhoneContact(Context context, ContactData contact){

        ContentValues values = new ContentValues();

        values.clear();
        values.put(ContactsContract.Contacts.DISPLAY_NAME, contact.getContactName());
        int rc1 = context.getContentResolver().update(RawContacts.CONTENT_URI,values,
                ContactsContract.Contacts._ID + "=?", new String[]{contact.getId()});

        values.clear();
        values.put(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE);
        values.put(Phone.NUMBER,contact.getNumber());
        int rc2 = context.getContentResolver().update(android.provider.ContactsContract.Data.CONTENT_URI,values,
                Data.RAW_CONTACT_ID + "=?", new String[]{contact.getId()+""});
        return (rc1>0 || rc2 >0) ? true : false;
    }

    /**根据contactId删除联系人数据*/
    public static int deletePhoneContact(Context context,String name, String contactId)
    {
        ContentResolver resolver = context.getContentResolver();
        int rc1 = 0, rc2 = 0;
        //删除data表中数据
        String where = ContactsContract.Data.CONTACT_ID + " =?";
        String[] whereparams = new String[] { contactId };
        rc1 = resolver.delete(ContactsContract.Data.CONTENT_URI, where, whereparams);

        //删除rawContact表中数据
        where = ContactsContract.RawContacts.CONTACT_ID + " =?";
        whereparams = new String[] { contactId };
        rc2 = resolver.delete(ContactsContract.RawContacts.CONTENT_URI, where, whereparams);

        return (rc1>0 && rc2>0) ? (rc1+rc2) : 0;
    }

    public static int deleteSIMContact(Context context,ContactData contact)throws Exception{
        Log.e("","deleteSIMContact name:"+contact.getContactName()+" id:"+contact.getId());
        Uri uri = Uri.parse("content://icc/adn");
        ContentResolver resolver = context.getContentResolver();
        int rc = resolver.delete(uri, "tag="+contact.getContactName()+" AND number="+contact.getNumber(),null);
        Log.e("", "rc2 = "+rc);
        return rc;
    }
    public void testUpdate(Context context)throws Exception{
        int id = 1;
        String phone = "999999";
        Uri uri = Uri.parse("content://com.android.contacts/data");//对data表的所有数据操作
        ContentResolver resolver =context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("data1", phone);
        resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/phone_v2",id+""});
    }
}
