import com.gome.keychain.base.bean.AccountInfo;

/**
 * Created by wty on 2017/11/21.
 * This specifies the contract between the view and the presenter.
 */

public interface AddEditAccountContract {

    interface View {

        void showSelectAppDialog();

        void showGeneratePasswordDialog();

        void showTitleEmptyToast();

        void updateAccountSuccess();

        void updateAccountFailed();

        void addAccountSuccess();

        void addAccountFailed();

        void changeRightButtonStatus();

        void discardChange();

    }

    interface Presenter {

        /**
         * 放弃更改,并返回上一页
         */
        void discardChange();

        /**
         * 添加关联应用
         */
        void addRelativeApplication();

        /**
         * 生成随机密码
         */
        void generatePassword();

        /**
         * 添加账户
         *
         * @param accountInfo
         * @return
         */
        void addAccount(AccountInfo accountInfo);

        /**
         * 更新账户信息
         * @param accountInfo
         */
        void update(AccountInfo accountInfo);


        void handleIntentFromAutoFillDialog();

        void handleIntentFromDetail();

    }


}