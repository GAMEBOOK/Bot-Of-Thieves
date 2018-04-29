package brightspark.botofthieves.util;

public enum EmojiUtil
{
    GREEN_HEART("\uD83D\uDC9A"),
    STAR("\u2B50"),
    NAME_BADGE("\uD83D\uDCDB"),
    ANCHOR("\u2693"),
    CROSSBONES("\uD83D\uDC80"),
    BOAT("\u26F5");

    private final String unicode;

    EmojiUtil(String unicode)
    {
        this.unicode = unicode;
    }

    public boolean equals(String unicode)
    {
        return this.unicode.equals(unicode);
    }

    @Override
    public String toString()
    {
        return unicode;
    }
}
