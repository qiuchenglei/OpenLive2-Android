package io.agora.message;

public class Message {
    public static final int MSG_TYPE_TEXT = 1;
    private String mContent;
    private int mType;

    public Message(int type, String content) {
        mType = type;
        mContent = content;
    }

    public Message(String content) {
        this(0, content);
    }

    public String getContent() {
        return mContent;
    }

    public int getType() {
        return mType;
    }
}
