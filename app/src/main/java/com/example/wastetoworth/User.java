package com.example.wastetoworth;
/**
 * Model class representing a User in the application.
 */
public class User {
    private final String name;
    private final String type;
    private final String location;
    public User(String name, String type, String location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }
    public String getName() { 
        return name; 
    }
    public String getType() { 
        return type; 
    }
    public String getLocation() { 
        return location; 
    }
}