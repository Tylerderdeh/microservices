package kz.zk.chat.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "message_reactions",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"message_id", "user_id", "reaction"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID messageId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String reaction; // 👍 ❤️ 😂 🔥
}