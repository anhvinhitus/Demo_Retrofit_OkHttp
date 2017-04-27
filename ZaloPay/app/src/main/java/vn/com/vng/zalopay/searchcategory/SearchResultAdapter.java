package vn.com.vng.zalopay.searchcategory;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.airbnb.epoxy.EpoxyAdapter;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by khattn on 3/28/17.
 * Adapter search result items
 */

class SearchResultAdapter extends EpoxyAdapter {
    private static final int NUMBER_APP_SHOW = 5;

    private List<SearchAppResultModel> listApp = new ArrayList<>();
    private List<SearchFriendResultModel> listFriend = new ArrayList<>();
    private SectionSeeMoreModel seeMoreApp;
    private SectionSeeMoreModel seeMoreFriend;

    interface OnModelClickListener {
        void onClickAppItem(InsideApp app, int position);

        void onClickSeeMoreApp();

        void onClickFriendItem(ZaloFriend app, int position);

        void onClickSeeMoreFriend();
    }

    private SearchResultAdapter.OnModelClickListener clickListener;
    private final Context context;

    SearchResultAdapter(Context context) {
        super();
        this.context = context;
        enableDiffing();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        clickListener = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    void setClickListener(SearchResultAdapter.OnModelClickListener listener) {
        clickListener = listener;
    }

    synchronized void setResult(List<InsideApp> appList, List<ZaloFriend> friendList, String searchText) {
        models.clear();
        setChangeColor(appList, friendList, searchText);

        if (appList.size() != 0) {
            listApp = appTransform(appList);

            models.add(new SectionTitleModel(context.getResources().getString(R.string.search_app)));
            if (listApp.size() > NUMBER_APP_SHOW) {
                models.addAll(listApp.subList(0, NUMBER_APP_SHOW));
                seeMoreApp = new SectionSeeMoreModel();
                seeMoreApp.setClickListener(seeMoreAppListener);
                models.add(seeMoreApp);
            } else {
                models.addAll(listApp);
            }
        }

        if (friendList.size() != 0) {
            listFriend = friendTransform(friendList);

            models.add(new SectionTitleModel(context.getResources().getString(R.string.search_friend)));
            if (listFriend.size() > NUMBER_APP_SHOW) {
                models.addAll(listFriend.subList(0, NUMBER_APP_SHOW));
                seeMoreFriend = new SectionSeeMoreModel();
                seeMoreFriend.setClickListener(seeMoreFriendListener);
                models.add(seeMoreFriend);
            } else {
                models.addAll(listFriend);
            }
        }

        notifyModelsChanged();
    }

    private void setChangeColor(List<InsideApp> appList, List<ZaloFriend> friendList, String text) {
        for (InsideApp app : appList) {
            int idx = Strings.getIndexOfSearchString(app.appName, text);
            app.appName = changeWordColor(app.appName, idx, idx + text.length());
        }

        for (ZaloFriend friend : friendList) {
            int idx = Strings.getIndexOfSearchString(friend.displayName, text);
            friend.displayName = changeWordColor(friend.displayName, idx, idx + text.length());
        }
    }

    private String changeWordColor(String text, int startIdx, int endIdx) {
        return (text.substring(0, startIdx)
                + "<font color='#9cdaff'>" + text.substring(startIdx, endIdx) + "</font>"
                + text.substring(endIdx));
    }

    void showAllApp() {
        int addIndex = models.indexOf(listApp.get(NUMBER_APP_SHOW - 1)) + 1;
        models.addAll(addIndex, listApp.subList(NUMBER_APP_SHOW, listApp.size()));
        models.remove(seeMoreApp);
        notifyModelsChanged();
    }

    void showAllFriend() {
        int addIndex = models.indexOf(listFriend.get(NUMBER_APP_SHOW - 1)) + 1;
        models.addAll(addIndex, listFriend.subList(NUMBER_APP_SHOW, listFriend.size()));
        models.remove(seeMoreFriend);
        notifyModelsChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).setRecycleChildrenOnDetach(true);
        }
    }

    private final SearchAppResultModel.OnItemClickListener appClickListener = appResultModel -> {
        if (clickListener != null) {
            clickListener.onClickAppItem(appResultModel.getApp(), getModelPosition(appResultModel));
        }
    };

    private final SectionSeeMoreModel.OnItemClickListener seeMoreAppListener = appResultModel -> {
        if (clickListener != null) {
            clickListener.onClickSeeMoreApp();
        }
    };

    private final SearchFriendResultModel.OnItemClickListener friendClickListener = friendResultModel -> {
        if (clickListener != null) {
            clickListener.onClickFriendItem(friendResultModel.getFriend(), getModelPosition(friendResultModel));
        }
    };

    private final SectionSeeMoreModel.OnItemClickListener seeMoreFriendListener = appResultModel -> {
        if (clickListener != null) {
            clickListener.onClickSeeMoreFriend();
        }
    };

    private SearchAppResultModel transform(InsideApp resource, boolean isLastPosition) {
        SearchAppResultModel appModel = new SearchAppResultModel(resource, isLastPosition);
        appModel.setClickListener(appClickListener);
        return appModel;
    }

    private List<SearchAppResultModel> appTransform(List<InsideApp> resources) {
        List<SearchAppResultModel> listModel = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            boolean isLastPosition = (i == resources.size() - 1);
            listModel.add(transform(resources.get(i), isLastPosition));
        }
        return listModel;
    }

    private SearchFriendResultModel transform(ZaloFriend resource, boolean isLastPosition) {
        SearchFriendResultModel model = new SearchFriendResultModel(resource, isLastPosition);
        model.setClickListener(friendClickListener);
        return model;
    }

    private List<SearchFriendResultModel> friendTransform(List<ZaloFriend> resources) {
        List<SearchFriendResultModel> listModel = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            boolean isLastPosition = (i == resources.size() - 1);
            listModel.add(transform(resources.get(i), isLastPosition));
        }
        return listModel;
    }
}
