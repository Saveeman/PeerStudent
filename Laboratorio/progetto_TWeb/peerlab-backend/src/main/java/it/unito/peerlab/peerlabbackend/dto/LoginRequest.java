package it.unito.peerlab.peerlabbackend.dto;

/** Body della POST /api/auth/login */
public record LoginRequest(
        String username,
        String password
) {
}
