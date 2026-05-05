package entity;

public class User {
    public int id;
    public String username;
    public String role; // MANAGER / EMPLOYEE
    public String fullName;

    public User(int id, String username, String role, String fullName) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }
}