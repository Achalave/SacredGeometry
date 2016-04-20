package main;


import java.util.Objects;

//@author Michael Haertling
public class Spell implements Comparable<Spell> {

    int level;
    String name;

    public Spell(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public Spell(String level, String name) {
        this(Integer.parseInt(level), name);
    }

    @Override
    public int compareTo(Spell t) {
        return name.compareTo(t.name);
    }

    @Override
    public boolean equals(Object o) {
        Spell sp = (Spell) o;
        return name.equals(sp.name);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.level;
        hash = 89 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
