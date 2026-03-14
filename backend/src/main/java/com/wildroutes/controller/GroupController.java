package com.wildroutes.controller;

import com.wildroutes.model.Group;
import com.wildroutes.model.GroupMember;
import com.wildroutes.model.User;
import com.wildroutes.repository.GroupMemberRepository;
import com.wildroutes.repository.GroupRepository;
import com.wildroutes.repository.UserRepository;
import com.wildroutes.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupController(GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@AuthenticationPrincipal CustomUserDetails current,
                                             @RequestBody Group payload) {
        User owner = userRepository.findById(current.getId()).orElseThrow();
        Group group = Group.builder()
                .name(payload.getName())
                .location(payload.getLocation())
                .tripPlan(payload.getTripPlan())
                .owner(owner)
                .createdAt(Instant.now())
                .build();
        groupRepository.save(group);

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(owner)
                .build();
        groupMemberRepository.save(member);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getGroup(@PathVariable Long id) {
        return groupRepository.findById(id)
                .map(group -> {
                    List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
                    Map<String, Object> body = new HashMap<>();
                    body.put("group", group);
                    body.put("members", members);
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<GroupMember> myGroups(@AuthenticationPrincipal CustomUserDetails current) {
        return groupMemberRepository.findByUserId(current.getId());
    }
}

