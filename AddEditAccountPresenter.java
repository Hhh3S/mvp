import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gome.keychain.base.bean.AccountInfo;
import com.gome.keychain.base.bean.EventBean;
import com.gome.keychain.base.constants.GMConstants;
import com.gome.keychain.model.manager.AccountInfoManager;
import com.gome.keychain.presenter.AccountInfoEngine;
import com.gome.keychain.ui.R;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by wty on 2017/11/21.
 */

public class AddEditAccountPresenter implements AddEditAccountContract.Presenter {


    private static final int COME_FROM_HOME = 1;
    private static final int COME_FROM_DIALOG = 2;
    private static final int COME_FROM_DETAIL = 3;

    private Context mContext;
    private AddEditAccountContract.View mAddEditAccountView;

    public AddEditAccountPresenter(Context context, AddEditAccountContract.View addEditAccountView) {
        mContext = context;
        mAddEditAccountView = addEditAccountView;
    }

    @Override
    public void discardChange() {
        mAddEditAccountView.discardChange();
    }

    @Override
    public void addRelativeApplication() {
        mAddEditAccountView.showSelectAppDialog();
    }

    @Override
    public void generatePassword() {
        mAddEditAccountView.showGeneratePasswordDialog();
    }

    @Override
    public void addAccount(AccountInfo accountInfo) {
        long insertOrReplace = AccountInfoEngine.insertOrReplace(accountInfo, mContext);
        if (insertOrReplace > 0) {
            mAddEditAccountView.addAccountSuccess();
        } else {
            mAddEditAccountView.addAccountFailed();
        }
    }

    @Override
    public void update(AccountInfo accountInfo) {
        long insertOrReplace = AccountInfoEngine.insertOrReplace(accountInfo, mContext);
        if (insertOrReplace > 0) {
            mAddEditAccountView.updateAccountSuccess();
        } else {
            mAddEditAccountView.updateAccountFailed();
        }
    }

    @Override
    public void handleIntentFromAutoFillDialog() {

    }

    @Override
    public void handleIntentFromDetail() {

    }

}