package com.wildroutes.controller;

import com.wildroutes.dto.GroupRequest;
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
                                             @RequestBody GroupRequest request) {
        User owner = userRepository.findById(current.getId()).orElseThrow();
        Group group = Group.builder()
                .name(request.getName())
                .location(request.getLocation())
                .tripPlan(request.getTripPlan())
                .owner(owner)
                .createdAt(Instant.now())
                .build();
        groupRepository.save(group);

        // ✅ always add creator
        addMember(group, owner);

        // ✅ add selected users
        if (request.getMemberIds() != null) {
            for (Long userId : request.getMemberIds()) {
                if (!userId.equals(owner.getId())) {
                    User u = userRepository.findById(userId).orElseThrow();
                    addMember(group, u);
                }
            }
        }
        return ResponseEntity.ok(group);
    }
    
    private void addMember(Group group, User user) {
        GroupMember gm = GroupMember.builder()
                .group(group)
                .user(user)
                .build();
        groupMemberRepository.save(gm);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getGroup(@PathVariable Long id) {

        return groupRepository.findById(id)
                .map(group -> {

                    List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());

                    Map<String, Object> res = new HashMap<>();

                    // ✅ group safe
                    Map<String, Object> g = new HashMap<>();
                    g.put("id", group.getId());
                    g.put("name", group.getName());
                    g.put("location", group.getLocation());
                    g.put("tripPlan", group.getTripPlan());

                    res.put("group", g);

                    // ✅ members safe
                    List<Map<String, Object>> mem = members.stream().map(m -> {
                        Map<String, Object> u = new HashMap<>();
                        u.put("id", m.getUser().getId());
                        u.put("username", m.getUser().getUsername());
                        return u;
                    }).toList();

                    res.put("members", mem);

                    return ResponseEntity.ok(res);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Map<String, Object>> myGroups(
            @AuthenticationPrincipal CustomUserDetails current) {

        List<GroupMember> memberships = groupMemberRepository.findByUserId(current.getId());

        return memberships.stream().map(m -> {
            Group g = m.getGroup();

            Map<String, Object> res = new HashMap<>();
            res.put("id", m.getId());

            Map<String, Object> group = new HashMap<>();
            group.put("id", g.getId());
            group.put("name", g.getName());
            group.put("location", g.getLocation());

            res.put("group", group);

            return res;
        }).toList();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {

        if (!groupRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        groupRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}

