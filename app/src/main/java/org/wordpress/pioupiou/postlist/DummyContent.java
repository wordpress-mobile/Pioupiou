package org.wordpress.pioupiou.postlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO: Kill this when PostStore in integrated in the adapter
public class DummyContent {
    public static final List<PostItem> ITEMS = new ArrayList<PostItem>();
    private static final int COUNT = 10;

    static {
        for (int i = 1; i <= COUNT; i++) {
            addItem(createPostItem(i));
        }
    }

    private static void addItem(PostItem item) {
        ITEMS.add(item);
    }

    private static PostItem createPostItem(int position) {
        return new PostItem(String.valueOf(position), "https://s.gravatar.com/avatar/d0d9f5e51bf985ade99a4017835da0b6",
                "Mister Piou", "My bro Polly has ceased to be #lovelyplumage #birdlife #nailedit",
                System.currentTimeMillis() - ((int) (new Random().nextFloat() * 1000 * 60 * 60 * 24)));
    }

    public static class PostItem {
        public final String id;
        public final String gravatarUrl;
        public final String authorName;
        public final String message;
        public final long date;

        public PostItem(String id, String gravatarUrl, String authorName, String message, long date) {
            this.id = id;
            this.gravatarUrl = gravatarUrl;
            this.authorName = authorName;
            this.message = message;
            this.date = date;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
