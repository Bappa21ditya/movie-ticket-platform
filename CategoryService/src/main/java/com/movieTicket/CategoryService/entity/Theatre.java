package com.movieTicket.CategoryService.entity;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "theatres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theatre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long theatreId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScreenStatus status = ScreenStatus.ACTIVE;

    @Version
    private Long version;
}
