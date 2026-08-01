package com.movieTicket.CategoryService.entity;
import com.movieTicket.CategoryService.enums.ScreenStatus;
import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long screenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScreenStatus status = ScreenStatus.ACTIVE;

    @Version
    private Long version;
}
