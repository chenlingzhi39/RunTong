package com.callba.phone.logic.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.callba.phone.Constant;
import com.callba.phone.DemoHelper;
import com.callba.phone.MyApplication;
import com.callba.phone.bean.BaseUser;
import com.callba.phone.bean.ContactMultiNumBean;
import com.callba.phone.bean.EaseUser;
import com.callba.phone.bean.Friend;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.manager.UserManager;
import com.callba.phone.util.EaseCommonUtils;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * 联系人业务逻辑管理
 *
 * @Author zhw
 * @Version V1.0
 * @Createtime：2014年5月23日 上午11:58:00
 */
public class ContactController {
    private static final String[] SEARCH_LETTER_ARRAY =
            {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                    "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                    "U", "V", "W", "X", "Y", "Z", "#"};

    //所有的联系人集合
    private List<ContactPersonEntity> mAllContactPersonEntities;
    private List<ContactMultiNumBean> personEntities;
    //检索的字母、位置索引表
    private Map<String, Integer> letterSearchMap;
    private Gson gson;
    private Context contaxt;

    public ContactController() {
        mAllContactPersonEntities = GlobalConfig.getInstance().getContactBeans();
        gson = new Gson();
        contaxt = MyApplication.getInstance().getApplicationContext();
    }

    /**
     * 获取ListView列表的搜索索引
     *
     * @return
     */
    public Map<String, Integer> getSearchMap() {
        if (letterSearchMap == null)
            sortContactByLetter(personEntities);

        return letterSearchMap;
    }

    /**
     * 获取用于ListView显示的数据(默认数据为当前联系人)
     *
     * @return
     */
    public List<ContactEntity> getFilterListContactEntities() {
        return sortContactByLetter(personEntities);
    }

    /**
     * 获取用于ListView显示的数据(默认数据为当前联系人，根据姓名排列，多个号码只显示一条数据)
     *
     * @return
     */
    public synchronized List<ContactMultiNumBean> getFilterListContactEntitiesNoDuplicate() {
        personEntities = new ArrayList<>();
        String phoneNumbers = "";
        List<String> contactPhones = new ArrayList<>();
        List<Friend> friends=new ArrayList<>();
        Logger.i("contact_size", mAllContactPersonEntities.size() + "");
        for (int i = 0; i < mAllContactPersonEntities.size(); i++) {
           /* if(i==mAllContactPersonEntities.size())
                phoneNumbers +=  Pattern.compile("[^0-9]").matcher(mAllContactPersonEntities.get(i).getPhoneNumber()).replaceAll("");
            else
            phoneNumbers +=  Pattern.compile("[^0-9]").matcher(mAllContactPersonEntities.get(i).getPhoneNumber()).replaceAll("")+",";*/
            friends.add(new Friend(mAllContactPersonEntities.get(i).getDisplayName(),Pattern.compile("[^0-9]").matcher(mAllContactPersonEntities.get(i).getPhoneNumber()).replaceAll("")));

            if (i == 0) {
                personEntities.add(new ContactMultiNumBean(mAllContactPersonEntities.get(0)));
                contactPhones.add(mAllContactPersonEntities.get(0).getPhoneNumber());
                personEntities.get(0).setContactPhones(contactPhones);
                continue;
            }
            if (!mAllContactPersonEntities.get(i).get_id().equals(mAllContactPersonEntities.get(i - 1).get_id())) {
                contactPhones = new ArrayList<>();
                contactPhones.add(mAllContactPersonEntities.get(i).getPhoneNumber());
                personEntities.add(new ContactMultiNumBean(mAllContactPersonEntities.get(i)));
            } else {
                contactPhones.add(mAllContactPersonEntities.get(i).getPhoneNumber());
            }
            personEntities.get(personEntities.size() - 1).setContactPhones(contactPhones);
        }
//        Logger.i("phoneNumbers", phoneNumbers);
//        Logger.i("add_url", Interfaces.ADD_FRIENDS + "?loginName=" + UserManager.getUsername(contaxt) + "&loginPwd=" + UserManager.getPassword(contaxt) + "&phoneNumbers=" + phoneNumbers);
        //FileUtils.writeObjectToFile(StorageUtils.getFilesDirectory(contaxt)+File.separator+"contact.txt",phoneNumbers);
        if(friends.size()>0)
        OkHttpUtils
                .post()
                .url(Interfaces.ADD_FRIENDS)
                .addParams("loginName", UserManager.getUsername(contaxt))
                .addParams("loginPwd", UserManager.getPassword(contaxt))
                .addParams("phoneNumbers", gson.toJson(friends))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("add_results", response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        OkHttpUtils
                                .post()
                                .url(Interfaces.GET_FRIENDS)
                                .addParams("loginName", UserManager.getUsername(contaxt))
                                .addParams("loginPwd", UserManager.getPassword(contaxt))
                                .build().execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                try {
                                    Logger.i("get_result", response);
                                    String[] result = response.split("\\|");
                                    if (result[0].equals("0")) {
                                        DemoHelper.getInstance().getContactList();
                                        ArrayList<BaseUser> list;
                                        list = gson.fromJson(result[1], new TypeToken<List<BaseUser>>() {
                                        }.getType());
                                        List<EaseUser> mList = new ArrayList<>();
                                        for (BaseUser baseUser : list) {
                                            EaseUser user = new EaseUser(baseUser.getPhoneNumber() + "-callba");
                                            user.setAvatar(baseUser.getUrl_head());
                                            user.setNick(baseUser.getNickname());
                                            user.setSign(baseUser.getSign());
                                            EaseCommonUtils.setUserInitialLetter(user);
                                            mList.add(user);
                                        }
                                        DemoHelper.getInstance().updateContactList(mList);
                                        LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(new Intent(Constant.ACTION_CONTACT_CHANAGED));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

		/*for(ContactPersonEntity contactPersonEntity : mAllContactPersonEntities) {

			if(!TextUtils.isEmpty(lastName) && lastName.equals(contactPersonEntity.getDisplayName())) {
				Logger.i("name_number",contactPersonEntity.getDisplayName()+contactPersonEntity.getPhoneNumber());
				if(personEntities.size() > 1) {

					List<String> contactPhones = personEntities.get(personEntities.size() - 1).getContactPhones();
					contactPhones.add(contactPersonEntity.getPhoneNumber());
				}
				continue;
			}

			lastName = contactPersonEntity.getDisplayName();
			
			personEntities.add(new ContactMultiNumBean(contactPersonEntity));
		}*/
        //FileUtils.writeObjectToFile(StorageUtils.getFilesDirectory(contaxt) + File.separator + "contacts.txt", gson.toJson(personEntities));
        return personEntities;
    }

    /**
     * 获取用于ListView显示的数据
     *
     * @param contactPersonEntities
     * @return
     */
    public List<ContactEntity> getFilterListContactEntities(List<ContactPersonEntity> contactPersonEntities) {
        return sortContactByLetter(contactPersonEntities);
    }

    /**
     * 根据关键字检索联系人（模糊检索手机号码和名字）
     *
     * @param searchWord
     * @return
     */
    public List<ContactEntity> searchContact(String searchWord, List<ContactEntity> searchSrcEntities) {
        if (TextUtils.isEmpty(searchWord)) {
            sortContactByLetter(personEntities);
            return searchSrcEntities;
        }

        //检索到的联系人
        List<ContactPersonEntity> searchedPersonEntities = new ArrayList<ContactPersonEntity>();

        for (ContactEntity contactEntity : searchSrcEntities) {
            if (contactEntity.getType() == ContactEntity.CONTACT_TYPE_CONTACT) {
                ContactPersonEntity personEntity = (ContactPersonEntity) contactEntity;

                if (personEntity.getDisplayName() == null
                        || personEntity.getPhoneNumber() == null) {
                    continue;
                }

                if (personEntity.getDisplayName().contains(searchWord)
                        || personEntity.getPhoneNumber().contains(searchWord)) {
                    searchedPersonEntities.add(personEntity);
                }
            }
        }

        return sortContactByLetter(searchedPersonEntities);
    }


    /**
     * 联系人按字母排序
     *
     * @author zhw
     */
    public List<ContactEntity> sortContactByLetter(List<? extends ContactPersonEntity> personEntities) {
        List<ContactEntity> entities = new ArrayList<ContactEntity>();
        letterSearchMap = new HashMap<String, Integer>();
        if (personEntities == null)
            return entities;
        if (personEntities.isEmpty()) {
            return entities;
        }
        for (String letter : SEARCH_LETTER_ARRAY) {
            boolean isLetterAdd = false;
            for (int i = 0; i < personEntities.size(); i++) {
                ContactPersonEntity contactPersonEntity = personEntities.get(i);
                if (letter.equals(contactPersonEntity.getTypeName())) {
                    if (!isLetterAdd) {
                        //添加字母索引
                        ContactIndexEntity contactIndexEntity = new ContactIndexEntity();
                        contactIndexEntity.setIndexName(letter);
                        entities.add(contactIndexEntity);

                        letterSearchMap.put(letter, entities.size() - 1);

                        isLetterAdd = true;
                    }
                    entities.add(contactPersonEntity);
                }
            }
        }

        return entities;
    }
}
 