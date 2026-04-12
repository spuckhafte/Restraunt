package com.swe.backend.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.swe.backend.model.AuthSessionDto;
import com.swe.backend.model.AuthUserDto;

@Repository
public class AuthRepository extends DbSupport {
    public Optional<AuthUserDto> findUserByUsernameAndPassword(String username, String password) {
        String sql = """
            SELECT id, username, role
            FROM app_users
            WHERE username = ? AND password = ? AND active = TRUE
            """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new AuthUserDto(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to query user", ex);
        }
    }

    public void createSession(long userId, String token, String effectiveRole) {
        String sql = "INSERT INTO user_sessions(token, user_id, effective_role, active) VALUES (?, ?, ?, TRUE)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setLong(2, userId);
            ps.setString(3, effectiveRole);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create user session", ex);
        }
    }

    public Optional<AuthSessionDto> findActiveSession(String token) {
        String sql = """
            SELECT s.token, s.created_at, s.active, s.effective_role, u.id AS user_id, u.username, u.role
            FROM user_sessions s
            JOIN app_users u ON u.id = s.user_id
            WHERE s.token = ? AND s.active = TRUE AND u.active = TRUE
            """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Timestamp createdAtRaw = rs.getTimestamp("created_at");
                Instant createdAt = createdAtRaw == null ? Instant.now() : createdAtRaw.toInstant();

                AuthUserDto user = new AuthUserDto(
                    rs.getLong("user_id"),
                    rs.getString("username"),
                    rs.getString("role")
                );

                return Optional.of(new AuthSessionDto(
                    rs.getString("token"),
                    user,
                    rs.getString("effective_role"),
                    createdAt,
                    rs.getBoolean("active")
                ));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to validate user session", ex);
        }
    }

    public boolean invalidateSession(String token) {
        String sql = "UPDATE user_sessions SET active = FALSE WHERE token = ? AND active = TRUE";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to invalidate user session", ex);
        }
    }

    public void updateSessionRole(String token, String role) {
        String sql = "UPDATE user_sessions SET effective_role = ? WHERE token = ? AND active = TRUE";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, token);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Session token is invalid");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update session role", ex);
        }
    }

    public void logManagerOverride(long managerUserId, String token, String fromRole, String toRole, String action) {
        String sql = "INSERT INTO manager_override_audit(manager_user_id, token, from_role, to_role, action_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, managerUserId);
            ps.setString(2, token);
            ps.setString(3, fromRole);
            ps.setString(4, toRole);
            ps.setString(5, action);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to audit manager override", ex);
        }
    }
}
