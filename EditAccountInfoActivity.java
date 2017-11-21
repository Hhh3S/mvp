import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.keychain.base.bean.AccountInfo;
import com.gome.keychain.presenter.AppInfoEngine;
import com.gome.keychain.base.bean.EventBean;
import com.gome.keychain.base.constants.GMConstants;
import com.gome.keychain.base.ui.BaseActivity;
import com.gome.keychain.model.bean.AppInfo;
import com.gome.keychain.presenter.AccountInfoEngine;
import com.gome.keychain.ui.R;
import com.gome.keychain.ui.addeditaccount.AddEditAccountContract;
import com.gome.keychain.ui.addeditaccount.AddEditAccountPresenter;
import com.gome.keychain.ui.view.GeneratePasswordDialog;
import com.gome.keychain.ui.view.SelectAppDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import gome.app.GomeAlertDialog;


public class EditAccountInfoActivity extends BaseActivity implements View.OnClickListener, AddEditAccountContract.View {

    private static final int COME_FROM_HOME = 1;
    private static final int COME_FROM_DIALOG = 2;
    private static final int COME_FROM_DETAIL = 3;

    private static final String TAG = EditAccountInfoActivity.class.getSimpleName();
    private Context mContext;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mDescriptionEditText;
    private ImageView mAppIconImageView;
    private EditText mAppNameEditText;
    private TextView mClickTipTextView;
    private View mGeneratePasswordItemView;

    private AppInfo mAppInfo;
    private AccountInfo mAccountInfo;
    private Map<String, String> sourceValues;
    //记录上一个Activity
    private int source;
    private AddEditAccountContract.Presenter mPresenter;

    public void setSource(int source) {
        this.source = source;
    }

