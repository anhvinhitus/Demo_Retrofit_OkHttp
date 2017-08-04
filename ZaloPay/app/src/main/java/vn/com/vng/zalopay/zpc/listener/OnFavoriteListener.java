package vn.com.vng.zalopay.zpc.listener;

import vn.com.vng.zalopay.domain.model.FavoriteData;

/**
 * Created by hieuvm on 7/28/17.
 * *
 */

public interface OnFavoriteListener {
    void onRemoveFavorite(FavoriteData favoriteData);

    void onAddFavorite(FavoriteData favoriteData);

    void onMaximumFavorite();

    void onSelectFavorite(FavoriteData favoriteData);
}
