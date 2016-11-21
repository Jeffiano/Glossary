package glossary.jiffy.com.glossary;

/**
 * Created by Jiffy on 2016/11/1.
 */
public class Word {
    public long _id;
    public String english;
    public String chinese;
    public String pronFilePath;
    public String pronUrl;
    public int inMemoryCount;
    public int cacheState;//0 not cached; 1: cached

    public static final int MEMORY_COUNT_IN = 1;
    public static final int MEMORY_COUNT_OUT = 0;
    public static final int MEMORY_COUNT_ALL = -1;

    public static final int CACHE_YES = 1;
    public static final int CACHE_NO = 0;


    public boolean isInMemory() {
        return this.inMemoryCount == MEMORY_COUNT_IN;
    }
    public boolean isCached(){return this.cacheState == 1;}
}
