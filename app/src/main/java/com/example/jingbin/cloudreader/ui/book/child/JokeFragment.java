package com.example.jingbin.cloudreader.ui.book.child;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;

import com.example.jingbin.cloudreader.MainActivity;
import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.adapter.JokeAdapter;
import com.example.jingbin.cloudreader.base.BaseFragment;
import com.example.jingbin.cloudreader.bean.wanandroid.DuanZiBean;
import com.example.jingbin.cloudreader.databinding.FragmentWanAndroidBinding;
import com.example.jingbin.cloudreader.databinding.HeaderItemJokeBinding;
import com.example.jingbin.cloudreader.utils.CommonUtils;
import com.example.jingbin.cloudreader.utils.DebugUtil;
import com.example.jingbin.cloudreader.viewmodel.wan.JokeViewModel;
import com.example.jingbin.cloudreader.viewmodel.wan.WanNavigator;
import com.example.xrecyclerview.XRecyclerView;

import java.util.List;
import java.util.Random;

/**
 * @author jingbin
 *         Updated by jingbin on 18/02/07.
 */
public class JokeFragment extends BaseFragment<FragmentWanAndroidBinding> implements WanNavigator.JokeNavigator {

    private static final String TYPE = "param1";
    private String mType = "综合";
    private boolean mIsPrepared;
    private boolean mIsFirst = true;
    private MainActivity activity;
    private JokeAdapter mAdapter;
    private HeaderItemJokeBinding headerBinding;
    private JokeViewModel viewModel;

    @Override
    public int setContent() {
        return R.layout.fragment_wan_android;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    public static JokeFragment newInstance(String param1) {
        JokeFragment fragment = new JokeFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(TYPE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showContentView();
        viewModel = new JokeViewModel(this);
        viewModel.setNavigator(this);
        initRefreshView();

        // 准备就绪
        mIsPrepared = true;
        /**
         * 因为启动时先走loadData()再走onActivityCreated，
         * 所以此处要额外调用load(),不然最初不会加载内容
         */
        loadData();
    }

    private void initRefreshView() {
        bindingView.srlBook.setColorSchemeColors(CommonUtils.getColor(R.color.colorTheme));
        bindingView.srlBook.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bindingView.srlBook.postDelayed(() -> {
                    bindingView.xrvBook.reset();
                    viewModel.setPage(1);
                    loadCustomData();
                }, 500);
            }
        });
        bindingView.xrvBook.setLayoutManager(new LinearLayoutManager(getActivity()));
        bindingView.xrvBook.setPullRefreshEnabled(false);
        bindingView.xrvBook.clearHeader();
        mAdapter = new JokeAdapter(getActivity());
        bindingView.xrvBook.setAdapter(mAdapter);
        headerBinding= DataBindingUtil.inflate(getLayoutInflater(), R.layout.header_item_joke, null, false);
        bindingView.xrvBook.addHeaderView(headerBinding.getRoot());

        bindingView.xrvBook.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                int page = viewModel.getPage();
                viewModel.setPage(++page);
                loadCustomData();
            }
        });
    }

    @Override
    protected void loadData() {
        DebugUtil.error("-----loadData");
        if (!mIsPrepared || !mIsVisible || !mIsFirst) {
            return;
        }

        bindingView.srlBook.setRefreshing(true);
        bindingView.srlBook.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadCustomData();
            }
        }, 500);
        DebugUtil.error("-----setRefreshing");
    }

    private void loadCustomData() {
        viewModel.showNhdzList();
//        viewModel.showQSBKList();
    }

    public void showNhdzData() {
        if (!bindingView.srlBook.isRefreshing()) {
            bindingView.srlBook.setRefreshing(true);
            bindingView.xrvBook.reset();
            viewModel.setPage(1);
            viewModel.showNhdzList();
        }
    }

    public void showQsbkData() {
        if (!bindingView.srlBook.isRefreshing()) {
            bindingView.srlBook.setRefreshing(true);
            bindingView.xrvBook.reset();
            viewModel.setPage(new Random(100).nextInt());
            viewModel.showQSBKList();
        }
    }

    @Override
    protected void onRefresh() {
        bindingView.srlBook.setRefreshing(true);
        loadCustomData();
    }

    @Override
    public void loadListFailure() {
        showContentView();
        if (bindingView.srlBook.isRefreshing()) {
            bindingView.srlBook.setRefreshing(false);
        }
        if (viewModel.getPage() == 0) {
            showError();
        } else {
            bindingView.xrvBook.refreshComplete();
        }
    }

    @Override
    public void showAdapterView(List<DuanZiBean> bean) {
        mAdapter.clear();
        mAdapter.addAll(bean);
        mAdapter.notifyDataSetChanged();
        bindingView.xrvBook.refreshComplete();

        mIsFirst = false;
    }

    @Override
    public void refreshAdapter(List<DuanZiBean> bean) {
        mAdapter.addAll(bean);
        mAdapter.notifyDataSetChanged();
        bindingView.xrvBook.refreshComplete();
    }

    @Override
    public void showListNoMoreLoading() {
        bindingView.xrvBook.noMoreLoading();
    }

    @Override
    public void showLoadSuccessView() {
        showContentView();
        bindingView.srlBook.setRefreshing(false);
    }
}
