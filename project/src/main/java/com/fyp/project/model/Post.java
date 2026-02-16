package com.fyp.project.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne    //one user multiple posts but one post only one user
    private Users user;

    private String content;
    private String image;
    private String video;
    private String postDate;

    @OneToMany(mappedBy = "post" , cascade = CascadeType.ALL )
    private List<Like> likes=new ArrayList<>();    

    //comments
    @OneToMany
    private List<Post> replyPosts = new ArrayList<>();

    @ManyToOne
    private Post replyFor;


    private boolean isReply;
    private boolean isPost;

}
