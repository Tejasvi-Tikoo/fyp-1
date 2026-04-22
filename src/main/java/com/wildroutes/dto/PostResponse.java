package com.wildroutes.dto;

import java.time.Instant;
import java.util.*;

import com.wildroutes.model.Comment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String story;
    private String imageUrl;
    private String location;
    private Instant createdAt;

    private String username;
    private Long userId;

    private int likeCount;
    private boolean liked;

    private List<Comment> comments;
}