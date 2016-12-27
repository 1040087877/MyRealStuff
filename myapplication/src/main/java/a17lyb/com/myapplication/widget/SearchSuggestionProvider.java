package a17lyb.com.myapplication.widget;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by 10400 on 2016/12/21.
 */

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider{
    public static final String AUTHORITY="a17lyb.com.myapplication.widget.SearchSuggestionProvider";
    public static final int MODE =DATABASE_MODE_QUERIES;
    public SearchSuggestionProvider(){
        setupSuggestions(AUTHORITY,MODE);
    }

}
