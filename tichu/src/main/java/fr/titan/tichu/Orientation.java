package fr.titan.tichu;

/**
 *
 */
public enum Orientation {

    O(0), N(1), E(2), S(3);

    private Orientation(int pos) {
        this.pos = pos;
    }

    private Orientation right;
    private Orientation left;
    private Orientation face;
    private int pos;

    public Orientation getNext() {
        return getLeft();
    }

    public Orientation getLeft() {
        if (left == null) {
            left = getByIndex((this.getPos() + 1) % 4);
        }
        return this.left;
    }

    public Orientation getRight() {
        if (right == null) {
            right = getByIndex((this.getPos() + 3) % 4);
        }
        return this.right;
    }

    public Orientation getFace() {
        if (face == null) {
            face = getByIndex((this.getPos() + 2) % 4);
        }
        return this.face;
    }

    private Orientation getByIndex(int index) {
        for (Orientation or : values()) {
            if (or.getPos() == index) {
                return or;
            }
        }
        return null;
    }

    public int getPos() {
        return pos;
    }
}
