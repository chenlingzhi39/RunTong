package com.callba.phone.manager;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.callba.R;
import com.callba.phone.bean.Contact;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/6/25.
 */
public class ContactsManager {
    private ContentResolver contentResolver;
    private static final String TAG = "ContactsManager";

    /**
     * Use a simple string represents the long.
     */
    private static final String COLUMN_CONTACT_ID =
            ContactsContract.Data.CONTACT_ID;
    private static final String COLUMN_RAW_CONTACT_ID =
            ContactsContract.Data.RAW_CONTACT_ID;
    private static final String COLUMN_MIMETYPE =
            ContactsContract.Data.MIMETYPE;
    private static final String COLUMN_NAME =
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
    private static final String COLUMN_NUMBER =
            ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String COLUMN_NUMBER_TYPE =
            ContactsContract.CommonDataKinds.Phone.TYPE;
    private static final String COLUMN_EMAIL =
            ContactsContract.CommonDataKinds.Email.DATA;
    private static final String COLUMN_EMAIL_TYPE =
            ContactsContract.CommonDataKinds.Email.TYPE;
    private static final String MIMETYPE_STRING_NAME =
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    private static final String MIMETYPE_STRING_PHONE =
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    private static final String MIMETYPE_STRING_EMAIL =
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;

    public ContactsManager(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    /**
     * Search and fill the contact information by the contact name given.
     * @param contact Only the name is necessary.
     */
    public Contact searchContact(String name) {
        Log.w(TAG, "**search start**");
        Contact contact = new Contact();
        contact.setName(name);
        Log.d(TAG, "search name: " + contact.getName());
        String id = getContactID(contact.getName());
        contact.setId(id);

        if(id.equals("0")) {
            Log.d(TAG, contact.getName() + " not exist. exit.");
        } else {
            Log.d(TAG, "find id: " + id);
            //Fetch Phone Number
            Cursor cursor = contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{COLUMN_NUMBER, COLUMN_NUMBER_TYPE},
                    COLUMN_CONTACT_ID + "='" + id + "'", null, null);
            while(cursor.moveToNext()) {
                contact.setNumber(cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER)));
                contact.setNumberType(cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER_TYPE)));
                Log.d(TAG, "find number: " + contact.getNumber());
                Log.d(TAG, "find numberType: " + contact.getNumberType());
            }
            //cursor.close();

            //Fetch email
            cursor = contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{COLUMN_EMAIL, COLUMN_EMAIL_TYPE},
                    COLUMN_CONTACT_ID + "='" + id + "'", null, null);
            while(cursor.moveToNext()) {
                contact.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                contact.setEmailType(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL_TYPE)));
                Log.d(TAG, "find email: " + contact.getEmail());
                Log.d(TAG, "find emailType: " + contact.getEmailType());
            }
            cursor.close();
        }
        Log.w(TAG, "**search end**");
        return contact;
    }

    /**
     *
     * @param contact The contact who you get the id from. The name of
     * the contact should be set.
     * @return 0 if contact not exist in contacts list. Otherwise return
     * the id of the contact.
     */
    public  String getContactID(String name) {
        String id = "0";
        Cursor cursor = contentResolver.query(
                android.provider.ContactsContract.Contacts.CONTENT_URI,
                new String[]{android.provider.ContactsContract.Contacts._ID},
                android.provider.ContactsContract.Contacts.DISPLAY_NAME +
                        "='" + name + "'", null, null);
        if(cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(
                    android.provider.ContactsContract.Contacts._ID));
        }
        return id;
    }

    /**
     * You must specify the contact's ID.
     * @param contact
     * @throws Exception The contact's name should not be empty.
     */
    public void addContact(Contact contact) {
        Log.w(TAG, "**add start**");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        String id = getContactID(contact.getName());
        if(!id.equals("0")) {
            Log.d(TAG, "contact already exist. exit.");
        } else if(contact.getName().trim().equals("")){
            Log.d(TAG, "contact name is empty. exit.");
        } else {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                    .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_NAME)
                    .withValue(COLUMN_NAME, contact.getName())
                    .build());
            Log.d(TAG, "add name: " + contact.getName());

            if(!contact.getNumber().trim().equals("")) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                        .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_PHONE)
                        .withValue(COLUMN_NUMBER, contact.getNumber())
                        .withValue(COLUMN_NUMBER_TYPE, contact.getNumberType())
                        .build());
                Log.d(TAG, "add number: " + contact.getNumber());
            }

            if(!contact.getEmail().trim().equals("")) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                        .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_EMAIL)
                        .withValue(COLUMN_EMAIL, contact.getEmail())
                        .withValue(COLUMN_EMAIL_TYPE, contact.getEmailType())
                        .build());
                Log.d(TAG, "add email: " + contact.getEmail());
            }

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                Log.d(TAG, "add contact success.");
            } catch (Exception e) {
                Log.d(TAG, "add contact failed.");
                Log.e(TAG, e.getMessage());
            }
        }
        Log.w(TAG, "**add end**");

    }

    /**
     * Delete contacts who's name equals contact.getName();
     * @param contact
     */
    public void deleteContact(Contact contact) {
        Log.w(TAG, "**delete start**");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        String id = getContactID(contact.getName());
        //delete contact
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID+"="+id, null)
                .build());
        //delete contact information such as phone number,email
        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(COLUMN_CONTACT_ID + "=" + id, null)
                .build());
        Log.d(TAG, "delete contact: " + contact.getName());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            Log.d(TAG, "delete contact success");
        } catch (Exception e) {
            Log.d(TAG, "delete contact failed");
            Log.e(TAG, e.getMessage());
        }
        Log.w(TAG, "**delete end**");
    }

    /**
     * @param contactOld The contact wants to be updated. The name should exists.
     * @param contactNew
     */
    public void updateContact(Contact contactOld, Contact contactNew) {
        Log.w(TAG, "**update start**");
        String id = getContactID(contactOld.getName());
        if(id.equals("0")) {
            Log.d(TAG, contactOld.getName()+" not exist.");
        } else if(contactNew.getName().trim().equals("")){
            Log.d(TAG, "contact name is empty. exit.");
        } else if(!getContactID(contactNew.getName()).equals("0")){
            Log.d(TAG, "new contact name already exist. exit.");
        } else {

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            //update name
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                            new String[]{id, MIMETYPE_STRING_NAME})
                    .withValue(COLUMN_NAME, contactNew.getName())
                    .build());
            Log.d(TAG, "update name: " + contactNew.getName());

            //update number
            if(!contactNew.getNumber().trim().equals("")) {
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                                new String[]{id, MIMETYPE_STRING_PHONE})
                        .withValue(COLUMN_NUMBER, contactNew.getNumber())
                        .withValue(COLUMN_NUMBER_TYPE, contactNew.getNumberType())
                        .build());
                Log.d(TAG, "update number: " + contactNew.getNumber());
            }

            //update email if mail
            if(!contactNew.getEmail().trim().equals("")) {
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                                new String[]{id, MIMETYPE_STRING_EMAIL})
                        .withValue(COLUMN_EMAIL, contactNew.getEmail())
                        .withValue(COLUMN_EMAIL_TYPE, contactNew.getEmailType())
                        .build());
                Log.d(TAG, "update email: " + contactNew.getEmail());
            }

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                Log.d(TAG, "update success");
            } catch (Exception e) {
                Log.d(TAG, "update failed");
                Log.e(TAG, e.getMessage());
            }
        }
        Log.w(TAG, "**update end**");
    }
    public static Bitmap getAvatar(Context context,String id,boolean is_high){
        ContentResolver cr = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                Long.parseLong(id));
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri,is_high);
        Bitmap contactPhoto = BitmapFactory.decodeStream(input);
            return contactPhoto;

    }

}

