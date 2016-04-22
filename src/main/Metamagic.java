package main;


import java.util.Objects;

/**
 *
 * @author Michael
 */
public class Metamagic implements Comparable<Metamagic> {

    int levelInc;
    String name;

    public Metamagic(int level, String name) {
        this.levelInc = level;
        this.name = name;
    }

    public Metamagic(String level, String name) {
        this(Integer.parseInt(level), name);
    }

    @Override
    public int compareTo(Metamagic t) {
        return name.compareTo(t.name);
    }

    @Override
    public boolean equals(Object o) {
        Metamagic sp = (Metamagic) o;
        return name.equals(sp.name);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public String toString(){
        return name+"@"+levelInc;
    }
    
}
