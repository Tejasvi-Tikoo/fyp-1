package com.wildroutes.dto;

import lombok.Data;
import java.util.List;

@Data
public class GroupRequest {
    private String name;
    private String location;
    private String tripPlan;
    private List<Long> memberIds;
}