package com.springboot.websocket.entity;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "member")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<RoleType> roles;

    public Set<String> getAuthorityStrings() {
        return roles.stream()
                .map(RoleType::getAuthority)
                .collect(Collectors.toSet());
    }
}
