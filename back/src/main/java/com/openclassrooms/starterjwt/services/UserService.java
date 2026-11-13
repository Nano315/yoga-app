package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.exception.UnauthorizedException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Supprime un utilisateur, uniquement si c'est l'utilisateur connecté.
     *
     * @param id                   identifiant de l'utilisateur à supprimer
     * @param authenticatedEmail   email de l'utilisateur connecté
     */
    public void delete(Long id, String authenticatedEmail) {
        User user = this.findById(id);

        if (!Objects.equals(authenticatedEmail, user.getEmail())) {
            throw new UnauthorizedException();
        }

        this.userRepository.deleteById(id);
    }

    public User findById(Long id) {
        return this.userRepository.findById(id).orElseThrow(NotFoundException::new);
    }
}
