package com.fyp.project.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.annotation.Generated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Data;


@Entity
@Data
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fullName;
    private String location;
    private String website;
    private String birthDate;
    private String email;
    private String password;
    private String mobile;
    private String image;
    private String backgroundImage;
    private String bio;
    private boolean req_user;
    private boolean login_with_google;  //signed in with google or password

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL )      //onetomany one is applied to class like 1 user but many posts many is applied to the parameter
    private List<Post>post=new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL )   //means if user deleted all likes will be removed
    private List<Like> likes=new ArrayList<>();

    //@Embedded
    //private Varification i should do it by adhaar

    @JsonIgnore
    @ManyToMany
    private List<Users> followers=new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    private List<Users> following=new ArrayList<>();

}
