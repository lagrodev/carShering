package org.example.carshering.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.Client;
import org.example.carshering.repository.UserRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Client> findUsersByFilter(Boolean banned, String roleName, String sortBy, String sortOrder) {
        StringBuilder query = new StringBuilder("SELECT u FROM User u WHERE 1=1 ");

        if (banned != null) query.append("AND u.banned = :banned ");
        if (roleName != null) query.append("AND u.role.name = :roleName ");

        String safeSortBy = switch (sortBy) {
            case "username" -> "u.username";
            case "email" -> "u.email";
            case "createdAt" -> "u.createdAt";
            default -> "u.id";
        };

        String direction = "desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        query.append("ORDER BY ").append(safeSortBy).append(" ").append(direction);

        TypedQuery<Client> typedQuery = entityManager.createQuery(query.toString(), Client.class);

        if (banned != null) typedQuery.setParameter("banned", banned);
        if (roleName != null) typedQuery.setParameter("roleName", roleName);

        return typedQuery.getResultList();
    }
}
