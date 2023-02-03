package br.com.yamarasolution.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.yamarasolution.exception.TokenRefreshException;
import br.com.yamarasolution.model.RefreshToken;
import br.com.yamarasolution.model.User;
import br.com.yamarasolution.repository.RefreshTokenRepository;
import br.com.yamarasolution.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

  @Value("${yamarasolution.jwt.refresh.expiration}")
  private Long refreshTokenDurationMs;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private UserRepository userRepository;

  /**
   * It returns an Optional of a RefreshToken object, which is found by the token
   * parameter
   * 
   * @param token The token that was sent to the client.
   * @return An Optional object.
   */
  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  /**
   * If the user has a refresh token that hasn't expired, return it. Otherwise,
   * create a new one and
   * return it
   * 
   * @param userId The user's id
   * @return RefreshToken
   */
  public RefreshToken createRefreshToken(UUID userId) {

    User user = userRepository.findById(userId).get();

    RefreshToken refreshTokenVeri = refreshTokenRepository.findByUserAndExpiryDateAfter(user, Instant.now());
    if (refreshTokenVeri != null) {
      return refreshTokenVeri;
    }

    RefreshToken refreshToken = new RefreshToken();

    refreshToken.setUser(userRepository.findById(userId).get());
    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
    refreshToken.setToken(UUID.randomUUID().toString());

    refreshToken = refreshTokenRepository.save(refreshToken);
    return refreshToken;
  }

  /**
   * If the token is expired, delete it and throw an exception
   * 
   * @param token The token that was sent to the client
   * @return A RefreshToken object.
   */
  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
    }

    return token;
  }

  /**
   * Delete all refresh tokens for a given user.
   * 
   * @param userId The user's ID
   * @return The number of rows deleted.
   */
  @Transactional
  public int deleteByUserId(UUID userId) {
    return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
  }

  /**
   * Delete all refresh tokens that have expired.
   */
  @Transactional
  public void deleteAllRefreshTokensExpired() {
    List<RefreshToken> tokens = refreshTokenRepository.findAll();

    if (!tokens.isEmpty()) {
      Instant now = Instant.now();

      tokens.forEach(token -> {
        if (now.isAfter(token.getExpiryDate())) {
          refreshTokenRepository.deleteById(token.getId());
        }
      });
    }

  }
}
