package org.example.carshering.repository;

import org.example.carshering.entity.Client;

import java.util.List;

public interface UserRepositoryCustom {
    List<Client> findUsersByFilter(Boolean banned, String roleName, String sortBy, String sortOrder);

}
