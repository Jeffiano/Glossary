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

    public static final int MEMORY_COUNT_IN = 1;
    public static final int MEMORY_COUNT_OUT = 0;
    public static final int MEMORY_COUNT_ALL = -1;

    public boolean isInMemory() {
        return this.inMemoryCount == MEMORY_COUNT_IN;
    }
}
