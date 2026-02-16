//Save delete update users methods to be extended

package com.fyp.project.repository;

import java.util.List;

// removed unused import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fyp.project.model.Users;

public interface UserRepository extends JpaRepository<Users, Long> {

    public Users findByEmail(String email);


    @Query("SELECT DISTINCT u FROM Users u WHERE u.fullName LIKE CONCAT('%',:query,'%') OR u.email LIKE CONCAT('%',:query,'%')")
    public List<Users> searchUser(@Param("query") String query);

    
    
}
