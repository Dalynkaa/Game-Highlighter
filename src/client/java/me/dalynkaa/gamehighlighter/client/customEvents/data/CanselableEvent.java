package me.dalynkaa.gamehighlighter.client.customEvents.data;

public abstract class CanselableEvent {
    private boolean isCanceled = false;

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }
}
