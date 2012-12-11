package com.moneydesktop.finance.tablet.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.BaseTabletFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage.ParentAnimationEvent;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.views.AmazingListView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;

import de.greenrobot.event.EventBus;

import java.util.List;

public class TransactionsTabletFragment extends BaseTabletFragment implements OnItemClickListener {
	
	public final String TAG = this.getClass().getSimpleName();
	
	private static TransactionsTabletFragment sFragment;

	private AmazingListView mTransactionsList;
	private TransactionsTabletAdapter mAdapter;
	private RelativeLayout mLoading;
	
	private boolean mLoaded = false;
	private boolean mWaiting = false;
	
	public static TransactionsTabletFragment newInstance() {
			
		sFragment = new TransactionsTabletFragment();
	
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
        this.mActivity.onFragmentAttached();
	}
    
    @Override
    public void onPause() {
        super.onPause();
        
        EventBus.getDefault().unregister(this);
    }
	
	@Override
	public void onResume() {
		super.onResume();

        this.mActivity.updateNavBar(getFragmentTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_transactions_view, null);

		mLoading = (RelativeLayout) mRoot.findViewById(R.id.loading);
		
		mTransactionsList = (AmazingListView) mRoot.findViewById(R.id.transactions);
		mTransactionsList.setOnItemClickListener(this);
		
		if (!mLoaded)
			getInitialTransactions();
		else
			setupList();
		
		return mRoot;
	}
	
	private void getInitialTransactions() {

		new AsyncTask<Integer, Void, Void>() {
			
			@Override
			protected Void doInBackground(Integer... params) {
				
				int page = params[0];

				List<Transactions> row1 = Transactions.getRows(page).second;

				mAdapter = new TransactionsTabletAdapter(mActivity, mTransactionsList, row1);
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {

			    mLoaded = true;
				setupList();
			};
			
		}.execute(1);
	}
	
	private void setupList() {

		mTransactionsList.setAdapter(mAdapter);
		mTransactionsList.setLoadingView(mActivity.getLayoutInflater().inflate(R.layout.loading_view, null));
		
		mAdapter.notifyMayHaveMorePages();
		
		if (!mWaiting) {
		    configureView();
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		Transactions transaction = (Transactions) parent.getItemAtPosition(position);
		
		if (transaction != null) {
			
//			TransactionDetailFragment detail = TransactionDetailFragment.newInstance(transaction.getId());
//			
//			FragmentTransaction ft = getFragmentManager().beginTransaction();
//			ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.out_right, R.anim.in_left);
//			ft.replace(R.id.fragment, detail);
//			ft.addToBackStack(null);
//			ft.commit();
		}
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_transactions);
	}

	private void configureView() {
        
		if (mLoaded && !mWaiting) {
			
		    animate(mLoading).alpha(0.0f).setDuration(400).setListener(new AnimatorListener() {
                
                public void onAnimationStart(Animator animation) {}
                
                public void onAnimationRepeat(Animator animation) {}
                
                public void onAnimationEnd(Animator animation) {
                    mLoading.setVisibility(View.GONE);
                }
                
                public void onAnimationCancel(Animator animation) {}
            });
            
            mTransactionsList.setVisibility(View.VISIBLE);
            animate(mTransactionsList).alpha(1.0f).setDuration(400);
		}
	}
	
	public void onEvent(ParentAnimationEvent event) {
		
	    if (!event.isOutAnimation() && !event.isFinished()) {
	        mWaiting = true;
	    }
	    
		if (event.isOutAnimation() && event.isFinished()) {
		    
		    mWaiting = false;
            
			configureView();
		}
	}
}
