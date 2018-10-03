package me.zeroeightsix.kami.gui.old.rgui.component.listen;

/**
 * Created by 086 on 30/06/2017.
 */
public interface KeyListener {

    public void onKeyDown(KeyEvent event);
    public void onKeyUp(KeyEvent event);

    public static class KeyEvent {
        int key;

        public KeyEvent(int key) {
            this.key = key;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }
    }
}
