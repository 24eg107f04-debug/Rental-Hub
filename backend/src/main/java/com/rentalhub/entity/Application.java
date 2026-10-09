package com.rentalhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "rental_applications")
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false) private User user;
    @ManyToOne(optional=false) private Property property;
    @NotBlank private String status;
    public Application() {}
    public Application(Long id, User user, Property property, String status){this.id=id;this.user=user;this.property=property;this.status=status;}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public User getUser(){return user;} public void setUser(User user){this.user=user;}
    public Property getProperty(){return property;} public void setProperty(Property property){this.property=property;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
}
