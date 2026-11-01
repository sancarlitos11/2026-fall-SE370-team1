package model; 

public class User {
    private int userId;
    private String name; 
    private String email;
    private String phoneNumber;
    private String role; 

    public User(int userId, String name, String email, String phoneNumber, String role) {
        this.userId = userId; 
        this.name = name; 
        this.email = email; 
        this.phoneNumber = phoneNumber; 
        this.role = role; 
    }

    public int getUserId(){
        return userId;
    }

    public String getName(){
        return name; 
    }

    public String getEmail(){
        return email;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public String getRole(){
        return role; 
    }
}
