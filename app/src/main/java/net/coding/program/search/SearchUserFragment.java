package net.coding.program.search;

import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.adapter.SearchUserAdapter;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Vernon on 15/11/30.
 */
@EFragment(R.layout.fragment_search_list)
public class SearchUserFragment extends RefreshBaseFragment {

    private static final String TAG = SearchTaskFragment.class.getSimpleName();
    final String url = Global.HOST_API + "/esearch/all?q=%s";
    final String tmp = "&types=%s&pageSize=10";
    String page = "&page=%s";
    int pos = 1;
    ArrayList<UserObject> mData = new ArrayList<>();
    private String keyword = "";
    private String tabPrams;
    private boolean hasMore = true;
    @ViewById
    ListView listView;
    @ViewById(R.id.emptyView)
    LinearLayout emptyView;

    SearchUserAdapter adapter;

    @AfterViews
    protected void init() {
        initRefreshLayout();
        setRefreshing(true);
        mFootUpdate.init(listView, mInflater, this);
        adapter = new SearchUserAdapter(mData, keyword, getActivity());
        listView.setAdapter(adapter);
        listView.setOnScrollListener(mOnScrollListener);
        loadMore();
    }

    @ItemClick
    final void listView(UserObject itemData) {
        String globalKey = itemData.global_key.replace("<em>", "").replace("</em>", "");
        CodingCompat.instance().launchUserDetailActivity(getActivity(), globalKey);
    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount == totalItemCount) {
                if (hasMore) {
                    pos++;
                    loadMore();
                }
            }
        }
    };

    public String getKeyword() {
        return keyword;
    }

    public String getTabPrams() {
        return tabPrams;
    }

    public void setTabPrams(String tabPrams) {
        this.tabPrams = tabPrams;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    private String getUrl(int pos) {
        String tag = "";
        tag = String.format(url, getKeyword()) + String.format(tmp, getTabPrams()) + String.format(page, pos + "");
        return tag;
    }

    @Override
    public void loadMore() {
        getNetwork(getUrl(pos), keyword);
    }

    @Override
    public void onRefresh() {
        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(keyword)) {
            setRefreshing(false);
            if (code == 0) {
                if (pos == 1) {
                    mData.clear();
                }
                JSONArray array = respanse.getJSONObject("data").getJSONObject("friends").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    UserObject oneData = new UserObject(item);
                    mData.add(oneData);
                }
                emptyView.setVisibility(mData.size() == 0 ? View.VISIBLE : View.GONE);
                if (array.length() > 0) {
                    mFootUpdate.updateState(code, false, mData.size());
                    hasMore = true;
                } else {
                    hasMore = false;
                    mFootUpdate.updateState(code, true, mData.size());
                }
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
                hasMore = false;
            }
        }
    }
}
