package com.callba.phone.pay.alipay;

    public class PartnerConfig {

        // 合作商户ID。用签约支付宝账号登录ms.alipay.com后，在账户信息页面获取。
        public static final String PARTNER = "2088002606900030";
        // 商户收款的支付宝账号
        public static final String SELLER = "pay@callda.com";
        // 商户（RSA）私钥
        public static final String RSA_PRIVATE =
                "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANwdzZJh23nuLVL47RxKZuzvQPzGSgQmBBK79uVtHiO5nuXmTxX7eZpEH7p86JPC+ipw1wdyr9A4qLNP6DHpa3Pm7+1EhYDvjemzQ3UFz8eoEIOCEsisUlbARiM/cJDR7k2WgIC8uB9GraETdutway0xJ/w/QxqP4GCXIg3X3YYjAgMBAAECgYAlUyRtwww9c8bZv/4tAuzLFpL1igY5B90+9AKcytEGi85G+7PDbX4kS5L5w36It9JF6hZ8W9U2QLVSmqUr+YHPuAKM5r7NcnsPPi4teT3F1OZ9BeT5NfcCxd0B37KNzeAkh5nS7w4LJFMcHqht5octAotfWGwhzaJqNpz9YkSC4QJBAO6L31WdoebTkODU/LEN45WMxJP1Cg5fEuajcAHdLLijhAsWmeb/OyhuoQt3+QEbjzTtnayShVlmS2QuRJ8wg9MCQQDsOLnAp/b2SZGTz0x18ltuB5VFp0SBDtJ+Enov3AMhSXEdbmeKTXgjuQ7fL7OX5hl3EhT0VCgqVPO//1+Mh5JxAkEA06xWTziDM7pEct1MJSg37LpurQPhGoOO2A3rFBxY1LZ0MwqpImsU6XnRecXJtQvpdYPsR/f0UDIPcCQGTGPWdQJAOT699Sw/Mww9FT7lXqfu9EkuKYURmt0Gsbq0laiRsZ1kpic9PEzt0mpkaj3bHfKLNUnSfitu2GNh9qNiemHYYQJBAKZ9izYb5E9f6tr55BwPPFymlsH4RcW1fMU/I0BOeG0mbygSW9wBphG+8wKykZ0/t68xRY745PEgJJu0g4XjA4A=";
        // 支付宝（RSA）公钥 用签约支付宝账号登录ms.alipay.com后，在密钥管理页面获取。
        public static final String RSA_ALIPAY_PUBLIC =
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
        // 支付宝安全支付服务apk的名称，必须与assets目录下的apk名称一致
        public static final String ALIPAY_PLUGIN_NAME = "alipay_plugin_20120428msp.apk";


}
