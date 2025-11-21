package org.example.carshering.service.interfaces;


import org.example.carshering.entity.Client;
import org.springframework.security.core.userdetails.UserDetailsService;
public interface ClientDetailsService extends UserDetailsService {



    void setEmailVerified(Client user);

}
