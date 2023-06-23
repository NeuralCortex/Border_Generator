package com.fx.gen.pojo;

/**
 *
 * @author pscha
 */
public class Address {

    private String name;
    private String wert;

    public Address(String name, String wert) {
        this.name = name;
        this.wert = wert;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWert() {
        return wert;
    }

    public void setWert(String wert) {
        this.wert = wert;
    }

    @Override
    public String toString() {
        return "AddressPOJO{" + "name=" + name + ", wert=" + wert + '}';
    }
}
