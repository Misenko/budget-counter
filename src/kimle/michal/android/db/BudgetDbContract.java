package kimle.michal.android.db;

import android.provider.BaseColumns;

public class BudgetDbContract {

    private BudgetDbContract() {
    }

    public static abstract class BudgetDbEntry implements BaseColumns {

        public static final String WEEK_TABLE = "week";
        public static final String CUT_TABLE = "cut";
        //cut table
        public static final String CUT_ID_COLUMN = CUT_TABLE + "." + _ID;
        public static final String CUT_VALUE_COLUMN = "value";
        public static final String CUT_TIMESTAMP_COLUMN = "timestamp";
        public static final String CUT_WEEK_ID_COLUMN = "week_id";
        //week table
        public static final String WEEK_ID_COLUMN = WEEK_TABLE + "." + _ID;
        public static final String WEEK_AMOUNT_COLUMN = "amount";
        public static final String WEEK_START_COLUMN = "start";
        public static final String WEEK_END_COLUMN = "end";
        public static final String WEEK_OVERALL_COLUMN = WEEK_AMOUNT_COLUMN + "-coalesce(sum(" + CUT_VALUE_COLUMN + "),0)";
        public static final String LIMIT = "limit";
        public static final String GROUP_BY = "group by";
    }
}
