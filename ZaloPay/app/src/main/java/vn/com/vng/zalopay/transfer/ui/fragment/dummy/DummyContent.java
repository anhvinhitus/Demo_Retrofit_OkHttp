package vn.com.vng.zalopay.transfer.ui.fragment.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.com.vng.zalopay.transfer.models.TransferRecent;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<ZaloFriend> ITEMS = new ArrayList<ZaloFriend>();
    public static final List<TransferRecent> ITEMS_TRANSFER_RECENT = new ArrayList<TransferRecent>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<Long, ZaloFriend> ITEM_MAP = new HashMap<Long, ZaloFriend>();
    public static final Map<Long, TransferRecent> ITEM_MAP_TRANSFER_RECENT = new HashMap<Long, TransferRecent>();

    private static final int COUNT = 3;

    static {
        // Add some sample items.
        for (int i = 1; i <= 25; i++) {
            addItem(createZaloFriend(i));
        }
        for (int i = 1; i <= COUNT; i++) {
            addItemTransferRecent(createTransferRecent(i));
        }
    }

    private static void addItem(ZaloFriend item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getUserId(), item);
    }

    private static ZaloFriend createZaloFriend(int position) {
        return new ZaloFriend(position, "userName" + position, "displayName" + position, "http://s240.avatar.talk.zdn.vn/b/a/2/b/1/240/fc04313a9e793ace59ed405e00091682.jpg", 1, true);
    }

    private static void addItemTransferRecent(TransferRecent item) {
        ITEMS_TRANSFER_RECENT.add(item);
        ITEM_MAP_TRANSFER_RECENT.put(item.getUserId(), item);
    }

    private static TransferRecent createTransferRecent(int position) {
        return new TransferRecent(position, ""+position, "userName" + position, "displayName" + position, "http://s240.avatar.talk.zdn.vn/b/a/2/b/1/240/fc04313a9e793ace59ed405e00091682.jpg", 1, "01/01/1990", true, "0988888888", 1, 1000000, "");
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
