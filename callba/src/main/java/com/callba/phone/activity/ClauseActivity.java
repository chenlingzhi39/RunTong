package com.callba.phone.activity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/16.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.clause,
        navigationId = R.drawable.press_back,
        toolbarTitle = R.string.private_clause
)
public class ClauseActivity extends BaseActivity {

    @InjectView(R.id.page)
    LinearLayout page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        TextView textView2=new TextView(this);
        textView2.setText("一．CALL吧服务提供发话人与受话人之间的私人语音或语音信息的传送。语音通信秘密受法律保护。我们绝不会公开、编辑或透露用户的语音或语音信息内容，除非有法律许可及国家有关公安、安全部门等要求。");
        textView2.setTextSize(15);
        page.addView(textView2);
        TextView textView1=new TextView(this);
        textView1.setText("二．关于承担风险和有限责任");
        textView1.setTextSize(15);
        textView1.getPaint().setFakeBoldText(true);
        page.addView(textView1);
        TextView textView3=new TextView(this);
        textView3.setText( "1.用户对使用语音服务个人承担责任。用户信息内容及其后果由用户负责。\n"+
                "2.注册用户都是绑定手机号，如遇骚扰请在CALL吧进行投诉。\n" +
                "3．由于互联网的网络状况、计算机操作系统、计算机病毒、黑客攻击、其他任何技术、互联网络、通信线路及相关的硬件原因造成CALL吧不能正常使用或通话质量问题，我们不承担任何责任。\n" +
                "4．因不可抗拒力而造成CALL吧不能使用的，我们不承担任何责任，不可抗拒力包括但不仅限于：地震、洪水、战争、军事行动、自然灾害、法律或政府政策等不可抗拒的事件。");
        textView3.setTextSize(15);
        page.addView(textView3);
        TextView textView4=new TextView(this);
        textView4.setText("三．关于用户管理");
        textView4.setTextSize(15);
        textView4.getPaint().setFakeBoldText(true);
        page.addView(textView4);
        TextView textView5=new TextView(this);
        textView5.setText( "用户不得利用CALL吧从事任何违反国家法律的活动。\n" +
                "1．用户须承诺不传输任何非法的、骚扰性的、中伤他人的、辱骂性的、恐吓性的、伤害性的、庸俗的，淫秽等语言及信息资料。\n" +
                "2．使用CALL吧服务不作非法用途。用户不能传输任何教唆他人构成犯罪行为的信息；不能传输涉及国家安全的信息；不能传输任何不符合当地法规、国家法律和国际法律的信息。\n" +
                "3．用户不得删除本软件及其他副本上一切关于版权的信息，对本软件进行反向工程，如反汇编、反编译等。若用户的行为不符合以上提到的服务条款，我们将作出独立判断立即取消用户服务。");
        textView5.setTextSize(15);
        page.addView(textView5);
        TextView textView6=new TextView(this);
        textView6.setText("四．关于通告");
        textView6.setTextSize(15);
        textView6.getPaint().setFakeBoldText(true);
        page.addView(textView6);
        TextView textView7=new TextView(this);
        textView7.setText( "    所有发给用户的通告都可通过用户聊天窗口发送服务条款的修改、服务变更、或其它重要事情都会以此形式进行。");
        textView7.setTextSize(15);
        page.addView(textView7);
        TextView textView8=new TextView(this);
        textView8.setText("五．关于法律");
        textView8.setTextSize(15);
        textView8.getPaint().setFakeBoldText(true);
        page.addView(textView8);
        TextView textView9=new TextView(this);
        textView9.setText( "    CALL吧语音及语音邮件服务条款与国家法律一致，如发生服务条款与法律条款有相抵触的内容，以法律条款为准。如国家有关部门发布新规定，导致本协议内容变更的，双方均同意按照变更后的规定履行本。\n" +
                "　　如果用户完全同意并接受所有条款，就可以开始申请成为CALL吧用户。如果用户下载并安装了CALL吧软件，表明已经接受了本协议。");
        textView9.setTextSize(15);
        page.addView(textView9);
    }


    @Override
    public void refresh(Object... params) {

    }

}
