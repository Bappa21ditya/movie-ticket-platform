package com.cineverse.booking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class DatabaseMigrationConfig {
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner migrateDatabaseConstraints() {
        return args -> {

            // ============================================================
            // BOOKING STATUS CONSTRAINT
            // ============================================================

            jdbcTemplate.execute("""
                ALTER TABLE booking
                DROP CONSTRAINT IF EXISTS booking_status_check
                """);

            jdbcTemplate.execute("""
                ALTER TABLE booking
                ADD CONSTRAINT booking_status_check
                CHECK (
                    status IN (
                        'PENDING',
                        'PAYMENT_IN_PROGRESS',
                        'CONFIRMED',
                        'CANCELLED',
                        'FAILED'
                    )
                )
                """);


            // ============================================================
            // SAGA CURRENT STEP CONSTRAINT
            // ============================================================

            jdbcTemplate.execute("""
                ALTER TABLE saga_instance
                DROP CONSTRAINT IF EXISTS saga_instance_current_step_check
                """);

            jdbcTemplate.execute("""
                ALTER TABLE saga_instance
                ADD CONSTRAINT saga_instance_current_step_check
                CHECK (
                    current_step IN (
                        'STARTED',
                        'HOLDING_SEAT',
                        'SEAT_HELD',
                        'PAYMENT_IN_PROGRESS',
                        'PAYMENT_SUCCESS',
                        'PAYMENT_FAILED',
                        'CONFIRMING_BOOKING',
                        'COMPENSATING',
                        'COMPLETED',
                        'FAILED',
                        'REFUND_PENDING'
                    )
                )
                """);
        };
    }
}
