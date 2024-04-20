package me.fckng0d.audioservicebackend.specification;

import jakarta.persistence.criteria.Path;
import me.fckng0d.audioservicebackend.model.user.UserProfileDataRelation;
import org.springframework.data.jpa.domain.Specification;

public class UserProfileDataRelationSpecifications {

    public static Specification<UserProfileDataRelation> findByUsername(String username) {
        return (root, query, criteriaBuilder) -> {
            Path<String> usernamePath = root.get("user").get("username");
            return criteriaBuilder.equal(usernamePath, username);
        };
    }
}

