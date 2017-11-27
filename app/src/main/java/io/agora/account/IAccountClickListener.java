package io.agora.account;

import io.agora.utils.LinkState;

public interface IAccountClickListener {
    void onAccountClick(int id, boolean isChecked, int mediaType);

    void onLinkOrTClicked(int uid, LinkState currentState);
}
