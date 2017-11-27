package io.agora.account;

import io.agora.utils.LinkState;

public class AccountData {
    public int id;
    public boolean isChecked;
    public int mediaType;
    public LinkState mLinkState;

    public AccountData(int id, boolean isChecked, int mediaT, LinkState linkState) {
        this.id = id;
        this.isChecked = isChecked;
        this.mediaType = mediaT;
        this.mLinkState = linkState;
    }

    @Override
    public String toString() {
        return "AccountData-->id:" + id + " , isChecked:" + isChecked + " , mediaType:" + mediaType;
    }
}
