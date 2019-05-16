package uk.gov.hmcts.reform.divorce.orchestration.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.hmcts.reform.divorce.orchestration.models.UserRole;
import uk.gov.hmcts.reform.divorce.orchestration.repositories.mapping.UserRolesMapper;

import java.util.List;

@RegisterMapper(UserRolesMapper.class)
public interface UserRolesRepository {
    @SqlUpdate("INSERT INTO user_roles ( "
        + "user_id, "
        + "role"
        + ") "
        + "VALUES ("
        + ":userId, "
        + ":role "
        + ")")
    void saveUserRole(
        @Bind("userId") String userId,
        @Bind("role") String role
    );

    @SqlQuery("SELECT * FROM user_roles WHERE user_roles.user_id = :userId")
    List<UserRole> getByUserId(@Bind("userId") String userId);
}