    //完成按钮点击事件
    private View.OnClickListener mSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            boolean usernameNotEmpty = !TextUtils.isEmpty(mUsernameEditText.getText().toString());
            boolean passwordNotEmpty = !TextUtils.isEmpty(mPasswordEditText.getText().toString());
            final String appName = mAppNameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(appName)) {
                Toast.makeText(mContext, R.string.title_is_null, Toast.LENGTH_SHORT).show();
                return;
            }

            if (usernameNotEmpty && passwordNotEmpty && mAppInfo != null) {//信息完整

                if (mAccountInfo != null) {
                    //更新
                    mAccountInfo.setAppName(appName);
                    mAccountInfo.setPackageName(mAppInfo.getPackname());
                    mAccountInfo.setUserName(mUsernameEditText.getText().toString());
                    mAccountInfo.setPassword(mPasswordEditText.getText().toString());
                    mAccountInfo.setRemark(mDescriptionEditText.getText().toString());
                    mAccountInfo.setModifyDate(new Date());
                    //AccountInfoEngine.insertOrReplace(mAccountInfo, mContext);

                    mPresenter.update(mAccountInfo);

                } else {
                    //新增
                    mAccountInfo = new AccountInfo();
                    mAccountInfo.setAppName(appName);
                    mAccountInfo.setPackageName(mAppInfo.getPackname());
                    mAccountInfo.setUserName(mUsernameEditText.getText().toString());
                    mAccountInfo.setPassword(mPasswordEditText.getText().toString());
                    mAccountInfo.setRemark(mDescriptionEditText.getText().toString());
                    mAccountInfo.setCreateDate(new Date());

                    mPresenter.addAccount(mAccountInfo);

                }


            } else {//信息不完整,未关联App或者未填写用户名或者未填写密码

                new GomeAlertDialog.Builder(mContext)
                        .setTitle(R.string.confirm_to_save)
                        .setMessage(R.string.account_info_incomplete)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mAccountInfo != null) {
                                    //更新
                                    if (!TextUtils.isEmpty(appName) && !appName.equals(getString(R.string.add_installed_app))) {
                                        mAccountInfo.setAppName(appName);
                                    }
                                    if (mAppInfo != null) {
                                        mAccountInfo.setPackageName(mAppInfo.getPackname());
                                    }
                                    mAccountInfo.setUserName(mUsernameEditText.getText().toString());
                                    mAccountInfo.setPassword(mPasswordEditText.getText().toString());
                                    mAccountInfo.setRemark(mDescriptionEditText.getText().toString());
                                    mAccountInfo.setModifyDate(new Date());

                                    mPresenter.update(mAccountInfo);

                                } else {
                                    //新增
                                    mAccountInfo = new AccountInfo();
                                    if (!TextUtils.isEmpty(appName) && !appName.equals(getString(R.string.add_installed_app))) {
                                        //应用名称不为空,且被编辑过
                                        mAccountInfo.setAppName(appName);
                                    }
                                    if (mAppInfo != null) {
                                        mAccountInfo.setPackageName(mAppInfo.getPackname());
                                    }
                                    mAccountInfo.setUserName(mUsernameEditText.getText().toString());
                                    mAccountInfo.setPassword(mPasswordEditText.getText().toString());
                                    mAccountInfo.setRemark(mDescriptionEditText.getText().toString());
                                    mAccountInfo.setCreateDate(new Date());

                                    mPresenter.addAccount(mAccountInfo);

                                }


                            }
                        })
                        .setNegativeButton(R.string.continue_complete, null)
                        .show();
            }
        }
    };

    @Override
    protected void initFirst() {
        mContext = this;
    }

    @Override
    protected int setContentView() {
        return R.layout.activity_edit_account_info;
    }

    @Override
    protected void initView() {

        mAppIconImageView = (ImageView) findViewById(R.id.iv_app_icon);
        mClickTipTextView = (TextView) findViewById(R.id.tv_click_tip);
        mGeneratePasswordItemView = findViewById(R.id.item_generate_password);
        mAppNameEditText = (EditText) findViewById(R.id.et_app_name);
        mUsernameEditText = (EditText) findViewById(R.id.et_username);
        mPasswordEditText = (EditText) findViewById(R.id.et_password);
        mDescriptionEditText = (EditText) findViewById(R.id.et_description);
    }

    /**
     * SingleTask处理
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(GMConstants.INTENT_EXTRA_PACKAGE_NAME)) {
            //从填充密码悬浮窗跳转过来,携带包名信息
            setSource(COME_FROM_DIALOG);
            String packageName = intent.getStringExtra(GMConstants.INTENT_EXTRA_PACKAGE_NAME);
            mAppInfo = AppInfoEngine.getAppInfoByPackageName(packageName);
            if (mAppInfo != null) {
                mAppIconImageView.setImageDrawable(mAppInfo.getIcon());
                mAppNameEditText.setText(mAppInfo.getName());
                mAppNameEditText.setSelection(mAppInfo.getName().length());
                mClickTipTextView.setText(R.string.tip_to_replace_app);
            }
            //初始化容器,保存进入该页面时各个EditText的原始值
            sourceValues = new HashMap<>();
            sourceValues.put("appName", mAppNameEditText.getText().toString());
            sourceValues.put("username", mUsernameEditText.getText().toString());
            sourceValues.put("password", mPasswordEditText.getText().toString());
            sourceValues.put("remark", mDescriptionEditText.getText().toString());
            changeRightButtonStatus();
        }
    }

    @Override
    protected void initData() {

        mPresenter = new AddEditAccountPresenter(this, this);

        setTitle(getString(R.string.edit));
        setTitleCenter();
        setLeftBtn(R.mipmap.ic_close, new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPresenter.discardChange();
            }
        });

        setSource(COME_FROM_HOME);
        setRightBtn(getString(R.string.action_done), mSaveClickListener);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(GMConstants.INTENT_EXTRA_PACKAGE_NAME)) {
                //从填充密码悬浮窗跳转过来,携带包名信息
                mPresenter.handleIntentFromAutoFillDialog();
                setSource(COME_FROM_DIALOG);
                String packageName = intent.getStringExtra(GMConstants.INTENT_EXTRA_PACKAGE_NAME);
                mAppInfo = AppInfoEngine.getAppInfoByPackageName(packageName);
                if (mAppInfo != null) {
                    mAppIconImageView.setImageDrawable(mAppInfo.getIcon());
                    mAppNameEditText.setText(mAppInfo.getName());
                    mAppNameEditText.setSelection(mAppInfo.getName().length());
                    mClickTipTextView.setText(R.string.tip_to_replace_app);
                }
            } else if (intent.hasExtra(GMConstants.INTENT_EXTRA_ACCOUNT_INFO_ID)) {
                //从账户详情页跳转过来,携带AccountInfo的ID信息
                setSource(COME_FROM_DETAIL);
                long id = intent.getLongExtra(GMConstants.INTENT_EXTRA_ACCOUNT_INFO_ID, 0);
                mAccountInfo = AccountInfoEngine.selectById(id, this);
                if (mAccountInfo != null) {
                    //app info
                    mAppInfo = AppInfoEngine.getAppInfoByPackageName(mAccountInfo.getPackageName());
                    if (!TextUtils.isEmpty(mAccountInfo.getPackageName()) && mAppInfo != null) {
                        //have related to app && find the app on system
                        mClickTipTextView.setText(R.string.tip_to_replace_app);
                        mAppIconImageView.setImageDrawable(mAppInfo.getIcon());
                    } else {
                        mAppIconImageView.setImageResource(R.drawable.gome_pic_add_app);
                        mClickTipTextView.setText(R.string.click_to_add_app);

                    }
                    //app name
                    mAppNameEditText.setText(mAccountInfo.getAppName());
                    mAppNameEditText.setSelection(mAccountInfo.getAppName().length());
                    //username
                    mUsernameEditText.setText(mAccountInfo.getUserName());
                    //password
                    mPasswordEditText.setText(mAccountInfo.getPassword());
                    //remark
                    mDescriptionEditText.setText(mAccountInfo.getRemark());

                }

            }

            //初始化容器,保存进入该页面时各个EditText的原始值
            sourceValues = new HashMap<>();
            sourceValues.put("appName", mAppNameEditText.getText().toString());
            sourceValues.put("username", mUsernameEditText.getText().toString());
            sourceValues.put("password", mPasswordEditText.getText().toString());
            sourceValues.put("remark", mDescriptionEditText.getText().toString());

        }

        changeRightButtonStatus();

    }

    @Override
    protected void initListener() {
        mAppIconImageView.setOnClickListener(this);
        mGeneratePasswordItemView.setOnClickListener(this);
        mAppNameEditText.addTextChangedListener(mTextWatcher);
        mUsernameEditText.addTextChangedListener(mTextWatcher);
        mPasswordEditText.addTextChangedListener(mTextWatcher);
        mDescriptionEditText.addTextChangedListener(mTextWatcher);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() >= 100) {
                Toast.makeText(mContext, R.string.et_max_length, Toast.LENGTH_SHORT).show();
            }
            changeRightButtonStatus();
        }
    };

    @Override
    protected void initOther() {

    }

    @Override
    public void onBackPressed() {
        mPresenter.discardChange();
    }

    @Override
    public void showSelectAppDialog() {
        SelectAppDialog selectAppDialog = new SelectAppDialog();
        selectAppDialog.setOnItemClickListener(new SelectAppDialog.onItemClickListener() {
            @Override
            public void onItemClick(AppInfo appInfo) {
                mAppInfo = appInfo;
                //appName没有被编辑过
                mAppNameEditText.setText(mAppInfo.getName());
                mAppNameEditText.setSelection(mAppInfo.getName().length());
                mClickTipTextView.setText(R.string.tip_to_replace_app);
                mAppIconImageView.setImageDrawable(mAppInfo.getIcon());
                changeRightButtonStatus();
            }
        });
        selectAppDialog.show(getFragmentManager(), "selectApp");
    }

    @Override
    public void showGeneratePasswordDialog() {
        //if the generate password dialog is showing then return
        GeneratePasswordDialog generatePasswordDialog = new GeneratePasswordDialog();
        generatePasswordDialog.setOnPasswordBackListener(new GeneratePasswordDialog.onPasswordBackListener() {
            @Override
            public void onPasswordBack(String password) {
                if (mPasswordEditText != null && !TextUtils.isEmpty(password)) {
                    mPasswordEditText.setText(password);
                    mPasswordEditText.setSelection(password.length());
                }
            }
        });
        generatePasswordDialog.show(getFragmentManager(), "generatePassword");
    }

    @Override
    public void showTitleEmptyToast() {

    }

    @Override
    public void updateAccountSuccess() {
        EventBus.getDefault().post(new EventBean(GMConstants.EVENT_HOME_LIST_UPDATE_ITEM, mAccountInfo));
        Toast.makeText(mContext, R.string.changes_saved, Toast.LENGTH_SHORT).show();
        Intent data = new Intent();
        data.putExtra(GMConstants.INTENT_EXTRA_ACCOUNT_INFO_ID, mAccountInfo.getId());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void updateAccountFailed() {

    }

    @Override
    public void addAccountSuccess() {
        EventBus.getDefault().post(new EventBean(GMConstants.EVENT_HOME_LIST_INSERT_ITEM, mAccountInfo));
        Toast.makeText(mContext, R.string.account_saved, Toast.LENGTH_SHORT).show();
        finishAndStartToDetail(mAccountInfo.getId());
    }

    @Override
    public void addAccountFailed() {
        Toast.makeText(mContext, R.string.account_save_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void changeRightButtonStatus() {

        //如果是编辑 判断是否被编辑过控制完成按钮的颜色

        //如果是添加 判断mAppNameEditText mUsernameEditText mPasswordEditText 是否为空来控制按钮的颜色

        boolean canSave = false;

        if (source == COME_FROM_HOME || source == COME_FROM_DIALOG) {

            canSave = !TextUtils.isEmpty(mAppNameEditText.getText().toString())
                    || !TextUtils.isEmpty(mUsernameEditText.getText().toString())
                    || !TextUtils.isEmpty(mPasswordEditText.getText().toString());

        } else if (source == COME_FROM_DETAIL) {

            canSave = isEdited();

        }

        if (canSave) {
            setRightBtnColor(getColor(R.color.blue_color));
            setRightBtnClickable(true);
        } else {
            setRightBtnColor(getColor(R.color.btn_color));
            setRightBtnClickable(false);
        }

    }

    @Override
    public void discardChange() {
        if (isEdited()) {
            new GomeAlertDialog.Builder(mContext)
                    .setTitle(R.string.quit_edit)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditAccountInfoActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            EditAccountInfoActivity.super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        if (isFastDoubleClick()) {
            return;
        }
        //点击AppIcon的位置,弹出选择app的对话框
        if (v.getId() == R.id.iv_app_icon) {
            mPresenter.addRelativeApplication();
        } else if (v.getId() == R.id.item_generate_password) {
            mPresenter.generatePassword();
        }
    }

    /**
     * 检查页面上的元素是否被编辑过
     * 如果没有被编辑过,点击左上角的X或者返回键返回的时候不弹出对话款提示
     *
     * @return
     */
    private boolean isEdited() {

        if (sourceValues == null) {
            return false;
        } else {
            String appName = sourceValues.get("appName");
            String username = sourceValues.get("username");
            String password = sourceValues.get("password");
            String remark = sourceValues.get("remark");
            return !mAppNameEditText.getText().toString().equals(appName) ||
                    !mUsernameEditText.getText().toString().equals(username) ||
                    !mPasswordEditText.getText().toString().equals(password) ||
                    !mDescriptionEditText.getText().toString().equals(remark);
        }
    }

    /**
     * 结束当前页,跳转到详情页
     */
    private void finishAndStartToDetail(long accountInfoId) {
        switch (source) {
            case COME_FROM_DETAIL:
                break;
            case COME_FROM_DIALOG:
            case COME_FROM_HOME://跳转到详情页
                Intent intent = new Intent(this, AccountInfoDetailActivity.class);
                intent.putExtra(GMConstants.INTENT_EXTRA_ACCOUNT_INFO_ID, accountInfoId);
                startActivity(intent);
                break;
        }
        finish();
    }
}