package io.agora.linker;

public interface LinkListener {
    void onConfirmClicked(int uid);

    void onCancelClicked(int uid);

    void onListEmptyNotify();
}
